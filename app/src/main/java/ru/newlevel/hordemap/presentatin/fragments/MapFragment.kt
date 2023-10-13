package ru.newlevel.hordemap.presentatin.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.*
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.app.LocationUpdatesBroadcastReceiver
import ru.newlevel.hordemap.app.MyAlarmReceiver
import ru.newlevel.hordemap.data.storage.models.MarkerDataModel
import ru.newlevel.hordemap.hasPermission
import ru.newlevel.hordemap.presentatin.MainActivity
import ru.newlevel.hordemap.presentatin.viewmodels.LocationUpdateViewModel
import ru.newlevel.hordemap.presentatin.viewmodels.MarkerViewModel
import java.util.*


class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private var userMarkersObserver: Observer<List<MarkerDataModel>>? = null
    private var staticMarkersObserver: Observer<List<MarkerDataModel>>? = null
    private val markerViewModel by viewModel<MarkerViewModel>()
    private val receiver = LocationUpdatesBroadcastReceiver()
    private val locationUpdateViewModel by viewModel<LocationUpdateViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_maps, container, false)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        return view
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

        userMarkersObserver = Observer {
            markerViewModel.createUsersMarkers(it, googleMap)
        }

        staticMarkersObserver = Observer {
            markerViewModel.createStaticMarkers(it, googleMap)
        }

       locationUpdateViewModel.startLocationUpdates()

        // настройки карты
        setupMap()
        // слушатели нажатий на карте
        mapListenersSetup()
        // Запускаем обсерверы
        startObservers()

        //Камера на Красноярск
        val location = LatLng(56.0901, 93.2329) //координаты красноярска
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLng(location))

        val filter = IntentFilter(LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES)
        requireContext().applicationContext.registerReceiver(receiver, filter)

        val intent = Intent(requireContext().applicationContext, MyAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext().applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        (requireContext().applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager?)?.setExactAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + 30000,
            pendingIntent
        )
    }


    private fun mapListenersSetup() {
        // Скрываем диалог при коротком клике по нему
        googleMap.setOnInfoWindowClickListener { obj: Marker -> obj.hideInfoWindow() }

        //Показываем только текст маркера, без перемещения к нему камеры
        googleMap.setOnMarkerClickListener { marker: Marker ->
            marker.showInfoWindow()
            true
        }

        googleMap.setOnInfoWindowLongClickListener {
            markerViewModel.deleteStaticMarker(it)
        }
    }

    @SuppressLint("MissingPermission")
    private fun setupMap() {
        googleMap.uiSettings.isZoomControlsEnabled = true
        val permissionApproved =
            context?.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) ?: return
        if (permissionApproved) {
            googleMap.isMyLocationEnabled = true

            googleMap.uiSettings.isMyLocationButtonEnabled = true
            googleMap.uiSettings.isCompassEnabled = true
        } else {
            (activity as MainActivity).requestFineLocationPermission()
        }
    }

    private fun stopObservers() {
        userMarkersObserver?.let {
            markerViewModel.stopMarkerUpdates()
            markerViewModel.userMarkersLiveData.removeObserver(it)
        }
        staticMarkersObserver?.let {
            markerViewModel.staticMarkersLiveData.removeObserver(it)
        }
    }

    private fun startObservers() {
        userMarkersObserver?.let {
            markerViewModel.startMarkerUpdates()
            markerViewModel.userMarkersLiveData.observe(viewLifecycleOwner, it)
        }
        staticMarkersObserver?.let {
            markerViewModel.staticMarkersLiveData.observe(viewLifecycleOwner, it)
        }
    }

    override fun onPause() {
        super.onPause()
        stopObservers()
    }

    override fun onStart() {
        super.onStart()
        startObservers()
    }

    override fun onDetach() {
        locationUpdateViewModel.stopLocationUpdates()
        super.onDetach()
    }
}
