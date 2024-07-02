package ru.newlevel.hordemap.presentation

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.app.ACTION_OPEN_MESSENGER
import ru.newlevel.hordemap.app.hasPermission
import ru.newlevel.hordemap.app.hideToBottomAnimation
import ru.newlevel.hordemap.app.showFromBottomAnimation
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

    private lateinit var mainFragment: MapFragment
    private val tracksFragment: TracksFragment by lazy { TracksFragment() }
    private lateinit var settingsFragment: SettingsFragment
    private val messengerFragment: MessengerFragment by lazy { MessengerFragment() }
    private val fragmentManager: FragmentManager by lazy { FragmentManager(supportFragmentManager) }
    private val googleAuthUiClient by inject<GoogleAuthUiClient>()
    private val mainViewModel by viewModel<MainViewModel>()
    private val notificationService: MessageNotificationService by lazy { MessageNotificationService(applicationContext) }
    private var currentFragment: Fragment? = null
    private lateinit var navView: BottomNavigationView
    private var syncJob: Job? = null
    private var newMessageJob: Job? = null
    private var isAppInBackground = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        windowSettings()
        setupNavView()
        navView.visibility = ViewGroup.GONE
        checkSignIn()
        onBackPressedListener()
    }

    private fun checkSignIn() {
        var signedUser: UserData?
        lifecycleScope.launch {
            signedUser = googleAuthUiClient.getSignedInUser()
            if (signedUser != null) {
                checkPermissionAndShowMap()
            } else {
                logOut()
            }
        }
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
        if (intent?.action == ACTION_OPEN_MESSENGER) {
            navView.selectedItemId = R.id.messengerFragment
            mainViewModel.resetNewMessageCount()
        }
    }

    private fun newMessageHandler() {
        navView.getOrCreateBadge(R.id.messengerFragment).apply {
            number = 0
        }
        syncJob = lifecycleScope.launch {
            mainViewModel.syncMessageData()
        }
        newMessageJob = lifecycleScope.launch {
            mainViewModel.newMessageAnnounced.collect {
                if (it > 0) {
                    if (isAppInBackground)
                        notificationService.showNotification(it)
                    if (currentFragment != messengerFragment)
                        navView.getOrCreateBadge(R.id.messengerFragment).apply {
                            isVisible = true
                            number = it
                        }
                } else {
                    navView.getOrCreateBadge(R.id.messengerFragment).isVisible = false
                    notificationService.hideNotification()
                }
            }
        }
    }

    private fun onBackPressedListener() {
        this.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                currentFragment?.let {
                    if (currentFragment != mainFragment)
                        navView.selectedItemId = R.id.mapFragment
                }
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
                    navView.hideToBottomAnimation()
                    mainViewModel.resetNewMessageCount()
                    true
                }

                R.id.tracksFragment -> {
                    navView.showFromBottomAnimation()
                    showFragment(tracksFragment)
                    true
                }

                R.id.settingsFragment -> {
                    navView.showFromBottomAnimation()
                    showFragment(settingsFragment)
                    true
                }

                else -> {
                    navView.showFromBottomAnimation()
                    showFragment(mainFragment)
                    true
                }
            }
        }
    }


    private fun showFragment(fragment: Fragment) {
        lifecycleScope.launch {
            currentFragment?.let {
                if (it != fragment && it != mainFragment)
                    fragmentManager.removeFragment(it)
            }
            if (fragment != mainFragment) {
                currentFragment = fragmentManager.addAndShowFragment(fragment)
            }
        }
    }

    private fun checkPermissionAndShowMap() {
        if (applicationContext.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            mainFragment = MapFragment()
            settingsFragment = SettingsFragment(mainFragment)
            fragmentManager.replaceFragment(mainFragment)
            currentFragment = mainFragment
            navView.visibility = ViewGroup.VISIBLE
            newMessageHandler()
            navView.selectedItemId = R.id.mapFragment
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

    override fun onChangeUserGroup() {
        mainViewModel.resetNewMessageCount()
        syncJob?.cancel()
        newMessageJob?.cancel()
        newMessageHandler()
    }

    override fun changeProfilePhoto(newPhotoUrl: Uri) {
        lifecycleScope.launch {
            googleAuthUiClient.profileUpdate(newUserPhoto = newPhotoUrl).errorMessage?.let {
                Toast.makeText(applicationContext, it, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun displayLocationUI() {
        lifecycleScope.launch {
            delay(150)
            checkPermissionAndShowMap()
        }
    }

    override fun logOut() {
        syncJob?.cancel()
        newMessageJob?.cancel()
        lifecycleScope.launch {
            googleAuthUiClient.signOut()
        }
        fragmentManager.replaceFragment(SingInFragment())
        navView.visibility = ViewGroup.GONE
    }

    fun goToRequestsPermissions() {
        fragmentManager.replaceFragment(PermissionRequestFragment())
        navView.visibility = ViewGroup.GONE
    }
}

interface DisplayLocationUi {
    fun onChangeUserGroup()
    fun changeProfilePhoto(newPhotoUrl: Uri)
    fun displayLocationUI()
    fun logOut()
}