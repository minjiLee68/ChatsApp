package com.sophia.chatsapp.adapter

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sophia.chatsapp.databinding.ItemContainerUserBinding
import com.sophia.chatsapp.listeners.UserListener
import com.sophia.chatsapp.model.User

class UsersAdapter(private val users: ArrayList<User>, val userListener: UserListener): ListAdapter<User,UsersAdapter.UserViewHolder>(

    object : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean =
            oldItem.name == newItem.name && oldItem.email == newItem.email
                    && oldItem.image == newItem.image
    }

) {

    inner class UserViewHolder(
        val binding: ItemContainerUserBinding
    ): RecyclerView.ViewHolder(binding.root) {

        fun setUserData(user: User) {
            binding.textName.text = user.name
            binding.textEmail.text = user.email
            binding.imageProfile.setImageBitmap(getUserImage(user.image!!))
            binding.bagConst.setOnClickListener {
                userListener.onUserClicked(user)
            }
        }
    }

    private fun getUserImage(encodedImage: String): Bitmap {
        val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder =
        UserViewHolder(ItemContainerUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ))


    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val userList = users[position]
        holder.setUserData(userList)
    }
}