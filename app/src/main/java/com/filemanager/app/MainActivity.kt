package com.filemanager.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var fab: FloatingActionButton
    private lateinit var searchEditText: EditText
    private lateinit var toolbar: Toolbar
    
    private var filesFragment: FilesFragment? = null
    private var storageFragment: StorageFragment? = null
    private var recentsFragment: RecentsFragment? = null

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
        private const val MANAGE_EXTERNAL_STORAGE_REQUEST_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_main)
        
        initViews()
        setSupportActionBar(toolbar)
        setupListeners()
        checkPermissions()
        
        // Apply window insets to relevant views
        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { view, insets ->
            val systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(bottomNavigation) { view, insets ->
            val systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        // Load default fragment
        loadFragment(getFilesFragment())
    }

    private fun initViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation)
        fab = findViewById(R.id.fab)
        searchEditText = findViewById(R.id.searchEditText)
        toolbar = findViewById(R.id.toolbar)
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.addCategory("android.intent.category.DEFAULT")
                    intent.data = Uri.parse(String.format("package:%s", applicationContext.packageName))
                    startActivityForResult(intent, MANAGE_EXTERNAL_STORAGE_REQUEST_CODE)
                } catch (e: Exception) {
                    val intent = Intent()
                    intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                    startActivityForResult(intent, MANAGE_EXTERNAL_STORAGE_REQUEST_CODE)
                }
            }
        } else {
            val permissions = mutableListOf<String>()
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            if (permissions.isNotEmpty()) {
                ActivityCompat.requestPermissions(this, permissions.toTypedArray(), PERMISSION_REQUEST_CODE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MANAGE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    // Permissions granted, refresh fragments
                    filesFragment?.let { loadFragment(it) }
                } else {
                    Toast.makeText(this, "Storage permissions are required for this app", Toast.LENGTH_LONG).show()
                }
            }
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