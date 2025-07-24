package com.filemanager.app

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

enum class OperationType { COPY, CUT }

object FileOperations {

    var selectedFiles: List<FileItem> = emptyList()
    var operationType: OperationType? = null

    suspend fun copyFiles(context: Context, destinationDir: File, onComplete: () -> Unit) {
        if (selectedFiles.isEmpty()) return

        withContext(Dispatchers.IO) {
            selectedFiles.forEach { fileItem ->
                val sourceFile = File(fileItem.path)
                val destinationFile = File(destinationDir, sourceFile.name)
                try {
                    sourceFile.copyTo(destinationFile, overwrite = true)
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to copy ${sourceFile.name}: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Copy complete", Toast.LENGTH_SHORT).show()
                onComplete()
            }
        }
    }

    suspend fun cutFiles(context: Context, destinationDir: File, onComplete: () -> Unit) {
        if (selectedFiles.isEmpty()) return

        withContext(Dispatchers.IO) {
            selectedFiles.forEach { fileItem ->
                val sourceFile = File(fileItem.path)
                val destinationFile = File(destinationDir, sourceFile.name)
                try {
                    sourceFile.copyTo(destinationFile, overwrite = true)
                    sourceFile.delete()
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to move ${sourceFile.name}: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Move complete", Toast.LENGTH_SHORT).show()
                onComplete()
            }
        }
    }

    suspend fun deleteFiles(context: Context, onComplete: () -> Unit) {
        if (selectedFiles.isEmpty()) return

        withContext(Dispatchers.IO) {
            selectedFiles.forEach { fileItem ->
                val fileToDelete = File(fileItem.path)
                try {
                    if (fileToDelete.isDirectory) {
                        fileToDelete.deleteRecursively()
                    } else {
                        fileToDelete.delete()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to delete ${fileToDelete.name}: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Delete complete", Toast.LENGTH_SHORT).show()
                onComplete()
            }
        }
    }

    fun clearOperations() {
        selectedFiles = emptyList()
        operationType = null
    }
}