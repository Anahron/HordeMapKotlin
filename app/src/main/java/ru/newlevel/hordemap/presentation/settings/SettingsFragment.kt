package ru.newlevel.hordemap.presentation.settings

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.view.iterator
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.app.SHADOW_QUALITY
import ru.newlevel.hordemap.app.SelectFilesContract
import ru.newlevel.hordemap.app.TAG
import ru.newlevel.hordemap.databinding.FragmentSettingBinding
import ru.newlevel.hordemap.domain.models.UserDomainModel
import ru.newlevel.hordemap.presentation.DisplayLocationUi
import kotlin.properties.Delegates

class SettingsFragment(private val callback: OnChangeSettings) : Fragment(R.layout.fragment_setting) {

    private val binding by viewBinding<FragmentSettingBinding>()
    private val settingsViewModel: SettingsViewModel by viewModel()
    private var checkedRadioButton by Delegates.notNull<Int>()
    private lateinit var currentUserSetting: UserDomainModel

    private var activityListener: DisplayLocationUi? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is DisplayLocationUi) {
            activityListener = context
        } else {
            throw RuntimeException("$context must implement DisplayLocationUi.displayLocationUI")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit = with(binding) {
        Log.e(TAG, "onViewCreated()")
        super.onViewCreated(view, savedInstanceState)
        setupUIComponents()
        setupRadioButtonListeners()
        setupGoBackListener()
        setupSeekBarListeners()
        setUpLogOutButton()
        setUpLoadingMapListeners()
        setUpResetButton()

        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                lifecycleScope.launch {
                    activityListener?.changeProfilePhoto(newPhotoUrl = uri)
                    settingsViewModel.loadProfilePhoto(uri, requireContext())?.let {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                settingsViewModel.setState(checkedId)
            }
        }

        circleImageView.setOnClickListener {
            pickImage.launch("image/*")
        }

        settingsViewModel.resultData.observe(viewLifecycleOwner) { user ->
            currentUserSetting = user
            loadImageIntoProfile()
            setLayoutParams(user)
            checkBox.isChecked = user.autoLoad
            tvUserAuthName.text = user.authName
            tvNickName.text = user.name
            binding.checkBox.isChecked = user.autoLoad
            sbTimeToSendData.value = user.timeToSendData.toFloat()
            sbStaticMarkerSize.value = user.staticMarkerSize.toFloat()
            sbUsersMarkerSize.value = user.usersMarkerSize.toFloat()
            val timeText = " ${user.timeToSendData}${getString(R.string.sec)}"
            tvTimeToSendData.text = timeText
            checkedRadioButton = user.selectedMarker
            for (i in 0 until radioGroup.childCount) {
                val radioButton = radioGroup.getChildAt(i) as ImageButton
                radioButton.alpha = if (radioButton.tag == checkedRadioButton.toString()) 1.0f else 0.3f
            }
        }

        lifecycleScope.launch {
            settingsViewModel.state.collect { state ->
                settingsViewModel.getUserSettings()
                when (state) {
                    is UiState.SettingsState -> {
                        setupSegmentButtons(R.id.btnToggleSettings)
                        changeUiToSettings()
                    }

                    is UiState.LoadMapState -> {
                        setupSegmentButtons(R.id.btnToggleLoadMap)
                        changeUiToLoadMap()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.e(TAG, "onResume")
    }

    private fun changeUiToLoadMap() {
        Log.e(TAG, "changeUiToLoadMap()")
        val pixels = requireContext().resources.displayMetrics.widthPixels
        val cardViewSettings = binding.cardViewSettings
        val cardViewLoadMap = binding.cardViewLoadMap
        val animator = ObjectAnimator.ofFloat(cardViewSettings, "translationX", -pixels.toFloat())
        animator.duration = 500
        animator.start()
        val animator2 = ObjectAnimator.ofFloat(cardViewLoadMap, "translationX", 0f)
        animator2.duration = 500
        animator2.start()
    }

    private fun changeUiToSettings() {
        Log.e(TAG, "changeUiToSettings")
        val cardViewSettings = binding.cardViewSettings
        val cardViewLoadMap = binding.cardViewLoadMap
        val pixels = requireContext().resources.displayMetrics.widthPixels
        val animator = ObjectAnimator.ofFloat(cardViewSettings, "translationX", 0f)
        animator.duration = 500
        animator.start()
        val animator2 = ObjectAnimator.ofFloat(cardViewLoadMap, "translationX", pixels.toFloat())
        animator2.duration = 500
        animator2.start()

    }

    private fun setUpLoadingMapListeners() = with(binding) {
        checkBox.setOnClickListener {
            settingsViewModel.saveAutoLoad(checkBox.isChecked)
            callback.onAutoLoadClick(checkBox.isChecked)
        }

        btnFromServer.setOnClickListener {
            callback.onLoadMapFromServerClick()
        }

        val activityLauncher = registerForActivityResult(SelectFilesContract()) { result ->
            result?.let {
                callback.onSelectFileClick(it)
            }
        }

        btnFromFiles.setOnClickListener {
            activityLauncher.launch("application/*")
        }
        btnLastSaved.setOnClickListener {
            Log.e(TAG, "   btnLastSaved.setOnClickListener clicked ")
            callback.onLoadLastGameMapClick()
        }

        btnCleanMap.setOnClickListener {
            callback.onAutoLoadClick(false)
            callback.onClearMapClick()
            settingsViewModel.saveUser(
                currentUserSetting.copy(
                    autoLoad = false
                )
            )
        }
    }

    private fun setupSegmentButtons(checkedId: Int) {
        val defaultColor = ContextCompat.getColor(requireContext(), R.color.slate_800)
        val selectedColor = ContextCompat.getColor(requireContext(), R.color.white)
        for (button in binding.toggleGroup) {
            if (button.id == checkedId) {
                binding.root.findViewById<MaterialButton>(button.id).setTextColor(selectedColor)
                button.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.main_green_dark)
            } else {
                button.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.white)
                binding.root.findViewById<MaterialButton>(button.id).setTextColor(defaultColor)
            }
        }
    }

    private fun setupGoBackListener() {
        binding.btnGoBackSettings.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setLayoutParams(user: UserDomainModel) {
        binding.imageUserMarker.apply {
            layoutParams.apply {
                width = user.usersMarkerSize
                height = user.usersMarkerSize
                requestLayout()
            }
        }
        binding.imageCustomMarker.apply {
            layoutParams.apply {
                width = user.staticMarkerSize
                height = user.staticMarkerSize
                requestLayout()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupUIComponents() {
        binding.cardViewLoadMap.translationX = requireContext().resources.displayMetrics.widthPixels.toFloat()
    }

    private fun loadImageIntoProfile() {
        if (currentUserSetting.profileImageUrl.isNotEmpty())
            Glide.with(requireContext())
                .load(currentUserSetting.profileImageUrl)
                .thumbnail(0.1f)
                .timeout(30_000)
                .into(binding.circleImageView)
    }

    private fun saveUserSelectedMarker(selectedMarker: Int) {
        settingsViewModel.saveUser(
            currentUserSetting.copy(
                selectedMarker = selectedMarker
            )
        )
    }

    private fun setupRadioButtonListeners() = with(binding) {
        radioButton0.setOnClickListener {
            saveUserSelectedMarker(it.tag.toString().toInt())
        }
        radioButton1.setOnClickListener {
            saveUserSelectedMarker(it.tag.toString().toInt())
        }
        radioButton2.setOnClickListener {
            saveUserSelectedMarker(it.tag.toString().toInt())
        }
        radioButton3.setOnClickListener {
            saveUserSelectedMarker(it.tag.toString().toInt())
        }
        radioButton4.setOnClickListener {
            saveUserSelectedMarker(it.tag.toString().toInt())
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val cardViewSettings = binding.cardViewSettings
        val cardViewLoadMap = binding.cardViewLoadMap
        val pixels = requireContext().resources.displayMetrics.widthPixels
        if (cardViewSettings.translationX.toInt() != 0)
            cardViewSettings.translationX = -pixels.toFloat()
        if (cardViewLoadMap.translationX.toInt() != 0)
            cardViewLoadMap.translationX = pixels.toFloat()
    }

    private fun setUpLogOutButton() {
        binding.btnSettingsPopUp.setOnClickListener {
            showUserPopupMenu(it)
        }
    }

    private fun setUpResetButton() {
        binding.btnSettingsReset.setOnClickListener {
            settingsViewModel.reset()
        }
    }

    private fun showUserPopupMenu(itemDotsView: View) {
        showBackgroundShadow()
        var onRenameClick = false
        val mainPopupMenu = PopupWindow(requireContext())
        mainPopupMenu.contentView = layoutInflater.inflate(
            R.layout.popup_user_settings,
            itemDotsView.rootView as ViewGroup,
            false
        )
        mainPopupMenu.setBackgroundDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.round_white
            )
        )
        mainPopupMenu.elevation = 18f
        mainPopupMenu.isFocusable = true
        mainPopupMenu.contentView?.findViewById<MaterialButton>(R.id.btnPopUpUserLogPut)
            ?.setOnClickListener {
                mainPopupMenu.dismiss()
                activityListener?.logOut()
            }
        mainPopupMenu.contentView?.findViewById<MaterialButton>(R.id.btnPopUpUserRename)
            ?.setOnClickListener {
                onRenameClick = true
                mainPopupMenu.dismiss()
                showInputDialog(requireContext(), onConfirm = { enteredText ->
                    settingsViewModel.saveUser(
                        currentUserSetting.copy(
                            name = enteredText
                        )
                    )
                })
            }
        mainPopupMenu.showAsDropDown(itemDotsView)
        mainPopupMenu.setOnDismissListener {
            Log.e(TAG, "onRenameClick = $onRenameClick")
            if (!onRenameClick)
                hideBackgroundShadow()
        }
    }

    private fun showInputDialog(context: Context, onConfirm: (String) -> Unit) {
        val customLayout = View.inflate(context, R.layout.rename_user_dialog, null)
        val editText = customLayout.findViewById<EditText>(R.id.description_edit_text)
        val alertDialog = AlertDialog.Builder(context)
            .setView(customLayout)
            .create()
        val confirmButton = customLayout.findViewById<AppCompatButton>(R.id.btnUserSettingsSaveUserName)
        confirmButton.isEnabled = false
        confirmButton.alpha = 0.4f
        confirmButton.setOnClickListener {
            onConfirm(editText.text.toString().trim())
            alertDialog.dismiss()
        }
        customLayout.findViewById<AppCompatButton>(R.id.btnUserSettingsCancel).setOnClickListener {
            alertDialog.dismiss()
        }
        alertDialog.window?.setBackgroundDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.round_white
            )
        )
        alertDialog.show()
        alertDialog.setOnDismissListener {
            hideBackgroundShadow()
        }
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                confirmButton.isEnabled = s.toString().trim().length >= 3
                if (confirmButton.isEnabled)
                    confirmButton.alpha = 1f
                else
                    confirmButton.alpha = 0.4f
            }

            override fun afterTextChanged(s: Editable) {
                editText.setOnEditorActionListener { _, actionId, event ->
                    if (actionId == EditorInfo.IME_ACTION_DONE || (event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER)) {
                        val imm =
                            requireContext().applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(editText.windowToken, 0)
                        editText.clearFocus()
                        val inputText = s.toString()
                        if (inputText.length > 2) {
                            confirmButton.isEnabled = true
                        } else {
                            Toast.makeText(requireContext(), R.string.name_must_be_3, Toast.LENGTH_SHORT).show()
                        }
                    }
                    true
                }
            }
        })
    }


    private fun setupSeekBarListeners() = with(binding) {
        val userMarkerSize = sbUsersMarkerSize
        val staticMarkerSize = sbStaticMarkerSize
        val timeToSendData = sbTimeToSendData

        userMarkerSize.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
            }

            override fun onStopTrackingTouch(slider: Slider) {
                settingsViewModel.saveUser(
                    currentUserSetting.copy(
                        usersMarkerSize = slider.value.toInt()
                    )
                )
                callback.onChangeMarkerSettings()
            }
        })
        staticMarkerSize.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
            }

            override fun onStopTrackingTouch(slider: Slider) {
                settingsViewModel.saveUser(
                    currentUserSetting.copy(
                        staticMarkerSize = slider.value.toInt()
                    )
                )
                callback.onChangeMarkerSettings()
            }
        })

        timeToSendData.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
            }

            override fun onStopTrackingTouch(slider: Slider) {
                settingsViewModel.saveUser(
                    currentUserSetting.copy(
                        timeToSendData = slider.value.toInt()
                    )
                )
            }
        })

    }

    private fun showBackgroundShadow() {
        val fadeInAnimation = ObjectAnimator.ofFloat(binding.shadow, "alpha", 0f, SHADOW_QUALITY)
        fadeInAnimation.duration = 200
        fadeInAnimation.start()
    }

    private fun hideBackgroundShadow() {
        val fadeOutAnimation =
            ObjectAnimator.ofFloat(binding.shadow, "alpha", binding.shadow.alpha, 0f)
        fadeOutAnimation.duration = 200
        fadeOutAnimation.start()
    }

    interface OnChangeSettings {
        fun onSelectFileClick(uri: Uri)
        fun onChangeMarkerSettings()
        fun onLoadLastGameMapClick()
        fun onLoadMapFromServerClick()
        fun onAutoLoadClick(isAutoLoad: Boolean)
        fun onClearMapClick()
    }
}