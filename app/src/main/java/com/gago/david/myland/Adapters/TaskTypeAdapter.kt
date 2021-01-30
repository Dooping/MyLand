package com.gago.david.myland.adapters

import android.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.gago.david.myland.R
import com.gago.david.myland.SettingsFragment.OnTaskListFragmentInteractionListener
import com.gago.david.myland.models.TaskTypeObject
import de.hdodenhof.circleimageview.CircleImageView

/**
 * [RecyclerView.Adapter] that can display a [TaskTypeObject] and makes a call to the
 * specified [OnTaskListFragmentInteractionListener].
 */
class TaskTypeAdapter(private val mValues: MutableList<TaskTypeObject>, private val mListener: OnTaskListFragmentInteractionListener?) : RecyclerView.Adapter<TaskTypeAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.task_type_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mItem = mValues[position]
        holder.task.text = mValues[position].name
        holder.description.text = mValues[position].description
        holder.remove.setOnClickListener { view ->
            val alertDialog = AlertDialog.Builder(view.rootView.context)
            alertDialog.setTitle(holder.mItem!!.name)
            alertDialog.setMessage(R.string.remove_task)
            alertDialog.setPositiveButton(R.string.yes
            ) { _, _ ->
                mListener!!.removeItem(holder.mItem!!)
                mValues.remove(holder.mItem!!)
                notifyDataSetChanged()
            }
            alertDialog.setNegativeButton(R.string.no
            ) { dialog, _ -> dialog.cancel() }
            alertDialog.show()
        }
        holder.mView.setOnClickListener { mListener?.selectItem(holder.mItem!!) }
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val task: TextView
        val description: TextView
        val remove: CircleImageView
        var mItem: TaskTypeObject? = null
        override fun toString(): String {
            return super.toString() + " '" + task.text + "'"
        }

        init {
            task = mView.findViewById(R.id.task)
            description = mView.findViewById(R.id.description)
            remove = mView.findViewById(R.id.remove)
        }
    }
}