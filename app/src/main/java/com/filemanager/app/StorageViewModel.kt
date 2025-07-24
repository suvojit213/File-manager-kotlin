package com.filemanager.app

import android.os.Environment
import android.os.StatFs
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

data class StorageInfo(
    val internalTotal: Double,
    val internalAvailable: Double,
    val internalUsedPercent: Int,
    val externalTotal: Double?,
    val externalAvailable: Double?,
    val externalUsedPercent: Int?,
    val isExternalStorageAvailable: Boolean
)

class StorageViewModel : ViewModel() {

    private val _storageInfo = MutableLiveData<StorageInfo>()
    val storageInfo: LiveData<StorageInfo> = _storageInfo

    fun loadStorageInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            // Internal Storage
            val internalPath = Environment.getDataDirectory()
            val internalStat = StatFs(internalPath.path)
            val internalTotal = internalStat.blockCountLong * internalStat.blockSizeLong
            val internalAvailable = internalStat.availableBlocksLong * internalStat.blockSizeLong
            val internalUsed = internalTotal - internalAvailable

            val internalTotalGB = internalTotal / (1024.0 * 1024.0 * 1024.0)
            val internalAvailableGB = internalAvailable / (1024.0 * 1024.0 * 1024.0)
            val internalUsedPercent = ((internalUsed.toDouble() / internalTotal.toDouble()) * 100).toInt()

            // External Storage (SD Card)
            var externalTotalGB: Double? = null
            var externalAvailableGB: Double? = null
            var externalUsedPercent: Int? = null
            var isExternalStorageAvailable = false

            val externalPath = Environment.getExternalStorageDirectory()
            if (externalPath != null) {
                val externalStat = StatFs(externalPath.path)
                val externalTotal = externalStat.blockCountLong * externalStat.blockSizeLong
                val externalAvailable = externalStat.availableBlocksLong * externalStat.blockSizeLong
                val externalUsed = externalTotal - externalAvailable

                externalTotalGB = externalTotal / (1024.0 * 1024.0 * 1024.0)
                externalAvailableGB = externalAvailable / (1024.0 * 1024.0 * 1024.0)
                externalUsedPercent = ((externalUsed.toDouble() / externalTotal.toDouble()) * 100).toInt()
                isExternalStorageAvailable = true
            }

            _storageInfo.postValue(
                StorageInfo(
                    internalTotalGB,
                    internalAvailableGB,
                    internalUsedPercent,
                    externalTotalGB,
                    externalAvailableGB,
                    externalUsedPercent,
                    isExternalStorageAvailable
                )
            )
        }
    }
}