package ru.newlevel.hordemap.presentatin

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.databinding.ActivityMainBinding
import ru.newlevel.hordemap.domain.models.UserDomainModel

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMainBinding
    private lateinit var loginVM: LoginVM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = Color.TRANSPARENT // Прозрачный цвет строки состояния
        supportActionBar?.hide()                  // Скрыть акшн бар

        loginVM = ViewModelProvider(this)[LoginVM::class.java]

        val isLoggedIn = loginVM.getUser() // Проверить состояние входа пользователя здесь


        if (isLoggedIn) {
            Toast.makeText(this, "Привет", Toast.LENGTH_LONG).show()
            // Пользователь вошел в систему, отображаем карту
            val mapFragment = SupportMapFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, mapFragment)
                .commit()
            mapFragment.getMapAsync(this)
        } else {
            Toast.makeText(this, "Логина нет", Toast.LENGTH_LONG).show()
            // Пользователь не вошел в систему, отображаем фрагмент с логином
            val loginFragment = LoginFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, loginFragment)
                .commit()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }
}