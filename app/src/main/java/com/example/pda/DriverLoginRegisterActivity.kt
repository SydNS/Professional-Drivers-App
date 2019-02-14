@file:Suppress("DEPRECATION")

package com.example.pda

//import CropImage.ActivityResult
import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.pda.DriverLoginRegisterActivity
import com.google.android.gms.location.LocationListener
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_driver_login_register.*

class DriverLoginRegisterActivity : AppCompatActivity() {

    private var driversDatabaseRef: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    private var firebaseAuthListner: FirebaseAuth.AuthStateListener? = null
    private var loadingBar: ProgressDialog? = null
    private var currentUser: FirebaseUser? = null
    var currentUserId: String? = null

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_login_register)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
        )

        mAuth = FirebaseAuth.getInstance()

        firebaseAuthListner = FirebaseAuth.AuthStateListener() {
            fun onAuthStateChanged() {
                currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    Toast.makeText(this, "Yo", Toast.LENGTH_LONG).show()
//                    val intent = Intent(this@DriverLoginRegisterActivity, DriverMapActivity::class.java)
//                    startActivity(intent)
                }
            }
        }

        loadingBar = ProgressDialog(this)
        register_driver_btn!!.visibility = View.INVISIBLE
        register_driver_btn!!.isEnabled = false
        create_driver_account!!.setOnClickListener {
            create_driver_account!!.visibility = View.INVISIBLE
            login_driver_btn!!.visibility = View.INVISIBLE
            driver_status!!.text = getString(R.string.driverregistration)
            register_driver_btn!!.visibility = View.VISIBLE
            register_driver_btn!!.isEnabled = true
        }
        register_driver_btn!!.setOnClickListener {
            val email = driver_email!!.text.toString()
            val password = driver_password!!.text.toString()
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(
                    this@DriverLoginRegisterActivity,
                    "Please write your Email...",
                    Toast.LENGTH_SHORT
                ).show()
            }
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(
                    this@DriverLoginRegisterActivity,
                    "Please write your Password...",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                loadingBar!!.setTitle("Please wait :")
                loadingBar!!.setMessage("While system is performing processing on your data...")
                loadingBar!!.show()
                mAuth!!.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            currentUserId = mAuth!!.currentUser?.getUid()
                            driversDatabaseRef =
                                FirebaseDatabase.getInstance().getReference().child("Users")
                                    .child("Drivers").child(currentUserId!!)
                            driversDatabaseRef!!.setValue(true)
//                            val intent = Intent(
//                                this@DriverLoginRegisterActivity,
//                                DriverMapActivity::class.java
//                            )
//                            startActivity(intent)
                            loadingBar!!.dismiss()
                        } else {
                            Toast.makeText(
                                this@DriverLoginRegisterActivity,
                                "Please Try Again. Error Occurred, while registering... ",
                                Toast.LENGTH_SHORT
                            ).show()
                            loadingBar!!.dismiss()
                        }
                    }
            }
        }
        login_driver_btn!!.setOnClickListener {
            val email = driver_email!!.text.toString()
            val password = driver_password!!.text.toString()
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(
                    this@DriverLoginRegisterActivity,
                    "Please write your Email...",
                    Toast.LENGTH_SHORT
                ).show()
            }
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(
                    this@DriverLoginRegisterActivity,
                    "Please write your Password...",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                loadingBar!!.setTitle("Please wait :")
                loadingBar!!.setMessage("While system is performing processing on your data...")
                loadingBar!!.show()
                mAuth!!.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                this@DriverLoginRegisterActivity,
                                "Sign In , Successful...",
                                Toast.LENGTH_SHORT
                            ).show()
                            val intent = Intent(
                                this@DriverLoginRegisterActivity,
                                DriversMapActivity::class.java
                            )
                            startActivity(intent)
                        } else {
                            Toast.makeText(
                                this@DriverLoginRegisterActivity,
                                "Error Occurred, while Signing In ... ",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        firebaseAuthListner?.let { mAuth?.addAuthStateListener(it) }
    }

    override fun onStop() {
        super.onStop()
        firebaseAuthListner?.let { mAuth?.removeAuthStateListener(it) }
    }

//    override fun onBackPressed() {
//        super.onBackPressed()
//        val startPageIntent = Intent(this, WelcomeActivity::class.java)
//        startPageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
//        startActivity(startPageIntent)
//    }
}