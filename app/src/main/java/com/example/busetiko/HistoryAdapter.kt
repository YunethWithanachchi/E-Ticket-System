package com.example.busetiko

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(
    private val ticketList: List<TicketHistory>
):RecyclerView.Adapter<HistoryAdapter.TicketViewHolder>() {
    inner class TicketViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        val txtTicketId: TextView =itemView.findViewById(R.id.txtTicketId)
        val txtRouteNo: TextView = itemView.findViewById(R.id.txtRouteNo)
        val txtFrom: TextView = itemView.findViewById(R.id.txtFrom)
        val txtTo: TextView = itemView.findViewById(R.id.txtTo)
        val txtDate: TextView = itemView.findViewById(R.id.txtDate)
        val txtPrice: TextView = itemView.findViewById(R.id.txtPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketViewHolder {
        val  view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history_ticket,parent,false)
        return TicketViewHolder(view)
    }

    override fun getItemCount(): Int = ticketList.size

    override fun onBindViewHolder(holder: TicketViewHolder, position: Int) {
        val ticket = ticketList[position]

        holder.txtTicketId.text = ticket.ticketNo
        holder.txtRouteNo.text = ticket.route.toString()
        holder.txtFrom.text = ticket.from
        holder.txtTo.text = ticket.to
        holder.txtDate.text = ticket.date
        holder.txtPrice.text = "Rs. ${ticket.price}"
    }

}