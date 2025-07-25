package com.filemanager.app

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

enum class SortType {
    NAME, DATE, SIZE, TYPE
}

class FilesViewModel : ViewModel() {

    private val _files = MutableLiveData<List<FileItem>>()
    val files: LiveData<List<FileItem>> = _files

    private val _allFiles = MutableLiveData<List<FileItem>>() // To hold all files for searching
    val allFiles: LiveData<List<FileItem>> = _allFiles

    private val _currentPath = MutableLiveData<String>()
    val currentPath: LiveData<String> = _currentPath

    fun loadFiles(path: String) {
        _currentPath.value = path
        viewModelScope.launch {
            val fileList = withContext(Dispatchers.IO) {
                val directory = File(path)
                val files = directory.listFiles()
                files?.map { file ->
                    val itemCount = if (file.isDirectory) {
                        file.listFiles()?.size ?: 0
                    } else 0

                    val details = if (file.isDirectory) {
                        "Folder"
                    } else {
                        formatSize(file.length())
                    }

                    val iconResId = when {
                        file.isDirectory -> R.drawable.ic_folder
                        file.name.endsWith(".pdf", true) -> R.drawable.ic_pdf_file
                        file.name.endsWith(".mp4", true) || file.name.endsWith(".avi", true) -> R.drawable.ic_video_file
                        file.name.endsWith(".apk", true) -> R.drawable.ic_apk_file
                        else -> R.drawable.ic_file
                    }

                    FileItem(
                        file = file,
                        name = file.name,
                        details = details,
                        isDirectory = file.isDirectory,
                        icon = iconResId
                    )
                } ?: emptyList()
            }

            // Sort: directories first, then files, both alphabetically
            val sortedFiles = fileList.sortedWith(compareBy<FileItem> { !it.isDirectory }.thenBy { it.name.lowercase() })

            _allFiles.postValue(sortedFiles) // Update all files
            _files.postValue(sortedFiles) // Also update the displayed files initially
        }
    }

    fun searchFiles(query: String) {
        val currentFiles = _allFiles.value ?: return
        if (query.isBlank()) {
            _files.postValue(currentFiles) // If query is empty, show all files
        } else {
            val filteredList = currentFiles.filter {
                it.name.contains(query, ignoreCase = true)
            }
            _files.postValue(filteredList)
        }
    }

    fun sortFiles(sortType: SortType) {
        val currentFiles = _allFiles.value ?: return
        val sortedList = when (sortType) {
            SortType.NAME -> currentFiles.sortedWith(compareBy<FileItem> { !it.isDirectory }.thenBy { it.name.lowercase() })
            SortType.DATE -> currentFiles.sortedWith(compareBy<FileItem> { !it.isDirectory }.thenByDescending { it.file.lastModified() })
            SortType.SIZE -> currentFiles.sortedWith(compareBy<FileItem> { !it.isDirectory }.thenBy { it.file.length() })
            SortType.TYPE -> currentFiles.sortedWith(compareBy<FileItem> { !it.isDirectory }.thenBy { it.file.extension.lowercase() })
        }
        _files.postValue(sortedList)
    }

    private fun formatSize(size: Long): String {
        if (size <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return String.format("%.2f %s", size / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
    }
}
