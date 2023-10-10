package ru.newlevel.hordemap.presentatin.fragments


import android.annotation.SuppressLint
import android.content.*
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import ru.newlevel.hordemap.app.GpsForegroundService
import ru.newlevel.hordemap.data.storage.models.MarkerModel
import ru.newlevel.hordemap.domain.models.UserDomainModel
import ru.newlevel.hordemap.domain.usecases.MarkerCreator
import ru.newlevel.hordemap.presentatin.viewmodels.GpsServiceViewModel
import ru.newlevel.hordemap.presentatin.viewmodels.MarkerViewModel
import ru.newlevel.hordemap.presentatin.viewmodels.MarkerViewModelFactory


class MapFragment(private val userDomainModel: UserDomainModel) : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var markerCreator: MarkerCreator
    private var userMarkersObserver: Observer<List<MarkerModel>>? = null
    private var staticMarkersObserver: Observer<List<MarkerModel>>? = null
    private lateinit var markerViewModel: MarkerViewModel
    private val receiver = LocationUpdateReceiver()
    private var gpsService = GpsServiceViewModel()

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
        mMap = googleMap

        markerCreator = MarkerCreator(requireContext(), mMap, userDomainModel)
        markerViewModel =
            ViewModelProvider(this, MarkerViewModelFactory())[MarkerViewModel::class.java]

        userMarkersObserver = Observer {
            Log.e("AAA", "Пришло в  userMarkersObserver" + it.toString())
            markerCreator.createUsersMarkers(it)
        }

        staticMarkersObserver = Observer {
            Log.e("AAA", "Пришло в staticMarkersObserver" + it.toString())
            markerCreator.createStaticMarkers(it)
        }

        gpsService.startForegroundService(requireContext())

        // настройки карты
        setupMap()
        // слушатели нажатий на карте
        mapListenersSetup()

        // Запускаем обсерверы
        startObservers()

        //Камера на Красноярск
        val location = LatLng(56.0901, 93.2329) //координаты красноярска
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location))

        val filter = IntentFilter(GpsForegroundService.ACTION_LOCATION_UPDATE)
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
        mMap.setOnInfoWindowClickListener { obj: Marker -> obj.hideInfoWindow() }

        //Показываем только текст маркера, без перемещения к нему камеры
        mMap.setOnMarkerClickListener { marker: Marker ->
            marker.showInfoWindow()
            true
        }

        mMap.setOnInfoWindowLongClickListener {
            markerViewModel.deleteStaticMarker(it)
        }
    }

    @SuppressLint("MissingPermission")
    private fun setupMap() {
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.isCompassEnabled = true
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


    override fun onPause() {
        super.onPause()
        stopObservers()
    }

    override fun onStart() {
        super.onStart()
        startObservers()
    }

    override fun onDestroy() {
        super.onDestroy()
        gpsService.stopForegroundService(requireContext())
    }
}
