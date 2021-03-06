package com.gago.david.myland.adapters

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.gago.david.myland.LandFragment
import com.gago.david.myland.LandOpenHelper
import com.gago.david.myland.R
import com.gago.david.myland.models.LandObject
import com.gago.david.myland.models.PriorityObject
import de.hdodenhof.circleimageview.CircleImageView

/**
 * [RecyclerView.Adapter] that can display a [LandObject] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 */
class MyLandRecyclerViewAdapter(private val mValues: List<LandObject>, private val mListener: LandFragment.OnListFragmentInteractionListener?, private val priorities: List<PriorityObject>) : RecyclerView.Adapter<MyLandRecyclerViewAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_land, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mItem = mValues[position]
        holder.mIdView.text = holder.mItem!!.name
        holder.mContentView.text = holder.mItem!!.Description
        holder.landImage.setImageBitmap(LandOpenHelper.getImage(holder.mItem!!.imageUri))
        holder.number.text = String.format("%d", holder.mItem!!.notifications)
        var colorChanged = false
        for (p in priorities) if (p.p_order == holder.mItem!!.priority) {
            holder.notification.setImageDrawable(ColorDrawable(Color.parseColor(p.color)))
            colorChanged = true
            break
        }
        if (!colorChanged) holder.notification.setImageDrawable(ColorDrawable(Color.WHITE))
        holder.mView.setOnClickListener { mListener?.onListFragmentInteraction(holder.mItem!!) }
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mIdView: TextView
        val mContentView: TextView
        val landImage: CircleImageView
        val notification: CircleImageView
        val number: TextView
        var mItem: LandObject? = null
        override fun toString(): String {
            return super.toString() + " '" + mContentView.text + "'"
        }

        init {
            mIdView = mView.findViewById(R.id.id)
            mContentView = mView.findViewById(R.id.content)
            landImage = mView.findViewById(R.id.land_image)
            notification = mView.findViewById(R.id.notification)
            number = mView.findViewById(R.id.number)
        }
    }
}