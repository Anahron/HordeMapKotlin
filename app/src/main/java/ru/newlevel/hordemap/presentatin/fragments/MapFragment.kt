package ru.newlevel.hordemap.presentatin.fragments


import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.app.MyLocationManager
import ru.newlevel.hordemap.data.storage.models.MarkerDataModel
import ru.newlevel.hordemap.domain.models.UserDomainModel
import ru.newlevel.hordemap.hasPermission
import ru.newlevel.hordemap.presentatin.MainActivity
import ru.newlevel.hordemap.presentatin.viewmodels.MarkerViewModel
import ru.newlevel.hordemap.presentatin.viewmodels.MarkerViewModelFactory


class MapFragment(private val userDomainModel: UserDomainModel) : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private var userMarkersObserver: Observer<List<MarkerDataModel>>? = null
    private var staticMarkersObserver: Observer<List<MarkerDataModel>>? = null
    private lateinit var markerViewModel: MarkerViewModel
    private val receiver = LocationUpdateReceiver()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_maps, container, false)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        return view
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

        markerViewModel = ViewModelProvider(this, MarkerViewModelFactory(requireContext()))[MarkerViewModel::class.java]

        userMarkersObserver = Observer {
            Log.e("AAA", "Пришло в  userMarkersObserver" + it.toString())
           markerViewModel.createUsersMarkers(it, googleMap)
        }

        staticMarkersObserver = Observer {
            Log.e("AAA", "Пришло в staticMarkersObserver" + it.toString())
            markerViewModel.createStaticMarkers(it, googleMap)
        }

      //  startForegroundService(requireContext(), userDomainModel.timeToSendData)

        // настройки карты
        setupMap()
        // слушатели нажатий на карте
        mapListenersSetup()
        // Запускаем обсерверы
        startObservers()

        //Камера на Красноярск
        val location = LatLng(56.0901, 93.2329) //координаты красноярска
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLng(location))

        val filter = IntentFilter(MyLocationManager.ACTION_LOCATION_UPDATE)
        requireContext().registerReceiver(receiver, filter)
    }

    inner class LocationUpdateReceiver : BroadcastReceiver() {
        val ACTION_LOCATION_UPDATE = "ru.newlevel.hordemap.ACTION_LOCATION_UPDATE"
        val EXTRA_LOCATION = "extra_location"
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_LOCATION_UPDATE) {
                val location = intent.getParcelableExtra<Location>(EXTRA_LOCATION)
                if (location != null) {
                    markerViewModel.sendCoordinates(location, userDomainModel)
                }
            }
        }
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
            markerViewModel.userMarkersLiveData.removeObserver(it)
            markerViewModel.stopMarkerUpdates()
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
    fun startForegroundService(context: Context, timeToSendData: Int) {
        Log.e("AAA", "startForegroundService вызван")
        val serviceIntent = Intent(context, MyLocationManager::class.java)
        serviceIntent.putExtra("timeToSendData", timeToSendData)
        ContextCompat.startForegroundService(context, serviceIntent)
    }

    fun stopForegroundService(context: Context) {
        val serviceIntent = Intent(context, MyLocationManager::class.java)
        context.stopService(serviceIntent)
    }

    override fun onPause() {
        startForegroundService(requireContext(), userDomainModel.timeToSendData)
        super.onPause()
        stopObservers()
    }

    override fun onStart() {
        super.onStart()
        startObservers()
        stopForegroundService(requireContext())
    }

    override fun onDestroy() {
        stopForegroundService(requireContext())
        super.onDestroy()
    }
}
