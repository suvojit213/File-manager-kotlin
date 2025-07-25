package com.filemanager.app

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import com.filemanager.app.databinding.DialogFilePropertiesBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class FilePropertiesDialog : DialogFragment() {

    private var _binding: DialogFilePropertiesBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_FILE_ITEM = "file_item"

        fun newInstance(fileItem: FileItem): FilePropertiesDialog {
            val args = Bundle().apply {
                putSerializable(ARG_FILE_ITEM, fileItem)
            }
            return FilePropertiesDialog().apply {
                arguments = args
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogFilePropertiesBinding.inflate(LayoutInflater.from(context))
        val fileItem = arguments?.getSerializable(ARG_FILE_ITEM) as FileItem

        binding.tvPropertyName.text = "Name: ${fileItem.name}"
        binding.tvPropertyPath.text = "Path: ${fileItem.file.absolutePath}"
        binding.tvPropertySize.text = "Size: ${formatSize(fileItem.file.length())}"
        binding.tvPropertyLastModified.text = "Last Modified: ${formatDate(fileItem.file.lastModified())}"
        binding.tvPropertyPermissions.text = "Permissions: ${getPermissionsString(fileItem.file)}"

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun formatSize(size: Long): String {
        if (size <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return String.format("%.2f %s", size / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
    }

    private fun formatDate(timestamp: Long): String {
        val date = Date(timestamp)
        val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        return formatter.format(date)
    }

    private fun getPermissionsString(file: File): String {
        val readable = if (file.canRead()) "R" else "-"
        val writable = if (file.canWrite()) "W" else "-"
        val executable = if (file.canExecute()) "X" else "-"
        return "$readable$writable$executable"
    }
}