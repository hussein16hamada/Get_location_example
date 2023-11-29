package com.hussein.testandroid

import android.Manifest
import android.app.Activity
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest

class TestFragment : Fragment() {
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private val BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 1002

    private val TAG = "LLLLLLLLLLLLL"

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private lateinit var locationTextView: TextView
    private lateinit var startLocationUpdatesButton: Button


    private lateinit var backgroundLocationTextView: TextView
    private lateinit var backgroundLocationUpdatesButton: Button

    private lateinit var physicalActivityTextView: TextView
    private lateinit var physicalActivityButton: Button


    private lateinit var locationSettingsLauncher: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>


    private lateinit var physicalActivityLauncher: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var requestPhysicalActivityPermissionLauncher: ActivityResultLauncher<String>
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

        backgroundLocationTextView = rootView.findViewById(R.id.backgroundLocationTextView)
        backgroundLocationUpdatesButton = rootView.findViewById(R.id.backgroundLocationButton)


        physicalActivityTextView = rootView.findViewById(R.id.physicalActivityTextView)
        physicalActivityButton = rootView.findViewById(R.id.startPhysicalActivityButton)


        //--------------------------------------------------------------------------------------------------------------------


        requestPhysicalActivityPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {

                    physicalActivityTextView.text="physical Activity is granted"
                    // Permission is granted, you can now use the activity recognition API
                    // Start recognizing activities here
                } else {
                    physicalActivityTextView.text="physical Activity is NOT granted"

                    // Permission is denied
                    // Handle the denial or provide information to the user
                }
            }

        locationCallBack()


        physicalActivityButton.setOnClickListener {
            requestActivityRecognitionPermission()
        }

        backgroundLocationUpdatesButton.setOnClickListener {
//            requestBackgroundLocation()
            backgroundLocationPermissionApi29()
        }

        startLocationUpdatesButton.setOnClickListener {
            requestLocationUpdates()
        }
        locationSettingsLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                onLocationSettingsResult(result)
            }

        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission())
            { isGranted ->
                if (isGranted) {
                    locationTextView.text = "location permission is granted"
//                showToast("isGranted")
                } else {
//                showToast("else")
                    locationTextView.text = "location permission is NOT granted"


                }
            }
        return rootView

    }


    private fun requestActivityRecognitionPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                "android.permission.ACTIVITY_RECOGNITION"
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is already granted
            // Start recognizing activities here
        } else {
            // Permission is not granted, request it
            requestPermissionLauncher.launch("android.permission.ACTIVITY_RECOGNITION")
        }
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
                locationTextView.text =
                    "Location: ${lastLocation?.latitude}, ${lastLocation?.longitude}"
            }
        }

    }

    // Function to check if GPS is enabled
    private fun isGPSEnabled(): Boolean {
        val locationManager =
            requireActivity().getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    // In your requestLocationUpdates method
    private fun requestLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            if (!isGPSEnabled()) {
                val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
                val client = LocationServices.getSettingsClient(requireActivity())
                val task = client.checkLocationSettings(builder.build())

                task.addOnSuccessListener {
                    // GPS is enabled, you can request location updates
                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        null
                    )
                }

                task.addOnFailureListener { exception ->
                    if (exception is ResolvableApiException) {
                        try {
                            // Show a dialog to the user to enable GPS
                            locationSettingsLauncher.launch(
                                IntentSenderRequest.Builder(exception.resolution.intentSender)
                                    .build()
                            )
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
            // it is callback maybe work maybe not !!
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
            Log.d(TAG, "onLocationSettingsResult: RESULT_OK")
            // Location settings are enabled
            // Proceed with requesting location updates
//            showToast("Location settings enabled")
        } else {
            Log.d(TAG, "onLocationSettingsResult: not enabled")

            // Location settings were not enabled
            // Handle this case
//            showToast("Location settings not enabled")
        }
    }


    //maybe work and maybe not !!!
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "requestCode: ${requestCode}")
        if (requestCode == BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE) {
            Log.d(TAG, "onRequestPermissionsResult: BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE")
            Log.d(TAG, "onRequestPermissionsResult: ${grantResults[0]}")
           backgroundLocationTextView.text="BACKGROUND_LOCATION_PERMISSION_ Granted "
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onRequestPermissionsResult: PERMISSION_GRANTED")

                requestLocationUpdates()
            }
        }
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            Log.d(TAG, "onRequestPermissionsResult: LOCATION_PERMISSION_REQUEST_CODE")
            Log.d(TAG, "onRequestPermissionsResult: ${grantResults[0]}")

            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onRequestPermissionsResult: PERMISSION_GRANTED")

                requestLocationUpdates()
            }
        }
    }


    private fun backgroundLocationPermissionApi29() {
//        Log.i( "login_em", "accessPermissionApi29() " );
        //not need
        val version = Build.VERSION.SDK_INT
        if (version < 29) {
//            whatToDoAfterSuccessAllPermission()
            return
        }
        val perm29 = arrayOf(
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
        try {
            ActivityCompat.requestPermissions(requireActivity(), perm29, BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE)
        } catch (e: Exception) {
            e.printStackTrace()
        } catch (e: Error) {
            e.printStackTrace()
        }
    }


    private fun physicalActivity(){

    }
}