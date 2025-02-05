package com.barjek.barcode.activity

import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.barjek.barcode.R
import com.barjek.barcode.databinding.ActivityHomePageBinding
import com.barjek.barcode.fragment.HomeFragment
import com.barjek.barcode.fragment.JadwalFragment
import com.barjek.barcode.fragment.LeaderboardFragment
import com.barjek.barcode.fragment.ProfileFragment
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class HomePageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomePageBinding

    private var sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    private lateinit var connectivityManager: ConnectivityManager
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {

        // Network capabilities have changed for the network
        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            val unmetered = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
        }

        // lost network connection
        override fun onLost(network: Network) {
            super.onLost(network)

//            val intent = Intent(this@HomePageActivity, NoInternetActivity::class.java)
//            startActivity(intent)
        }
    }
    val networkRequest = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        var timestamp = sdf.format(Date())

        connectivityManager = getSystemService(ConnectivityManager::class.java) as ConnectivityManager
        connectivityManager.requestNetwork(networkRequest, networkCallback)

//        if(!isConnectInternet()) {
//            val intent = Intent(this, NoInternetActivity::class.java)
//            startActivity(intent)
//        }

        replaceFragment(HomeFragment())
        binding.btnNavbar.setOnItemSelectedListener {item ->
            when (item.itemId) {
                R.id.home -> replaceFragment(HomeFragment())
                R.id.leaderboard -> replaceFragment(LeaderboardFragment())
                R.id.jadwal -> replaceFragment(JadwalFragment())
                R.id.profile -> replaceFragment(ProfileFragment())
            }

            return@setOnItemSelectedListener true
        }
        binding.btnCamera.setOnClickListener {
            timestamp = sdf.format(Date())
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout, fragment)
        fragmentTransaction.commit()
    }

//    private fun isConnectInternet(): Boolean {
//        val network = connectivityManager.activeNetwork ?: return false
//
//        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
//
//        return when {
//            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
//            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
//            else -> false
//        }
//    }

    override fun onDestroy() {
        super.onDestroy()

        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}