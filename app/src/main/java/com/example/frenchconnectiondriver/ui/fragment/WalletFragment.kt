package com.example.frenchconnectiondriver.ui.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.frenchconnectiondriver.R
import com.example.frenchconnectiondriver.databinding.FragmentHomeBinding
import com.example.frenchconnectiondriver.databinding.FragmentWalletBinding
import com.example.frenchconnectiondriver.ui.adapter.NewRideAdapter
import com.example.frenchconnectiondriver.ui.adapter.PayoutAdapter
import com.example.frenchconnectiondriver.ui.model.Booking
import com.example.frenchconnectiondriver.ui.util.UserSession
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class WalletFragment : Fragment() {

    private val binding: FragmentWalletBinding by lazy {
        FragmentWalletBinding.inflate(layoutInflater)
    }

    private val db = Firebase.firestore
    private lateinit var fragmentContext: Context
    private val newBookingList: ArrayList<Booking> = arrayListOf()

    private var totalPrice = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        getNewRide()
        return binding.root
    }

    private fun getNewRide() {
        binding.progressBar.visibility = View.VISIBLE
        db.collection("Bookings").addSnapshotListener { value, error ->

            if (error != null) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(fragmentContext, "${error!!.message}", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            newBookingList.clear()
            value!!.forEach { booking ->
                val bookings = booking.toObject(Booking::class.java)
                if(bookings.driverId == UserSession.user.id){
                    if (bookings.status == "rideCompleted" || bookings.status == "rated") {
                        newBookingList.add(bookings)
                        totalPrice += bookings.price!!.toDouble()
                    }
                }
            }
            binding.progressBar.visibility = View.GONE
            updateAdapter()
            binding.totalAmmountTv.text = "$ $totalPrice"
        }
    }

    private fun updateAdapter() {
        binding.apply {
            rideRV.layoutManager = LinearLayoutManager(fragmentContext)
            rideRV.adapter = PayoutAdapter(fragmentContext, newBookingList) { booking ->
                binding.progressBar.visibility = View.VISIBLE
                val payout = hashMapOf(
                    "amount" to booking.price,
                    "completionTimeStamp" to booking.completionTimeStamp,
                    "driverId" to UserSession.user.id,
                    "orderId" to booking.id,
                    "status" to "unpaid",
                    "type" to "order"
                )
                db.collection("Payouts").document().set(payout).addOnCompleteListener {
                    if(it.isSuccessful){
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(fragmentContext, "Request Sent.", Toast.LENGTH_SHORT).show()
                    }else{
                        binding.progressBar.visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context
    }

}