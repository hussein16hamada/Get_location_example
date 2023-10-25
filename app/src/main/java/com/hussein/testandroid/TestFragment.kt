package com.hussein.testandroid

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest

class TestFragment : Fragment() {
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private val TAG = "LLLLLLLLLLLLL"

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private lateinit var locationTextView: TextView
    private lateinit var startLocationUpdatesButton: Button

    private lateinit var locationSettingsLauncher: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var requestPermissionLauncher : ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_test, container, false)

        locationTextView = rootView.findViewById(R.id.locationTextView)
        startLocationUpdatesButton = rootView.findViewById(R.id.startLocationUpdatesButton)

        locationCallBack()


        startLocationUpdatesButton.setOnClickListener {
            requestLocationUpdates()
        }
        locationSettingsLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            onLocationSettingsResult(result)
        }

        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission())
        { isGranted ->
            if (isGranted) {
//                showToast("isGranted")
            }else{
//                showToast("else")

            }
        }
        return rootView

    }


    private fun locationCallBack() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
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
        val locationManager = requireActivity().getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    // In your requestLocationUpdates method
    private fun requestLocationUpdates() {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {

            if (!isGPSEnabled()) {
                val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
                val client = LocationServices.getSettingsClient(requireActivity())
                val task = client.checkLocationSettings(builder.build())

                task.addOnSuccessListener {
                    // GPS is enabled, you can request location updates
                    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
                }

                task.addOnFailureListener { exception ->
                    if (exception is ResolvableApiException) {
                        try {
                            // Show a dialog to the user to enable GPS
                            locationSettingsLauncher.launch(IntentSenderRequest.Builder(exception.resolution.intentSender).build())
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
//            ActivityCompat.requestPermissions(
//                requireActivity(),
//                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
//                    Manifest.permission.ACCESS_COARSE_LOCATION),
//                LOCATION_PERMISSION_REQUEST_CODE
//            )

            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
//            requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }

    // Handle the result of the location settings resolution
    private fun onLocationSettingsResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            // Location settings are enabled
            // Proceed with requesting location updates
//            showToast("Location settings enabled")
        } else {
            // Location settings were not enabled
            // Handle this case
//            showToast("Location settings not enabled")
        }
    }
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//         showToast("dfdfd")
//        if (requestCode ==0 && resultCode ==0){
//            println("gps not opened yet ! ${resultCode}")
//        }else if (requestCode ==0 && resultCode ==-1){
//            println("gps opened now  ${resultCode}")
//
//        }
//    }


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