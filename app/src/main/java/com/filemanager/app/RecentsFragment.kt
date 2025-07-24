package com.filemanager.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class RecentsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fileAdapter: FileAdapter
    private lateinit var recentsViewModel: RecentsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_files, container, false)
        
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        
        fileAdapter = FileAdapter { fileItem ->
            // Handle file click
        }
        
        recyclerView.adapter = fileAdapter

        recentsViewModel = ViewModelProvider(this).get(RecentsViewModel::class.java)

        recentsViewModel.recentFiles.observe(viewLifecycleOwner) {
            fileAdapter.submitList(it)
        }
        
        recentsViewModel.loadRecentFiles()
        
        return view
    }

    companion object {
        fun newInstance(): RecentsFragment {
            return RecentsFragment()
        }
    }
}

