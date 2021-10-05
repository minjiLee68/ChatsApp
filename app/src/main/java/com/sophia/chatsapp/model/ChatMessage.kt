package com.sophia.chatsapp.model

import java.util.*

data class ChatMessage(
    var senderId: String? = "",
    var receiverId: String? = "",
    var message: String? = "",
    var dateTime: String? = "",
    var dateObject: Date? = null
)