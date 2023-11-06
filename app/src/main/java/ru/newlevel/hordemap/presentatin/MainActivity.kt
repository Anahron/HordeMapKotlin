package ru.newlevel.hordemap.presentatin

import android.Manifest
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.app.hasPermission
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.presentatin.fragments.MapFragment
import ru.newlevel.hordemap.presentatin.fragments.PermissionRequestFragment
import ru.newlevel.hordemap.presentatin.viewmodels.SettingsViewModel

const val MY_PERMISSIONS_REQUEST_SENSOR = 506

class MainActivity : AppCompatActivity(R.layout.activity_main),
    PermissionRequestFragment.Callbacks {

    private val loginViewModel by viewModel<SettingsViewModel>()
    private var isFirstStart: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        UserEntityProvider.sessionId = System.currentTimeMillis()
        windowSettings()
        if (!applicationContext.hasPermission(Manifest.permission_group.SENSORS)) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission_group.SENSORS), MY_PERMISSIONS_REQUEST_SENSOR
            )
        }
        if (!applicationContext.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) requestPermission()
        else loginCheck()
    }

    private fun loginCheck() {
        //TODO удалить ресет (тест первого запуска)
        //  loginViewModel.reset()
        loginViewModel.checkLogin()
        loginViewModel.loginResultData.observe(this) {
            if (it.name.isNotEmpty()) {
                isFirstStart = false
                Toast.makeText(this, "Привет ${it.name}", Toast.LENGTH_LONG).show()
                val mapFragment = MapFragment(loginViewModel)
                supportFragmentManager.beginTransaction().replace(R.id.container, mapFragment)
                    .commit()
                loginViewModel.loginResultData.removeObservers(this)
            } else {
                requestPermission()
            }
        }
    }

    private fun windowSettings() {
        window.statusBarColor = Color.TRANSPARENT // Прозрачный цвет строки состояния
        supportActionBar?.hide()                  // Скрыть акшн бар
    }

    override fun displayLocationUI() {
        loginCheck()
    }

    fun requestPermission() {
        val fragment = PermissionRequestFragment()
        supportFragmentManager.beginTransaction().replace(R.id.container, fragment)
            .addToBackStack(null).commit()
    }
}