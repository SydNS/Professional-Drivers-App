package com.example.pda

//import CropImage.ActivityResult
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
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

class DriverLoginRegisterActivity : AppCompatActivity() {
    private var CreateDriverAccount: TextView? = null
    private var TitleDriver: TextView? = null
    private var LoginDriverButton: Button? = null
    private var RegisterDriverButton: Button? = null
    private var DriverEmail: EditText? = null
    private var DriverPassword: EditText? = null
    private var driversDatabaseRef: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    private var firebaseAuthListner: FirebaseAuth.AuthStateListener? = null
    private var loadingBar: ProgressDialog? = null
    private var currentUser: FirebaseUser? = null
    var currentUserId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_login_register)
        mAuth = FirebaseAuth.getInstance()

        firebaseAuthListner = FirebaseAuth.AuthStateListener() {
            fun onAuthStateChanged(@NonNull firebaseAuth:FirebaseAuth) {
                currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null)
                {
                    Toast.makeText(this,"Yo",Toast.LENGTH_LONG).show()
//                    val intent = Intent(this@DriverLoginRegisterActivity, DriverMapActivity::class.java)
//                    startActivity(intent)
                }
            }
        }
        CreateDriverAccount = findViewById<TextView>(R.id.create_driver_account)
        TitleDriver = findViewById<TextView>(R.id.titlr_driver)
        LoginDriverButton = findViewById<Button>(R.id.login_driver_btn)
        RegisterDriverButton = findViewById<Button>(R.id.register_driver_btn)
        DriverEmail = findViewById<EditText>(R.id.driver_email)
        DriverPassword = findViewById<EditText>(R.id.driver_password)
        loadingBar = ProgressDialog(this)
        RegisterDriverButton!!.visibility = View.INVISIBLE
        RegisterDriverButton!!.isEnabled = false
        CreateDriverAccount!!.setOnClickListener {
            CreateDriverAccount!!.visibility = View.INVISIBLE
            LoginDriverButton!!.visibility = View.INVISIBLE
            TitleDriver!!.text = "Driver Registration"
            RegisterDriverButton!!.visibility = View.VISIBLE
            RegisterDriverButton!!.isEnabled = true
        }
        RegisterDriverButton!!.setOnClickListener {
            val email = DriverEmail!!.text.toString()
            val password = DriverPassword!!.text.toString()
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
        LoginDriverButton!!.setOnClickListener {
            val email = DriverEmail!!.text.toString()
            val password = DriverPassword!!.text.toString()
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
                                "Error Occurred, while Signing In... ",
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
}