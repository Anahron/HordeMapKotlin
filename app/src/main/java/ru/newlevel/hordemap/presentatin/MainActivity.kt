package ru.newlevel.hordemap.presentatin

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.app.hasPermission
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.presentatin.fragments.MapFragment
import ru.newlevel.hordemap.presentatin.fragments.PermissionRequestFragment
import ru.newlevel.hordemap.presentatin.fragments.dialogs.TracksDialogFragment
import ru.newlevel.hordemap.presentatin.viewmodels.SettingsViewModel

const val MY_PERMISSIONS_REQUEST_SENSOR = 506

class MainActivity : AppCompatActivity(R.layout.activity_main),
    PermissionRequestFragment.Callbacks {

    private val loginViewModel by viewModel<SettingsViewModel>()
    private lateinit var mainFragment: Fragment
    private lateinit var tracksDialogFragment: TracksDialogFragment
    private lateinit var currentFragment: Fragment
    private lateinit var navView: BottomNavigationView
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

        mainFragment = MapFragment()
        currentFragment = mainFragment
        tracksDialogFragment = TracksDialogFragment()
        navView = findViewById(R.id.bottomNavigationView)
        navView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.messengerFragment -> {
                    if (tracksDialogFragment.isAdded)
                        supportFragmentManager.beginTransaction()
                            .remove(tracksDialogFragment)
                            .commit()
                    findNavController(R.id.container).navigate(R.id.messengerFragment)

                    val distance = 1000f
                    val animator1 = ObjectAnimator.ofFloat(navView, "translationY", distance)
                    animator1.duration = 900
                    val animator2 = ObjectAnimator.ofFloat(navView, "translationY", 0f)
                    animator2.duration = 900
                    val animatorSet = AnimatorSet()
                    animatorSet.playSequentially(animator1, animator2)
                    animatorSet.start()
                    false
                }

                R.id.tracksFragment -> {
                    showFragment(tracksDialogFragment)
                    navView.visibility = ViewGroup.VISIBLE
                    true
                }

                else -> {
                    navView.visibility = ViewGroup.VISIBLE
                    showFragment(mainFragment)
                    true
                }
            }
        }
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (currentFragment == tracksDialogFragment)
                    navView.selectedItemId = R.id.mapFragment
            }
        }
        this.onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun showFragment(fragment: Fragment) {
        if (fragment == mainFragment && mainFragment != currentFragment) {
            supportFragmentManager.beginTransaction().remove(tracksDialogFragment).commit()
            currentFragment = fragment
        } else if (fragment == tracksDialogFragment && currentFragment != tracksDialogFragment) {
            supportFragmentManager.beginTransaction().add(R.id.container, tracksDialogFragment)
                .addToBackStack("Track").commit()
            supportFragmentManager.beginTransaction().show(tracksDialogFragment).commit()
            currentFragment = fragment
        }
    }

    private fun loginCheck() {
        //TODO удалить ресет (тест первого запуска)
      //  loginViewModel.reset()
        loginViewModel.checkLogin()
        loginViewModel.loginResultData.observe(this) {
            if (it.name.isNotEmpty()) {
                findNavController(R.id.container).navigate(R.id.mapFragment)
                loginViewModel.loginResultData.removeObservers(this)
                navView.visibility = ViewGroup.VISIBLE
                Toast.makeText(this, "Привет ${it.name}", Toast.LENGTH_LONG).show()
            } else {
                navView.visibility = ViewGroup.GONE
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
        findNavController(R.id.container).navigate(R.id.action_splashSreenFragment_to_permissionRequestFragment)
    }
}