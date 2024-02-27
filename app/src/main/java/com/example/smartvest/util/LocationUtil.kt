package com.example.smartvest.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

private const val TAG = "LocationUtil"
private const val TIMEOUT: Long = 10000

@SuppressLint("MissingPermission")
object LocationUtil {
    val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    enum class MapType(val value: String) {
        MAP("m"),
        SATELLITE("k"),
        HYBRID("h"),
        TERRAIN("p")
    }

    enum class Zoom(val value: Int) {
        WORLD(1),
        CONTINENT(5),
        CITY(10),
        STREET(15),
        BUILDING(20)
    }

    fun getMapUrl(
        location: Location,
        zoom: Zoom = Zoom.CITY,
        mapType: MapType = MapType.MAP
    ): String {
        val lat = location.latitude.toString()
        val lon = location.longitude.toString()
        return "http://maps.google.com/maps?z=${zoom.value}&t=${mapType.value}&q=loc:$lat+$lon"
    }

    fun getCurrentLocation(
        context: Context,
        usePreciseLocation: Boolean = false,
        onSuccess: (Location?) -> Unit,
        onFailure: (Exception) -> Unit = { Log.e(TAG, "getLocation: $it") }
    ) {
        val priority = if (usePreciseLocation)
            Priority.PRIORITY_HIGH_ACCURACY  /* TODO: Make this a settings/based on battery % */
        else
            Priority.PRIORITY_BALANCED_POWER_ACCURACY

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.getCurrentLocation(
            priority,
            CancellationTokenSource().token,
        ).addOnSuccessListener {loc: Location? ->
            onSuccess(loc)
        }.addOnFailureListener {
            onFailure(it)
        }
    }

    fun getLastLocation(
        context: Context,
        onSuccess: (Location?) -> Unit,
        onFailure: (Exception) -> Unit = { Log.e(TAG, "getLocation: $it") }
    ) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { loc: Location? ->
                onSuccess(loc)
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }
}