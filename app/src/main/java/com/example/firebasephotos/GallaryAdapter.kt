package com.example.firebasephotos

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.firebasephotos.databinding.ImageItemBinding


class GallaryAdapter(val gridListener: onGridListener) : RecyclerView.Adapter<GallaryAdapter.ViewHolder>() {

    private fun loadImgFromURL(url: String, context: Context, imageview: ImageView){
        Glide
            .with(context)
            .load(url)
            .into(imageview)
    }

    var data = listOf<Uri>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount():Int = data.size


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding = ImageItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = data[position]

       loadImgFromURL(item.toString(), holder.binding.root.context, holder.binding.img)
       holder.binding.img.clipToOutline = true
    }

    inner class ViewHolder(val binding: ImageItemBinding) : RecyclerView.ViewHolder(binding.root) , View.OnClickListener {
        init {
            binding.img.setOnClickListener(this)
        }
        override fun onClick(v: View?) {

            gridListener.onClick(data[adapterPosition])
        }
    }

}

interface onGridListener {
    fun onClick(url : Uri)
}