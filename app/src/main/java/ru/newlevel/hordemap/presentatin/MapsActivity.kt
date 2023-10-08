package ru.newlevel.hordemap.presentatin

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.FirebaseApp
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.data.models.MarkerModel
import ru.newlevel.hordemap.databinding.ActivityMainBinding
import ru.newlevel.hordemap.domain.usecases.MarkerCreator


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMainBinding
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var markerViewModel: MarkerViewModel
    private lateinit var markersObserver: Observer<List<MarkerModel>>
    private lateinit var markerCreator: MarkerCreator
    private var _stateMap: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        FirebaseApp.initializeApp(applicationContext);

        windowSettings()
        loginViewModel =
            ViewModelProvider(this, LoginViewModelFactory(this))[LoginViewModel::class.java]

        markerViewModel =
            ViewModelProvider(this, MarkerViewModelFactory())[MarkerViewModel::class.java]

        loginViewModel.reset()
        loginViewModel.checkLogin()


        loginViewModel.loginResult.observe(this) {
            if (it.isNotEmpty()) {
                _stateMap = true
                Toast.makeText(this, "Привет $it", Toast.LENGTH_LONG).show()
                val mapFragment = SupportMapFragment()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, mapFragment)
                    .commit()
                mapFragment.getMapAsync(this)
            } else {
                _stateMap = false
                val loginFragment = LoginFragment(loginViewModel)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, loginFragment)
                    .commit()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        markerCreator = MarkerCreator(this, mMap)
        markersObserver = Observer {
            Log.e("AAA", it.toString())
            for (markerModel in it) {
                markerCreator.createMarkers(it)
            }
        }


        markerViewModel.startMarkerUpdates()
        markerViewModel.markersLiveData.observe(this, markersObserver)

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    override fun onStart() {
        super.onStart()
        if (_stateMap) {
            markerViewModel.startMarkerUpdates()
            markerViewModel.markersLiveData.observe(this, markersObserver)
        }
    }


    override fun onPause() {
        super.onPause()
        if (_stateMap) {
            markerViewModel.markersLiveData.removeObserver(markersObserver)
            markerViewModel.stopMarkerUpdates()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun windowSettings() {
        window.statusBarColor = Color.TRANSPARENT // Прозрачный цвет строки состояния
        supportActionBar?.hide()                  // Скрыть акшн бар
    }

}