package com.gago.david.myland.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.gago.david.myland.R
import com.gago.david.myland.models.PlantTypeObject

class PopupMenuAdapter(context: Context, objects: List<PlantTypeObject?>) : ArrayAdapter<PlantTypeObject?>(context, R.layout.menu_item, objects) {
    private val mListener: OnMenuItemInteractionListener?
    override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
        // Get the data item for this position
        var convertView = convertView
        val plant = getItem(position)
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.menu_item, parent, false)
        }
        // Lookup view for data population
        val icon = convertView.findViewById<ImageView>(R.id.icon)
        val plantText = convertView.findViewById<TextView>(R.id.plant)
        // Populate the data into the template view using the data object
        icon.setImageResource(plant!!.icon)
        icon.setColorFilter(Color.parseColor(plant.color))
        plantText.text = plant.name
        convertView.setOnClickListener { mListener?.onMenuItemInteraction(plant) }
        // Return the completed view to render on screen
        return convertView
    }

    interface OnMenuItemInteractionListener {
        fun onMenuItemInteraction(item: PlantTypeObject?)
    }

    init {
        mListener = context as OnMenuItemInteractionListener
    }
}