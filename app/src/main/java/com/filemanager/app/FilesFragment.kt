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

class FilesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fileAdapter: FileAdapter
    private var currentPath: String = Environment.getExternalStorageDirectory().absolutePath

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_files, container, false)
        
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        
        fileAdapter = FileAdapter(emptyList()) { fileItem ->
            if (fileItem.isDirectory) {
                navigateToFolder(fileItem.path)
            }
        }
        
        recyclerView.adapter = fileAdapter
        
        loadFiles()
        
        return view
    }

    private fun loadFiles() {
        val directory = File(currentPath)
        val files = mutableListOf<FileItem>()
        
        try {
            directory.listFiles()?.forEach { file ->
                val itemCount = if (file.isDirectory) {
                    file.listFiles()?.size ?: 0
                } else 0
                
                files.add(
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
        val sortedFiles = files.sortedWith(compareBy<FileItem> { !it.isDirectory }.thenBy { it.name.lowercase() })
        
        fileAdapter.updateFiles(sortedFiles)
    }

    private fun navigateToFolder(path: String) {
        currentPath = path
        loadFiles()
        
        // Update activity title or breadcrumb
        (activity as? MainActivity)?.updateCurrentPath(path)
    }

    fun navigateUp(): Boolean {
        val parentFile = File(currentPath).parentFile
        return if (parentFile != null && parentFile.canRead()) {
            currentPath = parentFile.absolutePath
            loadFiles()
            (activity as? MainActivity)?.updateCurrentPath(currentPath)
            true
        } else {
            false
        }
    }

    fun getCurrentPath(): String = currentPath

    companion object {
        fun newInstance(): FilesFragment {
            return FilesFragment()
        }
    }
}

