package com.example.getregulargps

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

fun interface LocationPermissionCallback {
    fun requirePermission()
}

class LocationManager(
    private val context: Context,
    private val permissionCallback: LocationPermissionCallback,
) {
    private var _currentLocation: MutableStateFlow<Location>? = null
    val currentLocation: StateFlow<Location>? = _currentLocation

    private var fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private val locationRequest: LocationRequest = LocationRequest.Builder(3000L)
        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        .build()
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)
            p0.locations.forEach { location ->
                Log.d("LocationManager.locationCallback", location.toString())
                _currentLocation.value = location
            }
        }
    }

    init {
        startLocationUpdates()
    }

    fun deInit() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun startLocationUpdates() {
        if (
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionCallback.requirePermission()
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }
}