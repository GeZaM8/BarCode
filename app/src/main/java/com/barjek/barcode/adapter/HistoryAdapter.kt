package com.barjek.barcode.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.barjek.barcode.R
import com.barjek.barcode.activity.HistoryDetailActivity
import com.barjek.barcode.databinding.ItemHistoryBinding
import com.barjek.barcode.model.Presence
import org.json.JSONArray

class HistoryAdapter(private var items: JSONArray) : RecyclerView.Adapter<HistoryAdapter.ViewPager>() {
    inner class ViewPager(val binding: ItemHistoryBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryAdapter.ViewPager {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewPager(binding)
    }

    override fun getItemCount(): Int {
        return items.length()
    }

    override fun onBindViewHolder(holder: HistoryAdapter.ViewPager, position: Int) {
        val item = items.getJSONObject(position)

        val img = when(item.getString("mood")) {
            "Happy" -> R.drawable.smile
            "Frown" -> R.drawable.frown
            "Neutral" -> R.drawable.neutral
            else -> R.drawable.neutral
        }
        val status = item.getString("status")

        holder.binding.textHari.text = item.getString("hari")
        holder.binding.textTgl.text = item.getString("tanggal")
        holder.binding.textJam.text = item.getString("timestamp")
        holder.binding.imgMood.setImageResource(img)
        holder.binding.textStatus.text = status
        holder.binding.textStatus.setBackgroundColor(
            if (status == "Hadir") holder.itemView.context.getColor(R.color.lightGreen)
            else holder.itemView.context.getColor(R.color.lightRed)
        )

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, HistoryDetailActivity::class.java).apply {
                putExtra("id_absensi", item.getString("id_absensi"))
            }
            holder.itemView.context.startActivity(intent)
        }
    }
}