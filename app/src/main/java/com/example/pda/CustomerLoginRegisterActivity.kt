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
import com.example.pda.CustomerLoginRegisterActivity
//import com.google.android.gms.location.LocationListener
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_customer_login_register.*
import kotlinx.android.synthetic.main.activity_driver_login_register.*

class CustomerLoginRegisterActivity : AppCompatActivity() {
     
    private var customersDatabaseRef: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    private var firebaseAuthListner: FirebaseAuth.AuthStateListener? = null
    private var loadingBar: ProgressDialog? = null
    private var currentUser: FirebaseUser? = null
    var currentUserId: String? = null
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_login_register)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
        )

        mAuth = FirebaseAuth.getInstance()

        firebaseAuthListner = FirebaseAuth.AuthStateListener() {
            fun onAuthStateChanged() {
                currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null)
                {
                    Toast.makeText(this,"Yo",Toast.LENGTH_LONG).show()
//                    val intent = Intent(this@DriverLoginRegisterActivity, DriverMapActivity::class.java)
//                    startActivity(intent)
                }
            }
        }

        loadingBar = ProgressDialog(this)
        customer_register_btn!!.visibility = View.INVISIBLE
        customer_register_btn!!.isEnabled = false
        customer_register_link!!.setOnClickListener {
            customer_register_link!!.visibility = View.INVISIBLE
            customer_login_btn!!.visibility = View.INVISIBLE
            customer_status!!.text = getString(R.string.customerregistration)
            customer_register_btn!!.visibility = View.VISIBLE
            customer_register_btn!!.isEnabled = true
        }
        customer_register_btn!!.setOnClickListener {
            val email = customer_email!!.text.toString()
            val password = customer_password!!.text.toString()
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(
                    this@CustomerLoginRegisterActivity,
                    "Please write your Email...",
                    Toast.LENGTH_SHORT
                ).show()
            }
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(
                    this@CustomerLoginRegisterActivity,
                    "Please write your Password...",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                loadingBar!!.setTitle("Please wait :")
                loadingBar!!.setMessage("Registering You .....")
                loadingBar!!.show()
                mAuth!!.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful()) {
                            currentUserId = mAuth!!.currentUser?.uid
                            customersDatabaseRef =
                                FirebaseDatabase.getInstance().reference.child("Users")
                                    .child("Customers").child(currentUserId!!)
                            customersDatabaseRef!!.setValue(true)

                            customer_login_btn!!.visibility = View.VISIBLE
                            customer_status!!.text = getString(R.string.customerlogin)
                            customer_register_btn!!.visibility = View.INVISIBLE
                            customer_register_btn!!.isEnabled = false

//                            val intent = Intent(
//                                this@CustomerLoginRegisterActivity,
//                                CustomersMapActivity::class.java
//                            )
//                            startActivity(intent)
                            loadingBar!!.dismiss()
                        } else {
                            Toast.makeText(
                                this@CustomerLoginRegisterActivity,
                                "Please Try Again. Error Occurred, while registering... ",
                                Toast.LENGTH_SHORT
                            ).show()
                            loadingBar!!.dismiss()
                        }
                    }
            }
        }
        customer_login_btn!!.setOnClickListener {
            val email = customer_email!!.text.toString()
            val password = customer_password!!.text.toString()
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(
                    this@CustomerLoginRegisterActivity,
                    "Please write your Email...",
                    Toast.LENGTH_SHORT
                ).show()
            }
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(
                    this@CustomerLoginRegisterActivity,
                    "Please write your Password...",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                loadingBar!!.setTitle("Please wait :")
                loadingBar!!.setMessage("Authenticating  and Processing on your data...")
                loadingBar!!.show()
                mAuth!!.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                this@CustomerLoginRegisterActivity,
                                "Sign In , Successful...",
                                Toast.LENGTH_SHORT
                            ).show()
                            val intent = Intent(
                                this@CustomerLoginRegisterActivity,
                                CustomersMapActivity::class.java
                            )
                            startActivity(intent)
                            finish()
                            loadingBar!!.dismiss()
                        } else {
                            Toast.makeText(
                                this@CustomerLoginRegisterActivity,
                                "Error Occurred, while Signing In... ",
                                Toast.LENGTH_SHORT
                            ).show()
                            loadingBar!!.dismiss()
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