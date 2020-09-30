@file:Suppress("DEPRECATION")

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
import com.example.pda.CustomerLoginRegisterActivity
//import com.google.android.gms.location.LocationListener
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CustomerLoginRegisterActivity : AppCompatActivity() {
    private var CreateCustomerAccount: TextView? = null
    private var TitleCustomer: TextView? = null
    private var LoginCustomerButton: Button? = null
    private var RegisterCustomerButton: Button? = null
    private var CustomerEmail: EditText? = null
    private var CustomerPassword: EditText? = null
    private var customersDatabaseRef: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    private var firebaseAuthListner: FirebaseAuth.AuthStateListener? = null
    private var loadingBar: ProgressDialog? = null
    private var currentUser: FirebaseUser? = null
    var currentUserId: String? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_login_register)

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

        CreateCustomerAccount = findViewById<TextView>(R.id.customer_register_link)
        TitleCustomer = findViewById<TextView>(R.id.customer_status)
        LoginCustomerButton = findViewById<Button>(R.id.customer_login_btn)
        RegisterCustomerButton = findViewById<Button>(R.id.customer_register_btn)
        CustomerEmail = findViewById<EditText>(R.id.customer_email)
        CustomerPassword = findViewById<EditText>(R.id.customer_password)
        loadingBar = ProgressDialog(this)
        RegisterCustomerButton!!.visibility = View.INVISIBLE
        RegisterCustomerButton!!.isEnabled = false
        CreateCustomerAccount!!.setOnClickListener {
            CreateCustomerAccount!!.visibility = View.INVISIBLE
            LoginCustomerButton!!.visibility = View.INVISIBLE
            TitleCustomer!!.text = "Driver Registration"
            RegisterCustomerButton!!.visibility = View.VISIBLE
            RegisterCustomerButton!!.isEnabled = true
        }
        RegisterCustomerButton!!.setOnClickListener {
            val email = CustomerEmail!!.text.toString()
            val password = CustomerPassword!!.text.toString()
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
                loadingBar!!.setMessage("While system is performing processing on your data...")
                loadingBar!!.show()
                mAuth!!.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful()) {
                            currentUserId = mAuth!!.currentUser?.uid
                            customersDatabaseRef =
                                FirebaseDatabase.getInstance().reference.child("Users")
                                    .child("Customers").child(currentUserId!!)
                            customersDatabaseRef!!.setValue(true)
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
        LoginCustomerButton!!.setOnClickListener {
            val email = CustomerEmail!!.text.toString()
            val password = CustomerPassword!!.text.toString()
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
                loadingBar!!.setMessage("While system is performing processing on your data...")
                loadingBar!!.show()
                mAuth!!.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                this@CustomerLoginRegisterActivity,
                                "Sign In , Successful...",
                                Toast.LENGTH_SHORT
                            ).show()
                            //                                val intent = Intent(
                            //                                    this@CustomerLoginRegisterActivity,
                            //                                    CustomersMapActivity::class.java
                            //                                )
                            //                                startActivity(intent)
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


    protected override fun onStart() {
        super.onStart()
        firebaseAuthListner?.let { mAuth?.addAuthStateListener(it) }
    }
    protected override fun onStop() {
        super.onStop()
        firebaseAuthListner?.let { mAuth?.removeAuthStateListener(it) }
    }

}