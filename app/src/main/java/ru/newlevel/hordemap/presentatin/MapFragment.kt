package ru.newlevel.hordemap.presentatin

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMapClickListener
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.data.storage.models.MarkerModel
import ru.newlevel.hordemap.domain.models.UserDomainModel
import ru.newlevel.hordemap.domain.usecases.MarkerCreator


class MapFragment(private val userDomainModel: UserDomainModel) : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var markerCreator: MarkerCreator
    private var userMarkersObserver: Observer<List<MarkerModel>>? = null
    private var staticMarkersObserver: Observer<List<MarkerModel>>? = null
    private lateinit var markerViewModel: MarkerViewModel


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
        markerViewModel = ViewModelProvider(this, MarkerViewModelFactory())[MarkerViewModel::class.java]
        markerViewModel.startMarkerUpdates()

        userMarkersObserver = Observer {
            Log.e("AAA", "Пришло в  userMarkersObserver" + it.toString())
            markerCreator.createUsersMarkers(it)
        }
        userMarkersObserver?.let {
            markerViewModel.userMarkersLiveData.observe(viewLifecycleOwner, it)
        }

        staticMarkersObserver = Observer {
            Log.e("AAA", "Пришло в staticMarkersObserver" + it.toString())
            markerCreator.createStaticMarkers(it)
        }
        staticMarkersObserver?.let {
            markerViewModel.staticMarkersLiveData.observe(viewLifecycleOwner, it)
        }

        // настройки карты
        mMap.uiSettings.isZoomControlsEnabled = true

//      Камера на Красноярск
        val location = LatLng(56.0901, 93.2329) //координаты красноярска

        mMap.moveCamera(CameraUpdateFactory.newLatLng(location))

        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true

        // Скрываем диалог при коротком клике по нему
        mMap.setOnInfoWindowClickListener { obj: Marker -> obj.hideInfoWindow() }

//      Показываем только текст маркера, без перемещения к нему камеры
        mMap.setOnMarkerClickListener { marker: Marker ->
            marker.showInfoWindow()
            true
        }
    }


    override fun onStart() {
        super.onStart()
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
        userMarkersObserver?.let {
            markerViewModel.userMarkersLiveData.removeObserver(it)
            markerViewModel.stopMarkerUpdates()
        }
        staticMarkersObserver?.let {
            markerViewModel.staticMarkersLiveData.removeObserver(it)
        }
    }
}
