package com.gago.david.myland.adapters

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import com.gago.david.myland.R
import com.gago.david.myland.models.PlantTypeObject

class PopupMenuAdapter(context: Context, objects: List<PlantTypeObject?>) : ArrayAdapter<PlantTypeObject?>(context, R.layout.menu_item, objects) {
    private val mListener: OnMenuItemInteractionListener?
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Get the data item for this position
        var newConvertView = convertView
        val plant = getItem(position)
        // Check if an existing view is being reused, otherwise inflate the view
        if (newConvertView == null) {
            newConvertView = LayoutInflater.from(context).inflate(R.layout.menu_item, parent, false)
        }
        // Lookup view for data population
        val icon = newConvertView?.findViewById<ImageView>(R.id.icon)
        val plantText = newConvertView?.findViewById<TextView>(R.id.plant)
        // Populate the data into the template view using the data object
        icon?.setImageResource(plant!!.icon)
        icon?.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(Color.parseColor(plant?.color), BlendModeCompat.SRC_IN)
        plantText?.text = plant?.name
        newConvertView?.setOnClickListener { mListener?.onMenuItemInteraction(plant) }
        // Return the completed view to render on screen
        return newConvertView!!
    }

    interface OnMenuItemInteractionListener {
        fun onMenuItemInteraction(item: PlantTypeObject?)
    }

    init {
        mListener = context as OnMenuItemInteractionListener
    }
}