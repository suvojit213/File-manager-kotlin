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

    fun loadRecentFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            val recentFiles = mutableListOf<FileItem>()

            try {
                // Get recent files from various directories
                val directories = listOf(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                )

                directories.forEach { dir ->
                    dir?.listFiles()?.forEach { file ->
                        if (file.isFile) {
                            recentFiles.add(
                                FileItem(
                                    name = file.name,
                                    path = file.absolutePath,
                                    isDirectory = false,
                                    lastModified = file.lastModified(),
                                    size = file.length()
                                )
                            )
                        }
                    }
                }
            } catch (e: SecurityException) {
                // Handle permission denied
            }

            // Sort by last modified (most recent first)
            val sortedFiles = recentFiles.sortedByDescending { it.lastModified }.take(20)

            _recentFiles.postValue(sortedFiles)
        }
    }
}