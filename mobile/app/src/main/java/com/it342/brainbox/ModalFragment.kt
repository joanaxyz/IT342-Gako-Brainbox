package com.it342.brainbox

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment

class ModalFragment : DialogFragment() {

    private var title: String? = null
    private var message: String? = null
    private var positiveText: String? = null
    private var negativeText: String? = null
    private var onPositiveClick: (() -> Unit)? = null
    private var onNegativeClick: (() -> Unit)? = null

    companion object {
        fun newInstance(
            title: String,
            message: String,
            positiveText: String = "OK",
            negativeText: String = "Cancel"
        ): ModalFragment {
            val fragment = ModalFragment()
            val args = Bundle().apply {
                putString("title", title)
                putString("message", message)
                putString("positiveText", positiveText)
                putString("negativeText", negativeText)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            title = it.getString("title")
            message = it.getString("message")
            positiveText = it.getString("positiveText")
            negativeText = it.getString("negativeText")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_modal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        view.findViewById<TextView>(R.id.modal_title).text = title
        view.findViewById<TextView>(R.id.modal_message).text = message
        
        val btnPositive = view.findViewById<Button>(R.id.btn_positive)
        btnPositive.text = positiveText
        btnPositive.setOnClickListener {
            onPositiveClick?.invoke()
            dismiss()
        }
        
        val btnNegative = view.findViewById<Button>(R.id.btn_negative)
        btnNegative.text = negativeText
        btnNegative.setOnClickListener {
            onNegativeClick?.invoke()
            dismiss()
        }
    }

    fun setPositiveListener(listener: () -> Unit) {
        onPositiveClick = listener
    }

    fun setNegativeListener(listener: () -> Unit) {
        onNegativeClick = listener
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}
