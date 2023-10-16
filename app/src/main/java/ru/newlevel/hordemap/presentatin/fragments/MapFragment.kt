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
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.collections.MarkerManager
import com.google.maps.android.data.kml.KmlLayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.app.LocationUpdatesBroadcastReceiver
import ru.newlevel.hordemap.app.MyAlarmReceiver
import ru.newlevel.hordemap.app.hasPermission
import ru.newlevel.hordemap.data.storage.models.MarkerDataModel
import ru.newlevel.hordemap.databinding.FragmentMapsBinding
import ru.newlevel.hordemap.presentatin.MainActivity
import ru.newlevel.hordemap.presentatin.viewmodels.LocationUpdateViewModel
import ru.newlevel.hordemap.presentatin.viewmodels.LoginViewModel
import ru.newlevel.hordemap.presentatin.viewmodels.MapViewModel
import ru.newlevel.hordemap.presentatin.viewmodels.REQUEST_CODE_PICK_KMZ_FILE
import java.util.*


class MapFragment(private val loginViewModel: LoginViewModel) : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentMapsBinding
    private lateinit var googleMap: GoogleMap
    private var userMarkersObserver: Observer<List<MarkerDataModel>>? = null
    private var staticMarkersObserver: Observer<List<MarkerDataModel>>? = null
    private val mapViewModel by viewModel<MapViewModel>()
    private val receiver = LocationUpdatesBroadcastReceiver()
    private val locationUpdateViewModel by viewModel<LocationUpdateViewModel>()
    private lateinit var markerCollection: MarkerManager.Collection

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapsBinding.inflate(inflater, container, false)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        return binding.root
    }

    override fun onMapReady(gMap: GoogleMap) {
        googleMap = gMap
        val markerManager = MarkerManager(gMap)
        markerCollection = MarkerManager(gMap).newCollection()

        userMarkersObserver = Observer {
            mapViewModel.createUsersMarkers(it, markerCollection)
        }
        staticMarkersObserver = Observer {
            mapViewModel.createStaticMarkers(it, markerCollection)
        }
        mapViewModel.isShowMarkers.observe(this) {
            if (it) binding.ibMarkers.setBackgroundResource(R.drawable.img_marker_orc_on)
            else binding.ibMarkers.setBackgroundResource(R.drawable.img_marker_orc_off)
        }
        mapViewModel.isAutoLoadMap.observe(this) {
            if (it)
                lifecycleScope.launch {
                    mapViewModel.loadLastGameMap()
                }
        }
        mapViewModel.kmzUri.observe(this) {
            lifecycleScope.launch {
                val inputStream = it?.let { mapViewModel.getInputSteam(it, requireContext()) }
                if (inputStream != null) {
                    val kmlLayer = KmlLayer(googleMap, inputStream, requireContext(), markerManager, null, null, null, null)
                    kmlLayer.addLayerToMap()
                    if (kmlLayer.isLayerOnMap && kmlLayer.groundOverlays != null) {
                        kmlLayer.groundOverlays.any { _it ->
                            googleMap.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        _it.latLngBox.center.latitude,
                                        _it.latLngBox.center.longitude
                                    ), 12F
                                )
                            )
                            true
                        }
                    }
                    withContext(Dispatchers.IO) {
                        inputStream.close()
                    }
                }
            }
        }

        setupMap()
        mapListenersSetup()
        menuListenersSetup()
        startObservers()
        //Камера на Красноярск
        val location = LatLng(56.0901, 93.2329) //координаты красноярска
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(location))

        val filter = IntentFilter(LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES)
        requireContext().applicationContext.registerReceiver(receiver, filter)
        //запуск обновления местоположений
        locationUpdateViewModel.startLocationUpdates()
        startAlarmManager()
