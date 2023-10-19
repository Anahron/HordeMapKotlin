package ru.newlevel.hordemap.presentatin.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.RadioButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.slider.Slider
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.app.makeLongToast
import ru.newlevel.hordemap.app.mapUserDataToDomain
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.databinding.SettingsFragmentBinding
import ru.newlevel.hordemap.domain.models.UserDomainModel
import ru.newlevel.hordemap.presentatin.viewmodels.MapViewModel
import ru.newlevel.hordemap.presentatin.viewmodels.SettingsViewModel
import kotlin.properties.Delegates


class SettingsFragment(private val mapViewModel: MapViewModel, private val settingsViewModel: SettingsViewModel): Fragment() {
    private lateinit var binding: SettingsFragmentBinding

    private lateinit var layoutParamsUser: ViewGroup.LayoutParams
    private lateinit var layoutParamsStatic: ViewGroup.LayoutParams
    private var checkedRadioButton by Delegates.notNull<Int>()
    private var user: UserDomainModel = UserEntityProvider.userEntity?.let { mapUserDataToDomain(it) }!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = SettingsFragmentBinding.inflate(inflater, container, false)
        layoutParamsUser = binding.imageUserMarker.layoutParams
        layoutParamsStatic = binding.imageCustomMarker.layoutParams

        setupUIComponents(layoutParamsUser, layoutParamsStatic)
        setupRadioButtonListeners()
        setupEditNameListener()
        setupSeekBarListeners()
        setupEditTextListener()

        return binding.root
    }

    private fun setupUIComponents(
        layoutParamsUser: ViewGroup.LayoutParams,
        layoutParamsStatic: ViewGroup.LayoutParams
    ) {
        binding.editName.setText(user.name)
        binding.sbTimeToSendData.value = user.timeToSendData.toFloat()
        binding.sbStaticMarkerSize.value = user.staticMarkerSize.toFloat()
        binding.sbUsersMarkerSize.value = user.usersMarkerSize.toFloat()
        layoutParamsStatic.width = user.staticMarkerSize
        layoutParamsStatic.height = user.staticMarkerSize
        layoutParamsUser.width = user.usersMarkerSize
        layoutParamsUser.height = user.usersMarkerSize
        binding.imageUserMarker.layoutParams = layoutParamsUser
        binding.imageCustomMarker.layoutParams = layoutParamsStatic
        binding.tvTimeToSendData.text = "${user.timeToSendData}${getString(R.string.sec)}"
        checkedRadioButton = user.selectedMarker
        for (i in 0 until binding.radioGroup.childCount) {
            val radioButton = binding.radioGroup.getChildAt(i) as? RadioButton
            radioButton?.alpha =
                if (radioButton?.tag == checkedRadioButton.toString()) 1.0f else 0.3f
        }

        settingsViewModel.resultData.observe(viewLifecycleOwner) { user ->
            layoutParamsStatic.width = user.staticMarkerSize
            layoutParamsStatic.height = user.staticMarkerSize
            layoutParamsUser.width = user.usersMarkerSize
            layoutParamsUser.height = user.usersMarkerSize
            binding.imageUserMarker.layoutParams = layoutParamsUser
            binding.imageCustomMarker.layoutParams = layoutParamsStatic
            binding.tvTimeToSendData.text = "${user.timeToSendData}${getString(R.string.sec)}"
        }
    }

    private fun setupRadioButtonListeners() {
        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedRadioButton = binding.radioGroup.findViewById<RadioButton>(checkedId)
            val checkedTag = selectedRadioButton.tag.toString()
            checkedRadioButton = checkedTag.toInt()
            user.selectedMarker =  checkedTag.toInt()
            settingsViewModel.saveUser(user)
            for (i in 0 until binding.radioGroup.childCount) {
                val radioButton = binding.radioGroup.getChildAt(i) as? RadioButton
                radioButton?.alpha = if (radioButton?.tag == checkedTag) 1.0f else 0.3f
            }
        }
    }

    private fun setupEditNameListener() {
        binding.editName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // Этот метод вызывается до изменения текста
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // Этот метод вызывается во время изменения текста
            }

            override fun afterTextChanged(s: Editable) {

                val inputText = s.toString()
                if (inputText.length>2) {
                    user.name = inputText
                    settingsViewModel.saveUser(user)
                } else{
                    makeLongToast("Имя должно быть длиннее 3х символов", requireContext())
                    binding.editName.setText(user.name)
                }
            }
        })
    }

    private fun setupSeekBarListeners() {
        val userMarkerSize = binding.sbUsersMarkerSize
        val staticMarkerSize = binding.sbStaticMarkerSize
        val timeToSendData = binding.sbTimeToSendData

        userMarkerSize.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
            }

            override fun onStopTrackingTouch(slider: Slider) {
                user.usersMarkerSize = userMarkerSize.value.toInt()
                settingsViewModel.saveUser(user)
                mapViewModel.reCreateMarkers()
            }
        })
        staticMarkerSize.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
            }

            override fun onStopTrackingTouch(slider: Slider) {
                user.staticMarkerSize = staticMarkerSize.value.toInt()
                settingsViewModel.saveUser(user)
                mapViewModel.reCreateMarkers()
            }
        })

        timeToSendData.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
            }

            override fun onStopTrackingTouch(slider: Slider) {
                user.timeToSendData = timeToSendData.value.toInt()
                binding.tvTimeToSendData.text =
                    "$timeToSendData.value.toInt() ${getString(R.string.sec)}"
                settingsViewModel.saveUser(user)
            }
        })
    }

    private fun setupEditTextListener() {
        binding.editName.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_DONE || (event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Скрыть клавиатуру
                    val imm =
                        requireContext().applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(binding.editName.windowToken, 0)
                    return true
                }
                return false
            }
        })
    }

}