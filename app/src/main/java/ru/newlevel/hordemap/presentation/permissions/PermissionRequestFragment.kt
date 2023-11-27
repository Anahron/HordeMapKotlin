package ru.newlevel.hordemap.presentation.permissions

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.newlevel.hordemap.BuildConfig
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.app.REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE
import ru.newlevel.hordemap.app.REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE
import ru.newlevel.hordemap.app.TAG
import ru.newlevel.hordemap.app.hasPermission
import ru.newlevel.hordemap.databinding.FragmentPermissionRequestBinding
import ru.newlevel.hordemap.presentation.DisplayLocationUi


class PermissionRequestFragment : Fragment(R.layout.fragment_permission_request) {

    private val binding: FragmentPermissionRequestBinding by viewBinding()

    private val permissionViewModel by viewModel<PermissionViewModel>()
    private var activityListener: DisplayLocationUi? = null

    private val requestBatteryOptimizationLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.e(TAG, result.toString())
            permissionViewModel.turnToAddLocationState()
        }

    private val permissionBackgroundLocation =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            when {
                granted -> {
                    activityListener?.displayLocationUI()
                }

                else -> {
                    permissionDenied(REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE)
                }
            }
        }

    private val permissionsLocation =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
            if (map.values.contains(true))
                permissionViewModel.turnToAddBackLocationState()
            else
                permissionDenied(REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE)
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is DisplayLocationUi) {
            activityListener = context
        } else {
            throw RuntimeException("$context must implement PermissionRequestFragment.Callbacks")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionViewModel.state.observe(this@PermissionRequestFragment) { state ->
            when (state) {
                is PermissionState.InfoState -> {
                    binding.apply {
                        titleTextView.text = context?.getString(R.string.info_title)
                        detailsTextView.text = context?.getString(R.string.info)
                        editName.isGone = true
                        btnUserNameRequest.isGone = true
                        btnBackWorking.isGone = true
                        btnAccept.isGone = false
                        permissionRequestButton.isGone = true
                        permissionBackgroundRequestButton.isGone = true
                        btnAccept.setOnClickListener {
                            permissionViewModel.turnToAddBackWorkingState()
                        }
                    }
                }

                is PermissionState.AddBackWorking -> {
                    if (!isBatteryOptimizationIgnored()) {
                        binding.btnBackWorking.setOnClickListener {
                            requestBatteryOptimizationPermission()
                        }
                    } else {
                        permissionViewModel.turnToAddLocationState()
                    }
                    binding.apply {
                        titleTextView.text = context?.getString(R.string.back_working_title)
                        detailsTextView.text =
                            context?.getString(R.string.back_working_details_text)
                        btnAccept.isGone = true
                        editName.isGone = true
                        btnUserNameRequest.isGone = true
                        btnBackWorking.isGone = false
                        permissionRequestButton.isGone = true
                        permissionBackgroundRequestButton.isGone = true
                    }
                }

                is PermissionState.AddLocationPermState -> {
                    if (context?.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) == false) {
                        binding.permissionRequestButton.setOnClickListener {
                            requestFineLocationPermission()
                        }
                    } else {
                        permissionViewModel.turnToAddBackLocationState()
                    }
                    binding.apply {
                        titleTextView.text =
                            context?.getString(R.string.fine_location_access_rationale_title_text)
                        detailsTextView.text =
                            context?.getString(R.string.fine_location_access_rationale_details_text)
                        btnAccept.isGone = true
                        editName.isGone = true
                        btnBackWorking.isGone = true
                        btnUserNameRequest.isGone = true
                        permissionRequestButton.isGone = false
                        permissionBackgroundRequestButton.isGone = true
                    }
                }

                is PermissionState.AddBackLocationState -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && context?.hasPermission(
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ) == false
                    ) {
                        binding.permissionBackgroundRequestButton.setOnClickListener {
                            requestBackgroundLocationPermission()
                        }
                    } else {
                        activityListener?.displayLocationUI()
                    }
                    binding.apply {
                        titleTextView.text =
                            context?.getString(R.string.back_location_access_rationale_title_text)
                        detailsTextView.text =
                            context?.getString(R.string.back_location_permission_rationale_details_text)
                        editName.isGone = true
                        btnBackWorking.isGone = true
                        btnAccept.isGone = true
                        btnUserNameRequest.isGone = true
                        permissionRequestButton.isGone = true
                        permissionBackgroundRequestButton.isGone = false
                    }
                }
                else -> {}
            }
        }
    }

    private fun isBatteryOptimizationIgnored(): Boolean {
        val pm: PowerManager =
            requireContext().getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(requireContext().packageName)
    }

    override fun onDetach() {
        super.onDetach()
        activityListener = null
    }

    private fun permissionDenied(requestCode: Int) {
        val permissionDeniedExplanation =
            if (requestCode == REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE) {
                R.string.fine_permission_denied_explanation
            } else {
                R.string.background_permission_denied_explanation
            }
        val snackbar = Snackbar.make(
            binding.frameLayout,
            permissionDeniedExplanation,
            Snackbar.LENGTH_LONG
        )
        val snackbarView = snackbar.view
        val textView =
            snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.maxLines = 5
        snackbar
            .setAction(R.string.settings) {
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri = Uri.fromParts(
                    "package",
                    BuildConfig.APPLICATION_ID,
                    null
                )
                intent.data = uri
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
            .show()
    }

    @SuppressLint("BatteryLife")
    private fun requestBatteryOptimizationPermission() {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
        intent.data = Uri.parse("package:${requireContext().packageName}")
        requestBatteryOptimizationLauncher.launch(intent)
    }

    private fun requestFineLocationPermission() {
        permissionsLocation.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestBackgroundLocationPermission() {
        permissionBackgroundLocation.launch(
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            ActivityOptionsCompat.makeBasic()
        )
    }
}
