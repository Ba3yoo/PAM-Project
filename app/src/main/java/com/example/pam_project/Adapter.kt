package com.example.pam_project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class Adapter(private val dataSet: Array<String>) :
RecyclerView.Adapter<Adapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rowTitle: TextView
        val rowText: TextView
        val delButton : Button
        val editButton : Button
        init {
            rowTitle = view.findViewById(R.id.rowTitle)
            rowText = view.findViewById(R.id.rowText)
            delButton = view.findViewById(R.id.delButton)
            editButton = view.findViewById(R.id.editButton)

            delButton.setOnClickListener {
//                onItemClick?.invoke(dataSet[adapterPosition])
            }
            editButton.setOnClickListener {

            }
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.row, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

//        viewHolder.textView.text = dataSet[position]
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}