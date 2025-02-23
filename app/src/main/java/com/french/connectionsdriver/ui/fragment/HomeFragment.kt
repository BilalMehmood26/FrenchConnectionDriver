package com.french.connectionsdriver.ui.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.french.connectionsdriver.R
import com.french.connectionsdriver.databinding.FragmentHomeBinding
import com.french.connectionsdriver.ui.activity.EditProfileActivity
import com.french.connectionsdriver.ui.adapter.NewRideAdapter
import com.french.connectionsdriver.ui.model.Booking
import com.french.connectionsdriver.ui.util.UserSession
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class HomeFragment : Fragment() {

    private val binding: FragmentHomeBinding by lazy {
        FragmentHomeBinding.inflate(layoutInflater)
    }

    private val db = Firebase.firestore
    private lateinit var fragmentContext: Context
    private val newBookingList: ArrayList<Booking> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding.apply {
            userNameTv.text = UserSession.user.userName

            if (UserSession.user.image.equals("")) {
                profileIv.setImageResource(R.drawable.dummy_profile)
            } else {
                Glide.with(requireActivity()).load(UserSession.user.image).into(profileIv)
            }
            createVehicle()
            yourProfileBtn.setOnClickListener {
                startActivity(Intent(fragmentContext,EditProfileActivity::class.java))
            }
            getNewRide()
        }

        return binding.root
    }

    private fun updateAdapter() {
        binding.apply {
            newRideRV.layoutManager = LinearLayoutManager(fragmentContext)
            newRideRV.adapter = NewRideAdapter(fragmentContext, newBookingList) { booking ->
                val acceptRide = hashMapOf(
                    "driverId" to Firebase.auth.currentUser!!.uid,
                    "driverLat" to UserSession.user.lat,
                    "driverLng" to UserSession.user.lng,
                    "status" to "driverAccepted",
                    "vehicleId" to ""
                )
                db.collection("Bookings").document(booking.id!!).update(acceptRide as Map<String, Any>).addOnCompleteListener{ task ->
                    if(task.isSuccessful){
                        Toast.makeText(fragmentContext, "Accepted", Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(fragmentContext, "${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun createVehicle(){
        val userUpdate = hashMapOf(
            "userId" to Firebase.auth.currentUser!!.uid
        ) as Map<String, Any>
        db.collection("Vehicle").document(Firebase.auth.currentUser!!.uid).set(userUpdate)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
            }.addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(
                    fragmentContext,
                    it.message.toString(),
                    Toast.LENGTH_SHORT
                ).show()
            }
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
                if (bookings.status == "booked") {
                    newBookingList.add(bookings)
                }
            }
            binding.progressBar.visibility = View.GONE
            updateAdapter()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context
    }
}