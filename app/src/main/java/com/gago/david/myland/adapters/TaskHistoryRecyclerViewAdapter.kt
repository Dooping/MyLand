package com.gago.david.myland.adapters

import androidx.recyclerview.widget.RecyclerView
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.gago.david.myland.models.TaskObject

import com.gago.david.myland.databinding.TaskItemBinding
import de.hdodenhof.circleimageview.CircleImageView

/**
 * [RecyclerView.Adapter] that can display a [TaskObject].
 * Used to display task history
 */
class TaskHistoryRecyclerViewAdapter(
    private val values: List<TaskObject>
) : RecyclerView.Adapter<TaskHistoryRecyclerViewAdapter.ViewHolder>() {

    private lateinit var dateFormat: java.text.DateFormat

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        dateFormat = DateFormat.getDateFormat(parent.context)
        return ViewHolder(
            TaskItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.idView.text = item.taskType
        holder.idView.isEnabled = false
        holder.contentView.text = item.observations
        holder.contentView.isEnabled = false
        if (item.completedDate != null) {
            holder.dateView.text = dateFormat.format(item.completedDate!!)
            holder.dateView.isEnabled = false
        }
        holder.notificationView.visibility = View.GONE
    }

    override fun getItemCount(): Int {
        return values.size
    }

    inner class ViewHolder(binding: TaskItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val idView: TextView = binding.id
        val contentView: TextView = binding.content
        val dateView: TextView = binding.date
        val notificationView: CircleImageView = binding.notification

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }
}