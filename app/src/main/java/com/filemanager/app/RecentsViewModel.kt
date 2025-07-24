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
            val sortedFiles = recentFiles.sortedByDescending { it.file.lastModified() }.take(MAX_RECENT_FILES)

            _recentFiles.postValue(sortedFiles)
        }
    }

    private fun scanDirectory(directory: File, recentFiles: MutableList<FileItem>) {
        directory.listFiles()?.forEach { file ->
            if (file.isFile) {
                recentFiles.add(
                    FileItem(
                        file = file,
                        name = file.name,
                        details = formatSize(file.length()),
                        isDirectory = false,
                        icon = when {
                            file.name.endsWith(".pdf", true) -> R.drawable.ic_pdf_file
                            file.name.endsWith(".mp4", true) || file.name.endsWith(".avi", true) -> R.drawable.ic_video_file
                            file.name.endsWith(".apk", true) -> R.drawable.ic_apk_file
                            else -> R.drawable.ic_file
                        }
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

    private fun formatSize(size: Long): String {
        if (size <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return String.format("%.2f %s", size / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
    }
}