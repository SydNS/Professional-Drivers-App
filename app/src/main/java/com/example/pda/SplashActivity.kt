package com.example.pda

//import CropImage.ActivityResult
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
//import com.google.android.gms.location.LocationListener

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val thread: Thread = object : Thread() {
            override fun run() {
                try {
                    sleep(1000)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    val mainIntent = Intent(this@SplashActivity, WelcomeActivity::class.java)
                    startActivity(mainIntent)
                }
            }
        }
        thread.start()
    }

    override fun onPause() {
        super.onPause()
        finish()
    }
}