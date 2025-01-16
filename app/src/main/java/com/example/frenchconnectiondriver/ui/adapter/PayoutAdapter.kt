package com.example.frenchconnectiondriver.ui.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.frenchconnectiondriver.R
import com.example.frenchconnectiondriver.databinding.ItemPayoutBinding
import com.example.frenchconnectiondriver.ui.model.Booking
import com.example.frenchconnectiondriver.ui.model.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PayoutAdapter(
    val context: Context,
    private val list: ArrayList<Booking>,
    private val acceptBtn: (Booking) -> Unit
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

            db.collection("Users").document(item.userId!!).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result.toObject(User::class.java)
                    nameTv.text = user!!.userName
                    if (user.image!!.isNotEmpty()) {
                        Glide.with(context).load(user.image).into(profileIv)
                    } else {
                        profileIv.setImageResource(R.drawable.main_logo)
                    }
                } else {
                    Log.d("LOGGER", "is Fail")
                    Toast.makeText(
                        context,
                        task.exception!!.message.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }


            timeDateTv.text = formatDateTime(item.bookingDate!!)
            pickUpTv.text = item.pickUp!!.address
            dropoffTv.text = item.destinations[0].address
            ratingTv.text = item.rating.toString()
            ratingBar.rating = item.rating!!.toFloat()
            priceTv.text = "$ ${item.price}"
            timeTv.text = item.time
            distanceTv.text = item.distance

            rideAcceptBtn.setOnClickListener {
                acceptBtn.invoke(item)
            }
        }
    }

    private fun formatDateTime(millis: Long): String {
        val formatter = SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a", Locale.ENGLISH)
        val date = Date(millis)
        return formatter.format(date)
    }
}