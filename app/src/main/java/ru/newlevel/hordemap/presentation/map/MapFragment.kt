package ru.newlevel.hordemap.presentation.map

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.button.MaterialButton
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.app.MyAlarmReceiver
import ru.newlevel.hordemap.app.REQUEST_CODE_POST_NOTIFICATION
import ru.newlevel.hordemap.app.TAG
import ru.newlevel.hordemap.app.hasPermission
import ru.newlevel.hordemap.app.hideToRight
import ru.newlevel.hordemap.app.showAtRight
import ru.newlevel.hordemap.databinding.FragmentMapsBinding
import ru.newlevel.hordemap.presentation.MainActivity
import ru.newlevel.hordemap.presentation.map.utils.MapInteractionHandler
import ru.newlevel.hordemap.presentation.map.utils.MapOverlayManager
import ru.newlevel.hordemap.presentation.settings.SettingsFragment
import ru.newlevel.hordemap.presentation.tracks.TrackTransferViewModel
import java.util.Date
import kotlin.math.roundToInt

@Suppress("DEPRECATION")
class MapFragment : Fragment(R.layout.fragment_maps), OnMapReadyCallback, SettingsFragment.OnChangeSettings {

    private val mapViewModel by viewModel<MapViewModel>()
    private val tracksTransferViewModel by viewModel<TrackTransferViewModel>()
    private val binding: FragmentMapsBinding by viewBinding()
    private val mapOverlayManager: MapOverlayManager by lazy { MapOverlayManager(googleMap) }
    private lateinit var googleMap: GoogleMap
    private var pendingIntent: PendingIntent? = null
    private var angle = 0f
    private var isCompassActive = false
    private var isUserMoveCamera = true
    private val mapInteractionHandler = MapInteractionHandler {
        rotateCamera()
    }

    private fun init() {
        setupMap()
        mapButtonsListenersSetup()
        markersObservers()
        overlayObserver()
        tracksObserver()
        compassObserver()
        mapListenersSetup()
        startBackgroundWork()
    }

    private fun rotateCamera() {
        try {
            if (!isCompassActive)
                this.angle = googleMap.cameraPosition.bearing
            val cameraPosition = CameraPosition.builder()
                .target(LatLng(googleMap.myLocation.latitude, googleMap.myLocation.longitude)) // Центр карты
                .zoom(googleMap.cameraPosition.zoom) // Уровень масштабирования остается неизменным
                .bearing(this.angle) // Угол поворота карты
                .tilt(googleMap.cameraPosition.tilt) // Угол наклона карты остается неизменным
                .build()
            isUserMoveCamera = false
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 100,
                object : GoogleMap.CancelableCallback {
                    override fun onCancel() {
                        isUserMoveCamera = true
                    }

                    override fun onFinish() {
                        isUserMoveCamera = true
                    }

                })
            isUserMoveCamera = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPause() {
        super.onPause()
        mapInteractionHandler.onPause()
    }

    override fun onResume() {
        super.onResume()
        mapInteractionHandler.onResume()
    }

    private fun requestNotificationPermission() {
        if (!requireContext().hasPermission(Manifest.permission.POST_NOTIFICATIONS) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE_POST_NOTIFICATION
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this@MapFragment)
        requestNotificationPermission()
    }

    override fun onMapReady(gMap: GoogleMap) {
        Log.e(TAG, "onMapReady")
        googleMap = gMap
        val location = LatLng(56.0901, 93.2329) //координаты красноярска
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(location))
        lifecycleScope.launch {
            if (this@MapFragment.isAdded) {
                init()
            }
        }
    }

