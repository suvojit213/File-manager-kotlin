package com.filemanager.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider

class StorageFragment : Fragment() {

    private lateinit var internalStorageText: TextView
    private lateinit var internalStorageProgress: ProgressBar
    private lateinit var sdCardStorageText: TextView
    private lateinit var sdCardStorageProgress: ProgressBar
    private lateinit var storageViewModel: StorageViewModel

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

        storageViewModel = ViewModelProvider(this).get(StorageViewModel::class.java)

        storageViewModel.storageInfo.observe(viewLifecycleOwner) {
            internalStorageText.text = String.format("%.1f GB of %.1f GB Free", it.internalAvailable, it.internalTotal)
            internalStorageProgress.progress = it.internalUsedPercent

            if (it.isExternalStorageAvailable) {
                sdCardStorageText.text = String.format("%.1f GB of %.1f GB Free", it.externalAvailable, it.externalTotal)
                sdCardStorageProgress.progress = it.externalUsedPercent!!
            } else {
                sdCardStorageText.text = "No SD Card detected"
                sdCardStorageProgress.progress = 0
            }
        }
        
        storageViewModel.loadStorageInfo()
        
        return view
    }
}