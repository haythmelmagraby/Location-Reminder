package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private val REQUEST_LOCATION_PERMISSION = 1
    private val TAG = SelectLocationFragment::class.java.simpleName
    private lateinit var poi: PointOfInterest
    private val client by lazy {
        LocationServices.getFusedLocationProviderClient(requireActivity())
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapId) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        binding.btnChooseLocation.setOnClickListener {
            onLocationSelected()
        }

        return binding.root
    }

    private fun onLocationSelected() {
        _viewModel.reminderSelectedLocationStr.value = poi.name
        _viewModel.latitude.value = poi.latLng.latitude
        _viewModel.longitude.value = poi.latLng.longitude
        _viewModel.navigationCommand.value = NavigationCommand.Back
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(p0: GoogleMap) {

        map = p0
        enableMyLocation()
        setMapLongClick(map)
        setPoiClick(map)
        setMapStyle(map)

    }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapClickListener {
            map.clear()
            poi = PointOfInterest(it, "id", "Drop Pin")
            val snippet = String.format(
                Locale.getDefault(),
                "lat: %1$.5f, Long: %2$.5f",
                it.latitude,
                it.longitude
            )
            map.addMarker(
                MarkerOptions()
                    .position(it)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )
        }
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener {
            map.clear()
            val poiMarker = map.addMarker(
                MarkerOptions().position(it.latLng).title(it.name)
            )
            poi = it
            poiMarker?.showInfoWindow()
        }
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireActivity(),
                    R.raw.map_syle
                )
            )
            if (!success) {
                Log.e(TAG, "Style parsing faild")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "can't find style. Error: ", e)
        }
    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        map.clear()
        if (isPermissionGranted()) {
            map.isMyLocationEnabled = true

            client.lastLocation.addOnSuccessListener {
                it?.let {
                    val latLng = LatLng(it.latitude, it.latitude)
                    Log.i(TAG, "the latlng $latLng")
                    poi = PointOfInterest(latLng, "id", "Drop Pin")
                }
            }

        } else {
            requestPermissions(
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.size > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            } else {
                Toast.makeText(
                    context,
                    "We need location permission to remind you at location",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

}

