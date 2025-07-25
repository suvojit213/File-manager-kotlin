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
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.recyclerview.widget.GridLayoutManager
import android.view.ActionMode
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class FilesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fileAdapter: FileAdapter
    internal lateinit var filesViewModel: FilesViewModel

    private var isGridView = false
    private lateinit var layoutManagerList: LinearLayoutManager
    private lateinit var layoutManagerGrid: GridLayoutManager

    private var actionMode: ActionMode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_files, container, false)
        
        recyclerView = view.findViewById(R.id.recyclerView)
        
        layoutManagerList = LinearLayoutManager(context)
        layoutManagerGrid = GridLayoutManager(context, 3)
        recyclerView.layoutManager = layoutManagerList
        
        fileAdapter = FileAdapter(
            onClick = { fileItem -> onFileClicked(fileItem) },
            onLongClick = { fileItem -> onFileLongClicked(fileItem); true },
            onSelectionChange = { count ->
                if (count == 0) {
                    actionMode?.finish()
                } else {
                    actionMode?.title = "$count selected"
                }
            }
        )
        
        recyclerView.adapter = fileAdapter

        filesViewModel = ViewModelProvider(this).get(FilesViewModel::class.java)

        filesViewModel.files.observe(viewLifecycleOwner) {
            recyclerView.alpha = 0f // Make it invisible initially
            fileAdapter.submitList(it) {
                // Animation after list is submitted and laid out
                recyclerView.animate().alpha(1f).setDuration(300).start()
            }
        }

        filesViewModel.currentPath.observe(viewLifecycleOwner) {
            (activity as? MainActivity)?.updateCurrentPath(it)
        }
        
        filesViewModel.loadFiles(Environment.getExternalStorageDirectory().absolutePath)
        
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.files_menu, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as? androidx.appcompat.widget.SearchView

        searchView?.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filesViewModel.searchFiles(query ?: "")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filesViewModel.searchFiles(newText ?: "")
                return true
            }
        })
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_toggle_layout -> {
                toggleLayout()
                true
            }
            R.id.action_search -> {
                true
            }
            R.id.sort_by_name -> {
                filesViewModel.sortFiles(SortType.NAME)
                true
            }
            R.id.sort_by_date -> {
                filesViewModel.sortFiles(SortType.DATE)
                true
            }
            R.id.sort_by_size -> {
                filesViewModel.sortFiles(SortType.SIZE)
                true
            }
            R.id.sort_by_type -> {
                filesViewModel.sortFiles(SortType.TYPE)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun toggleLayout() {
        isGridView = !isGridView
        if (isGridView) {
            recyclerView.layoutManager = layoutManagerGrid
            activity?.invalidateOptionsMenu() // Invalidate options menu to redraw icons
        } else {
            recyclerView.layoutManager = layoutManagerList
            activity?.invalidateOptionsMenu() // Invalidate options menu to redraw icons
        }
        fileAdapter.notifyDataSetChanged() // Notify adapter of layout change
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

    private fun onFileClicked(fileItem: FileItem) {
        if (actionMode != null) {
            // Agar selection mode on hai, toh click par select/deselect karein
            toggleSelection(fileItem)
        } else {
            // Normal click logic
            if (fileItem.isDirectory) {
                navigateToFolder(fileItem.file.absolutePath)
            } else {
                // ... file open karne ka code ...
            }
        }
    }

    private fun onFileLongClicked(fileItem: FileItem) {
        if (actionMode == null) {
            // Action mode start karein
            actionMode = activity?.startActionMode(actionModeCallback)
        }
        toggleSelection(fileItem)
    }

    private fun toggleSelection(fileItem: FileItem) {
        fileItem.isSelected = !fileItem.isSelected // State toggle karein
        val index = fileAdapter.currentList.indexOf(fileItem)
        if (index != -1) {
            fileAdapter.notifyItemChanged(index) // Sirf uss item ko refresh karein
        }

        val selectedCount = fileAdapter.currentList.count { it.isSelected }
        if (selectedCount == 0) {
            actionMode?.finish()
        } else {
            actionMode?.title = "$selectedCount selected"
            actionMode?.invalidate()
        }
    }

    // ActionModeCallback ke andar onDestroyActionMode mein selection clear karein
    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            mode?.menuInflater?.inflate(R.menu.file_context_menu, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            val selectedFiles = fileAdapter.currentList.filter { it.isSelected }
            val currentPath = getCurrentPath()
            when (item?.itemId) {
                R.id.action_copy -> {
                    FileOperations.selectedFiles = selectedFiles.toList()
                    FileOperations.operationType = OperationType.COPY
                    Toast.makeText(context, "Copied ${selectedFiles.size} items", Toast.LENGTH_SHORT).show()
                    mode?.finish()
                    return true
                }
                R.id.action_cut -> {
                    FileOperations.selectedFiles = selectedFiles.toList()
                    FileOperations.operationType = OperationType.CUT
                    Toast.makeText(context, "Cut ${selectedFiles.size} items", Toast.LENGTH_SHORT).show()
                    mode?.finish()
                    return true
                }
                R.id.action_delete -> {
                    lifecycleScope.launch {
                        FileOperations.deleteFiles(requireContext(), onComplete = {
                            filesViewModel.loadFiles(currentPath)
                            mode?.finish()
                        })
                    }
                    return true
                }
                R.id.action_paste -> {                    // This should ideally be handled by a separate paste button, not in action mode                    // For now, we'll just show a toast if somehow triggered here                    Toast.makeText(context, "Paste action not available here", Toast.LENGTH_SHORT).show()                    return true                }                R.id.action_properties -> {
                    if (selectedFiles.isNotEmpty()) {
                        FilePropertiesDialog.newInstance(selectedFiles.first()).show(parentFragmentManager, "FilePropertiesDialog")
                    }
                    mode?.finish()
                    return true
                }
                R.id.action_compress -> {
                    lifecycleScope.launch {
                        val destinationZipFile = File(currentPath, "archive.zip")
                        FileOperations.zipFiles(requireContext(), destinationZipFile, onComplete = {
                            filesViewModel.loadFiles(currentPath)
                            mode?.finish()
                        })
                    }
                    return true
                }
                R.id.action_extract -> {
                    if (selectedFiles.size == 1 && selectedFiles.first().name.endsWith(".zip", true)) {
                        lifecycleScope.launch {
                            val zipFile = selectedFiles.first().file
                            val destinationDir = File(currentPath)
                            FileOperations.unzipFile(requireContext(), zipFile, destinationDir, onComplete = {
                                filesViewModel.loadFiles(currentPath)
                                mode?.finish()
                            })
                        }
                    } else {
                        Toast.makeText(context, "Please select a single .zip file to extract.", Toast.LENGTH_SHORT).show()
                    }
                    return true
                }
                else -> false
            }
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            // Sabhi selections ko false karein
            fileAdapter.currentList.forEach { it.isSelected = false }
            fileAdapter.notifyDataSetChanged() // Poori list ko refresh karein
            actionMode = null
        }
    }

    companion object {
        fun newInstance(): FilesFragment {
            return FilesFragment()
        }
    }
}
