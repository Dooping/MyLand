package com.gago.david.myland.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v7.util.SortedList
import android.support.v7.widget.RecyclerView
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import com.gago.david.myland.R
import com.gago.david.myland.TaskEditFragment
import com.gago.david.myland.adapters.TaskListAdapter
import com.gago.david.myland.models.PriorityObject
import com.gago.david.myland.models.TaskObject
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

class TaskListAdapter(items: ArrayList<TaskObject>, private val mListener: TaskEditFragment.OnFragmentInteractionListener?, private val priorities: List<PriorityObject>?) : RecyclerView.Adapter<TaskListAdapter.ViewHolder>(), Filterable {
    private val mValues: SortedList<TaskObject>
    private val mFilter: CustomFilter
    private val originalList: ArrayList<TaskObject>
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mItem = mValues[position]
        holder.mIdView.text = holder.mItem?.taskType
        holder.mContentView.text = holder.mItem?.observations
        if (holder.mItem?.targetDate == null) holder.date.text = "" else {
            val dateFormat = DateFormat.getDateFormat(mListener as Context?)
            val s = dateFormat.format(holder.mItem?.targetDate)
            holder.date.text = s
        }
        if (priorities != null) {
            for (p: PriorityObject in priorities) if (p.p_order == holder.mItem?.priority) {
                holder.mNotificationView.setImageDrawable(ColorDrawable(Color.parseColor(p.color)))
                break
            }
        }
        holder.mView.setOnClickListener { mListener?.selectTask(holder.mItem) }
        setFadeAnimation(holder.mView)
    }

    private fun setFadeAnimation(view: View) {
        val anim = AlphaAnimation(0.0f, 1.0f)
        anim.duration = 500
        view.startAnimation(anim)
    }

    override fun getItemCount(): Int {
        return mValues.size()
    }

    override fun getFilter(): Filter {
        return mFilter
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mIdView: TextView
        val mContentView: TextView
        val date: TextView
        val mNotificationView: CircleImageView
        var mItem: TaskObject? = null
        override fun toString(): String {
            return super.toString() + " '" + mContentView.text + "'"
        }

        init {
            mIdView = mView.findViewById(R.id.id)
            mContentView = mView.findViewById(R.id.content)
            mNotificationView = mView.findViewById(R.id.notification)
            date = mView.findViewById(R.id.date)
        }
    }

    inner class CustomFilter(private val mAdapter: TaskListAdapter, private val filteredList: SortedList<TaskObject>) : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val results = FilterResults()
            val filteredResults = ArrayList<TaskObject>()
            if (constraint.length == 0) {
                filteredResults.addAll(originalList)
            } else {
                val filterPattern = constraint.toString().split(" ").toTypedArray()
                when (filterPattern[0]) {
                    "all" -> filteredResults.addAll(originalList)
                    "land" -> {
                        var i = 0
                        while (i < originalList.size) {
                            if (originalList[i].plantIndex == null) {
                                filteredResults.add(originalList[i])
                            }
                            i++
                        }
                    }
                    "group" -> {
                        val idx: MutableList<Int> = ArrayList()
                        run {
                            var i: Int = 1
                            while (i < filterPattern.size) {
                                idx.add(Integer.valueOf(filterPattern.get(i)))
                                i++
                            }
                        }
                        var i = 0
                        while (i < originalList.size) {
                            if (idx.contains(originalList[i].plantIndex)) {
                                filteredResults.add(originalList[i])
                            }
                            i++
                        }
                    }
                    "item" -> {
                        var i = 0
                        while (i < originalList.size) {
                            if ((Integer.valueOf(filterPattern[1]) == originalList[i].plantIndex)) {
                                filteredResults.add(originalList[i])
                            }
                            i++
                        }
                    }
                }
            }
            Log.v("Count Number ", "" + filteredResults.size)
            results.values = filteredResults
            results.count = filteredResults.size
            return results
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            filteredList.clear()
            filteredList.addAll((results.values as ArrayList<TaskObject>))
            //System.out.println("Count Number 2 " + ((List<TaskObject>) results.values).size());
            mAdapter.notifyDataSetChanged()
        }
    }

    init {
        mValues = SortedList(TaskObject::class.java, object : SortedList.Callback<TaskObject>() {
            override fun compare(a: TaskObject, b: TaskObject): Int {
                return if (a.priority.compareTo(b.priority) == 0) a.creationDate.compareTo(b.creationDate) else a.priority.compareTo(b.priority)
            }

            override fun onInserted(position: Int, count: Int) {
                notifyItemRangeInserted(position, count)
            }

            override fun onRemoved(position: Int, count: Int) {
                notifyItemRangeRemoved(position, count)
            }

            override fun onMoved(fromPosition: Int, toPosition: Int) {
                notifyItemMoved(fromPosition, toPosition)
            }

            override fun onChanged(position: Int, count: Int) {
                notifyItemRangeChanged(position, count)
            }

            override fun areContentsTheSame(oldItem: TaskObject, newItem: TaskObject): Boolean {
                // return whether the items' visual representations are the same or not.
                return (oldItem == newItem)
            }

            override fun areItemsTheSame(item1: TaskObject, item2: TaskObject): Boolean {
                return (item1 == item2)
            }
        })
        originalList = items
        mValues.addAll(items)
        mFilter = CustomFilter(this@TaskListAdapter, mValues)
    }
}