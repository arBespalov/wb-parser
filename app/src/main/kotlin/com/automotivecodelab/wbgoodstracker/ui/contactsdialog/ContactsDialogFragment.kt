package com.automotivecodelab.wbgoodstracker.ui.contactsdialog

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.automotivecodelab.wbgoodstracker.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ContactsDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setView(R.layout.contacts_dialog_layout)
            .create()
    }
}
