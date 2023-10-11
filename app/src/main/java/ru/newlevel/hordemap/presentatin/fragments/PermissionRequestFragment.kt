package ru.newlevel.hordemap.presentatin.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import ru.newlevel.hordemap.BuildConfig
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.databinding.FragmentPermissionRequestBinding
import ru.newlevel.hordemap.hasPermission
import ru.newlevel.hordemap.requestPermissionWithRationale

private const val TAG = "AAA"

class PermissionRequestFragment : Fragment() {

    // Type of permission to request (fine or background). Set by calling Activity.
    private lateinit var binding: FragmentPermissionRequestBinding

    private var activityListener: Callbacks? = null
    private var fineLocationPermissionApproved: Boolean = false
    private var backgroundLocationPermissionApproved: Boolean = false

    // If the user denied a previous permission request, but didn't check "Don't ask again", these
    // Snackbars provided an explanation for why user should approve, i.e., the additional
    // rationale.

    private val backgroundRationalSnackbar by lazy {
        Snackbar.make(
            binding.frameLayout,
            R.string.background_location_permission_rationale,
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction(R.string.ok) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE
                )
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Callbacks) {
            activityListener = context
        } else {
            throw RuntimeException("$context must implement PermissionRequestFragment.Callbacks")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPermissionRequestBinding.inflate(inflater, container, false)
        binding.apply {
            // iconImageView.setImageResource(R.drawable.ic_location_on_24px)

            titleTextView.text =
                getString(R.string.fine_location_access_rationale_title_text)

            detailsTextView.text =
                getString(R.string.fine_location_access_rationale_details_text)

            permissionRequestButton.text =
                getString(R.string.enable_fine_location_button_text)

            permissionBackgroundRequestButton.text =
                getString(R.string.enable_background_location_button_text)
            permissionBackgroundRequestButton.isGone = true
        }

        binding.permissionRequestButton.setOnClickListener {
            requestFineLocationPermission()
        }

        binding.permissionBackgroundRequestButton.setOnClickListener {
            requestBackgroundLocationPermission()
        }

        val intent = Intent()
        val pm : PowerManager = requireContext().getSystemService(Context.POWER_SERVICE) as PowerManager
        if (!pm.isIgnoringBatteryOptimizations(requireContext().packageName)) {
            intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            intent.data = Uri.parse("package:${context?.packageName}")
            requireContext().startActivity(intent)
        }

        return binding.root
    }

    override fun onDetach() {
        super.onDetach()

        activityListener = null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.d(TAG, "onRequestPermissionResult")

        when (requestCode) {
            REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE -> when {
                grantResults.isEmpty() ->
                    Log.d(TAG, "User interaction was cancelled.")
                grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        fineLocationPermissionApproved = true
                     changeButton()
                    } else activityListener?.displayLocationUI()
                }
                else -> {
                    permissionDenied(requestCode)
                }
            }
            REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE -> when {
                grantResults.isEmpty() ->
                    Log.d(TAG, "User interaction was cancelled.")
                grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                    backgroundLocationPermissionApproved = true
                    if (fineLocationPermissionApproved)
                        activityListener?.displayLocationUI()
                }
                else -> {
                    permissionDenied(requestCode)
                }
            }
        }
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
                // Build intent that displays the App settings screen.
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
    private fun changeButton(){
        binding.permissionRequestButton.isGone = true
        binding.permissionBackgroundRequestButton.isGone = false
    }

    private fun requestFineLocationPermission() {
        val permissionApproved = context?.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) ?: return
        if (permissionApproved) {
            fineLocationPermissionApproved = true
            if (backgroundLocationPermissionApproved)
                activityListener?.displayLocationUI()
            else {
                changeButton()
            }
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    private fun requestBackgroundLocationPermission() {
        val permissionApproved =
            context?.hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) ?: return
        if (permissionApproved) {
            backgroundLocationPermissionApproved = true
            if (fineLocationPermissionApproved)
                activityListener?.displayLocationUI()
        } else {
            requestPermissionWithRationale(
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE,
                backgroundRationalSnackbar
            )
        }
    }

    interface Callbacks {
        fun displayLocationUI()
    }

    companion object {
        private const val REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE = 34
        private const val REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE = 56
    }
}
