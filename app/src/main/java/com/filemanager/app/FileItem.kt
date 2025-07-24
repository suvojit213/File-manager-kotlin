package com.filemanager.app

import java.io.File

data class FileItem(
    val file: File,
    val name: String,
    val details: String,
    val isDirectory: Boolean,
    val icon: Int,
    var isSelected: Boolean = false
)

