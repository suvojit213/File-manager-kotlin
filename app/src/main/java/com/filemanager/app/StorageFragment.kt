package com.filemanager.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.filemanager.app.databinding.FragmentStorageBinding

class StorageFragment : Fragment() {

    private var _binding: FragmentStorageBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: StorageViewModel
    private lateinit var storageAdapter: StorageLocationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStorageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(StorageViewModel::class.java)

        setupRecyclerView()
        observeStorageVolumes()
    }

    private fun setupRecyclerView() {
        storageAdapter = StorageLocationAdapter(
            onClick = { storageVolume ->
                // Navigate to FilesFragment with the selected storage path
                val action = StorageFragmentDirections.actionStorageFragmentToFilesFragment(storageVolume.path)
                findNavController().navigate(action)
            },
            formatSize = { size -> viewModel.formatSize(size) }
        )
        binding.rvStorageLocations.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = storageAdapter
        }
    }

    private fun observeStorageVolumes() {
        viewModel.storageVolumes.observe(viewLifecycleOwner) {
            storageAdapter.submitList(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}