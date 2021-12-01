package com.gago.david.myland.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.gago.david.myland.LandEditActivity
import com.gago.david.myland.R
import com.gago.david.myland.models.PlantTypeObject

class AddItemDialogFragment(val item: PlantTypeObject) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view: View = layoutInflater.inflate(R.layout.add_item_dialog, null)
        val description = view.findViewById<EditText>(R.id.description)

        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
            .setPositiveButton("Ok") { _, _ -> (activity as LandEditActivity).addItemDialogOkButton(description.text.toString(), item.name) }
            .setNegativeButton(R.string.cancel) { _, _ ->  }
        val icon = view.findViewById<ImageView>(R.id.icon)
        icon.setImageResource(item.icon)
        icon.colorFilter = PorterDuffColorFilter(Color.parseColor(item.color), PorterDuff.Mode.SRC_IN)
        view.findViewById<TextView>(R.id.title).text = item.name

        builder.setView(view)
        return builder.create()
    }
}