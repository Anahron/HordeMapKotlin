package ru.newlevel.hordemap.presentatin

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
import com.google.android.gms.maps.model.MarkerOptions
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.data.models.MarkerModel
import ru.newlevel.hordemap.domain.models.UserDomainModel
import ru.newlevel.hordemap.domain.usecases.MarkerCreator


class MapFragment(private val userDomainModel: UserDomainModel) : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var markerCreator: MarkerCreator
    private var markersObserver: Observer<List<MarkerModel>>? = null
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

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        markerCreator = MarkerCreator(requireContext(), mMap, userDomainModel)
        markerViewModel =
            ViewModelProvider(this, MarkerViewModelFactory())[MarkerViewModel::class.java]

        markersObserver = Observer {
            Log.e("AAA", it.toString())
            for (markerModel in it) {
                markerCreator.createUsersMarkers(it)
            }
        }

        // настройки карты
        mMap.uiSettings.isZoomControlsEnabled = true

        //добавления маркера в центр карты
        val centerLatLng = LatLng(0.0, 0.0)
        mMap.addMarker(MarkerOptions().position(centerLatLng).title("Center Marker"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(centerLatLng))

        markersObserver?.let {
            markerViewModel.startMarkerUpdates()
            markerViewModel.markersLiveData.observe(viewLifecycleOwner, it)
        }

    }


    override fun onStart() {
        super.onStart()
        markersObserver?.let {
            markerViewModel.startMarkerUpdates()
            markerViewModel.markersLiveData.observe(viewLifecycleOwner, it)
        }
    }

    override fun onPause() {
        super.onPause()
        markersObserver?.let {
            markerViewModel.markersLiveData.removeObserver(it)
            markerViewModel.stopMarkerUpdates()
        }
    }
}
