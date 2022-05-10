package com.gago.david.myland.adapters

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gago.david.myland.R
import kotlinx.android.synthetic.main.user_name.view.*

class UserAdapter(private val items : ArrayList<String>?, val context: Context, private val listener: (String) -> Unit) : RecyclerView.Adapter<ViewHolder>() {

    // Gets the number of animals in the list
    override fun getItemCount(): Int {
        return items!!.size
    }

    // Inflates the item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.user_name, parent, false))
    }

    // Binds each animal in the ArrayList to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items!![position], listener)
        //holder.user?.text = items.get(position)
    }


    interface OnUserListInteraction {
        fun select(name: String)
    }
}

class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    // Holds the TextView that will add each animal to
    val user = view.user!!
    fun bind(item: String, listener: (String) -> Unit) = with(itemView) {
        user.text = item
        setOnClickListener { listener(item) }
    }
}