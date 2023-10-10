package ru.newlevel.hordemap.presentatin

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.FirebaseApp
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.databinding.ActivityMainBinding
import ru.newlevel.hordemap.presentatin.fragments.LoginFragment
import ru.newlevel.hordemap.presentatin.fragments.MapFragment
import ru.newlevel.hordemap.presentatin.viewmodels.LoginViewModel
import ru.newlevel.hordemap.presentatin.viewmodels.LoginViewModelFactory

class MainActivity : AppCompatActivity() {

    private val REQUEST_LOCATION_PERMISSION = 1
    private lateinit var binding: ActivityMainBinding
    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        windowSettings()

        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Разрешение уже предоставлено
            loginCheck()
        } else {
            // Запросить разрешение у пользователя
            ActivityCompat.requestPermissions(
                this,
                arrayOf(permission),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Разрешение предоставлено, можно открыть карту
                loginCheck()
            } else {
               Toast.makeText(this, "Разрешите геолокацию", Toast.LENGTH_LONG).show()
            }
        }
    }



    private fun loginCheck() {
        loginViewModel =
            ViewModelProvider(this, LoginViewModelFactory(this))[LoginViewModel::class.java]

        //TODO удалить ресет (тест первого запуска)
        loginViewModel.reset()
        loginViewModel.checkLogin()

        loginViewModel.loginResult.observe(this) {
            if (it.name.isNotEmpty()) {
                Toast.makeText(this, "Привет ${it.name}", Toast.LENGTH_LONG).show()
                val mapFragment = MapFragment(it)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, mapFragment)
                    .commit()
            } else {
                val loginFragment = LoginFragment(loginViewModel)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, loginFragment)
                    .commit()
            }
        }
    }

    private fun windowSettings() {
        window.statusBarColor = Color.TRANSPARENT // Прозрачный цвет строки состояния
        supportActionBar?.hide()                  // Скрыть акшн бар
    }
}