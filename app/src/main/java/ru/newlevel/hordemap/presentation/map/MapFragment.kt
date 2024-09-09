package ru.newlevel.hordemap.presentation.map

import android.Manifest
import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.os.Bundle
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
import com.google.android.gms.maps.model.Marker
import com.google.android.material.button.MaterialButton
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.app.MyAlarmManager
import ru.newlevel.hordemap.app.REQUEST_CODE_POST_NOTIFICATION
import ru.newlevel.hordemap.app.TAG
import ru.newlevel.hordemap.app.convertDpToPx
import ru.newlevel.hordemap.app.getLatLng
import ru.newlevel.hordemap.app.hasPermission
import ru.newlevel.hordemap.app.hideToRight
import ru.newlevel.hordemap.app.showAtRight
import ru.newlevel.hordemap.app.toDistanceText
import ru.newlevel.hordemap.databinding.FragmentMapsBinding
import ru.newlevel.hordemap.presentation.MainActivity
import ru.newlevel.hordemap.presentation.map.utils.MapInteractionHandler
import ru.newlevel.hordemap.presentation.map.utils.MapOverlayManager
import ru.newlevel.hordemap.presentation.settings.SettingsFragment
import ru.newlevel.hordemap.presentation.tracks.TrackTransferViewModel
import kotlin.math.roundToInt

