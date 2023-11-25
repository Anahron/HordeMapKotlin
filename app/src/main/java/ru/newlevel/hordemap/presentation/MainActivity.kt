package ru.newlevel.hordemap.presentation

import android.Manifest
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.os.PowerManager.FULL_WAKE_LOCK
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.app.hasPermission
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.presentation.map.MapFragment
import ru.newlevel.hordemap.presentation.messenger.MessengerFragment
import ru.newlevel.hordemap.presentation.permissions.PermissionRequestFragment
import ru.newlevel.hordemap.presentation.settings.SettingsFragment
import ru.newlevel.hordemap.presentation.sign_in.GoogleAuthUiClient
import ru.newlevel.hordemap.presentation.sign_in.SingInFragment
import ru.newlevel.hordemap.presentation.tracks.TracksFragment


class MainActivity : AppCompatActivity(R.layout.activity_main), DisplayLocationUi {

    private val mainFragment: MapFragment by lazy { MapFragment() }
    private val tracksFragment: TracksFragment by lazy { TracksFragment() }
    private val settingsFragment: SettingsFragment by lazy { SettingsFragment(mainFragment) }
    private val messengerFragment: MessengerFragment by lazy { MessengerFragment() }
    private val googleAuthUiClient by inject<GoogleAuthUiClient>()
    private lateinit var currentFragment: Fragment
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var navView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        UserEntityProvider.sessionId = System.currentTimeMillis()
        windowSettings()
        setupNavView()
        navView.visibility = ViewGroup.GONE
        if (googleAuthUiClient.getSignedInUser() != null)
            checkPermissionAndShowMap()
        else
            logOut()
        //   setupWakeLock()
        onBackPressedListener()
    }

    private fun onBackPressedListener() {
        this.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (currentFragment != mainFragment) navView.selectedItemId = R.id.mapFragment
            }
        })
    }

    private fun setupWakeLock() {
        val pm = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wl: PowerManager.WakeLock = pm.newWakeLock(FULL_WAKE_LOCK, "HordeMap:wakelock")
        wl.acquire(600 * 60 * 1000L /*600 minutes*/)
    }

    private fun setupNavView() {
        navView = findViewById(R.id.bottomNavigationView)
        navView.setOnItemSelectedListener {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
            when (it.itemId) {
                R.id.messengerFragment -> {
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                    showFragment(messengerFragment)
                    hideNavView()
                    true
                }

                R.id.tracksFragment -> {
                    showFragment(tracksFragment)
                    true
                }

                R.id.settingsFragment -> {
                    showFragment(settingsFragment)
                    true
                }

                else -> {
                    showFragment(mainFragment)
                    true
                }
            }
        }
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
            supportFragmentManager.beginTransaction().setCustomAnimations(
                R.anim.slide_in_bottom,
                R.anim.slide_out_bottom,
                R.anim.slide_in_bottom,
                R.anim.slide_out_bottom,
            ).hide(fragment).commit()
            handler.postDelayed({
                supportFragmentManager.beginTransaction().remove(fragment).commit()
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
        if (navView.translationY != 0F) showNavView()
        if (currentFragment != fragment) {
            removeFragment(currentFragment)
            addAndShowFragment(fragment)
            currentFragment = fragment
        }
    }

    private fun checkPermissionAndShowMap() {
        if (applicationContext.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            supportFragmentManager.beginTransaction().setCustomAnimations(
                R.anim.slide_in_bottom,
                R.anim.slide_out_bottom,
                R.anim.slide_in_bottom,
                R.anim.slide_out_bottom,
            ).replace(R.id.container, mainFragment).addToBackStack(null).commit()
            currentFragment = mainFragment
            navView.visibility = ViewGroup.VISIBLE
        } else {
            navView.visibility = ViewGroup.GONE
            goToRequestsPermissions()
        }
    }

    private fun windowSettings() {
        window.statusBarColor = Color.TRANSPARENT // Прозрачный цвет строки состояния
        supportActionBar?.hide()                  // Скрыть акшн бар
    }

    override fun displayLocationUI() {
        handler.postDelayed({
            checkPermissionAndShowMap()
            navView.selectedItemId = R.id.mapFragment
        }, 150)
    }

    override fun logOut() {
        lifecycleScope.launch {
            googleAuthUiClient.signOut()
        }
        supportFragmentManager.beginTransaction().setCustomAnimations(
            R.anim.slide_in_bottom,
            R.anim.slide_out_bottom,
            R.anim.slide_in_bottom,
            R.anim.slide_out_bottom,
        ).replace(R.id.container, SingInFragment()).addToBackStack(null).commit()
        navView.visibility = ViewGroup.GONE
    }

    fun goToRequestsPermissions() {
        supportFragmentManager.beginTransaction().setCustomAnimations(
            R.anim.slide_in_bottom,
            R.anim.slide_out_bottom,
            R.anim.slide_in_bottom,
            R.anim.slide_out_bottom,
        ).replace(R.id.container, PermissionRequestFragment()).addToBackStack(null).commit()
        navView.visibility = ViewGroup.GONE
    }
}