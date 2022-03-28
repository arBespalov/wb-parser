package com.automotivecodelab.wbgoodstracker.ui.errordialogfrag

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.navigation.fragment.navArgs
import com.automotivecodelab.wbgoodstracker.BuildConfig
import com.automotivecodelab.wbgoodstracker.R
import com.automotivecodelab.wbgoodstracker.data.NoInternetConnectionException
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ErrorDialogFragment : BottomSheetDialogFragment() {

    private val args: ErrorDialogFragmentArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(R.layout.error_bottom_sheet)

        val errorMessage = when {
            BuildConfig.DEBUG -> args.throwable.message.toString()
            args.throwable is NoInternetConnectionException -> getString(R.string.no_connection)
            else -> getString(R.string.error_body)
        }

        bottomSheetDialog.findViewById<TextView>(R.id.text)?.text = errorMessage
        bottomSheetDialog.findViewById<Button>(R.id.ok)?.setOnClickListener { dismiss() }

        bottomSheetDialog.findViewById<FrameLayout>(
            com.google.android.material.R.id.design_bottom_sheet
        )?.let {
            BottomSheetBehavior.from(it).state = BottomSheetBehavior.STATE_EXPANDED
        }
        return bottomSheetDialog
    }
}
