package com.example.tictactoe.utils

import android.graphics.Typeface
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.example.tictactoe.R


object SnackBarUtils {

    fun showCustomSnackBar(
        view: View, message: String, actionText: String, backgroundColor: Int
    ) {
        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
            .setAction(actionText) { Log.d("Snackbar", "Action clicked") }
            .setBackgroundTint(backgroundColor)
            .setActionTextColor(ContextCompat.getColor(view.context, R.color.white))
            .setTextColor(ContextCompat.getColor(view.context, R.color.white))

        val snackbarView = snackbar.view
        val textView =
            snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.apply {
            textSize = 18f
            setTypeface(typeface, Typeface.BOLD)
        }

        val params = snackbarView.layoutParams as ViewGroup.MarginLayoutParams
        params.setMargins(30, 30, 30, 50)
        snackbarView.layoutParams = params

        val actionTextView =
            snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_action)
        actionTextView.apply {
            textSize = 18f
            setTypeface(typeface, Typeface.BOLD)
        }

        snackbar.show()
    }
}
