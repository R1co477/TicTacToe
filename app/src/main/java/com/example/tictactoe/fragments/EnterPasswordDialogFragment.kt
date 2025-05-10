package com.example.tictactoe.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.example.tictactoe.R
import com.example.tictactoe.databinding.DialogFragmentEnterPasswordBinding

class EnterPasswordDialogFragment : DialogFragment() {
    private var _binding: DialogFragmentEnterPasswordBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogFragmentEnterPasswordBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()

        dialog.setOnShowListener {
            setupListeners()
        }

        return dialog
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            window.setBackgroundDrawableResource(R.drawable.bg_dialog_fragment_add_room)

            val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
            window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupListeners() {
        with(binding) {
            btCancel.setOnClickListener {
                dismiss()
            }
            btJoin.setOnClickListener {
                val password = etPassword.text.toString()
                val result = bundleOf(KEY_PASSWORD to password)
                parentFragmentManager.setFragmentResult(REQUEST_KEY, result)
                dismiss()
            }
        }
    }

    companion object {
        @JvmStatic
        val TAG = AddRoomDialogFragment::class.java.simpleName

        @JvmStatic
        val REQUEST_KEY = "$TAG:enterPassword"
        const val KEY_PASSWORD = "KEY_PASSWORD"
    }
}