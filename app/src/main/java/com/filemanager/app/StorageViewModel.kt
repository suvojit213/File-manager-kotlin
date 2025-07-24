package com.filemanager.app

import android.app.Application
import android.os.Environment
import android.os.StatFs
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.io.File

class StorageViewModel(application: Application) : AndroidViewModel(application) {

    private val _storageVolumes = MutableLiveData<List<StorageVolume>>()
    val storageVolumes: LiveData<List<StorageVolume>> = _storageVolumes

    init {
        loadStorageVolumes()
    }

    private fun loadStorageVolumes() {
        val volumes = mutableListOf<StorageVolume>()

        // Internal Storage
        val internalStorage = Environment.getExternalStorageDirectory()
        val internalStat = StatFs(internalStorage.path)
        val internalTotalBytes = internalStat.totalBytes
        val internalAvailableBytes = internalStat.availableBytes
        volumes.add(StorageVolume(
            name = "Internal Storage",
            path = internalStorage.absolutePath,
            totalSpace = internalTotalBytes,
            freeSpace = internalAvailableBytes,
            isPrimary = true
        ))

        // SD Card (External Storage - non-primary)
        // This is a simplified detection. A more robust solution would involve StorageManager API.
        val externalStorages = getApplication<Application>().getExternalFilesDirs(null)
        externalStorages.forEach { file ->
            if (file != null && file.absolutePath.contains("sdcard", ignoreCase = true) && !file.absolutePath.contains("emulated")) {
                val sdCardRoot = file.parentFile?.parentFile?.parentFile?.parentFile // Adjust to get the actual root of the SD card
                if (sdCardRoot != null && sdCardRoot.canRead()) {
                    val sdStat = StatFs(sdCardRoot.path)
                    val sdTotalBytes = sdStat.totalBytes
                    val sdAvailableBytes = sdStat.availableBytes
                    volumes.add(StorageVolume(
                        name = "SD Card",
                        path = sdCardRoot.absolutePath,
                        totalSpace = sdTotalBytes,
                        freeSpace = sdAvailableBytes,
                        isPrimary = false
                    ))
                }
            }
        }
        _storageVolumes.value = volumes
    }

    data class StorageVolume(
        val name: String,
        val path: String,
        val totalSpace: Long,
        val freeSpace: Long,
        val isPrimary: Boolean
    )

    fun formatSize(size: Long): String {
        if (size <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return String.format("%.2f %s", size / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
    }
}