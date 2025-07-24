package com.filemanager.app

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CreateNewDialog(
    context: Context,
    private val onCreateCallback: (String, Boolean) -> Unit
) : Dialog(context) {

    private lateinit var titleEditText: EditText
    private lateinit var typeRadioGroup: RadioGroup
    private lateinit var folderRadioButton: RadioButton
    private lateinit var fileRadioButton: RadioButton
    private lateinit var cancelButton: Button
    private lateinit var okButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_create_new)
        
        initViews()
        setupListeners()
    }

    private fun initViews() {
        titleEditText = findViewById(R.id.titleEditText)
        typeRadioGroup = findViewById(R.id.typeRadioGroup)
        folderRadioButton = findViewById(R.id.folderRadioButton)
        fileRadioButton = findViewById(R.id.fileRadioButton)
        cancelButton = findViewById(R.id.cancelButton)
        okButton = findViewById(R.id.okButton)
    }

    private fun setupListeners() {
        cancelButton.setOnClickListener {
            dismiss()
        }
        
        okButton.setOnClickListener {
            val title = titleEditText.text.toString().trim()
            if (title.isNotEmpty()) {
                val isFolder = folderRadioButton.isChecked
                onCreateCallback(title, isFolder)
                dismiss()
            }
        }
    }
}

