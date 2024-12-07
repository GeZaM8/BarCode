package com.barjek.barcode.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.barjek.barcode.R
import com.barjek.barcode.adapter.SlideStartPagerAdapter
import com.barjek.barcode.databinding.ActivityStartBinding

class StartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStartBinding

    private var titlesList = mutableListOf<String>()
    private var detailsList = mutableListOf<String>()
    private var gambarList = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityStartBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        addToLists(R.string.slidetitle1, R.string.slidedetail1, R.drawable.gambar_2)
        addToLists(R.string.slidetitle2, R.string.slidedetail2, R.drawable.gambar_3)
        addToLists(R.string.slidetitle3, R.string.slidedetail3, R.drawable.gambar_4)
        addToLists(R.string.slidetitle4, R.string.slidedetail4, R.drawable.gambar_1)

        binding.viewPager.adapter = SlideStartPagerAdapter(titlesList, detailsList, gambarList)
        binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        binding.indicator.setViewPager(binding.viewPager)
    }

    private fun addToLists(titleRes: Int, detailRes: Int, gambar: Int) {
        val title = getString(titleRes)
        val detail = getString(detailRes)

        titlesList.add(title)
        detailsList.add(detail)
        gambarList.add(gambar)
    }
}