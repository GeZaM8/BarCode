package com.barjek.barcode.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.barjek.barcode.R
import com.barjek.barcode.databinding.ActivityNoInsideBinding
import com.barjek.barcode.geofence.GeofenceHelper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

class NoInsideActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNoInsideBinding
    private lateinit var geofenceReceiver: BroadcastReceiver
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private var hasStartedActivity = false

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityNoInsideBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    GeofenceHelper.setupGeofence(this@NoInsideActivity)
                }
            }
        }
        GeofenceHelper.checkLocation(this, fusedLocationClient, locationCallback)

        geofenceReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == "com.barjek.LOCAL_GEOFENCE_EVENT") {
                    val inside = intent.getBooleanExtra("transition", false)
                    if (inside && !hasStartedActivity) {
                        hasStartedActivity = true

                        val intent = Intent(this@NoInsideActivity, CameraActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }

        val intentFilter = IntentFilter("com.barjek.LOCAL_GEOFENCE_EVENT")
        LocalBroadcastManager.getInstance(this).registerReceiver(geofenceReceiver, intentFilter)

        binding.btnTry.setOnClickListener {
            GeofenceHelper.setupGeofence(this@NoInsideActivity)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(geofenceReceiver)
    }

    override fun onStop() {
        super.onStop()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onResume() {
        super.onResume()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }
}