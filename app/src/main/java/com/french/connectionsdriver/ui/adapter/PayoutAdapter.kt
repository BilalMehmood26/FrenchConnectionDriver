package com.french.connectionsdriver.ui.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.french.connectionsdriver.R
import com.french.connectionsdriver.databinding.ItemPayoutBinding
import com.french.connectionsdriver.ui.model.Booking
import com.french.connectionsdriver.ui.model.Payout
import com.french.connectionsdriver.ui.model.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PayoutAdapter(
    val context: Context,
    private val list: ArrayList<Payout>,
    private val acceptBtn: (Payout) -> Unit
) :
    RecyclerView.Adapter<PayoutAdapter.ViewHolder>()  {

    private val db = Firebase.firestore

    inner class ViewHolder(val binding: ItemPayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)= ViewHolder(
        ItemPayoutBinding.inflate(
            LayoutInflater.from(context),
            parent,
            false
        )
    )

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.binding.apply {
            statusTv.text = item.status

            db.collection("Bookings").document(item.orderId).get().addOnCompleteListener {
                if(it.isSuccessful){
                    val order = it.result?.toObject(Booking::class.java)
                    if (order != null) {
                        timeDateTv.text = formatDateTime(order.bookingDate!!)
                        pickUpTv.text = order.pickUp?.address ?: "Unknown"
                        dropoffTv.text = order.destinations?.getOrNull(0)?.address ?: "Unknown"
                        ratingTv.text = order.rating?.toString() ?: "No Rating"
                        ratingBar.rating = order.rating?.toFloat() ?: 0f
                        distanceTv.text = order.distance ?: "N/A"
                        priceTv.text = "$ ${item.amount}"
                    } else {
                        Log.e("Booking", "Order is null")
                    }
                }
            }

            when(item.status){
                "unpaid" -> rideAcceptBtn.setText("Request")
                "request" -> rideAcceptBtn.setText("Pending")
            }

            Log.d("LOGGER", "onBindViewHolder: ${item.id}")
            rideAcceptBtn.setOnClickListener {
                if(item.status.equals("unpaid")){
                    acceptBtn.invoke(item)
                }
            }
        }
    }

    private fun formatDateTime(millis: Long): String {
        val formatter = SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a", Locale.ENGLISH)
        val date = Date(millis)
        return formatter.format(date)
    }
}