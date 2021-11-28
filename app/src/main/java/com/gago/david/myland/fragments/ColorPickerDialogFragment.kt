package com.gago.david.myland.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.gago.david.myland.R
import android.app.AlertDialog
import android.graphics.Color
import com.gago.david.myland.MainActivity
import com.flask.colorpicker.ColorPickerView

class ColorPickerDialogFragment(private val initialColor: String?)  : DialogFragment() {
    var color = Color.parseColor(initialColor)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
            .setTitle(R.string.color_picker_title)
            .setPositiveButton("Ok") { _, _ -> (activity as MainActivity).onColorSelected(color) }
            .setNegativeButton(R.string.cancel) { _, _ ->  }
        val view: View = layoutInflater.inflate(R.layout.color_picker_dialog, null)
        val colorPickerView: ColorPickerView = view.findViewById(R.id.color_picker_view)
        colorPickerView.addOnColorChangedListener { selectedColor -> // Handle on color change
            color = selectedColor
        }
        colorPickerView.setColor(Color.parseColor(initialColor), true)
        colorPickerView.setInitialColor(Color.parseColor(initialColor), true)
        builder.setView(view)

        return builder.create()
    }
}