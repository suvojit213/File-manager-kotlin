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

                    fileList.add(
                        FileItem(
                            name = file.name,
                            path = file.absolutePath,
                            isDirectory = file.isDirectory,
                            lastModified = file.lastModified(),
                            size = if (file.isFile) file.length() else 0L,
                            itemCount = itemCount
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
}
