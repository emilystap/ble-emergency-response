package com.example.smartvest.util

import android.Manifest
import android.location.Location
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

private const val TAG = "LocationUtil"

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

    fun getMapUrlAwait(
        usePreciseLocation: Boolean = true,
        zoom: Zoom = Zoom.CITY,
        mapType: MapType = MapType.MAP,
        fusedLocationClient: FusedLocationProviderClient,
        scope: CoroutineScope
    ): String? {
        val location: Location? = getLocationAwait(
            usePreciseLocation = usePreciseLocation,
            fusedLocationClient = fusedLocationClient,
            scope = scope
        )
        if (location == null) {
            Log.w(TAG, "Location is null")
            return null
        }

        val lat = location.latitude.toString()
        val lon = location.longitude.toString()
        return "http://maps.google.com/maps?z=${zoom.value}&t=${mapType.value}&q=loc:$lat+$lon"
    }

    private fun getLocationAwait(
        usePreciseLocation: Boolean = true,
        fusedLocationClient: FusedLocationProviderClient,
        scope: CoroutineScope
    ): Location? {
        var result: Location? = null
        scope.launch(Dispatchers.IO) {
            val priority = if (usePreciseLocation) {
                Priority.PRIORITY_HIGH_ACCURACY
            } else {
                Priority.PRIORITY_BALANCED_POWER_ACCURACY
            }
            withTimeoutOrNull(30000) {
                result = fusedLocationClient.getCurrentLocation(
                    priority,
                    CancellationTokenSource().token,
                ).await()
            }  /* TODO: Figure out async/await/withTimeoutOrNull */
        }
        return result
    }
}