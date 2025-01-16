package com.example.frenchconnectiondriver.ui.model

import java.io.Serializable

data class Booking(
    val bookingDate: Long? = 0,
    val completionTimeStamp: Long? = 0,
    val carType: String? = "",
    val distance: String? = "",
    val driverId: String? = "",
    val driverLat: Double? = 0.0,
    val driverLng: Double? = 0.0,
    val driverPrice: String? = "",
    val id: String? = "",
    val pickupDate: Long? = 0,
    val pm_id: String? = "",
    val price: String? = "",
    val rating: Int? = 0,
    val rideType: String? = "",
    val status: String? = "",
    val time: String? = "",
    val userId: String? = "",
    val vehicleId: String? = "",
    val pickUp: PickUp? = PickUp(),
    val destinations: List<DropOff> = ArrayList(),
):Serializable{
    data class PickUp(
        val address: String? = "",
        val lat: Double? = 0.0,
        val lng: Double? = 0.0
    ): Serializable

    data class DropOff(
        val address: String? = "",
        val lat: Double? = 0.0,
        val lng: Double? = 0.0
    ): Serializable
}
