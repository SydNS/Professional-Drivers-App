package com.example.pda

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_welcome.*

//Remember Welcome Activity is our Main Activity - SimCoder MainActivity
class WelcomeActivity : AppCompatActivity() {
    private val mAuth: FirebaseAuth? = null
    private val firebaseAuthListner: AuthStateListener? = null
    private val currentUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.pda.R.layout.activity_welcome)


//        mAuth = FirebaseAuth.getInstance();
//
//        firebaseAuthListner = new FirebaseAuth.AuthStateListener() {
//            @Override
//            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
//            {
//                currentUser = FirebaseAuth.getInstance().getCurrentUser();
//
//                if(currentUser != null)
//                {
//                    Intent intent = new Intent(WelcomeActivity.this, WelcomeActivity.class);
//                    startActivity(intent);
//                }
//            }
//        };
        driver_welcome_btn?.setOnClickListener {
            val driverIntent = Intent(this,DriverLoginRegisterActivity::class.java)
            startActivity(driverIntent)
        }
        customer_welcome_btn?.setOnClickListener {
            val customerIntent = Intent(this,CustomerLoginRegisterActivity::class.java            )
            startActivity(customerIntent)
        }
    }
    //    @Override
    //    protected void onStart()
    //    {
    //        super.onStart();
    //
    //        mAuth.addAuthStateListener(firebaseAuthListner);
    //    }
    //
    //
    //    @Override
    //    protected void onStop()
    //    {
    //        super.onStop();
    //
    //        mAuth.removeAuthStateListener(firebaseAuthListner);
    //    }
}