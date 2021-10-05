package com.sophia.chatsapp.activityes

import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager

import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.sophia.chatsapp.adapter.ChatAdapter
import com.sophia.chatsapp.databinding.ActivityChatBinding
import com.sophia.chatsapp.model.ChatMessage
import com.sophia.chatsapp.model.User
import com.sophia.chatsapp.utillties.Constants
import com.sophia.chatsapp.utillties.PreferenceManager

import java.text.SimpleDateFormat
import java.util.Date
import kotlin.collections.ArrayList
import java.util.HashMap
import java.util.Locale
import java.util.Collections

class ChatActivity() : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var receiverUser: User
    private lateinit var chatMessages: ArrayList<ChatMessage>
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var database: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setListeners()
        loadReceiverDetails()
        init()
        listenMessages()
    }

    private fun init() {
        preferenceManager = PreferenceManager(applicationContext)
        chatMessages = ArrayList()
        chatAdapter = ChatAdapter(
            chatMessages,
            getBitmapFromEncodedString(receiverUser.image!!),
            preferenceManager.getString(Constants.KEY_UESR_ID)
        )
        binding.chatRecyclerview.adapter = chatAdapter
        database = FirebaseFirestore.getInstance()
    }

    private fun sendMessage() {
        val message: HashMap<String,Any> = HashMap()
        message[Constants.KEY_SENDER_ID] = preferenceManager.getString(Constants.KEY_UESR_ID)
        message[Constants.KEY_RECEIVER_ID] = receiverUser.id.toString()
        message[Constants.KEY_MESSAGE] = binding.inputMessage.text.toString()
        message[Constants.KEY_TIMESTAMP] = Date()
        database.collection(Constants.KEY_COLLECTION).add(message)
        binding.inputMessage.text = null
    }

    private fun listenMessages() {
        database.collection(Constants.KEY_COLLECTION)
            .whereEqualTo(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_UESR_ID))
            .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.id)
            .addSnapshotListener(eventListener)
        database.collection(Constants.KEY_COLLECTION)
            .whereEqualTo(Constants.KEY_SENDER_ID,receiverUser.id)
            .whereEqualTo(Constants.KEY_RECEIVER_ID,preferenceManager.getString(Constants.KEY_UESR_ID))
            .addSnapshotListener(eventListener)

    }

    private val eventListener: EventListener<QuerySnapshot> = EventListener { value, error ->
        if (error != null) {
            return@EventListener
        }
        if (value != null) {
            val count = chatMessages.size
            for (documentChange: DocumentChange in value.documentChanges) {
                if (documentChange.type == DocumentChange.Type.ADDED) {
                    val chatMessage = ChatMessage()
                    chatMessage.senderId = documentChange.document.getString(Constants.KEY_SENDER_ID)
                    chatMessage.receiverId = documentChange.document.getString(Constants.KEY_RECEIVER_ID)
                    chatMessage.message = documentChange.document.getString(Constants.KEY_MESSAGE)
                    chatMessage.dateTime = getReadableDateTime(documentChange.document.getDate(Constants.KEY_TIMESTAMP)!!)
                    chatMessage.dateObject = documentChange.document.getDate(Constants.KEY_TIMESTAMP)
                    chatMessages.add(chatMessage)
                }
            }
//            Collections.sort(chatMessages, ((obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject)))
            Collections.sort(chatMessages, Comparator { obj1, obj2 -> obj1.dateObject!!.compareTo(obj2.dateObject) })
            if (count == 0) {
                chatAdapter.notifyDataSetChanged()
            }else {
                //notifyItemRangeInserted = 자동 스크롤
                chatAdapter.notifyItemRangeInserted(chatMessages.size,chatMessages.size)
                binding.chatRecyclerview.smoothScrollToPosition(chatMessages.size - 1)
            }
            binding.chatRecyclerview.visibility = View.VISIBLE
        }
        binding.progressBar.visibility = View.GONE
    }

    private fun getBitmapFromEncodedString(encodedImage: String): Bitmap {
        val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun loadReceiverDetails() {
        receiverUser = intent.getSerializableExtra(Constants.KEY_USER) as User
        binding.textName.text = receiverUser.name
        Log.d(TAG, "name--" + receiverUser.name)
    }

    private fun setListeners() {
        binding.imageBack.setOnClickListener { onBackPressed() }
        binding.layoutSend.setOnClickListener { sendMessage() }
    }

    private fun getReadableDateTime(date: Date): String {
        return SimpleDateFormat("MMMM dd, yyyy - hh:mm a",Locale.getDefault()).format(date)
    }
}