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
import com.gago.david.myland.AddLandActivity
import com.gago.david.myland.LandEditMapActivity
import com.gago.david.myland.R
import com.gago.david.myland.models.PlantTypeObject
import com.mapbox.geojson.Point

class AddLandDetailsDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view: View = layoutInflater.inflate(R.layout.add_land_dialog, null)
        val state = view.findViewById<EditText>(R.id.state)
        val name = view.findViewById<EditText>(R.id.name)

        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
            .setPositiveButton("Ok") { _, _ -> (activity as AddLandActivity).addLandDetailsCallback(name.text.toString(), state.text.toString()) }
            .setNegativeButton(R.string.cancel) { _, _ ->  (activity as AddLandActivity).addLandDetailsCancel()}

        builder.setView(view)
        return builder.create()
    }
}