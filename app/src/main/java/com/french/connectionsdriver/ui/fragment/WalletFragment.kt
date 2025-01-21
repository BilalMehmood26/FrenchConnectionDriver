package com.french.connectionsdriver.ui.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.french.connectionsdriver.databinding.FragmentWalletBinding
import com.french.connectionsdriver.ui.adapter.PayoutAdapter
import com.french.connectionsdriver.ui.model.Booking
import com.french.connectionsdriver.ui.model.Payout
import com.french.connectionsdriver.ui.util.UserSession
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class WalletFragment : Fragment() {

    private val binding: FragmentWalletBinding by lazy {
        FragmentWalletBinding.inflate(layoutInflater)
    }

    private val db = Firebase.firestore
    private lateinit var fragmentContext: Context
    private val newBookingList: ArrayList<Payout> = arrayListOf()

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
        db.collection("Payouts").addSnapshotListener { value, error ->

            if (error != null) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(fragmentContext, "${error!!.message}", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            newBookingList.clear()
            value!!.forEach { booking ->
                val bookings = booking.toObject(Payout::class.java)
                if (bookings.driverId == UserSession.user.id) {
                    newBookingList.add(bookings)
                    bookings.id = booking.id
                    if (bookings.status == "unpaid") {
                        totalPrice += bookings.amount
                    }
                }
            }
            binding.totalAmmountTv.text = "$ $totalPrice"
            binding.progressBar.visibility = View.GONE
            updateAdapter()

        }
    }

    private fun updateAdapter() {
        binding.apply {
            rideRV.layoutManager = LinearLayoutManager(fragmentContext)
            rideRV.adapter = PayoutAdapter(fragmentContext, newBookingList) { booking ->
                binding.progressBar.visibility = View.VISIBLE

                val payout = hashMapOf(
                    "status" to "request",
                )

                db.collection("Payouts").document(booking.id).update(payout as Map<String, Any>)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            binding.progressBar.visibility = View.GONE
                            totalPrice -= booking.amount
                            binding.totalAmmountTv.text = "$ $totalPrice"
                            Toast.makeText(fragmentContext, "Request Sent.", Toast.LENGTH_SHORT)
                                .show()
                        } else {
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