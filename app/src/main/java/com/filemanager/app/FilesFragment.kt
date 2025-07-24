package com.filemanager.app

import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class FilesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fileAdapter: FileAdapter
    internal lateinit var filesViewModel: FilesViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_files, container, false)
        
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        
        fileAdapter = FileAdapter { fileItem ->
            if (fileItem.isDirectory) {
                navigateToFolder(fileItem.path)
            }
        }
        
        recyclerView.adapter = fileAdapter

        filesViewModel = ViewModelProvider(this).get(FilesViewModel::class.java)

        filesViewModel.files.observe(viewLifecycleOwner) {
            fileAdapter.submitList(it)
        }

        filesViewModel.currentPath.observe(viewLifecycleOwner) {
            (activity as? MainActivity)?.updateCurrentPath(it)
        }
        
        filesViewModel.loadFiles(Environment.getExternalStorageDirectory().absolutePath)
        
        return view
    }

    private fun navigateToFolder(path: String) {
        filesViewModel.loadFiles(path)
    }

    fun navigateUp(): Boolean {
        val currentPath = filesViewModel.currentPath.value ?: return false
        val parentFile = File(currentPath).parentFile
        return if (parentFile != null && parentFile.canRead()) {
            filesViewModel.loadFiles(parentFile.absolutePath)
            true
        } else {
            false
        }
    }

    fun getCurrentPath(): String = filesViewModel.currentPath.value ?: Environment.getExternalStorageDirectory().absolutePath

    companion object {
        fun newInstance(): FilesFragment {
            return FilesFragment()
        }
    }
}

