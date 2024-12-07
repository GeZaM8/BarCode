package com.barjek.barcode.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.barjek.barcode.activity.RegisterActivity
import com.barjek.barcode.databinding.ItemStartPageBinding

class SlideStartPagerAdapter(private var title: List<String>, private var details: List<String>, private var gambar: List<Int>): RecyclerView.Adapter<SlideStartPagerAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(val binding: ItemStartPageBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SlideStartPagerAdapter.Pager2ViewHolder {
        val binding = ItemStartPageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Pager2ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return title.size
    }

    override fun onBindViewHolder(holder: SlideStartPagerAdapter.Pager2ViewHolder, position: Int) {
        holder.binding.title.text = title[position]
        holder.binding.details.text = details[position]
        holder.binding.gambar.setImageResource(gambar[position])

        if (position == 3) {
            val params = holder.binding.gambar.layoutParams as ViewGroup.MarginLayoutParams
            params.setMargins(0, 0, 0, 200)
            holder.binding.gambar.layoutParams = params

            holder.binding.title.textSize = 36F

            holder.binding.btnSignUp.visibility = View.VISIBLE
            holder.binding.btnSignUp.setOnClickListener {
                val intent = Intent(holder.itemView.context, RegisterActivity::class.java)
                holder.itemView.context.startActivity(intent)
                (holder.itemView.context as Activity).finish()
            }
        } else {
            holder.binding.btnSignUp.visibility = View.GONE
        }
    }
}