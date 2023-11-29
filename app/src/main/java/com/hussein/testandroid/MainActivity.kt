package com.hussein.testandroid

import android.Manifest
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest

class MainActivity : AppCompatActivity() {

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private val TAG = "LLLLLLLLLLLLL"

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private lateinit var locationTextView: TextView
    private lateinit var startLocationUpdatesButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        locationTextView = findViewById(R.id.locationTextView)
//        startLocationUpdatesButton = findViewById(R.id.startLocationUpdatesButton)

//        locationCallBack()
//
//
//        startLocationUpdatesButton.setOnClickListener {
//            requestLocationUpdates()
//        }
        val fragment = TestFragment() // Replace `YourFragment` with the actual fragment you want to open

        val transaction = supportFragmentManager.beginTransaction()

        transaction.replace(R.id.fragmentContainerView, fragment)

        transaction.addToBackStack(null) // Optional: Allows the user to navigate back
        transaction.commit()


    }

    private fun locationCallBack() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 10_000 // Update interval in milliseconds
            fastestInterval = 5_000 // Fastest update interval in milliseconds
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                val lastLocation = locationResult.lastLocation
                locationTextView.text = "Location: ${lastLocation?.latitude}, ${lastLocation?.longitude}"
            }
        }

    }

    // Function to check if GPS is enabled
    private fun isGPSEnabled(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    // In your requestLocationUpdates method
    private fun requestLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            if (!isGPSEnabled()) {
                val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
                val client = LocationServices.getSettingsClient(this)
                val task = client.checkLocationSettings(builder.build())

                task.addOnSuccessListener {
                    // GPS is enabled, you can request location updates
                    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
                }

                task.addOnFailureListener { exception ->
                    if (exception is ResolvableApiException) {
                        try {
                            // Show a dialog to the user to enable GPS
                            exception.startResolutionForResult(this, 0)
                        } catch (sendEx: IntentSender.SendIntentException) {
                            // Ignore the error
                        }
                    }
                }
            } else {
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
                Log.d(TAG, "requestLocationUpdates: if")
            }
        } else {
            Log.d(TAG, "requestLocationUpdates: else")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode ==0 && resultCode ==0){
            println("gps not opened yet ! ${resultCode}")
        }else if (requestCode ==0 && resultCode ==-1){
            println("gps opened now  ${resultCode}")

        }
    }
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        Log.d(TAG, "requestCode: ${requestCode}")
//
//        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
//            Log.d(TAG, "onRequestPermissionsResult: LOCATION_PERMISSION_REQUEST_CODE")
//            Log.d(TAG, "onRequestPermissionsResult: ${grantResults[0]}")
//
//            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Log.d(TAG, "onRequestPermissionsResult: PERMISSION_GRANTED")
//
//                requestLocationUpdates()
//            }
//        }
//    }
}