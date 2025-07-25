package com.filemanager.app

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.io.FileInputStream
import java.io.BufferedInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

enum class OperationType { COPY, CUT }

object FileOperations {

    var selectedFiles: List<FileItem> = emptyList()
    var operationType: OperationType? = null

    suspend fun copyFiles(context: Context, destinationDir: File, onComplete: () -> Unit) {
        if (selectedFiles.isEmpty()) return

        withContext(Dispatchers.IO) {
            selectedFiles.forEach { fileItem ->
                val sourceFile = fileItem.file
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
                val sourceFile = fileItem.file
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
                val fileToDelete = fileItem.file
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

    suspend fun zipFiles(context: Context, destinationZipFile: File, onComplete: () -> Unit) {
        if (selectedFiles.isEmpty()) return

        withContext(Dispatchers.IO) {
            try {
                ZipOutputStream(BufferedOutputStream(FileOutputStream(destinationZipFile))).use { output ->
                    selectedFiles.forEach { fileItem ->
                        zipFile(fileItem.file, output, "")
                    }
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Compression complete: ${destinationZipFile.name}", Toast.LENGTH_SHORT).show()
                    onComplete()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to compress files: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun zipFile(file: File, output: ZipOutputStream, parentPath: String) {
        val entryName = if (parentPath.isEmpty()) file.name else "$parentPath/${file.name}"
        if (file.isDirectory) {
            val entry = ZipEntry("$entryName/")
            output.putNextEntry(entry)
            output.closeEntry()
            file.listFiles()?.forEach { childFile ->
                zipFile(childFile, output, entryName)
            }
        } else {
            val entry = ZipEntry(entryName)
            output.putNextEntry(entry)
            FileInputStream(file).use { input ->
                input.copyTo(output)
            }
            output.closeEntry()
        }
    }

    suspend fun unzipFile(context: Context, zipFile: File, destinationDir: File, onComplete: () -> Unit) {
        if (!zipFile.exists() || !zipFile.isFile) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Invalid zip file.", Toast.LENGTH_SHORT).show()
            }
            return
        }

        withContext(Dispatchers.IO) {
            try {
                ZipInputStream(BufferedInputStream(FileInputStream(zipFile))).use { zipInput ->
                    var entry: ZipEntry?
                    while (zipInput.nextEntry.also { entry = it } != null) {
                        val entryFile = File(destinationDir, entry!!.name)
                        if (entry!!.isDirectory) {
                            entryFile.mkdirs()
                        } else {
                            entryFile.parentFile?.mkdirs()
                            FileOutputStream(entryFile).use { output ->
                                zipInput.copyTo(output)
                            }
                        }
                        zipInput.closeEntry()
                    }
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Extraction complete to ${destinationDir.name}", Toast.LENGTH_SHORT).show()
                    onComplete()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to extract file: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun clearOperations() {
        selectedFiles = emptyList()
        operationType = null
    }
}