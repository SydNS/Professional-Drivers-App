//package com.example.pda
//
//import CropImage.ActivityResult
//import android.Manifest
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.location.Location
//import android.location.LocationListener
//import android.os.Bundle
//import android.support.v4.app.ActivityCompat
//import android.support.v4.app.FragmentActivity
//import android.support.v7.widget.Toolbar
//import android.view.View
//import android.widget.Button
//import android.widget.RelativeLayout
//import android.widget.TextView
//import androidx.fragment.app.FragmentActivity
//import com.example.pda.CustomersMapActivity
//import com.firebase.geofire.GeoFire
//import com.firebase.geofire.GeoLocation
//import com.firebase.geofire.GeoQuery
//import com.firebase.geofire.GeoQueryEventListener
//import com.google.android.gms.common.ConnectionResult
//import com.google.android.gms.common.api.GoogleApiClient
//import com.google.android.gms.location.LocationListener
//import com.google.android.gms.location.LocationRequest
//import com.google.android.gms.location.LocationServices
//import com.google.android.gms.maps.CameraUpdateFactory
//import com.google.android.gms.maps.GoogleMap
//import com.google.android.gms.maps.OnMapReadyCallback
//import com.google.android.gms.maps.SupportMapFragment
//import com.google.android.gms.maps.model.BitmapDescriptor
//import com.google.android.gms.maps.model.BitmapDescriptorFactory
//import com.google.android.gms.maps.model.LatLng
//import com.google.android.gms.maps.model.Marker
//import com.google.android.gms.maps.model.MarkerOptions
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.auth.FirebaseUser
//import com.google.firebase.database.DataSnapshot
//import com.google.firebase.database.DatabaseError
//import com.google.firebase.database.DatabaseReference
//import com.google.firebase.database.FirebaseDatabase
//import com.google.firebase.database.ValueEventListener
//import com.squareup.picasso.Picasso
//import de.hdodenhof.circleimageview.CircleImageView
//import java.util.*
//
//class CustomersMapActivity : FragmentActivity(), OnMapReadyCallback,
//    GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
//    LocationListener {
//    private var mMap: GoogleMap? = null
//    private var googleApiClient: GoogleApiClient? = null
//    var LastLocation: Location? = null
//    var locationRequest: LocationRequest? = null
//    private var Logout: Button? = null
//    private var SettingsButton: Button? = null
//    private var CallCabCarButton: Button? = null
//    private var mAuth: FirebaseAuth? = null
//    private var currentUser: FirebaseUser? = null
//    private var CustomerDatabaseRef: DatabaseReference? = null
//    private var CustomerPickUpLocation: LatLng? = null
//    private var DriverAvailableRef: DatabaseReference? = null
//    private var DriverLocationRef: DatabaseReference? = null
//    private var DriversRef: DatabaseReference? = null
//    private var radius = 1
//    private var driverFound: Boolean? = false
//    private var requestType = false
//    private var driverFoundID: String? = null
//    private var customerID: String? = null
//    var DriverMarker: Marker? = null
//    var PickUpMarker: Marker? = null
//    var geoQuery: GeoQuery? = null
//    private var DriverLocationRefListner: ValueEventListener? = null
//    private var txtName: TextView? = null
//    private var txtPhone: TextView? = null
//    private var txtCarName: TextView? = null
//    private var profilePic: CircleImageView? = null
//    private var relativeLayout: RelativeLayout? = null
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_customers_map)
//        mAuth = FirebaseAuth.getInstance()
//        currentUser = mAuth.getCurrentUser()
//        customerID = FirebaseAuth.getInstance().getCurrentUser().getUid()
//        CustomerDatabaseRef =
//            FirebaseDatabase.getInstance().getReference().child("Customer Requests")
//        DriverAvailableRef =
//            FirebaseDatabase.getInstance().getReference().child("Drivers Available")
//        DriverLocationRef = FirebaseDatabase.getInstance().getReference().child("Drivers Working")
//        Logout = findViewById(R.id.logout_customer_btn) as Button?
//        SettingsButton = findViewById(R.id.settings_customer_btn) as Button?
//        CallCabCarButton = findViewById(R.id.call_a_car_button) as Button?
//        txtName = findViewById(R.id.name_driver)
//        txtPhone = findViewById(R.id.phone_driver)
//        txtCarName = findViewById(R.id.car_name_driver)
//        profilePic = findViewById(R.id.profile_image_driver)
//        relativeLayout = findViewById(R.id.rel1)
//
//
//        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        val mapFragment: SupportMapFragment = getSupportFragmentManager()
//            .findFragmentById(R.id.map) as SupportMapFragment
//        mapFragment.getMapAsync(this)
//        SettingsButton!!.setOnClickListener {
//            val intent = Intent(this@CustomersMapActivity, SettingsActivity::class.java)
//            intent.putExtra("type", "Customers")
//            startActivity(intent)
//        }
//        Logout!!.setOnClickListener {
//            mAuth.signOut()
//            LogOutUser()
//        }
//        CallCabCarButton!!.setOnClickListener {
//            if (requestType) {
//                requestType = false
//                geoQuery.removeAllListeners()
//                DriverLocationRefListner?.let { it1 -> DriverLocationRef!!.removeEventListener(it1) }
//                if (driverFound != null) {
//                    DriversRef = driverFoundID?.let { it1 ->
//                        FirebaseDatabase.getInstance().getReference()
//                            .child("Users").child("Drivers").child(it1)
//                            .child("CustomerRideID")
//                    }
//                    DriversRef.removeValue()
//                    driverFoundID = null
//                }
//                driverFound = false
//                radius = 1
//                val customerId: String = FirebaseAuth.getInstance().getCurrentUser().getUid()
//                val geoFire = GeoFire(CustomerDatabaseRef)
//                geoFire.removeLocation(customerId)
//                PickUpMarker?.remove()
//                DriverMarker?.remove()
//                CallCabCarButton!!.text = "Call a Cab"
//                relativeLayout!!.visibility = View.GONE
//            } else {
//                requestType = true
//                val customerId: String = FirebaseAuth.getInstance().getCurrentUser().getUid()
//                val geoFire = GeoFire(CustomerDatabaseRef)
//                geoFire.setLocation(
//                    customerId,
//                    GeoLocation(LastLocation!!.latitude, LastLocation!!.longitude)
//                )
//                CustomerPickUpLocation = LatLng(LastLocation!!.latitude, LastLocation!!.longitude)
//                PickUpMarker = mMap.addMarker(
//                    MarkerOptions().position(CustomerPickUpLocation).title("My Location")
//                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.user))
//                )
//                CallCabCarButton!!.text = "Getting your Driver..."
//                closetDriverCab
//            }
//        }
//    }//we tell driver which customer he is going to have
//
//    //Show driver location on customerMapActivity
//    //anytime the driver is called this method will be called
//    //key=driverID and the location
//    private val closetDriverCab: Unit
//        private get() {
//            val geoFire = GeoFire(DriverAvailableRef)
//            geoQuery = geoFire.queryAtLocation(
//                GeoLocation(
//                    CustomerPickUpLocation.latitude,
//                    CustomerPickUpLocation.longitude
//                ), radius
//            )
//            geoQuery.removeAllListeners()
//            geoQuery.addGeoQueryEventListener(object : GeoQueryEventListener() {
//                fun onKeyEntered(key: String?, location: GeoLocation?) {
//                    //anytime the driver is called this method will be called
//                    //key=driverID and the location
//                    if (!driverFound!! && requestType) {
//                        driverFound = true
//                        driverFoundID = key
//
//
//                        //we tell driver which customer he is going to have
//                        DriversRef = FirebaseDatabase.getInstance().getReference().child("Users")
//                            .child("Drivers").child(driverFoundID)
//                        val driversMap: HashMap<*, *> = HashMap<Any?, Any?>()
//                        driversMap["CustomerRideID"] = customerID
//                        DriversRef.updateChildren(driversMap)
//
//                        //Show driver location on customerMapActivity
//                        GettingDriverLocation()
//                        CallCabCarButton!!.text = "Looking for Driver Location..."
//                    }
//                }
//
//                fun onKeyExited(key: String?) {}
//                fun onKeyMoved(key: String?, location: GeoLocation?) {}
//                fun onGeoQueryReady() {
//                    if (!driverFound!!) {
//                        radius = radius + 1
//                        closetDriverCab
//                    }
//                }
//
//                fun onGeoQueryError(error: DatabaseError?) {}
//            })
//        }
//
//    //and then we get to the driver location - to tell customer where is the driver
//    private fun GettingDriverLocation() {
//        DriverLocationRefListner = driverFoundID?.let {
//            DriverLocationRef?.child(it).child("l")
//                .addValueEventListener(object : ValueEventListener {
//                    fun onDataChange(dataSnapshot: DataSnapshot) {
//                        if (dataSnapshot.exists() && requestType) {
//                            val driverLocationMap = dataSnapshot.getValue() as List<Any?>
//                            var LocationLat = 0.0
//                            var LocationLng = 0.0
//                            CallCabCarButton!!.text = "Driver Found"
//                            relativeLayout!!.visibility = View.VISIBLE
//                            assignedDriverInformation
//                            if (driverLocationMap[0] != null) {
//                                LocationLat = driverLocationMap[0].toString().toDouble()
//                            }
//                            if (driverLocationMap[1] != null) {
//                                LocationLng = driverLocationMap[1].toString().toDouble()
//                            }
//
//                            //adding marker - to pointing where driver is - using this lat lng
//                            val DriverLatLng = LatLng(LocationLat, LocationLng)
//                            if (DriverMarker != null) {
//                                DriverMarker.remove()
//                            }
//                            val location1 = Location("")
//                            location1.latitude = CustomerPickUpLocation.latitude
//                            location1.longitude = CustomerPickUpLocation.longitude
//                            val location2 = Location("")
//                            location2.latitude = DriverLatLng.latitude
//                            location2.longitude = DriverLatLng.longitude
//                            val Distance = location1.distanceTo(location2)
//                            if (Distance < 90) {
//                                CallCabCarButton!!.text = "Driver's Reached"
//                            } else {
//                                CallCabCarButton!!.text = "Driver Found: $Distance"
//                            }
//                            DriverMarker = mMap.addMarker(
//                                MarkerOptions().position(DriverLatLng).title("your driver is here")
//                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.car))
//                            )
//                        }
//                    }
//
//                    @JvmName("onCancelled1")
//                    override fun onCancelled(p0: DatabaseError) {
//                        TODO("Not yet implemented")
//                    }
//
//                    fun onCancelled(databaseError: DatabaseError?) {}
//                })
//        }
//    }
//
//    fun onMapReady(googleMap: GoogleMap?) {
//        mMap = googleMap
//
//        // now let set user location enable
//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) !== PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            ) !== PackageManager.PERMISSION_GRANTED
//        ) {
//            return
//        }
//        buildGoogleApiClient()
//        mMap.setMyLocationEnabled(true)
//    }
//
//    fun onConnected(@Nullable bundle: Bundle?) {
//        locationRequest = LocationRequest()
//        locationRequest.setInterval(1000)
//        locationRequest.setFastestInterval(1000)
//        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//            !== PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            ) !== PackageManager.PERMISSION_GRANTED
//        ) {
//            //
//            return
//        }
//        //it will handle the refreshment of the location
//        //if we dont call it we will get location only once
//        LocationServices.FusedLocationApi.requestLocationUpdates(
//            googleApiClient,
//            locationRequest,
//            this
//        )
//    }
//
//    fun onConnectionSuspended(i: Int) {}
//    fun onConnectionFailed(@NonNull connectionResult: ConnectionResult?) {}
//    fun onLocationChanged(location: Location) {
//        //getting the updated location
//        LastLocation = location
//        val latLng = LatLng(location.latitude, location.longitude)
//        mMap?.moveCamera(CameraUpdateFactory.newLatLng(latLng))
//        mMap?.animateCamera(CameraUpdateFactory.zoomTo(12))
//    }
//
//    //create this method -- for useing apis
//    @Synchronized
//    protected fun buildGoogleApiClient() {
//        googleApiClient = Builder(this)
//            .addConnectionCallbacks(this)
//            .addOnConnectionFailedListener(this)
//            .addApi(LocationServices.API)
//            .build()
//        googleApiClient?.connect()
//    }
//
//    protected override fun onStop() {
//        super.onStop()
//    }
//
//    fun LogOutUser() {
//        val startPageIntent = Intent(this@CustomersMapActivity, WelcomeActivity::class.java)
//        startPageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
//        startActivity(startPageIntent)
//        finish()
//    }
//
//    private val assignedDriverInformation: Unit
//        private get() {
//            val reference: DatabaseReference = FirebaseDatabase.getInstance().getReference()
//                .child("Users").child("Drivers").child(driverFoundID)
//            reference.addValueEventListener(object : ValueEventListener() {
//                fun onDataChange(dataSnapshot: DataSnapshot) {
//                    if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
//                        val name: String = dataSnapshot.child("name").getValue().toString()
//                        val phone: String = dataSnapshot.child("phone").getValue().toString()
//                        val car: String = dataSnapshot.child("car").getValue().toString()
//                        txtName!!.text = name
//                        txtPhone!!.text = phone
//                        txtCarName!!.text = car
//                        if (dataSnapshot.hasChild("image")) {
//                            val image: String = dataSnapshot.child("image").getValue().toString()
//                            Picasso.get().load(image).into(profilePic)
//                        }
//                    }
//                }
//
//                fun onCancelled(databaseError: DatabaseError?) {}
//            })
//        }
//}