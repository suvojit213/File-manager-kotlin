package com.filemanager.app

import android.os.Environment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class RecentsViewModel : ViewModel() {

    private val _recentFiles = MutableLiveData<List<FileItem>>()
    val recentFiles: LiveData<List<FileItem>> = _recentFiles

    private val MAX_RECENT_FILES = 100 // Limit the number of recent files

    fun loadRecentFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            val recentFiles = mutableListOf<FileItem>()
            val rootDir = Environment.getExternalStorageDirectory()

            try {
                // Recursively scan files
                scanDirectory(rootDir, recentFiles)
            } catch (e: SecurityException) {
                // Handle permission denied
                e.printStackTrace()
            }

            // Sort by last modified (most recent first) and take the top N
            val sortedFiles = recentFiles.sortedByDescending { it.lastModified }.take(MAX_RECENT_FILES)

            _recentFiles.postValue(sortedFiles)
        }
    }

    private fun scanDirectory(directory: File, recentFiles: MutableList<FileItem>) {
        directory.listFiles()?.forEach { file ->
            if (file.isFile) {
                recentFiles.add(
                    FileItem(
                        name = file.name,
                        path = file.absolutePath,
                        isDirectory = false,
                        lastModified = file.lastModified(),
                        size = file.length(),
                        extension = file.extension
                    )
                )
            } else if (file.isDirectory && file.canRead() && !isExcludedDirectory(file)) {
                scanDirectory(file, recentFiles)
            }
        }
    }

    private fun isExcludedDirectory(directory: File): Boolean {
        val excludedNames = listOf("android", "obb", "data", ".thumbnails", ".nomedia")
        return excludedNames.any { directory.name.lowercase() == it }
    }
}