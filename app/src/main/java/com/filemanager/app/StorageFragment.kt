package com.filemanager.app

import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment

class StorageFragment : Fragment() {

    private lateinit var internalStorageText: TextView
    private lateinit var internalStorageProgress: ProgressBar
    private lateinit var sdCardStorageText: TextView
    private lateinit var sdCardStorageProgress: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_storage, container, false)
        
        internalStorageText = view.findViewById(R.id.internalStorageText)
        internalStorageProgress = view.findViewById(R.id.internalStorageProgress)
        sdCardStorageText = view.findViewById(R.id.sdCardStorageText)
        sdCardStorageProgress = view.findViewById(R.id.sdCardStorageProgress)
        
        updateStorageInfo()
        
        return view
    }

    private fun updateStorageInfo() {
        // Internal Storage
        val internalPath = Environment.getDataDirectory()
        val internalStat = StatFs(internalPath.path)
        val internalTotal = internalStat.blockCountLong * internalStat.blockSizeLong
        val internalAvailable = internalStat.availableBlocksLong * internalStat.blockSizeLong
        val internalUsed = internalTotal - internalAvailable
        
        val internalTotalGB = internalTotal / (1024.0 * 1024.0 * 1024.0)
        val internalAvailableGB = internalAvailable / (1024.0 * 1024.0 * 1024.0)
        val internalUsedPercent = ((internalUsed.toDouble() / internalTotal.toDouble()) * 100).toInt()
        
        internalStorageText.text = String.format("%.1f GB of %.1f GB Free", internalAvailableGB, internalTotalGB)
        internalStorageProgress.progress = internalUsedPercent
        
        // External Storage (SD Card)
        val externalPath = Environment.getExternalStorageDirectory()
        if (externalPath != null) {
            val externalStat = StatFs(externalPath.path)
            val externalTotal = externalStat.blockCountLong * externalStat.blockSizeLong
            val externalAvailable = externalStat.availableBlocksLong * externalStat.blockSizeLong
            val externalUsed = externalTotal - externalAvailable
            
            val externalTotalGB = externalTotal / (1024.0 * 1024.0 * 1024.0)
            val externalAvailableGB = externalAvailable / (1024.0 * 1024.0 * 1024.0)
            val externalUsedPercent = ((externalUsed.toDouble() / externalTotal.toDouble()) * 100).toInt()
            
            sdCardStorageText.text = String.format("%.1f GB of %.1f GB Free", externalAvailableGB, externalTotalGB)
            sdCardStorageProgress.progress = externalUsedPercent
        } else {
            sdCardStorageText.text = "No SD Card detected"
            sdCardStorageProgress.progress = 0
        }
    }

    companion object {
        fun newInstance(): StorageFragment {
            return StorageFragment()
        }
    }
}

