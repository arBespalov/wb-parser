package com.automotivecodelab.wbgoodstracker.ui.errordialogfrag

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.navigation.fragment.navArgs
import com.automotivecodelab.wbgoodstracker.BuildConfig
import com.automotivecodelab.wbgoodstracker.R
import com.automotivecodelab.wbgoodstracker.data.NoInternetConnectionException
import com.automotivecodelab.wbgoodstracker.domain.AddItemUseCase
import com.automotivecodelab.wbgoodstracker.domain.ItemsQuotaExceededException
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ErrorDialogFragment : BottomSheetDialogFragment() {

    private val args: ErrorDialogFragmentArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val errorMessage = when {
            BuildConfig.DEBUG -> args.throwable.message.toString()
            args.throwable is NoInternetConnectionException -> getString(R.string.no_connection)
            args.throwable is ItemsQuotaExceededException -> getString(
                R.string.items_count_quota,
                AddItemUseCase.itemsCountLimit.toString()
            )
            else -> getString(R.string.error_body)
        }
        val bottomSheetDialog = BottomSheetDialog(requireContext()).apply {
            setContentView(R.layout.error_bottom_sheet)
            findViewById<TextView>(R.id.text)?.text = errorMessage
            findViewById<Button>(R.id.ok)?.setOnClickListener { dismiss() }
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        return bottomSheetDialog
    }
}
