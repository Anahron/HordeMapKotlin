package ru.newlevel.hordemap.presentation

import android.Manifest
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.app.ACTION_OPEN_MESSENGER
import ru.newlevel.hordemap.app.hasPermission
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.device.MessageNotificationService
import ru.newlevel.hordemap.presentation.map.MapFragment
import ru.newlevel.hordemap.presentation.messenger.MessengerFragment
import ru.newlevel.hordemap.presentation.permissions.PermissionRequestFragment
import ru.newlevel.hordemap.presentation.settings.SettingsFragment
import ru.newlevel.hordemap.presentation.sign_in.GoogleAuthUiClient
import ru.newlevel.hordemap.presentation.sign_in.SingInFragment
import ru.newlevel.hordemap.presentation.sign_in.UserData
import ru.newlevel.hordemap.presentation.tracks.TracksFragment


class MainActivity : AppCompatActivity(R.layout.activity_main), DisplayLocationUi {

    private val mainFragment: MapFragment by lazy { MapFragment() }
    private val tracksFragment: TracksFragment by lazy { TracksFragment() }
    private val settingsFragment: SettingsFragment by lazy { SettingsFragment(mainFragment) }
    private val messengerFragment: MessengerFragment by lazy { MessengerFragment() }
    private val googleAuthUiClient by inject<GoogleAuthUiClient>()
    private val mainViewModel by viewModel<MainViewModel>()
    private val notificationService: MessageNotificationService by lazy { MessageNotificationService(applicationContext) }
    private lateinit var currentFragment: Fragment
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var navView: BottomNavigationView
    private var syncJob: Job? = null
    private var newMessageJob: Job? = null
    private var isAppInBackground = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        UserEntityProvider.sessionId = System.currentTimeMillis()
        windowSettings()
        setupNavView()
        navView.visibility = ViewGroup.GONE
        var signedUser: UserData?
        lifecycleScope.launch {
            signedUser = googleAuthUiClient.getSignedInUser()
            if (signedUser != null) {
                checkPermissionAndShowMap()
            } else {
                logOut()
            }
        }
        onBackPressedListener()
    }

    override fun onStart() {
        super.onStart()
        isAppInBackground = false
    }

    override fun onStop() {
        super.onStop()
        isAppInBackground = true
    }
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.action == ACTION_OPEN_MESSENGER)
            navView.selectedItemId = R.id.messengerFragment
    }

    private fun newMessageHandler() {
        syncJob = lifecycleScope.launch {
            mainViewModel.syncMessageData()
        }
        newMessageJob = lifecycleScope.launch {
            mainViewModel.newMessageAnnounced.collect {
                if (it > 0) {
                    if (isAppInBackground)
                        notificationService.showNotification(it)
                    navView.getOrCreateBadge(R.id.messengerFragment).apply {
                        isVisible = true
                        number = it
                    }
                } else
                    navView.getOrCreateBadge(R.id.messengerFragment).isVisible = false
            }
        }
    }

    private fun onBackPressedListener() {
        this.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (currentFragment != mainFragment)
                    navView.selectedItemId = R.id.mapFragment
            }
        })
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
                    mainViewModel.resetNewMessageCount()
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
            newMessageHandler()
        } else {
            syncJob?.cancel()
            newMessageJob?.cancel()
            navView.visibility = ViewGroup.GONE
            goToRequestsPermissions()
        }
    }

    private fun windowSettings() {
        window.statusBarColor = Color.TRANSPARENT
        supportActionBar?.hide()
    }

    override fun changeProfilePhoto(newPhotoUrl: Uri) {
        lifecycleScope.launch {
            googleAuthUiClient.profileUpdate(newUserPhoto = newPhotoUrl).errorMessage?.let {
                Toast.makeText(applicationContext, it, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun displayLocationUI() {
        handler.postDelayed({
            checkPermissionAndShowMap()
            currentFragment = mainFragment
            navView.selectedItemId = R.id.mapFragment
        }, 150)
    }

    override fun logOut() {
        syncJob?.cancel()
        newMessageJob?.cancel()
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

interface DisplayLocationUi {

    fun changeProfilePhoto(newPhotoUrl: Uri)
    fun displayLocationUI()
    fun logOut()
}