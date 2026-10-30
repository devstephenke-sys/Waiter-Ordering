package com.example

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class WaiterOrderApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    // Initialize Firebase
    FirebaseApp.initializeApp(this)
  }
}
