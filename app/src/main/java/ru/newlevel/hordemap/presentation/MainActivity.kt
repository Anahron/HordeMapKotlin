package ru.newlevel.hordemap.presentation

import android.Manifest
import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.app.hasPermission
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.presentation.map.MapFragment
import ru.newlevel.hordemap.presentation.messenger.MessengerFragment
import ru.newlevel.hordemap.presentation.permissions.PermissionRequestFragment
import ru.newlevel.hordemap.presentation.settings.SettingsViewModel
import ru.newlevel.hordemap.presentation.tracks.TracksFragment

const val MY_PERMISSIONS_REQUEST_SENSOR = 506

class MainActivity : AppCompatActivity(R.layout.activity_main),
    PermissionRequestFragment.Callbacks {

    private val loginViewModel by viewModel<SettingsViewModel>()
    private var mainFragment: Fragment = MapFragment()
    private var tracksFragment: TracksFragment = TracksFragment()
    private var messengerFragment: MessengerFragment = MessengerFragment()
    private lateinit var currentFragment: Fragment
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var navView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //TODO удалить ресет (тест первого запуска)
      //   loginViewModel.reset()
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

        currentFragment = mainFragment
        navView = findViewById(R.id.bottomNavigationView)
        navView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.messengerFragment -> {
                    showFragment(messengerFragment)
                    hideNavView()
                    true
                }

                R.id.tracksFragment -> {
                    showFragment(tracksFragment)
                    true
                }

                else -> {
                    showFragment(mainFragment)
                    true
                }
            }
        }
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (currentFragment != mainFragment)
                    navView.selectedItemId = R.id.mapFragment
            }
        }
        this.onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun hideNavView() {
        navView.translationY = 0f
        val animator = ObjectAnimator.ofFloat(navView, "translationY", 500f)
        animator.duration = 500
        animator.interpolator = AccelerateInterpolator()
        animator.start()
    }

    private fun showNavView() {
        navView.translationY = 500f
        val animator = ObjectAnimator.ofFloat(navView, "translationY", 0f)
        animator.duration = 500
        animator.start()
    }

    private fun removeFragment(fragment: Fragment) {
        if (fragment != mainFragment) {
            supportFragmentManager.clearBackStack("${fragment.id}")
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_bottom,
                    R.anim.slide_out_bottom,
                    R.anim.slide_in_bottom,
                    R.anim.slide_out_bottom,
                ).hide(fragment).commit()
            handler.postDelayed({
                supportFragmentManager.beginTransaction().remove(fragment)
                    .commit()
            }, 300)
        }
    }

    private fun addAndShowFragment(fragment: Fragment) {
        if (fragment != mainFragment || !mainFragment.isAdded) {
            supportFragmentManager.beginTransaction().setCustomAnimations(
                R.anim.slide_in_bottom,
                R.anim.slide_out_bottom,
                R.anim.slide_in_bottom,
                R.anim.slide_out_bottom,
            ).add(R.id.container, fragment).addToBackStack("${fragment.id}").show(fragment).commit()
        }
    }

    private fun showFragment(fragment: Fragment) {
        if (navView.translationY != 0F)
            showNavView()
        if (currentFragment != fragment) {
            if (currentFragment == tracksFragment)
                handler.postDelayed({
                    addAndShowFragment(fragment)
                }, 150)
            else
                addAndShowFragment(fragment)
            removeFragment(currentFragment)
            currentFragment = fragment
        }
    }

    private fun loginCheck() {
        loginViewModel.checkLogin()
        loginViewModel.loginResultData.observe(this) {
            Log.e("AAA",  "loginViewModel.loginResultData.observe"  + this )
            if (it.name.isNotEmpty()) {
                addAndShowFragment(mainFragment)
                loginViewModel.loginResultData.removeObservers(this)
                navView.visibility = ViewGroup.VISIBLE
                val string: String = this.getString(R.string.hello)
                Toast.makeText(this, (string +" "+ it.name), Toast.LENGTH_LONG).show()
            } else {
                loginViewModel.loginResultData.removeObservers(this)
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
        val permissionRequestFragment = PermissionRequestFragment()
        supportFragmentManager.beginTransaction().setCustomAnimations(
            R.anim.slide_in_bottom,
            R.anim.slide_out_bottom,
            R.anim.slide_in_bottom,
            R.anim.slide_out_bottom,
        ).replace(R.id.container, permissionRequestFragment).addToBackStack(null).commit()
    }
}