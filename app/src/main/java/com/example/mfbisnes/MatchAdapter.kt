package com.example.mfbisnes

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mfbisnes.databinding.ItemMatchBinding
import com.bumptech.glide.Glide

class MatchAdapter(private val matches: List<Match>) : RecyclerView.Adapter<MatchAdapter.MatchViewHolder>() {

    inner class MatchViewHolder(val binding: ItemMatchBinding) : RecyclerView.ViewHolder(binding.root) {


        init {
            itemView.setOnClickListener {
                val match = matches[adapterPosition]
                val intent = Intent(itemView.context, ChatRoomActivity::class.java)
                intent.putExtra("userId", match.userId)
                itemView.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val binding = ItemMatchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MatchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        val currentMatch = matches[position]
        holder.binding.match = currentMatch
        Glide.with(holder.itemView.context)
            .load(currentMatch.userImage)
            .placeholder(R.drawable.ic_baseline_account_circle_24)
            .into(holder.binding.userImageView)
    }

    override fun getItemCount(): Int = matches.size

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}


