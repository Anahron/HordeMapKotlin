package ru.newlevel.hordemap.presentatin.fragments

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.collections.MarkerManager
import com.google.maps.android.data.kml.KmlLayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.app.BgLocationWorker
import ru.newlevel.hordemap.app.MyAlarmReceiver
import ru.newlevel.hordemap.app.hasPermission
import ru.newlevel.hordemap.data.storage.models.MarkerDataModel
import ru.newlevel.hordemap.databinding.FragmentMapsBinding
import ru.newlevel.hordemap.presentatin.MainActivity
import ru.newlevel.hordemap.presentatin.viewmodels.LocationUpdateViewModel
import ru.newlevel.hordemap.presentatin.viewmodels.MapState
import ru.newlevel.hordemap.presentatin.viewmodels.MapViewModel
import ru.newlevel.hordemap.presentatin.viewmodels.SettingsViewModel
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class MapFragment(private val settingsViewModel: SettingsViewModel) :
    Fragment(R.layout.fragment_maps), OnMapReadyCallback {

    private val binding: FragmentMapsBinding by viewBinding()
    private val locationUpdateViewModel by viewModel<LocationUpdateViewModel>()
    private val mapViewModel by viewModel<MapViewModel>()
    private lateinit var googleMap: GoogleMap
    private var userMarkersObserver: Observer<List<MarkerDataModel>>? = null
    private var staticMarkersObserver: Observer<List<MarkerDataModel>>? = null
    private lateinit var userMarkerCollection: MarkerManager.Collection
    private lateinit var staticMarkerCollection: MarkerManager.Collection
    private var messengerDialog: MessengerDialogFragment? = null

    private fun init() {
        setupMap()
        menuListenersSetup()
        userMarkerCollection = MarkerManager(googleMap).newCollection()
        staticMarkerCollection = MarkerManager(googleMap).newCollection()
        markerStateObserver()
        overlayObserver()
        compassObserver()
        mapListenersSetup()
        startBackgroundWork()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(gMap: GoogleMap) {
        mapViewModel.turnToDefaultState()
        googleMap = gMap
        val location = LatLng(56.0901, 93.2329) //координаты красноярска
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(location))
        init()
    }

    private fun markerStateObserver() {
        mapViewModel.state.observe(this@MapFragment) { state ->
            when (state) {
                is MapState.LoadingState -> {

                }
                is MapState.DefaultState -> {
                    binding.drawableSettings.closeDrawer(GravityCompat.END)
                    binding.ibMarkers.setBackgroundResource(R.drawable.img_marker_orc_on)
                    userMarkersObserver = Observer {
                        userMarkerCollection.markers.forEach { marker -> marker.remove() }
                        mapViewModel.createUsersMarkers(
                            it, markerCollection = userMarkerCollection
                        )
                    }
                    staticMarkersObserver = Observer {
                        staticMarkerCollection.markers.forEach { marker -> marker.remove() }
                        mapViewModel.createStaticMarkers(
                            it, markerCollection = staticMarkerCollection
                        )
                    }
                    startMarkerObservers()
                }
                is MapState.MarkersOffState -> {
                    stopMarkerObservers()
                    staticMarkerCollection.hideAll()
                    userMarkerCollection.hideAll()
                    binding.ibMarkers.setBackgroundResource(R.drawable.img_marker_orc_off)
                }
                else -> {}
            }
        }
    }

    private fun compassObserver() {
        mapViewModel.compassAngle.observe(this) { angle ->
            binding.imgCompass.visibility = View.VISIBLE
            binding.tvCompass.visibility = View.VISIBLE
            binding.imgCompass.rotation = -angle
            binding.tvCompass.text =
                Math.round(if (angle > 0) angle else angle + 360).toString() + "\u00B0 "
        }
    }

    private fun overlayObserver() {
        var kmlLayer: KmlLayer? = null
        val markerManager = MarkerManager(googleMap)
        mapViewModel.isAutoLoadMap.observe(this) {
            if (it) lifecycleScope.launch {
                mapViewModel.loadLastGameMap()
            }
        }
        mapViewModel.kmzUri.observe(this) {
            if (kmlLayer != null) kmlLayer!!.removeLayerFromMap()
            lifecycleScope.launch {
                val inputStream = it.let { mapViewModel.getInputSteam(it!!, requireContext()) }
                if (inputStream != null) {
                    kmlLayer = KmlLayer(
                        googleMap,
                        inputStream,
                        requireContext(),
                        markerManager,
                        null,
                        null,
                        null,
                        null
                    )
                    kmlLayer!!.addLayerToMap()
                    if (kmlLayer!!.isLayerOnMap && kmlLayer!!.groundOverlays != null) {
                        kmlLayer!!.groundOverlays.any { _it ->
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
    }

    private fun stopMarkerObservers() {
        userMarkersObserver?.let {
            mapViewModel.stopMarkerUpdates()
            mapViewModel.userMarkersLiveData.removeObserver(it)
        }
        staticMarkersObserver?.let {
            mapViewModel.staticMarkersLiveData.removeObserver(it)
        }
    }

    private fun startMarkerObservers() {
        userMarkersObserver?.let {
            mapViewModel.startMarkerUpdates()
            mapViewModel.userMarkersLiveData.observe(viewLifecycleOwner, it)
        }
        staticMarkersObserver?.let {
            mapViewModel.staticMarkersLiveData.observe(viewLifecycleOwner, it)
        }
    }

    private fun startBackgroundWork() {
        locationUpdateViewModel.startLocationUpdates()
        startAlarmManager()
       // buildWorkManager()
    }

    private fun buildRoute(destination: LatLng) {
        mapViewModel.setDestination(destination)
        mapViewModel.setRoutePolyline(googleMap.addPolyline(mapViewModel.createRoute(
            LatLng(
                googleMap.myLocation.latitude,
                googleMap.myLocation.longitude
            ), destination, requireContext().applicationContext
        )))
        mapViewModel.distanceText.observe(viewLifecycleOwner)
        {
            lifecycleScope.launch(Dispatchers.Main) {
                binding.distanceTextView.visibility = View.VISIBLE
                binding.distanceTextView.text = it
            }
        }
    }

    private fun mapListenersSetup() {
        googleMap.setOnMyLocationChangeListener { location ->
            val currentLatLng = LatLng(location.latitude, location.longitude)
            mapViewModel.updateRoute(currentLatLng)
        }
        staticMarkerCollection.setOnInfoWindowClickListener {
            it.hideInfoWindow()
        }
        staticMarkerCollection.setOnMarkerClickListener { marker: Marker ->
            marker.showInfoWindow()
            true
        }
        staticMarkerCollection.setOnInfoWindowLongClickListener {
            mapViewModel.deleteStaticMarker(it)
        }
        googleMap.setOnMapLongClickListener { latLng: LatLng ->
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Выберите действие").setItems(
                arrayOf<CharSequence>(
                    "Построить маршрут", "Очистить маршрут", "Поставить маркер"
                )
            ) { _: DialogInterface?, which: Int ->
                when (which) {
                    0 -> {
                        buildRoute(latLng)
                    }
                    1 -> {
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
        binding.imgCompass.setOnClickListener {
            binding.imgCompass.layoutParams.height =
                if (binding.imgCompass.layoutParams.width != convertDpToPx(50)) convertDpToPx(50) else convertDpToPx(
                    250
                )
            binding.imgCompass.layoutParams.width =
                if (binding.imgCompass.layoutParams.width != convertDpToPx(50)) convertDpToPx(50) else convertDpToPx(
                    250
                )
            binding.imgCompass.requestLayout()
        }
    }

    private fun menuListenersSetup() {
        val fragmentTrans = childFragmentManager.beginTransaction()
        val loadMapFragment = LoadMapDialogFragment(
            mapViewModel = mapViewModel, settingsViewModel = settingsViewModel
        )
        val settingsFragment =
            SettingsFragment(mapViewModel = mapViewModel, settingsViewModel = settingsViewModel)
        fragmentTrans.add(R.id.fragment_container, settingsFragment)
        fragmentTrans.add(R.id.fragment_container, loadMapFragment)
        fragmentTrans.commit()

        binding.drawableSettings.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
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
            val fragmentTransaction = childFragmentManager.beginTransaction()
            loadMapFragment.let {
                fragmentTransaction.hide(it)
                fragmentTransaction.show(settingsFragment)
                fragmentTransaction.commit()
            }
            binding.drawableSettings.openDrawer(GravityCompat.END)
        }
        binding.ibLoadMap.setOnClickListener {
            val fragmentTransaction = childFragmentManager.beginTransaction()
            settingsFragment.let {
                fragmentTransaction.hide(it)
                fragmentTransaction.show(loadMapFragment)
                fragmentTransaction.commit()
            }
            binding.drawableSettings.openDrawer(GravityCompat.END)
        }
        binding.ibMessenger.setOnClickListener {
            if (messengerDialog == null) {
                messengerDialog = MessengerDialogFragment()
            }

            if (!messengerDialog!!.isAdded) {
                messengerDialog!!.show(this.childFragmentManager, "messengerDialog")
            }
        }
    }

    private fun createStaticMarkerDialog(latLng: LatLng) {
        val dialogFragment = OnMapClickInfoDialog(object : OnMapClickInfoDialogResult {
            override fun onMapClickInfoDialogResult(
                description: String, checkedRadioButton: Int
            ) {
                mapViewModel.sendMarker(latLng, description, checkedRadioButton)
            }
        })
        dialogFragment.show(this.childFragmentManager, "customDialog")
    }

    private fun setupMap() {
        googleMap.uiSettings.isZoomControlsEnabled = true
        if (requireContext().hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) || requireContext().hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            googleMap.isMyLocationEnabled = true
            googleMap.uiSettings.isCompassEnabled = true
            googleMap.uiSettings.isMapToolbarEnabled = false
        } else {
            (activity as MainActivity).requestPermission()
        }
    }

    private fun buildWorkManager() {
        val workManager = WorkManager.getInstance(requireContext().applicationContext)
        workManager.enqueueUniquePeriodicWork(
            BgLocationWorker.workName,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<BgLocationWorker>(
                5,
                TimeUnit.MINUTES,
            ).build(),
        )
    }

    private fun startAlarmManager() {
        val intent = Intent(requireContext().applicationContext, MyAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext().applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )
        (requireContext().applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager?)?.setExactAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + 600000,
            pendingIntent
        )
    }

    override fun onPause() {
        super.onPause()
        mapViewModel.compassDeActivate()
        stopMarkerObservers()
    }

    override fun onResume() {
        super.onResume()
        mapViewModel.compassActivate()
        startMarkerObservers()
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

    private fun convertDpToPx(dp: Int): Int {
        val density: Float = resources.displayMetrics.density
        return (dp.toFloat() * density).roundToInt()
    }
}

