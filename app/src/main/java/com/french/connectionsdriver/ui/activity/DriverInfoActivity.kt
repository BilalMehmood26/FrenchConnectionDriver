package com.french.connectionsdriver.ui.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.french.connectionsdriver.databinding.ActivityDriverInfoBinding
import com.french.connectionsdriver.ui.model.Vehicle
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class DriverInfoActivity : AppCompatActivity() {

    private val binding: ActivityDriverInfoBinding by lazy {
        ActivityDriverInfoBinding.inflate(layoutInflater)
    }

    private var imageURI: Uri? = null
    private val mAuth = Firebase.auth
    private var db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.apply {
            licenseImageBtn.setOnClickListener {
                openGalleryOrFilePicker()
            }

            fetchInfo()

            saveDriverBtn.setOnClickListener {
                saveInfo(
                    licenseNumber = drivingLicenseTv.text.toString(),
                    driverCar = carTv.text.toString(),
                    carMake = makeTv.text.toString(),
                    carModel = modelTv.text.toString(),
                    driverCarNumber = tagTv.text.toString()
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            imageURI = data!!.data
            Log.d("LOGGER", "onActivityResult: $imageURI")
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGalleryOrFilePicker() {
        ImagePicker.with(this)
            .cropSquare()
            .compress(1024)
            .maxResultSize(1080, 1080)
            .start()
    }

    private fun saveInfo(
        licenseNumber: String,
        driverCar: String,
        carMake: String,
        carModel: String,
        driverCarNumber: String
    ) {

        binding.progressBar.visibility = View.VISIBLE
        if (imageURI != null) {
            val storage =
                FirebaseStorage.getInstance().reference.child("Driver/${mAuth.currentUser!!.uid}.jpg")

            var uploadTask = storage.putFile(imageURI!!)
            uploadTask.addOnSuccessListener {
                storage.downloadUrl.addOnSuccessListener {
                    val userUpdate = hashMapOf(
                        "licenseNumber" to licenseNumber,
                        "driverCar" to driverCar,
                        "carMake" to carMake,
                        "carModel" to carModel,
                        "driverCarNumber" to driverCarNumber,
                        "image" to it.toString()
                    ) as Map<String, Any>

                    db.collection("Vehicle").document(mAuth.currentUser!!.uid).update(userUpdate)
                        .addOnSuccessListener {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(this, "Driver Info Updated", Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(
                                this,
                                it.message.toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }.addOnFailureListener {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, it.message.toString(), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        } else {
            val userUpdate = hashMapOf(
                "licenseNumber" to licenseNumber,
                "driverCar" to driverCar,
                "carMake" to carMake,
                "carModel" to carModel,
                "driverCarNumber" to driverCarNumber
            ) as Map<String, Any>
            db.collection("Vehicle").document(mAuth.currentUser!!.uid).update(userUpdate)
                .addOnSuccessListener {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Driver Info Updated", Toast.LENGTH_SHORT)
                        .show()
                }.addOnFailureListener {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        this,
                        it.message.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

    }

    private fun fetchInfo() {
        binding.progressBar.visibility = View.VISIBLE
        db.collection("Vehicle").document(Firebase.auth.currentUser!!.uid).get()
            .addOnSuccessListener { response ->

                if (response.exists()) {
                    binding.progressBar.visibility = View.GONE
                    val vehicle = response.toObject(Vehicle::class.java)

                    binding.apply {
                        drivingLicenseTv.setText(vehicle!!.licenseNumber)
                        carTv.setText(vehicle.driverCar)
                        makeTv.setText(vehicle.carMake)
                        modelTv.setText(vehicle.carModel)
                        tagTv.setText(vehicle.driverCarNumber)
                    }
                }
            }.addOnFailureListener { error ->
            binding.progressBar.visibility = View.GONE
            Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
        }
    }
}