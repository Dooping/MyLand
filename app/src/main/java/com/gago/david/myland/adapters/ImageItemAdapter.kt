package com.gago.david.myland.adapters

import android.graphics.PorterDuff
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.gago.david.myland.R

class ImageItemAdapter(private val mValues: List<Int>, private val colorTint: Int) : RecyclerView.Adapter<ImageItemAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.image_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mItem = mValues[position]
        holder.mIdView.setImageResource(mValues[position])
        holder.mIdView.setColorFilter(colorTint, PorterDuff.Mode.SRC_IN)
        holder.mView.setOnClickListener { /*if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }*/
        }
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mIdView: ImageView
        var mItem = 0
        override fun toString(): String {
            return super.toString() + " '" + mIdView.toString() + "'"
        }

        init {
            mIdView = mView.findViewById(R.id.image)
        }
    }
}