package com.rohitthebest.passwordsaver.util

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

fun View.show() {

    try {
        this.visibility = View.VISIBLE

    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun View.hide() {

    try {
        this.visibility = View.GONE

    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun View.invisible() {

    try {
        this.visibility = View.INVISIBLE

    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun RecyclerView.changeVisibilityOfFABOnScrolled(fab: FloatingActionButton) {

    this.addOnScrollListener(object : RecyclerView.OnScrollListener() {

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            try {
                if (dy > 0 && fab.visibility == View.VISIBLE) {

                    fab.hide()
                } else if (dy < 0 && fab.visibility != View.VISIBLE) {

                    fab.show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    })
}

fun TextView.changeTextColor(context: Context, color: Int) {

    this.setTextColor(ContextCompat.getColor(context, color))
}

fun EditText.getLength(): Int {

    return this.text.toString().trim().length;
}

fun EditText?.isTextValid(): Boolean {

    return this?.text.toString().isValid()
}

fun String?.isValid(): Boolean {

    return this != null
            && this.trim().isNotEmpty()
            && this.trim().isNotBlank()
            && this.trim() != "null"
}

fun String.shuffle(): String {

    val arr = ArrayList<String>()

    this.forEach {

        arr.add(it.toString())
    }

    arr.shuffle()

    return arr.joinToString("")
}

inline fun EditText.onTextChangedListener(
    crossinline onTextChanged: (s: CharSequence?) -> Unit
) {

    this.addTextChangedListener(object : TextWatcher {

        override fun beforeTextChanged(
            s: CharSequence?,
            start: Int,
            count: Int,
            after: Int
        ) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            onTextChanged(s)
        }

        override fun afterTextChanged(s: Editable?) {}
    })

}

inline fun SeekBar.onProgressChangeListener(
    crossinline onProgressChanged: (progress: Int) -> Unit
) {
    this.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            onProgressChanged(progress)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {

        }

    })

}


inline fun View.showSnackBarWithActionAndDismissListener(
    text: String,
    actionText: String,
    crossinline action: (View) -> Unit,
    crossinline dismissListener: () -> Unit
) {

    Snackbar.make(this, text, Snackbar.LENGTH_LONG)
        .setAction(actionText) {

            action(it)
        }
        .addCallback(object : Snackbar.Callback() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                super.onDismissed(transientBottomBar, event)

                dismissListener()
            }
        })
        .show()
}

inline fun SearchView.searchText(

    crossinline onTextChanged: (newText: String?) -> Unit
) {

    this.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean =
            true

        override fun onQueryTextChange(newText: String?): Boolean {

            onTextChanged(newText)
            return true
        }

    })
}