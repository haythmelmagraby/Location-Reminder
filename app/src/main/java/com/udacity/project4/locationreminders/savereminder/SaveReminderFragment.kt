package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startIntentSenderForResult
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit

private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
private const val TAG = "SaveReminderFragment"
const val GEOFENCE_RADIUS_IN_METERS = 100f
val GEOFENCE_EXPIRATION_IN_MILLISECONDS: Long = TimeUnit.HOURS.toMillis(1)


class SaveReminderFragment : BaseFragment() {
    private lateinit var reminderDataItem: ReminderDataItem

    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private val runningQOrLater =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q
    private lateinit var geofencingClient: GeofencingClient
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireActivity(), GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(requireActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i("Save Geofence", "created")
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }


        binding.saveReminder.setOnClickListener {
            createReminderAndSave()
        }
    }


    fun createReminderAndSave(resolve: Boolean = true) {
        val title = _viewModel.reminderTitle.value
        val description = _viewModel.reminderDescription.value
        val location = _viewModel.reminderSelectedLocationStr.value
        val latitude = _viewModel.latitude.value
        val longitude = _viewModel.longitude.value


        val reminderDataItem =
            ReminderDataItem(title, description, location, latitude, longitude)


        if (_viewModel.validateEnteredData(reminderDataItem)) {
            if (foregroundAndBackgroundLocationPermissionApproved()) {
                checkDeviceLocationSettingsAndStartGeofence(reminderDataItem,resolve)
            } else {
                requestForegroundAndBackgroundLocationPermissions()
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE -> {
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    createReminderAndSave()
                } else {
                    requestForegroundAndBackgroundLocationPermissions()
                }
                return
            }

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            createReminderAndSave(false)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    private fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
        val forgroundLocationApproved =
            (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ))
        val backgroundLocationApproved =
            if (runningQOrLater) {
                PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            } else {
                true
            }

        return forgroundLocationApproved && backgroundLocationApproved
    }

    private fun requestForegroundAndBackgroundLocationPermissions() {
        if (foregroundAndBackgroundLocationPermissionApproved())
            return
        var permissionArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val resultCode = when {
            runningQOrLater -> {
                permissionArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            }
            else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        }
        Log.d(TAG, "Request forground only location permission")
        requestPermissions(permissionArray, resultCode)


    }

    private fun addGeofence(reminderData: ReminderDataItem) {

        val geofence = Geofence.Builder()
            .setRequestId(reminderData.id)
            .setCircularRegion(
                reminderData.latitude!!,
                reminderData.longitude!!,
                GEOFENCE_RADIUS_IN_METERS
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        geofencingRequest.geofences
        geofencingClient = LocationServices.getGeofencingClient(requireActivity())

        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {
                addOnSuccessListener {

                    Log.e("add geofence", geofence.requestId)
                    _viewModel.saveReminder(reminderData)
                }
                addOnFailureListener {
                    Toast.makeText(
                        requireActivity(),
                        R.string.geofences_not_added,
                        Toast.LENGTH_SHORT
                    ).show()
                    if ((it.message != null)) {
                        Log.i(TAG, it.message!!)
                    }
                }
            }
        }

    }


    private fun checkDeviceLocationSettingsAndStartGeofence(
        reminderDataItem: ReminderDataItem,
        resolve: Boolean = true
    ) {
        // create a location request
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        // Build the location request
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        // get the location services client setting to check it
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask = settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    this.startIntentSenderForResult(
                        exception.getResolution().getIntentSender(),
                        REQUEST_TURN_DEVICE_LOCATION_ON, null, 0, 0,
                        0, null
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error geting location setting resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                    binding.saveReminderContainer,
                    R.string.location_required_error,
                    Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettingsAndStartGeofence(reminderDataItem)
                }.show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful && reminderDataItem.title != null && reminderDataItem.location != null) {
                Log.i("Save Geofence", "will start add")
                addGeofence(reminderDataItem)
            } else {
                Log.i("Save Geofence", "will not start")
            }
        }
    }

    companion object {
        internal const val ACTION_GEOFENCE_EVENT =
            "SaveReminderFragment.LocationReminder.action.ACTION_GEOFENCE_EVENT"
    }


}
