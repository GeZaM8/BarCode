package com.barjek.barcode.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
//        Log.d("GeofenceReceiver", "onReceive called")
//        Log.d("GeofenceReceiver", "Intent data: ${intent.extras}")
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent == null) {
            Log.e("GeofenceReceiverNull", "Error: geofencingEvent is null")
            return
        }

        if (geofencingEvent.hasError()) {
            Log.e("GeofenceReceiver", "Error: ${geofencingEvent.errorCode}")
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition
        var inside = false
        var transitionMessage = ""
        when (geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                transitionMessage = "Masuk ke dalam geofence"
                inside = true
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                transitionMessage = "Keluar dari geofence"
                inside = false
            }
        }

//        Toast.makeText(context, transitionMessage, Toast.LENGTH_SHORT).show()
//        Log.d("GeofenceReceiver", transitionMessage)

        val localIntent = Intent("com.barjek.LOCAL_GEOFENCE_EVENT").apply {
            putExtra("transition", inside)
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent)

//        val location = geofencingEvent.triggeringLocation
//
//        Log.d(
//            "GeofenceReceiver",
//            "Transition: $geofenceTransition, Latitude: ${location?.latitude}, Longitude: ${location?.longitude}"
//        )
    }
}