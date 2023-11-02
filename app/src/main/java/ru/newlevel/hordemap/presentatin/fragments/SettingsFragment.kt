package ru.newlevel.hordemap.presentatin.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.RadioButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
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


class SettingsFragment(
    private val mapViewModel: MapViewModel,
    private val settingsViewModel: SettingsViewModel
) : Fragment(R.layout.settings_fragment) {

    private val binding: SettingsFragmentBinding by viewBinding()
    private var checkedRadioButton by Delegates.notNull<Int>()
    private var user: UserDomainModel = UserEntityProvider.userEntity?.let { mapUserDataToDomain(it) }!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUIComponents()
        setupRadioButtonListeners()
        setupEditNameListener()
        setupSeekBarListeners()
        setupEditTextListener()
    }

    private fun setLayoutParams(user: UserDomainModel) {
        binding.imageUserMarker.apply {
            layoutParams.apply {
                width = user.usersMarkerSize
                height = user.usersMarkerSize
                requestLayout() }
        }
        binding.imageCustomMarker.apply {
            layoutParams.apply {
                width = user.staticMarkerSize
                height = user.staticMarkerSize
                requestLayout()
            }
        }
    }

    private fun setupUIComponents() = with(binding) {
        editName.setText(user.name)
        sbTimeToSendData.value = user.timeToSendData.toFloat()
        sbStaticMarkerSize.value = user.staticMarkerSize.toFloat()
        sbUsersMarkerSize.value = user.usersMarkerSize.toFloat()
        tvTimeToSendData.text = "${user.timeToSendData}${getString(R.string.sec)}"
        setLayoutParams(user)
        checkedRadioButton = user.selectedMarker
        for (i in 0 until radioGroup.childCount) {
            val radioButton = radioGroup.getChildAt(i) as? RadioButton
            radioButton?.alpha =
                if (radioButton?.tag == checkedRadioButton.toString()) 1.0f else 0.3f
        }

        settingsViewModel.resultData.observe(viewLifecycleOwner) { user ->
            setLayoutParams(user)
            tvTimeToSendData.text = "${user.timeToSendData}${getString(R.string.sec)}"
        }
    }

    private fun setupRadioButtonListeners() = with(binding) {
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val checkedTag = selectedRadioButton.tag.toString()
            checkedRadioButton = checkedTag.toInt()
            user.selectedMarker = checkedTag.toInt()
            settingsViewModel.saveUser(user)
            for (i in 0 until radioGroup.childCount) {
                val radioButton = radioGroup.getChildAt(i) as? RadioButton
                radioButton?.alpha = if (radioButton?.tag == checkedTag) 1.0f else 0.3f
            }
        }
    }

    private fun setupEditNameListener() {
        binding.editName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                val inputText = s.toString()
                if (inputText.length > 2) {
                    user.name = inputText
                    settingsViewModel.saveUser(user)
                } else {
                    makeLongToast("Имя должно быть длиннее 3х символов", requireContext())
                    binding.editName.setText(user.name)
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
                tvTimeToSendData.text =
                    "$timeToSendData.value.toInt() ${getString(R.string.sec)}"
                settingsViewModel.saveUser(user)
            }
        })
    }

    private fun setupEditTextListener() = with(binding) {
        editName.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_DONE || (event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER)) {
                    val imm =
                        requireContext().applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(editName.windowToken, 0)
                    return true
                }
                return false
            }
        })
    }

}