package com.filemanager.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var fab: FloatingActionButton
    private lateinit var searchEditText: EditText
    
    private var filesFragment: FilesFragment? = null
    private var storageFragment: StorageFragment? = null
    private var recentsFragment: RecentsFragment? = null

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        initViews()
        setupListeners()
        checkPermissions()
        
        // Load default fragment
        loadFragment(getFilesFragment())
    }

    private fun initViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation)
        fab = findViewById(R.id.fab)
        searchEditText = findViewById(R.id.searchEditText)
    }

    private fun setupListeners() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_recents -> {
                    loadFragment(getRecentsFragment())
                    true
                }
                R.id.nav_files -> {
                    loadFragment(getFilesFragment())
                    true
                }
                R.id.nav_storage -> {
                    loadFragment(getStorageFragment())
                    true
                }
                else -> false
            }
        }
        
        fab.setOnClickListener {
            showCreateNewDialog()
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun getFilesFragment(): FilesFragment {
        if (filesFragment == null) {
            filesFragment = FilesFragment.newInstance()
        }
        return filesFragment!!
    }

    private fun getStorageFragment(): StorageFragment {
        if (storageFragment == null) {
            storageFragment = StorageFragment.newInstance()
        }
        return storageFragment!!
    }

    private fun getRecentsFragment(): RecentsFragment {
        if (recentsFragment == null) {
            recentsFragment = RecentsFragment.newInstance()
        }
        return recentsFragment!!
    }

    private fun showCreateNewDialog() {
        val dialog = CreateNewDialog(this) { title, isFolder ->
            createNewItem(title, isFolder)
        }
        dialog.show()
    }

    private fun createNewItem(title: String, isFolder: Boolean) {
        val currentPath = filesFragment?.getCurrentPath() ?: Environment.getExternalStorageDirectory().absolutePath
        val newFile = File(currentPath, title)
        
        try {
            if (isFolder) {
                if (newFile.mkdirs()) {
                    Toast.makeText(this, "Folder created successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to create folder", Toast.LENGTH_SHORT).show()
                }
            } else {
                if (newFile.createNewFile()) {
                    Toast.makeText(this, "File created successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to create file", Toast.LENGTH_SHORT).show()
                }
            }
            
            // Refresh the files list
            filesFragment?.let { fragment ->
                // Reload files in the fragment
                val method = fragment.javaClass.getDeclaredMethod("loadFiles")
                method.isAccessible = true
                method.invoke(fragment)
            }
            
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateCurrentPath(path: String) {
        // Update UI to show current path
        supportActionBar?.subtitle = path.substringAfterLast("/")
    }

    private fun checkPermissions() {
        val permissions = mutableListOf<String>()
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
            != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) 
            != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                // Request MANAGE_EXTERNAL_STORAGE permission for Android 11+
                Toast.makeText(this, "Please grant storage management permission", Toast.LENGTH_LONG).show()
            }
        }
        
        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Permissions granted, refresh fragments
                filesFragment?.let { loadFragment(it) }
            } else {
                Toast.makeText(this, "Storage permissions are required for this app", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        if (currentFragment is FilesFragment) {
            if (!currentFragment.navigateUp()) {
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
    }
}

