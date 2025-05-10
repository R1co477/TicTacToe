package com.example.tictactoe.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.example.tictactoe.R
import com.example.tictactoe.databinding.DialogFragmentAddRoomBinding

class AddRoomDialogFragment : DialogFragment() {

    private var _binding: DialogFragmentAddRoomBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogFragmentAddRoomBinding.inflate(layoutInflater)
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
            btCreate.setOnClickListener {
                val name = etRoomName.text.toString()

                if (name.isBlank()) {
                    etRoomName.error = getString(R.string.error_room_name_required)
                    return@setOnClickListener
                }

                val isProtected = !checkBoxPassword.isChecked
                val password = etPassword.text.toString()

                val result = bundleOf(
                    KEY_NAME to name,
                    KEY_IS_PROTECTED to isProtected,
                    KEY_PASSWORD to password
                )
                parentFragmentManager.setFragmentResult(REQUEST_KEY, result)
                dismiss()
            }

            btCancel.setOnClickListener {
                dismiss()
            }

            checkBoxPassword.setOnCheckedChangeListener { _, isChecked ->
                val visibility = if (isChecked) View.VISIBLE else View.GONE
                tvPassword.visibility = visibility
                etPassword.visibility = visibility
            }
        }
    }

    companion object {
        @JvmStatic
        val TAG = AddRoomDialogFragment::class.java.simpleName

        @JvmStatic
        val REQUEST_KEY = "$TAG:addRoom"
        const val KEY_NAME = "NAME"
        const val KEY_IS_PROTECTED = "IS_PROTECTED"
        const val KEY_PASSWORD = "PASSWORD"
    }
}