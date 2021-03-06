@file:Suppress("DEPRECATION", "UNCHECKED_CAST")

package com.example.pda


import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.database.FirebaseDatabase.getInstance
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_customers_map.*
import java.util.*
import kotlin.collections.HashMap

open class CustomersMapActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
    LocationListener {

    private lateinit var mMap: GoogleMap
    lateinit var googleApiClient: GoogleApiClient
    lateinit var location: Location
    private lateinit var lastLocation: Location
    lateinit var locationRequest: LocationRequest

    //getting request customer's id
    private var customerID = ""
    private var customerPickupLocation: LatLng? = null

    var driverLocationref: DatabaseReference? = null
    val geoQuery: GeoQuery? = null

//    private var driverID: String? = null
//    private var AssignedCustomerRef: DatabaseReference? = null
//    private var AssignedCustomerPickUpRef: DatabaseReference? = null
//    var PickUpMarker: Marker? = null
//    private var AssignedCustomerPickUpRefListner: ValueEventListener? = null
//    private var txtName: TextView? = null
//    private var txtPhone: TextView? = null

    //private var settingsbtn: Button? = null
    private var currentLogOutCustomerStatus = false
    var radius = 1.0
    var driverFound = false
    var requestType = false
    var driver_found_id: String? = null
    var DriversRef: DatabaseReference? = null
    var DriverMarker: Marker? = null
    var PickUpMarker: Marker? = null
    var DriverLocationRefListener: ValueEventListener? = null


    //    private var logoutbtn: Button? = null
//    private var profilePic: CircleImageView? = null
    private var relativeLayout: RelativeLayout? = null
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customers_map)


        mAuth = FirebaseAuth.getInstance()
        customerID = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val CustomersDatabaseRef = getInstance().reference.child("Customers Request")
        driverLocationref = getInstance().reference.child("Drivers Available")

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        logoutbtncustomer.setOnClickListener {
            currentLogOutCustomerStatus = true
            DisconnectDriver()
            mAuth?.signOut()
            LogOutCustomer()
        }
        customer_request.setOnClickListener {

            if (requestType) {

                requestType = false
                geoQuery?.removeAllListeners()
                driverLocationref?.removeEventListener(DriverLocationRefListener!!)

                if (driverFound != null) {
                    val DriversRef = getInstance().getReference().child("Users").child("Drivers")
                        .child(driver_found_id!!)
                    DriversRef.setValue(true)
                    driver_found_id = null
                }
                driverFound = false
                radius = 1.0

                val customerId = FirebaseAuth.getInstance().currentUser?.uid
                val geoFire = GeoFire(CustomersDatabaseRef)
                geoFire.removeLocation(customerId)

                if (PickUpMarker != null) {
                    PickUpMarker?.remove()
                    customer_request.text = "Request a Driver"
                }

            } else {
                requestType = true
                val geoFire = GeoFire(CustomersDatabaseRef)
                geoFire.setLocation(
                    customerID, GeoLocation(
                        lastLocation.latitude,
                        lastLocation.longitude
                    )
                )
                customerPickupLocation = LatLng(lastLocation.latitude, lastLocation.longitude)

                mMap.addMarker(
                    MarkerOptions().position(customerPickupLocation!!).title("Pick up Location")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.personpindrop))
                )
                customer_request.text = getString(R.string.gettingyouadriver)
                getClosestDriver()
            }

        }
    }

    private fun getClosestDriver() {
        val geoFire = GeoFire(driverLocationref)

        val geoQuery: GeoQuery = geoFire.queryAtLocation(
            GeoLocation(
                customerPickupLocation!!.latitude,
                customerPickupLocation!!.longitude
            ), radius
        )

        geoQuery.addGeoQueryEventListener(object : GeoQueryEventListener {
            override fun onKeyEntered(key: String, location: GeoLocation) {
                //anytime the driver is called this method will be called
                //key=driverID and the location
                if (!driverFound && requestType) {
                    driverFound = true
                    driver_found_id = key


//                    //we tell driver which customer he is going to have
                    DriversRef =
                        getInstance().reference.child("Users").child("Drivers").child(
                            driver_found_id!!
                        )
                    val driversMap = HashMap<String, Any>()
                    driversMap["CustomerRideID"] = customerID
                    DriversRef!!.updateChildren(driversMap)

                    //Show driver location on customerMapActivity
                    GettingDriverLocation()
                    customer_request.text = "Looking for Driver Location..."


                }
            }

            override fun onKeyExited(key: String) {}
            override fun onKeyMoved(key: String, location: GeoLocation) {}
            override fun onGeoQueryReady() {
                if (!driverFound) {
                    radius += 1
                    getClosestDriver()
                }
            }

            override fun onGeoQueryError(error: DatabaseError?) {}
        })


    }

    private fun GettingDriverLocation() {

//this will look for the driver around and online , if the driver accepts the request
// then, the driver working node is available and we get his location using his ID through the DataSnapshot

        DriverLocationRefListener =
            driverLocationref?.child(driver_found_id!!)?.child("l")?.addValueEventListener(object :
                ValueEventListener {
                @RequiresApi(Build.VERSION_CODES.KITKAT)
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists() && requestType) {
                        val driverLocationoncustomersap: List<Objects> = p0.value as List<Objects>
                        var LocationLat = 0.0
                        var LocationLng = 0.0
                        customer_request.text = getString(R.string.driverfound)


                        if (driverLocationoncustomersap.get(0) != null) {
                            LocationLat = driverLocationoncustomersap[0].toString() as Double
                        }
                        if (driverLocationoncustomersap.get(1) != null) {
                            LocationLng = driverLocationoncustomersap[1].toString() as Double
                        }

                        val driverlatlngoncustomermap = LatLng(LocationLat, LocationLng)

                        if (DriverMarker != null) {
                            DriverMarker?.remove()
                        }

                        //customers location
                        val location1 = Location("")
                        location1.latitude = customerPickupLocation!!.latitude
                        location1.longitude = customerPickupLocation!!.longitude

                        //drivers locations
                        val location2 = Location("")
                        location2.latitude = driverlatlngoncustomermap.latitude
                        location2.longitude = driverlatlngoncustomermap.longitude

                        //calculate the distance between the driver and the customer
                        val distance = location1.distanceTo(location2)
                        if (distance < 90) {
                            customer_request.text = getString(R.string.driverishere)
                        } else {
                            customer_request.text = "driver is ${distance.toString()}m away"
                        }

                        DriverMarker = mMap.addMarker(
                            MarkerOptions().position(driverlatlngoncustomermap)
                                .title("Your Driver is here").icon(BitmapDescriptorFactory.fromResource(R.drawable.driver))
                        )
                    }
                }

                override fun onCancelled(p0: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker and move the camera
        buildGoogleApiClient()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        mMap.isMyLocationEnabled = true


    }

    override fun onConnected(p0: Bundle?) {
        locationRequest = LocationRequest()
        locationRequest.interval = 100000
        locationRequest.fastestInterval = 100000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
            googleApiClient,
            locationRequest,
            this
        )
    }

    override fun onConnectionSuspended(p0: Int) {
        TODO("Not yet implemented")
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        TODO("Not yet implemented")
    }

    override fun onLocationChanged(location: Location) {

        lastLocation = location

        val latlong = LatLng(location.latitude, location.longitude)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlong))
        mMap.addMarker(
            MarkerOptions().position(latlong)
                .title("my Location")
        )
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17F))
        DisconnectDriver()


        val userID: String? = FirebaseAuth.getInstance().currentUser?.uid
        val DriversAvailabilityRef: DatabaseReference =
            getInstance().reference.child("Drivers Available")
        val geoFireAvailability = GeoFire(DriversAvailabilityRef)
        val DriversWorkingRef: DatabaseReference =
            getInstance().reference.child("Drivers Working")
        val geoFireWorking = GeoFire(DriversWorkingRef)

        when (customerID) {
            "" -> {
                geoFireWorking.removeLocation(userID)
                geoFireAvailability.setLocation(
                    userID,
                    GeoLocation(location.latitude, location.longitude)
                )
            }
            else -> {
                geoFireAvailability.removeLocation(userID)
                geoFireWorking.setLocation(
                    userID,
                    GeoLocation(location.latitude, location.longitude)
                )
            }
        }


    }

    @Synchronized
    protected fun buildGoogleApiClient() {
        googleApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()
        googleApiClient.connect()
    }


    override fun onStop() {
        super.onStop()

        if (!currentLogOutCustomerStatus) {
            DisconnectDriver()
        }

    }

    private fun DisconnectDriver() {
        val userID: String? = FirebaseAuth.getInstance().currentUser?.uid
        val database = Firebase.database.reference
//        Toast.makeText(this,userID.toString(),Toast.LENGTH_SHORT).show()
        val DriversAvailabiltyRef: DatabaseReference = database.child("Drivers Available")
        val geoFire = GeoFire(DriversAvailabiltyRef)
        geoFire.removeLocation(userID)

    }

    private fun LogOutCustomer() {
        Toast.makeText(this, "SuccessFully Logged Out", Toast.LENGTH_LONG).show()
//        val startPageIntent = Intent(this, WelcomeActivity::class.java)
//        startPageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
//        startActivity(startPageIntent)
        onBackPressed()
        finish()
    }


    open fun bitmapDescriptorFromVector(
        context: Context,
        @DrawableRes vectorDrawableResourceId: Int
    ): BitmapDescriptor? {
        val background: Drawable? =
            ContextCompat.getDrawable(context, R.drawable.pda_driver)
        background?.setBounds(0, 0, background.intrinsicWidth, background.intrinsicHeight)
        val vectorDrawable: Drawable? = ContextCompat.getDrawable(context, vectorDrawableResourceId)
        vectorDrawable?.setBounds(
            40,
            20,
            vectorDrawable.intrinsicWidth + 40,
            vectorDrawable.intrinsicHeight + 20
        )
        val bitmap: Bitmap = Bitmap.createBitmap(
            background!!.intrinsicWidth,
            background.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        background.draw(canvas)
        vectorDrawable?.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }


    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        mAuth = FirebaseAuth.getInstance()
        val currentUser: FirebaseUser? = mAuth?.currentUser

        Log.e("error", currentUser.toString())
        if (currentUser == null) {
            startActivity(
                Intent(
                    this,
                    WelcomeActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            finish()
        }
    }


}