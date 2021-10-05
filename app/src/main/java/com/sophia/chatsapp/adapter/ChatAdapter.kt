package com.sophia.chatsapp.adapter

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sophia.chatsapp.databinding.ItemContainerReceivedMessageBinding
import com.sophia.chatsapp.databinding.ItemContainerSentMessageBinding
import com.sophia.chatsapp.model.ChatMessage

class ChatAdapter(
    private val chatMessages: ArrayList<ChatMessage>,
    private val receiverProfileImage: Bitmap,
    private val senderId: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class SentMessageViewHolder(val binding: ItemContainerSentMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun setData(chatMessage: ChatMessage) {
            binding.textMessage.text = chatMessage.message
            binding.textDateTime.text = chatMessage.dateTime
        }

    }

    inner class ReceivedMessageViewHolder(val binding: ItemContainerReceivedMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun setData(chatMessage: ChatMessage, receiverProfileImage: Bitmap) {
            binding.textMessage.text = chatMessage.message
            binding.textDateTime.text = chatMessage.dateTime
            binding.imageProfile.setImageBitmap(receiverProfileImage)
        }

    }

    val VIEW_TYPE_SENT = 1
    val VIEW_TYPE_RECEIVED = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VIEW_TYPE_SENT) {
            return SentMessageViewHolder(
                ItemContainerSentMessageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else {
            return ReceivedMessageViewHolder(
                ItemContainerReceivedMessageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(getItemViewType(position)) {
            VIEW_TYPE_SENT -> {
                (holder as SentMessageViewHolder).setData(chatMessages[position])
            }
            VIEW_TYPE_RECEIVED -> {
                (holder as ReceivedMessageViewHolder).setData(chatMessages[position], receiverProfileImage)
            }
        }
    }

    override fun getItemCount(): Int {
        return chatMessages.size
    }

    override fun getItemViewType(position: Int): Int {
        if (chatMessages.get(position).senderId.equals(senderId)) {
            return VIEW_TYPE_SENT
        } else {
            return VIEW_TYPE_RECEIVED
        }
    }
}