@Suppress("DEPRECATION")
class MapFragment : Fragment(R.layout.fragment_maps), OnMapReadyCallback,
    SettingsFragment.OnChangeSettings {

    private val mapViewModel by viewModel<MapViewModel>()
    private val tracksTransferViewModel by viewModel<TrackTransferViewModel>()
    private val binding: FragmentMapsBinding by viewBinding()
    private val myAlarmManager by inject<MyAlarmManager>()
    private val mapOverlayManager: MapOverlayManager by lazy { MapOverlayManager(googleMap) }
    private lateinit var googleMap: GoogleMap
    private var compassAngle = 0f
    private var isCompassActive = false
    private var isUserMoveCamera = true
    private var mapFragment: SupportMapFragment? = null
    private var jobUsersMarkerUpdate: Job? = null
    private var jobStaticMarkerUpdate: Job? = null
    private val mapInteractionHandler = MapInteractionHandler {
        rotateCamera()
    }

    private fun init() {
        setupGoogleMapUi()
        mapButtonsListenersSetup()
        markersObservers()
        overlayObserver()
        tracksObserver()
        compassObserver()
        mapListenersSetup()
        startBackgroundWork()
        requestNotificationPermission()
    }

    private fun rotateCamera() {
        try {
            if (!isCompassActive)
                this.compassAngle = googleMap.cameraPosition.bearing
            val cameraPosition = CameraPosition.builder()
                .target(googleMap.myLocation.getLatLng())
                .zoom(googleMap.cameraPosition.zoom)
                .bearing(this.compassAngle)
                .tilt(googleMap.cameraPosition.tilt)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.e(TAG, "MapFragment onViewCreated $this")
        super.onViewCreated(view, savedInstanceState)
        if (mapFragment == null) {
            mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            mapFragment?.getMapAsync(this@MapFragment)
        }
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
        if (jobUsersMarkerUpdate?.isActive == true) {
            jobUsersMarkerUpdate?.cancel()
        }
        if (jobStaticMarkerUpdate?.isActive == true) {
            jobStaticMarkerUpdate?.cancel()
        }
        val lifecycle = viewLifecycleOwner.lifecycle
        jobUsersMarkerUpdate = lifecycle.coroutineScope.launch {
            launch {
                lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    mapViewModel.userMarkersFlow.collectLatest { data ->
                        mapViewModel.insertUserMarkersToLocalDB(data)
                    }
                }
            }
            launch {
                lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    mapViewModel.userMarkersFlowLocal.collectLatest { data ->
                        mapOverlayManager.createUsersMarkers(
                            data = data,
                            context = requireContext()
                        )
                    }
                }
            }
        }
        jobStaticMarkerUpdate = lifecycle.coroutineScope.launch {
            launch {
                lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    mapViewModel.staticMarkersFlow.collectLatest { data ->
                        Log.e(TAG, "ИЗ ФАЕРБЕЙЗА staticMarkersFlow.collectLatest  " + data)
                        mapViewModel.insertStaticMarkersToLocalDB(data)
                    }
                }
            }
            launch {
                lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    mapViewModel.staticMarkersFlowLocal.collectLatest { data ->
                        Log.e(TAG, "ЛОКАЛЬНО staticMarkersFlowLocal  " + data)
                        mapOverlayManager.createStaticMarkers(
                            data = data,
                            context = requireContext()
                        )
                    }
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
                    if (!isCompassActive) {
                        setupCompassUi()
                    }
                    compassAngle = angle
                    binding.imgCompass.rotation = -angle
                    binding.tvCompass.text =
                        (if (angle > 0) angle else angle + 360).roundToInt().toString() + "\u00B0 "
                }
            }
        }
    }

    private fun setupCompassUi() {
        isCompassActive = true
        binding.imgCompassBackground.visibility = View.VISIBLE
        binding.imgCompass.visibility = View.VISIBLE
        binding.tvCompass.visibility = View.VISIBLE
    }

    private fun tracksObserver() {
        tracksTransferViewModel.trackToShowOnMap.observe(viewLifecycleOwner) { listLatLng ->
            if (listLatLng.isNotEmpty()) {
                mapOverlayManager.addPolyline(PolyUtil.simplify(listLatLng, 22.0))
                cameraUpdate(listLatLng[0])
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
        // дистанция и угол до точки
        lifecycle.coroutineScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mapOverlayManager.distanceText.collect {
                    if (it.first != 0.0) {
                        binding.distanceTextView.visibility = View.VISIBLE
                        binding.distanceTextView.text = it.first.toDistanceText()
                        binding.bearingTextView.visibility = View.VISIBLE
                        binding.bearingTextView.text = it.second
                    } else {
                        binding.distanceTextView.visibility = View.GONE
                        binding.bearingTextView.visibility = View.GONE
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
                    mapOverlayManager.createOverlay(uri, requireContext(), googleMap).onSuccess {
                        cameraUpdate(it)
                    }.onFailure { e ->
                        e.message?.let { makeLongText(it) }
                    }
                }
            }
        }
    }

    private fun cameraUpdate(latLng: LatLng, zoom: Float = 14F) {
        val update = CameraUpdateFactory.newLatLngZoom(latLng, zoom)
        googleMap.animateCamera(update)
    }

    private fun startBackgroundWork() {
        mapViewModel.startLocationUpdates()
        myAlarmManager.startAlarmManager()
    }

    private fun buildRoute(destination: LatLng) {
        try {
            mapOverlayManager.createRoute(
                googleMap.myLocation,
                destination,
                requireContext()
            )
        } catch (e: Exception) {
            makeLongText(getString(R.string.no_gps_connection))
        }
    }

    private fun mapListenersSetup() {
        mapOverlayManager.setOnInfoWindowLongClickListener {
            onMarkerLongClickMenu(it)
        }
        googleMap.setOnMapClickListener {
            mapInteractionHandler.onCameraMove(true)
        }
        googleMap.setOnCameraMoveStartedListener {
            mapInteractionHandler.onCameraMove(isUserMoveCamera)
        }
        googleMap.setOnCameraIdleListener {
            mapInteractionHandler.onCameraIdle()
        }
        googleMap.setOnMyLocationChangeListener { location ->
            if (mapOverlayManager.isRoutePolylineNotNull()) {
                mapOverlayManager.updateRoute(location)
            }
        }
        googleMap.setOnMapLongClickListener { latLng ->
            mapInteractionHandler.onCameraMove(true)
            onMapLongClickMenu(latLng)
        }

    }

    private fun onMarkerLongClickMenu(marker: Marker) {
        // location -> pixels window for popup
        val projection = googleMap.projection
        val point = projection.toScreenLocation(marker.position)
        val markerPopupMenu = PopupWindow(requireContext())
        markerPopupMenu.contentView = layoutInflater.inflate(
            R.layout.popup_marker_long_click, binding.root as ViewGroup, false
        )
        markerPopupMenu.setBackgroundDrawable(
            ContextCompat.getDrawable(
                requireContext(), R.drawable.round_white
            )
        )
        markerPopupMenu.elevation = 18f
        markerPopupMenu.isFocusable = true
        markerPopupMenu.contentView?.findViewById<MaterialButton>(R.id.btnDeleteMarker)
            ?.setOnClickListener {
                markerPopupMenu.dismiss()
                lifecycleScope.launch {
                    mapOverlayManager.deleteMarker(marker)
                }
            }
        markerPopupMenu.contentView?.findViewById<MaterialButton>(R.id.btnMarkerShowDistance)
            ?.setOnClickListener {
                markerPopupMenu.dismiss()
                if (!mapOverlayManager.isRoutePolylineNotNull()) showOrHideTrackBtn(true)
                buildRoute(marker.position)
            }
        markerPopupMenu.showAtLocation(
            binding.root, Gravity.NO_GRAVITY, point.x, point.y
        )
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
        mainPopupMenu.contentView?.findViewById<MaterialButton>(R.id.btnMapPutMarker)
            ?.setOnClickListener {
                mainPopupMenu.dismiss()
                createStaticMarkerDialog(latLng)
            }
        mainPopupMenu.contentView?.findViewById<MaterialButton>(R.id.btnMapShowDistance)
            ?.setOnClickListener {
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
                cameraUpdate(googleMap.myLocation.getLatLng(), 16f)
            } catch (e: Exception) {
                makeLongText(getString(R.string.no_gps_connection))
            }
        }
        binding.imgCompass.setOnClickListener {
            val newSize =
                if (binding.imgCompass.layoutParams.width != requireContext().convertDpToPx(45)) {
                    binding.imgCompassBackground.visibility = View.VISIBLE
                    requireContext().convertDpToPx(45)
                } else {
                    binding.imgCompassBackground.visibility = View.GONE
                    requireContext().convertDpToPx(250)
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
        OnMapClickInfoDialog { description, checkedItem, isLocal ->
            lifecycleScope.launch {
                mapViewModel.sendMarker(latLng, description, checkedItem, isLocal)
            }
        }.show(this.childFragmentManager, "customDialog")
    }

    @SuppressLint("MissingPermission")
    private fun setupGoogleMapUi() {
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isMapToolbarEnabled = false
        googleMap.uiSettings.isCompassEnabled = true
        googleMap.uiSettings.isMyLocationButtonEnabled = false
        if (requireContext().hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) || requireContext().hasPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            googleMap.isMyLocationEnabled = true
        } else {
            (activity as MainActivity).goToRequestsPermissions()
        }
    }


    override fun onDetach() {
        mapViewModel.stopLocationUpdates()
        myAlarmManager.stopAlarmManager()
        super.onDetach()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapInteractionHandler.onDestroy()
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

    override fun onLoadMapFromServerClick(url: String) {
        requireActivity().onBackPressedDispatcher.onBackPressed()
        makeLongText(requireContext().getString(R.string.load_map_started))
        lifecycleScope.launch {
            mapViewModel.loadMapFromServer(requireContext().applicationContext, url)?.let {
                it.message?.let { it1 -> makeLongText(it1) }
            }
        }
    }

    override fun onChangeUserGroup(userGroup: Int) {
        lifecycleScope.launch {
            mapViewModel.deleteUserFromOldGroup(userGroup)
            mapOverlayManager.removeMarkers()
            jobStaticMarkerUpdate?.cancel()
            jobUsersMarkerUpdate?.cancel()
            jobUsersMarkerUpdate = null
            jobStaticMarkerUpdate = null
            markersObservers()
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
                requireActivity(),
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_CODE_POST_NOTIFICATION
            )
        }
    }
}