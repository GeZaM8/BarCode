package com.barjek.barcode.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.barjek.barcode.R
import com.barjek.barcode.databinding.ActivityHomePageBinding
import com.barjek.barcode.fragment.HomeFragment
import com.barjek.barcode.fragment.JadwalFragment
import com.barjek.barcode.fragment.LeaderboardFragment
import com.barjek.barcode.fragment.ProfileFragment

class HomePageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomePageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

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
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout, fragment)
        fragmentTransaction.commit()
    }
}