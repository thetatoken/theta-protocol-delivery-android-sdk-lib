package org.theta.delivery.sample

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import java.util.*

class LogsAdapter(private val context: Context, private val logsList: ArrayList<ThetaEventWithDate>) : RecyclerView.Adapter<LogsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        return ViewHolder(LogView(context))
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        viewHolder.view.setModel(logsList[i])
    }

    override fun getItemCount(): Int {
        return logsList.size
    }

    internal fun addLog(log: ThetaEventWithDate) {
        logsList.add(log)
        notifyDataSetChanged()
    }

    inner class ViewHolder(var view: LogView) : RecyclerView.ViewHolder(view)
}
