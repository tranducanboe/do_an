package com.example.do_an.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.do_an.R
import com.example.do_an.databinding.ItemProfileBinding
import com.example.do_an.model.User

class UserAdapter(var context: Context, var userList: ArrayList<User>):RecyclerView.Adapter<UserAdapter.UserViewHolder>() {
    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val binding = ItemProfileBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        var v = LayoutInflater.from(context).inflate(R.layout.item_profile, parent, false)
        return UserViewHolder(v)
    }

    override fun getItemCount(): Int = userList.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.binding.userName.text = user.name
        Glide.with(context).load(user.profileImage)
            .placeholder(R.drawable.avata)
            .into(holder.binding.avata)
    }
}