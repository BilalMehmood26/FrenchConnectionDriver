package com.example.frenchconnectiondriver.ui.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.frenchconnectiondriver.R
import com.example.frenchconnectiondriver.databinding.ActivityYourDestnationBinding
import com.example.frenchconnectiondriver.ui.model.Booking
import com.example.frenchconnectiondriver.ui.model.User
import com.example.frenchconnectiondriver.ui.util.UserSession
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class YourDestnationActivity : AppCompatActivity(), OnMapReadyCallback {

    private val binding: ActivityYourDestnationBinding by lazy {
        ActivityYourDestnationBinding.inflate(layoutInflater)
    }

    private var myGoogleMap: GoogleMap? = null
    private var driverName = "--"
    private var pickUpLat = 0.0
    private var pickUpLng = 0.0
    private val REQUEST_CODE = 1000

    private var toID = ""
    private var messageId = ""

    private lateinit var booking : Booking
    private var driverPhoneNumber = ""

    private var rideID: String? = ""
    private var driverCarType: String? = ""
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        booking = intent.getSerializableExtra("booking") as Booking

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map_fragment) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                REQUEST_CODE
            )
        }

        binding.apply {
            backBtn.setOnClickListener {
                finish()
            }

            phoneBtn.setOnClickListener {
                val dialIntent = Intent(Intent.ACTION_DIAL)
                dialIntent.data = Uri.parse("tel:$driverPhoneNumber")
                if (driverPhoneNumber.isNotEmpty()) {
                    startActivity(dialIntent)
                } else {
                    Toast.makeText(
                        this@YourDestnationActivity,
                        "Phone Number Not Available",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            pickUpLat = booking.pickUp!!.lat!!
            pickUpLng = booking.pickUp!!.lng!!
            rideID = booking.id

            db.collection("Bookings").document(booking.id!!).addSnapshotListener { value, error ->

                if(error!=null){
                    Toast.makeText(this@YourDestnationActivity, "${error.message.toString()}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val booking = value!!.toObject(Booking::class.java)
                booking?.apply {

                    db.collection("Users").document(userId!!).get().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = task.result.toObject(User::class.java)
                            nameTv.text = user!!.userName
                            ratingTv.text = user.totalRating.toString()
                            driverPhoneNumber = user.phoneNumber!!
                            if (user.image!!.isNotEmpty()) {
                                Glide.with(this@YourDestnationActivity).load(user.image).into(profileIV)
                            } else {
                                profileIV.setImageResource(R.drawable.main_logo)
                            }
                        } else {
                            Log.d("LOGGER", "is Fail")
                            Toast.makeText(
                                this@YourDestnationActivity,
                                task.exception!!.message.toString(),
                                Toast
                                    .LENGTH_SHORT
                            ).show()
                        }
                    }
                    driverCarType = carType
                    timeDateTv.text = formatDateTime(bookingDate!!)
                    distanceTv.text = distance
                    pickUpTv.text = pickUp!!.address
                    dropoffTv.text  = destinations[0].address
                    priceTv.text = "$ $price"
                    statusTv.text = status
                    nameTv.text = driverName

                    when(status){
                        "driverAccepted" ->{
                            markCompleteBtn.setText("Arrived")
                        }

                        "driverReached" ->{
                            markCompleteBtn.setText("Start Ride")
                        }

                        "rideStarted" ->{
                            markCompleteBtn.setText("Complete Ride")
                        }
                    }

                    markCompleteBtn.setOnClickListener {
                        when(status){
                            "driverAccepted" ->{
                                updateRideStatus("driverReached",id!!)
                            }

                            "driverReached" ->{
                                updateRideStatus("rideStarted",id!!)
                            }

                            "rideStarted" ->{
                                updateRideCompletedStatus("rideCompleted",id!!)
                            }
                        }
                    }

                    msgBtn.setOnClickListener {
                        startMessage()
                    }
                }
            }
        }

    }

    private fun startMessage() {
        val timeStamp = System.currentTimeMillis()

        val messageMap = hashMapOf(
            "content" to "i am on the way",
            "fromID" to Firebase.auth.currentUser!!.uid,
            "toID" to toID,
            "messageId" to messageId,
            "read" to false,
            "timestamp" to timeStamp,
            "type" to "text"
        )

        val participants = hashMapOf(
            Firebase.auth.currentUser!!.uid to true,
            UserSession.user.id to true
        )

        val lastMessageMap = hashMapOf(
            "lastMessage" to messageMap,
            "participants" to participants,
            "chatType" to "one",
            "carType" to driverCarType
        )

        binding.progressBar.visibility = View.VISIBLE
        FirebaseFirestore.getInstance().collection("Chat").document(rideID!!).set(lastMessageMap)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val conversationID = UUID.randomUUID().toString()
                    FirebaseFirestore.getInstance().collection("Chat").document(rideID!!)
                        .collection("Conversation").document(conversationID).set(messageMap)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                binding.progressBar.visibility = View.GONE
                                val intent = Intent(this, ConversationActivity::class.java)
                                intent.putExtra("userID", UserSession.user.id)
                                intent.putExtra("messageId", rideID)
                                startActivity(intent)
                                overridePendingTransition(
                                    androidx.appcompat.R.anim.abc_fade_in,
                                    androidx.appcompat.R.anim.abc_fade_out
                                )
                            } else {
                                binding.progressBar.visibility = View.GONE
                                Toast.makeText(
                                    this,
                                    task.exception!!.message.toString(),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    Toast.makeText(this, it.exception!!.message.toString(), Toast.LENGTH_SHORT)
                        .show()
                    binding.progressBar.visibility = View.GONE
                }
            }
    }

    private fun updateRideStatus(status:String,rideID:String){
        binding.progressBar.visibility = View.VISIBLE

        db.collection("Bookings").document(rideID).update("status",status).addOnCompleteListener {
            if(it.isSuccessful){
                binding.progressBar.visibility = View.GONE
                if(status == "rideCompleted") finish()
            }else{
                Toast.makeText(this, "${it.exception!!.message}", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
            }
        }

    }

    private fun updateRideCompletedStatus(status:String,rideID:String){
        binding.progressBar.visibility = View.VISIBLE

        val rideCompleted = hashMapOf(
            "status" to status,
            "completionTimeStamp" to System.currentTimeMillis()
        )
        db.collection("Bookings").document(rideID).update(rideCompleted as Map<String, Any>).addOnCompleteListener {
            if(it.isSuccessful){
                binding.progressBar.visibility = View.GONE
                if(status == "rideCompleted") finish()
            }else{
                Toast.makeText(this, "${it.exception!!.message}", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
            }
        }

        db.collection("Chat").document(rideID).delete()
    }

    override fun onMapReady(googelMaps: GoogleMap) {
        myGoogleMap = googelMaps
        val latLng = LatLng(pickUpLat, pickUpLng)

        myGoogleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
        myGoogleMap?.addMarker(
            MarkerOptions().icon(setIcon(this, R.drawable.ic_car)).position(latLng)
        )
        myGoogleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
    }

    private fun setIcon(context: Activity, drawableID: Int): BitmapDescriptor {

        val drawable = ActivityCompat.getDrawable(context, drawableID)
        drawable!!.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun formatDateTime(millis: Long): String {
        val formatter = SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a", Locale.ENGLISH)
        val date = Date(millis)
        return formatter.format(date)
    }
}