package com.example.predar.helpers

import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.NonNull
import com.example.predar.R
import kotlinx.android.synthetic.main.reslove_dialog.*

open class ResolveDialog(
    @NonNull context: Context,
    var onPositiveCallback: PositiveButtonListener,
    var title: String,
    var editTextVisibility: Int,
    var positiveButtonVisibility: Int
) : Dialog(context) {

    val DIALOG_SIDE_MARGIN = 20f //size in dp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)

        setContentView(R.layout.reslove_dialog)

        setupDialogLayoutParams()

        rd_title.text = title
        anchor_id.visibility = editTextVisibility
        resolve.visibility = positiveButtonVisibility
        cancel.setOnClickListener { dismiss() }
        resolve.setOnClickListener {
            onPositiveCallback.onPositiveButtonClicked(anchor_id.text.toString())
            dismiss()
        }

    }

    private fun setupDialogLayoutParams() {
        val metrics = context.resources.displayMetrics
        val dialogWindowWidth = metrics.widthPixels - dp2px(context.resources, DIALOG_SIDE_MARGIN).toInt()
        window!!.setLayout(dialogWindowWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    fun dp2px(resources: Resources, dp: Float): Float {
        val scale = resources.displayMetrics.density
        return dp * scale + 0.5f
    }

    interface PositiveButtonListener {
        fun onPositiveButtonClicked(dialogValue: String)
    }

}