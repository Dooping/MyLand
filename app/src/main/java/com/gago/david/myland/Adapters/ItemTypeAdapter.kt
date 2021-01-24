package com.gago.david.myland.adapters

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.gago.david.myland.R
import com.gago.david.myland.SettingsFragment
import com.gago.david.myland.models.PlantTypeObject
import de.hdodenhof.circleimageview.CircleImageView

/**
 * [RecyclerView.Adapter] that can display a [PlantTypeObject] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 */
class ItemTypeAdapter(private val mValues: MutableList<PlantTypeObject>, private val mListener: SettingsFragment.OnListFragmentInteractionListener?) : RecyclerView.Adapter<ItemTypeAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.plant_type_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mItem = mValues[position]
        holder.name.text = mValues[position].name
        holder.icon.setImageResource(mValues[position].icon)
        holder.icon.setColorFilter(Color.parseColor(mValues[position].color))
        holder.remove.setOnClickListener { view ->
            val alertDialog = AlertDialog.Builder(view.rootView.context)
            alertDialog.setTitle(holder.mItem!!.name)
            alertDialog.setMessage(R.string.remove_item)
            val filter: ColorFilter = PorterDuffColorFilter(Color.parseColor(holder.mItem!!.color), PorterDuff.Mode.SRC_IN)
            val icon = view.rootView.context.resources.getDrawable(holder.mItem!!.icon)
            icon.colorFilter = filter
            alertDialog.setIcon(icon)
            alertDialog.setPositiveButton(R.string.yes
            ) { dialog, which ->
                mListener!!.removeItem(holder.mItem)
                mValues.remove(holder.mItem!!)
                notifyDataSetChanged()
            }
            alertDialog.setNegativeButton(R.string.no
            ) { dialog, which -> dialog.cancel() }
            alertDialog.show()
        }
        holder.mView.setOnClickListener { mListener?.selectItem(holder.mItem) }
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val name: TextView
        val icon: CircleImageView
        val remove: CircleImageView
        var mItem: PlantTypeObject? = null
        override fun toString(): String {
            return super.toString() + " '" + name.text + "'"
        }

        init {
            name = mView.findViewById(R.id.plant)
            icon = mView.findViewById(R.id.icon)
            remove = mView.findViewById(R.id.remove)
        }
    }
}