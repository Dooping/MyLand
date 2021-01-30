package com.gago.david.myland.adapters

import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import com.gago.david.myland.R
import java.util.*

class ImageAdapter(context: Context, private val list: ArrayList<Int>, private val tintColor: Int) : ArrayAdapter<Int?>(context, R.layout.image_item, list as List<Int?>) {
    private class ViewHolder {
        var image: ImageView? = null
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Get the data item for this position
        var newConvertView = convertView
        val dataModel = list[position]
        // Check if an existing view is being reused, otherwise inflate the view
        val viewHolder: ViewHolder // view lookup cache stored in tag
        if (newConvertView == null) {
            //Log.v("adapter", "null");
            viewHolder = ViewHolder()
            val inflater = LayoutInflater.from(getContext())
            newConvertView = inflater.inflate(R.layout.image_item, parent, false)
            viewHolder.image = newConvertView.findViewById(R.id.image)
            newConvertView.tag = viewHolder
        } else {
            //Log.v("adapter", "not null");
            viewHolder = newConvertView.tag as ViewHolder
        }


        //Log.v("adapter", " "+dataModel);
        viewHolder.image!!.setImageResource(dataModel)
        viewHolder.image!!.setColorFilter(tintColor, PorterDuff.Mode.SRC_IN)
        // Return the completed view to render on screen
        return newConvertView!!
    }
}