    private fun markersObservers() {
        val lifecycle = viewLifecycleOwner.lifecycle
        lifecycle.coroutineScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mapViewModel.userMarkersFlow.collectLatest { data ->
                    mapOverlayManager.createUsersMarkers(
                        data = data, context = requireContext()
                    )
                    Log.e(TAG, "userMarkersLiveData.collect UserMarkerData $data")
                }
            }
        }
        lifecycle.coroutineScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mapViewModel.staticMarkersFlow.collectLatest { data ->
                    mapOverlayManager.createStaticMarkers(
                        data = data, context = requireContext()
                    )
                    Log.e(TAG, "staticMarkersLiveData.collect StaticMarkersData $data")
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun compassObserver() {
        val lifecycle = viewLifecycleOwner.lifecycle
        lifecycle.coroutineScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mapViewModel.compassAngleFlow.collectLatest { angle ->
                    if (!isCompassActive)
                        binding.imgCompassBackground.visibility = View.VISIBLE
                    isCompassActive = true
                    this@MapFragment.angle = angle
                    binding.imgCompass.visibility = View.VISIBLE
                    binding.tvCompass.visibility = View.VISIBLE
                    binding.imgCompass.rotation = -angle
                    binding.tvCompass.text = (if (angle > 0) angle else angle + 360).roundToInt().toString() + "\u00B0 "
                }
            }
        }
    }

    private fun tracksObserver() {
        tracksTransferViewModel.trackToShowOnMap.observe(viewLifecycleOwner) { listLatLng ->
            if (listLatLng.isNotEmpty()) {
                mapOverlayManager.addPolyline(PolyUtil.simplify(listLatLng, 22.0))
                cameraUpdate(
                    listLatLng[0].latitude, listLatLng[0].longitude
                )
                showOrHideTrackBtn(true)
            } else {
                mapOverlayManager.removeRoute()
                showOrHideTrackBtn(false)
            }
        }
    }

    private fun showOrHideTrackBtn(isNeedToShow: Boolean) {
        val ibTrackHide = binding.ibTrackHide
        if (isNeedToShow) {
            ibTrackHide.translationX = 500f
            ibTrackHide.showAtRight(500f)
        } else {
            ibTrackHide.translationX = 0f
            ibTrackHide.hideToRight(500f)
        }
    }

    private fun overlayObserver() {
        val lifecycle = viewLifecycleOwner.lifecycle
        lifecycle.coroutineScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mapOverlayManager.distanceText.collect {
                    if (it.isNotEmpty()) {
                        binding.distanceTextView.visibility = View.VISIBLE
                        binding.distanceTextView.text = it
                    } else {
                        binding.distanceTextView.visibility = View.GONE
                    }
                }
            }
        }
        mapViewModel.isAutoLoadMap.observe(viewLifecycleOwner) {
            if (it) onLoadLastGameMapClick()
        }
        mapViewModel.mapOverlayUri.observe(viewLifecycleOwner) { overlayUri ->
            mapOverlayManager.removeOverlays()
            overlayUri?.let { uri ->
                lifecycleScope.launch {
                    mapOverlayManager.createOverlay(uri, requireContext(), googleMap).onSuccess { latLng ->
                        cameraUpdate(latLng.latitude, latLng.longitude)
                    }.onFailure { e ->
                        e.message?.let { makeLongText(it) }
                    }
                }
            }
        }
    }

    private fun cameraUpdate(latitude: Double, longitude: Double) {
        val update = CameraUpdateFactory.newLatLngZoom(
            LatLng(
                latitude, longitude
            ), 14F
        )
        googleMap.animateCamera(update)
    }

    private fun startBackgroundWork() {
        mapViewModel.startLocationUpdates()
        startAlarmManager()
    }

    private fun buildRoute(destination: LatLng) {
        try {
            mapOverlayManager.createRoute(
                LatLng(googleMap.myLocation.latitude, googleMap.myLocation.longitude),
                destination,
                requireContext().applicationContext
            )
        } catch (e: Exception) {
            makeLongText(getString(R.string.no_gps_connection))
        }
    }

    private fun mapListenersSetup() {
        googleMap.setOnMapClickListener {
            mapInteractionHandler.onCameraMove(true)
        }
        googleMap.setOnCameraMoveStartedListener {
            Log.e(TAG, "isUserMoveCamera = $isUserMoveCamera")
            mapInteractionHandler.onCameraMove(isUserMoveCamera)
        }
        googleMap.setOnCameraIdleListener {
            mapInteractionHandler.onCameraIdle()
        }
        googleMap.setOnMyLocationChangeListener { location ->
            if (mapOverlayManager.isRoutePolylineNotNull()) {
                val currentLatLng = LatLng(location.latitude, location.longitude)
                mapOverlayManager.updateRoute(currentLatLng)
            }
        }
        googleMap.setOnMapLongClickListener { latLng ->
            mapInteractionHandler.onCameraMove(true)
            onMapLongClickMenu(latLng)
        }

    }

    private fun onMapLongClickMenu(latLng: LatLng) {
        // location -> pixels window for popup
        val projection = googleMap.projection
        val point = projection.toScreenLocation(latLng)
        val mainPopupMenu = PopupWindow(requireContext())
        mainPopupMenu.contentView = layoutInflater.inflate(
            R.layout.popup_map_long_click, binding.root as ViewGroup, false
        )
        mainPopupMenu.setBackgroundDrawable(
            ContextCompat.getDrawable(
                requireContext(), R.drawable.round_white
            )
        )
        mainPopupMenu.elevation = 18f
        mainPopupMenu.isFocusable = true
        mainPopupMenu.contentView?.findViewById<MaterialButton>(R.id.btnMapPutMarker)?.setOnClickListener {
            mainPopupMenu.dismiss()
            createStaticMarkerDialog(latLng)
        }
        mainPopupMenu.contentView?.findViewById<MaterialButton>(R.id.btnMapShowDistance)?.setOnClickListener {
            mainPopupMenu.dismiss()
            if (!mapOverlayManager.isRoutePolylineNotNull()) showOrHideTrackBtn(true)
            buildRoute(latLng)
        }
        mainPopupMenu.showAtLocation(
            binding.root, Gravity.NO_GRAVITY, point.x, point.y
        )
    }

    private fun mapButtonsListenersSetup() {
        binding.ibMyLocation.setOnClickListener {
            mapInteractionHandler.onCameraMove(true)
            try {
                val myLocation = googleMap.myLocation
                cameraUpdate(
                    myLocation.latitude, myLocation.longitude
                )
            } catch (e: Exception) {
                makeLongText(getString(R.string.no_gps_connection))
            }
        }
        binding.imgCompass.setOnClickListener {
            val newSize =
                if (binding.imgCompass.layoutParams.width != convertDpToPx(45)) {
                    binding.imgCompassBackground.visibility = View.VISIBLE
                    convertDpToPx(45)
                } else {
                    binding.imgCompassBackground.visibility = View.GONE
                    convertDpToPx(250)
                }
            binding.imgCompass.layoutParams.height = newSize
            binding.imgCompass.layoutParams.width = newSize
            binding.imgCompass.requestLayout()
        }
        binding.imgMapRotate.setOnClickListener {
            if (mapInteractionHandler.getIsStopped()) {
                binding.imgMapRotate.setBackgroundResource(R.drawable.img_map_rotate_on)
                mapInteractionHandler.start()
            } else {
                binding.imgMapRotate.setBackgroundResource(R.drawable.img_map_rotate_off)
                mapInteractionHandler.stop()
            }
        }
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
            mapOverlayManager.removeRoute()
            tracksTransferViewModel.clearTrack()
            showOrHideTrackBtn(false)
        }

        binding.ibMarkers.setOnClickListener {
            if (mapOverlayManager.isMarkersVisible) {
                mapOverlayManager.hideAllMarkers()
                binding.ibMarkers.setBackgroundResource(R.drawable.img_map_hide_markers)
                mapOverlayManager.isMarkersVisible = false
            } else {
                mapOverlayManager.showAllMarkers()
                binding.ibMarkers.setBackgroundResource(R.drawable.img_map_show_markers)
                mapOverlayManager.isMarkersVisible = true
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
            (activity as MainActivity).goToRequestsPermissions()
        }
    }

    private fun startAlarmManager() {
        Log.e("AAA", "startAlarmManager at " + Date(System.currentTimeMillis()))
        val intent = Intent(requireContext().applicationContext, MyAlarmReceiver::class.java)
        pendingIntent = PendingIntent.getBroadcast(
            requireContext().applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            (requireContext().applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager?)?.setExactAndAllowWhileIdle(
                AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 240000, it
            )
        }
    }

    override fun onDetach() {
        mapViewModel.stopLocationUpdates()
        pendingIntent?.let {
            (requireContext().applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager?)?.cancel(it)
        }
        super.onDetach()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapInteractionHandler.onDestroy()
    }

    private fun convertDpToPx(dp: Int): Int {
        val density: Float = resources.displayMetrics.density
        return (dp.toFloat() * density).roundToInt()
    }

    override fun onChangeMarkerSettings() {
        mapOverlayManager.removeMarkers()
        lifecycleScope.launch {
            mapOverlayManager.createStaticMarkers(
                data = mapViewModel.staticMarkersFlow.first(), context = requireContext()
            )
        }
        lifecycleScope.launch {
            mapOverlayManager.createUsersMarkers(
                data = mapViewModel.userMarkersFlow.first(), context = requireContext()
            )
        }
    }

    override fun onLoadLastGameMapClick() {
        lifecycleScope.launch {
            mapViewModel.loadLastGameMap(requireContext())?.let {
                it.message?.let { it1 -> makeLongText(it1) }
            }
        }
    }

    override fun onLoadMapFromServerClick() {
        makeLongText(requireContext().getString(R.string.load_map_started))
        lifecycleScope.launch {
            mapViewModel.loadMapFromServer(requireContext().applicationContext)?.let {
                it.message?.let { it1 -> makeLongText(it1) }
            }
        }
    }

    override fun onSelectFileClick(uri: Uri) {
        lifecycleScope.launch {
            mapViewModel.saveGameMapToFile(uri, requireContext())?.let {
                it.message?.let { it1 -> makeLongText(it1) }
            }
        }
    }

    override fun onAutoLoadClick(isAutoLoad: Boolean) {
        mapViewModel.setIsAutoLoadMap(isAutoLoad)
    }

    override fun onClearMapClick() {
        mapOverlayManager.removeOverlays()
        mapViewModel.cleanUriForMap()
    }


    private fun makeLongText(text: String) {
        Toast.makeText(
            requireContext().applicationContext, text, Toast.LENGTH_LONG
        ).show()
    }

}