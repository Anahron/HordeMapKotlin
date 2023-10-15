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
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
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

class MapFragment(private val loginViewModel: LoginViewModel) : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentMapsBinding
    private lateinit var googleMap: GoogleMap
    private var userMarkersObserver: Observer<List<MarkerDataModel>>? = null
    private var staticMarkersObserver: Observer<List<MarkerDataModel>>? = null
    private val mapViewModel by viewModel<MapViewModel>()
    private val receiver = LocationUpdatesBroadcastReceiver()
    private val locationUpdateViewModel by viewModel<LocationUpdateViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapsBinding.inflate(inflater, container, false)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        return binding.root
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

        userMarkersObserver = Observer {
            mapViewModel.createUsersMarkers(it, googleMap)
        }

        staticMarkersObserver = Observer {
            mapViewModel.createStaticMarkers(it, googleMap)
        }
        mapViewModel._isShowMarkers.observe(this) {
            if (it) binding.ibMarkers.setBackgroundResource(R.drawable.img_marker_orc_on)
            else binding.ibMarkers.setBackgroundResource(R.drawable.img_marker_orc_off)
        }
        mapViewModel._isAutoLoadMap.observe(this) {
            if (it)
                lifecycleScope.launch {
                     mapViewModel.loadLastGameMap()
                }
        }
        mapViewModel._kmzUri.observe(this) {
            lifecycleScope.launch {
                val inputStream = it?.let { mapViewModel.getInputSteam(it, requireContext()) }
                if (inputStream != null) {
                    val kmlLayer =
                        KmlLayer(googleMap, inputStream, requireContext().applicationContext)
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
        // настройки карты
        setupMap()
        // слушатели кликов
        mapListenersSetup()
        menuListenersSetup()
        // Запускаем обсерверы
        startObservers()
        //Камера на Красноярск
        val location = LatLng(56.0901, 93.2329) //координаты красноярска
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLng(location))

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
        val intent = Intent(requireContext().applicationContext, MyAlarmReceiver::class.java)
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
            createAlertDialog()
        }
    }

    private fun createAlertDialog() {
        val dialogFragment =
            LoadMapDialogFragment(mapViewModel = mapViewModel, loginViewModel = loginViewModel)
        dialogFragment.show(this.childFragmentManager, "customDialog")
    }

    @SuppressLint("PotentialBehaviorOverride")
    private fun mapListenersSetup() {
        // Скрываем диалог при коротком клике по нему
        googleMap.setOnInfoWindowClickListener { obj: Marker -> obj.hideInfoWindow() }

        //Показываем только текст маркера, без перемещения к нему камеры
        googleMap.setOnMarkerClickListener { marker: Marker ->
            marker.showInfoWindow()
            true
        }

        googleMap.setOnInfoWindowLongClickListener {
            mapViewModel.deleteStaticMarker(it)
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
        val intent = Intent(requireContext().applicationContext, MyAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext().applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )
        (requireContext().applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager?)?.cancel(
            pendingIntent
        )
        super.onDetach()
    }
}

