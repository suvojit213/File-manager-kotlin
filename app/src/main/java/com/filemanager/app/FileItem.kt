package com.filemanager.app

data class FileItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val lastModified: Long,
    val size: Long = 0L,
    val itemCount: Int = 0,
    val extension: String = ""
)

