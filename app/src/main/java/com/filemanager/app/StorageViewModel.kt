package com.filemanager.app

import android.app.Application
import android.content.Context
import android.os.Environment
import android.os.StatFs
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.core.content.ContextCompat
import java.io.File

class StorageViewModel(application: Application) : AndroidViewModel(application) {

    private val _storage = MutableLiveData<List<FileItem>>()
    val storage: LiveData<List<FileItem>> = _storage

    fun loadStorageLocations(context: Context) {
        val storageItems = mutableListOf<FileItem>()
        val externalStorageVolumes: Array<File> = ContextCompat.getExternalFilesDirs(context, null)

        // Primary/Internal Storage
        val primaryExternalStorage = externalStorageVolumes.firstOrNull()
        if (primaryExternalStorage != null) {
            val path = primaryExternalStorage.path.split("/Android")[0]
            val file = File(path)
            val totalSpace = file.totalSpace
            val freeSpace = file.freeSpace
            val usedSpace = totalSpace - freeSpace
            val details = "Used ${formatSize(usedSpace)} of ${formatSize(totalSpace)}"

            storageItems.add(
                FileItem(
                    file = file,
                    name = "Internal Storage",
                    details = details,
                    isDirectory = true,
                    icon = R.drawable.ic_phone_storage_ios
                )
            )
        }

        // Secondary/SD Card Storage
        if (externalStorageVolumes.size > 1) {
            val secondaryExternalStorage = externalStorageVolumes.getOrNull(1)
            if (secondaryExternalStorage != null) {
                val path = secondaryExternalStorage.path.split("/Android")[0]
                val file = File(path)
                // Check if it's a valid, readable directory before adding
                if (file.exists() && file.isDirectory && file.canRead()) {
                    val totalSpace = file.totalSpace
                    val freeSpace = file.freeSpace
                    val usedSpace = totalSpace - freeSpace
                    val details = "Used ${formatSize(usedSpace)} of ${formatSize(totalSpace)}"

                    storageItems.add(
                        FileItem(
                            file = file,
                            name = "SD Card",
                            details = details,
                            isDirectory = true,
                            icon = R.drawable.ic_sd_card_ios
                        )
                    )
                }
            }
        }
        _storage.value = storageItems
    }

    private fun formatSize(size: Long): String {
        if (size <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return String.format("%.1f %s", size / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
    }
}