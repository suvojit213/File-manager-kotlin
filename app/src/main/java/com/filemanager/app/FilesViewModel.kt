package com.filemanager.app

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class FilesViewModel : ViewModel() {

    private val _files = MutableLiveData<List<FileItem>>()
    val files: LiveData<List<FileItem>> = _files

    private val _currentPath = MutableLiveData<String>()
    val currentPath: LiveData<String> = _currentPath

    fun loadFiles(path: String) {
        _currentPath.value = path
        viewModelScope.launch(Dispatchers.IO) {
            val directory = File(path)
            val fileList = mutableListOf<FileItem>()

            try {
                directory.listFiles()?.forEach { file ->
                    val itemCount = if (file.isDirectory) {
                        file.listFiles()?.size ?: 0
                    } else 0

                    val details = if (file.isDirectory) {
                        "Folder"
                    } else {
                        formatSize(file.length())
                    }

                    val iconResId = when {
                        file.isDirectory -> R.drawable.ic_folder_ios
                        file.name.endsWith(".pdf", true) -> R.drawable.ic_pdf_file
                        file.name.endsWith(".mp4", true) || file.name.endsWith(".avi", true) -> R.drawable.ic_video_file
                        file.name.endsWith(".apk", true) -> R.drawable.ic_apk_file
                        else -> R.drawable.ic_file
                    }

                    fileList.add(
                        FileItem(
                            file = file,
                            name = file.name,
                            details = details,
                            isDirectory = file.isDirectory,
                            icon = iconResId
                        )
                    )
                }
            } catch (e: SecurityException) {
                // Handle permission denied
            }

            // Sort: directories first, then files, both alphabetically
            val sortedFiles = fileList.sortedWith(compareBy<FileItem> { !it.isDirectory }.thenBy { it.name.lowercase() })

            _files.postValue(sortedFiles)
        }
    }

    private fun formatSize(size: Long): String {
        if (size <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return String.format("%.2f %s", size / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
    }
}
