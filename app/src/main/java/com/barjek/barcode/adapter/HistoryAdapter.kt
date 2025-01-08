package com.barjek.barcode.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.barjek.barcode.R
import com.barjek.barcode.databinding.ItemHistoryBinding
import com.barjek.barcode.model.Presence

class HistoryAdapter(private var items: List<Presence>) : RecyclerView.Adapter<HistoryAdapter.ViewPager>() {
    inner class ViewPager(val binding: ItemHistoryBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryAdapter.ViewPager {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewPager(binding)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: HistoryAdapter.ViewPager, position: Int) {
        val item = items[position]

        val dateArray = item.date.split("-")
        val img = when(item.mood) {
            "Happy" -> R.drawable.smile
            "Frown" -> R.drawable.frown
            "Neutral" -> R.drawable.neutral
            else -> R.drawable.neutral
        }

        holder.binding.textHari.text = dateArray.first()
        holder.binding.textTgl.text = "${dateArray[1]}-${dateArray[2]}-${dateArray[3]}"
        holder.binding.textJam.text = item.timestamp
        holder.binding.imgMood.setImageResource(img)
    }
}