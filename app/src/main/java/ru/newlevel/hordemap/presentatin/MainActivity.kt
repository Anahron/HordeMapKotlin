package ru.newlevel.hordemap.presentatin

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.databinding.ActivityMainBinding
import ru.newlevel.hordemap.hasPermission
import ru.newlevel.hordemap.presentatin.fragments.LoginFragment
import ru.newlevel.hordemap.presentatin.fragments.MapFragment
import ru.newlevel.hordemap.presentatin.fragments.PermissionRequestFragment
import ru.newlevel.hordemap.presentatin.viewmodels.LoginViewModel

class MainActivity : AppCompatActivity(), PermissionRequestFragment.Callbacks {

    private lateinit var binding: ActivityMainBinding
    val loginViewModel by viewModel<LoginViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        windowSettings()

        if (!applicationContext.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) requestFineLocationPermission()
        else loginCheck()
    }


    private fun loginCheck() {
        //TODO удалить ресет (тест первого запуска)
        //    loginViewModel.reset()
        loginViewModel.checkLogin()

        loginViewModel.loginResult.observe(this) {
            if (it.name.isNotEmpty()) {
                Toast.makeText(this, "Привет ${it.name}", Toast.LENGTH_LONG).show()
                val mapFragment = MapFragment(loginViewModel)
                supportFragmentManager.beginTransaction().replace(R.id.container, mapFragment)
                    .commit()
            } else {
                val loginFragment = LoginFragment(loginViewModel)
                supportFragmentManager.beginTransaction().replace(R.id.container, loginFragment)
                    .commit()
            }
        }
    }

    private fun windowSettings() {
        //  window.statusBarColor = Color.TRANSPARENT // Прозрачный цвет строки состояния
        supportActionBar?.hide()                  // Скрыть акшн бар
    }

    override fun displayLocationUI() {
        loginCheck()
    }

    fun requestFineLocationPermission() {
        val fragment = PermissionRequestFragment()

        supportFragmentManager.beginTransaction().replace(R.id.container, fragment)
            .addToBackStack(null).commit()
    }
}