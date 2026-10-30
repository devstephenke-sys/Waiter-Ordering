package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class WaiterOrderApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    try {
      // Initialize Firebase
      FirebaseApp.initializeApp(this)
      Log.d("WaiterOrderApp", "Firebase initialized successfully")
    } catch (e: Exception) {
      Log.e("WaiterOrderApp", "Failed to initialize Firebase", e)
      // Don't crash the app if Firebase fails to initialize
    }
  }
}
