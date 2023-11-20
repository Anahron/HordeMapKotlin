package ru.newlevel.hordemap.presentation.map

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
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
import androidx.lifecycle.lifecycleScope
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
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.app.MyAlarmReceiver
import ru.newlevel.hordemap.app.hasPermission
import ru.newlevel.hordemap.databinding.FragmentMapsBinding
import ru.newlevel.hordemap.presentation.MainActivity
import ru.newlevel.hordemap.presentation.settings.SettingsFragment
import ru.newlevel.hordemap.presentation.tracks.TrackTransferViewModel
import kotlin.math.roundToInt

class MapFragment : Fragment(R.layout.fragment_maps), OnMapReadyCallback {

    private val tracksTransferViewModel by viewModel<TrackTransferViewModel>()
    private val binding: FragmentMapsBinding by viewBinding()
    private val mapViewModel by viewModel<MapViewModel>()
    private lateinit var googleMap: GoogleMap
    private lateinit var markerManager: MarkerManager
    private lateinit var userMarkerCollection: MarkerManager.Collection
    private lateinit var staticMarkerCollection: MarkerManager.Collection

    private fun init() {
        setupMap()
        menuListenersSetup()
        markerManager = MarkerManager(googleMap)
        userMarkerCollection = markerManager.newCollection()
        staticMarkerCollection = markerManager.newCollection()
        markerStateObserver()
        overlayObserver()
        tracksObserver()
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
        mapViewModel.state.observe(this) { state ->
            when (state) {
                is MapState.LoadingState -> {
                }

                is MapState.DefaultState -> {
                    startMarkerObservers()
                    binding.drawableSettings.closeDrawer(GravityCompat.END)
                    binding.ibMarkers.setBackgroundResource(R.drawable.img_map_show_markers)
                    mapViewModel.userMarkersLiveData.observe(this) {
                        userMarkerCollection.markers.forEach { marker -> marker.remove() }
                        mapViewModel.createUsersMarkers(
                            it, markerCollection = userMarkerCollection
                        )
                    }
                    mapViewModel.staticMarkersLiveData.observe(this) {
                        staticMarkerCollection.markers.forEach { marker -> marker.remove() }
                        mapViewModel.createStaticMarkers(
                            it, markerCollection = staticMarkerCollection
                        )
                    }
                }

                is MapState.MarkersOffState -> {
                    stopMarkerObservers()
                    staticMarkerCollection.hideAll()
                    userMarkerCollection.hideAll()
                    binding.ibMarkers.setBackgroundResource(R.drawable.img_map_hide_markers)
                }

                else -> {}
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun compassObserver() {
        mapViewModel.compassAngle.observe(this) { angle ->
            binding.imgCompass.visibility = View.VISIBLE
            binding.tvCompass.visibility = View.VISIBLE
            binding.imgCompass.rotation = -angle
            binding.tvCompass.text =
                Math.round(if (angle > 0) angle else angle + 360).toString() + "\u00B0 "
        }
    }


    private fun tracksObserver() {
        tracksTransferViewModel.trackToShowOnMap.observe(this) { listLatLng ->
            if (listLatLng.isNotEmpty()) {
                mapViewModel.setRoutePolyline(
                    googleMap.addPolyline(
                        mapViewModel.createRoute(
                            listLatLng
                        )
                    )
                )
                val update = CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        listLatLng[0].latitude,
                        listLatLng[0].longitude
                    ), 15F
                )
                googleMap.moveCamera(update)
                showOrHideTrackBtn(true)
            } else {
                mapViewModel.removeRoute()
                showOrHideTrackBtn(false)
            }
        }
    }

    private fun showOrHideTrackBtn(isNeedToShow: Boolean) {
        val ibTrackHide = binding.ibTrackHide
        if (isNeedToShow) {
            ibTrackHide.translationX = 500f
            val animator = ObjectAnimator.ofFloat(ibTrackHide, "translationX", 0f)
            animator.duration = 500
            animator.start()
        } else {
            ibTrackHide.translationX = 0f
            val animator = ObjectAnimator.ofFloat(ibTrackHide, "translationX", 500f)
            animator.duration = 500
            animator.start()
        }
    }

    private fun overlayObserver() {
        var kmlLayer: KmlLayer? = null
        mapViewModel.isAutoLoadMap.observe(this) {
            if (it) lifecycleScope.launch {
                mapViewModel.loadLastGameMap()
            }
        }
        mapViewModel.kmzUri.observe(this) { kmzUri ->
            if (kmzUri != null) {
                kmlLayer?.removeLayerFromMap()
                lifecycleScope.launch {
                    kmzUri.let { mapViewModel.getInputSteam(kmzUri, requireContext()) }
                        .let { stream ->
                            stream?.let {
                                kmlLayer = KmlLayer(
                                    googleMap,
                                    it,
                                    requireContext(),
                                    markerManager,
                                    null,
                                    null,
                                    null,
                                    null
                                )
                            }

                            kmlLayer?.addLayerToMap()
                            kmlLayer?.groundOverlays?.first()?.let { overlay ->
                                val center = overlay.latLngBox.center
                                val update = CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        center.latitude,
                                        center.longitude
                                    ), 12F
                                )
                                googleMap.animateCamera(update)
                            }
                        }
                }
            } else kmlLayer?.removeLayerFromMap()
        }
    }


    private fun stopMarkerObservers() {
        mapViewModel.stopMarkerUpdates()
    }

    private fun startMarkerObservers() {
        mapViewModel.startMarkerUpdates()
    }

    private fun startBackgroundWork() {
        mapViewModel.startLocationUpdates()
        startAlarmManager()
    }

    private fun buildRoute(destination: LatLng) {
        mapViewModel.setDestination(destination)
        mapViewModel.setRoutePolyline(
            googleMap.addPolyline(
                mapViewModel.createRoute(
                    LatLng(
                        googleMap.myLocation.latitude,
                        googleMap.myLocation.longitude
                    ), destination, requireContext().applicationContext
                )
            )
        )
        mapViewModel.distanceText.observe(viewLifecycleOwner) {
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
        binding.ibMyLocation.setOnClickListener {
            val myLocation = googleMap.myLocation
            val update = CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    myLocation.latitude,
                    myLocation.longitude
                ), 15F
            )
            googleMap.animateCamera(update)
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
                    "Построить маршрут", "Поставить маркер"
                )
            ) { _: DialogInterface?, which: Int ->
                when (which) {
                    0 -> {
                        if (!mapViewModel.isRoutePolylineNotNull())
                            showOrHideTrackBtn(true)
                        buildRoute(latLng)
                    }
                    1 -> {
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
            mapViewModel = mapViewModel
        )
        val settingsFragment = SettingsFragment(mapViewModel = mapViewModel)
        fragmentTrans.add(R.id.fragment_container, settingsFragment)
        fragmentTrans.add(R.id.fragment_container, loadMapFragment)
        fragmentTrans.commit()

        binding.drawableSettings.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        binding.ibMapType.setOnClickListener {
            if (googleMap.mapType == GoogleMap.MAP_TYPE_NORMAL) {
                googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                it.setBackgroundResource(R.drawable.img_btn_map_type_normal)
            } else if (googleMap.mapType == GoogleMap.MAP_TYPE_HYBRID) {
                googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                it.setBackgroundResource(R.drawable.img_btn_map_type_hybrid)
            }
        }
        binding.ibTrackHide.setOnClickListener {
            mapViewModel.removeRoute()
            tracksTransferViewModel.clearTrack()
            showOrHideTrackBtn(false)
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

    @SuppressLint("MissingPermission")
    private fun setupMap() {
        googleMap.uiSettings.isZoomControlsEnabled = true
        if (requireContext().hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) || requireContext().hasPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            googleMap.isMyLocationEnabled = true
            googleMap.uiSettings.isCompassEnabled = true
            googleMap.uiSettings.isMapToolbarEnabled = false
            googleMap.uiSettings.isMyLocationButtonEnabled = false
        } else {
            (activity as MainActivity).requestPermission()
        }
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
    }

    override fun onResume() {
        super.onResume()
        mapViewModel.compassActivate()
    }

    override fun onDetach() {
        mapViewModel.stopLocationUpdates()
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