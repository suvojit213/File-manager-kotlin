package com.filemanager.app

import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class RecentsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fileAdapter: FileAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_files, container, false)
        
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        
        fileAdapter = FileAdapter(emptyList()) { fileItem ->
            // Handle file click
        }
        
        recyclerView.adapter = fileAdapter
        
        loadRecentFiles()
        
        return view
    }

    private fun loadRecentFiles() {
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
        
        fileAdapter.updateFiles(sortedFiles)
    }

    companion object {
        fun newInstance(): RecentsFragment {
            return RecentsFragment()
        }
    }
}

