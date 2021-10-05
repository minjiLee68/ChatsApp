package com.sophia.chatsapp.listeners

import com.sophia.chatsapp.model.User

interface UserListener {
    fun onUserClicked(user: User)
}