//        val workManager = WorkManager.getInstance(requireContext().applicationContext)
//        workManager.enqueueUniquePeriodicWork(
//            BgLocationWorker.workName,
//            ExistingPeriodicWorkPolicy.KEEP,
//            PeriodicWorkRequestBuilder<BgLocationWorker>(
//                1,
//                TimeUnit.MINUTES,
//            ).build(),)
    }

    @SuppressLint("SetTextI18n")
    private fun buildRoute(destination: LatLng) {
        mapViewModel.getRoutePolyline()?.remove()
        mapViewModel.removeRoute()
        mapViewModel.setDestination(destination)
        mapViewModel.getDestination()?.let {
            mapViewModel.createRoute(
                LatLng(
                    googleMap.myLocation.latitude,
                    googleMap.myLocation.longitude
                ), it, requireContext().applicationContext
            )
        }?.let {
            googleMap.addPolyline(
                it
            )
        }?.let {
            mapViewModel.setRoutePolyline(
                it
            )
        }
        mapViewModel.distanceText.observe(viewLifecycleOwner) {
            binding.distanceTextView.visibility = View.VISIBLE
            binding.distanceTextView.text = it
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_KMZ_FILE && data != null) {
            val uri = data.data
            if (uri != null) {
                lifecycleScope.launch {
                    mapViewModel.saveGameMapToFile(uri)
                    mapViewModel.setUriForMap(uri)
                }
            }
        }
    }

    private fun startAlarmManager() {
        val intent =
            Intent(requireContext().applicationContext, MyAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext().applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )
        (requireContext().applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager?)?.setExactAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + 1000,
            pendingIntent
        )
    }

    private fun menuListenersSetup() {
        binding.ibMapType.setOnClickListener {
            if (googleMap.mapType == GoogleMap.MAP_TYPE_NORMAL) {
                googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                it.setBackgroundResource(R.drawable.map_type_normal)
            } else if (googleMap.mapType == GoogleMap.MAP_TYPE_HYBRID) {
                googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                it.setBackgroundResource(R.drawable.map_type_hybrid)
            }
        }
        binding.ibMarkers.setOnClickListener {
            mapViewModel.showOrHideMarkers()
        }
        binding.ibSettings.setOnClickListener {
            val loginFragment = LoginFragment(loginViewModel)
            this.fragmentManager?.beginTransaction()?.replace(R.id.container, loginFragment)
                ?.commit()
        }
        binding.ibLoadMap.setOnClickListener {
            createMapLoadDialog()
        }
    }

    private fun createStaticMarkerDialog(latLng: LatLng) {
        val dialogFragment =
            OnMapClickInfoDialog(
                mapViewModel = mapViewModel,
                latLng
            )
        dialogFragment.show(this.childFragmentManager, "customDialog")
    }

    private fun createMapLoadDialog() {
        val dialogFragment = LoadMapDialogFragment(
            mapViewModel = mapViewModel,
            loginViewModel = loginViewModel,
            this
        )
        dialogFragment.show(this.childFragmentManager, "customDialog")
    }

    @SuppressLint("PotentialBehaviorOverride")
    private fun mapListenersSetup() {
        googleMap.setOnMyLocationChangeListener { location ->
            val currentLatLng = LatLng(location.latitude, location.longitude)
            val destination = mapViewModel.getDestination()
            if (mapViewModel.getRoutePolyline() != null && destination != null) {
                mapViewModel.updateRoute(currentLatLng, destination)
                mapViewModel.getRoutePolyline()?.points = listOf(currentLatLng, destination)
            }
        }
        markerCollection.setOnInfoWindowClickListener {
            it.hideInfoWindow()
        }
        markerCollection.setOnMarkerClickListener { marker: Marker ->
            marker.showInfoWindow()
            true
        }
        markerCollection.setOnInfoWindowLongClickListener {
            mapViewModel.deleteStaticMarker(it)
        }
        googleMap.setOnMapLongClickListener { latLng: LatLng ->
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Выберите действие").setItems(
                arrayOf<CharSequence>(
                    "Построить маршрут",
                    "Очистить маршрут",
                    "Поставить маркер"
                )
            ) { _: DialogInterface?, which: Int ->
                when (which) {
                    0 -> {
                        buildRoute(latLng)
                    }
                    1 -> {
                        mapViewModel.getRoutePolyline()?.remove()
                        mapViewModel.removeRoute()
                        binding.distanceTextView.visibility = View.GONE
                    }
                    2 -> {
                        createStaticMarkerDialog(latLng)
                    }
                }
            }
            builder.show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun setupMap() {
        googleMap.uiSettings.isZoomControlsEnabled = true
        val permissionApproved =
            context?.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) ?: return
        if (permissionApproved) {
            googleMap.isMyLocationEnabled = true
            googleMap.uiSettings.isCompassEnabled = true
            googleMap.uiSettings.isMapToolbarEnabled = false
        } else {
            (activity as MainActivity).requestFineLocationPermission()
        }
    }

    private fun stopObservers() {
        userMarkersObserver?.let {
            mapViewModel.stopMarkerUpdates()
            mapViewModel.userMarkersLiveData.removeObserver(it)
        }
        staticMarkersObserver?.let {
            mapViewModel.staticMarkersLiveData.removeObserver(it)
        }
    }

    private fun startObservers() {
        userMarkersObserver?.let {
            mapViewModel.startMarkerUpdates()
            mapViewModel.userMarkersLiveData.observe(viewLifecycleOwner, it)
        }
        staticMarkersObserver?.let {
            mapViewModel.staticMarkersLiveData.observe(viewLifecycleOwner, it)
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
        val intent =
            Intent(requireContext().applicationContext, MyAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext().applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )
        (requireContext().applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager?)?.cancel(
            pendingIntent
        )
        super.onDetach()
    }
}

