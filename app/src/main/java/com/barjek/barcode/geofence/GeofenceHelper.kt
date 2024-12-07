package com.barjek.barcode.geofence

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

object GeofenceHelper {
    private lateinit var geofencingClient: GeofencingClient
    private val geofenceList = mutableListOf<Geofence>()

    private fun getGeofencePendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    fun setupGeofence(context: Context) {
        geofencingClient = LocationServices.getGeofencingClient(context)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(context, "Location permission is not granted", Toast.LENGTH_SHORT).show()
            return
        }

        val geofenceCoordinates = listOf(
            GeofenceCoordinates("Titik Tengah", -6.321500879409239, 106.89947556710065, 70f),
            GeofenceCoordinates("Lobby", -6.321807839333644, 106.89875497448661, 60f),
            GeofenceCoordinates("Dekat Masjid An-Nur", -6.320836064557816, 106.89944734457164, 80f),
        )

        geofenceCoordinates.forEach { coordinates ->
            val geofence = Geofence.Builder()
                .setRequestId(coordinates.id)
                .setCircularRegion(
                    coordinates.latitude,
                    coordinates.longitude,
                    coordinates.radius
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                .build()

            geofenceList.add(geofence)
        }

        val pendingIntent = getGeofencePendingIntent(context)
//        Log.d("Geofence", "PendingIntent created: $pendingIntent")

        try {
            geofencingClient.addGeofences(getGeofencingRequest(), pendingIntent)
                .addOnCompleteListener {
                }
        } catch (e: SecurityException) {
            Toast.makeText(context, "SecurityException: ${e.message}", Toast.LENGTH_SHORT).show()
//            Log.d("Geofence", "SecurityException: ${e.message}")
        }
    }

    private fun getGeofencingRequest(): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(geofenceList)
        }.build()
    }

    fun checkLocation(context: Context, fusedLocationClient: FusedLocationProviderClient, locationCallback: LocationCallback) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(context, "Lokasi Diperlukan", Toast.LENGTH_SHORT).show()
            return
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000L
        ).build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    data class GeofenceCoordinates(val id: String, val latitude: Double, val longitude: Double, val radius: Float)
}