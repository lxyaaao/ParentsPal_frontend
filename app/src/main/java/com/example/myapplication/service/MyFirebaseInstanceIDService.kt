package com.example.myapplication.service

import com.google.firebase.messaging.FirebaseMessagingService

class MyFirebaseInstanceIDService: FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        println("New Token: $token")
    }

}