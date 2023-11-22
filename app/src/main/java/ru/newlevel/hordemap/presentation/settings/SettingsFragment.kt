package ru.newlevel.hordemap.presentation.settings

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.slider.Slider
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.databinding.FragmentSettingsBinding
import ru.newlevel.hordemap.domain.models.UserDomainModel
import kotlin.properties.Delegates

class SettingsFragment: Fragment(R.layout.fragment_settings) {

    private val settingsViewModel: SettingsViewModel by viewModel()
    private val binding: FragmentSettingsBinding by viewBinding()
    private var checkedRadioButton by Delegates.notNull<Int>()
    private var user: UserDomainModel = UserEntityProvider.userEntity!!
    private var mCallback: OnChangeMarkerSettings? = null
    fun attachCallback(callback: OnChangeMarkerSettings) {
        this.mCallback = callback
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUIComponents()
        setupRadioButtonListeners()
        setupEditNameListener()
        setupSeekBarListeners()
        setupEditTextListener()
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        binding.editName.setText(user.name)
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
    private fun setupUIComponents() = with(binding) {
        editName.setText(user.name)
        sbTimeToSendData.value = user.timeToSendData.toFloat()
        sbStaticMarkerSize.value = user.staticMarkerSize.toFloat()
        sbUsersMarkerSize.value = user.usersMarkerSize.toFloat()
        val timeToSend = " ${user.timeToSendData}${getString(R.string.sec)}"
        tvTimeToSendData.text = timeToSend
        setLayoutParams(user)
        checkedRadioButton = user.selectedMarker
        for (i in 0 until radioGroup.childCount) {
            val radioButton = radioGroup.getChildAt(i) as? RadioButton
            radioButton?.alpha =
                if (radioButton?.tag == checkedRadioButton.toString()) 1.0f else 0.3f
        }

        settingsViewModel.resultData.observe(viewLifecycleOwner) { user ->
            setLayoutParams(user)
            tvTimeToSendData.text = " ${user.timeToSendData}${getString(R.string.sec)}"
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
        binding.editName.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                val textSize = binding.editName.text?.length
                textSize?.let {
                    if (it < 3)
                        binding.editName.setText(user.name)
                }
            }
        }
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
                    Toast.makeText(requireContext(), R.string.name_must_be_3, Toast.LENGTH_SHORT)
                        .show()
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
                mCallback?.onChangeMarkerSettings()
            }
        })
        staticMarkerSize.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
            }

            override fun onStopTrackingTouch(slider: Slider) {
                user.staticMarkerSize = staticMarkerSize.value.toInt()
                settingsViewModel.saveUser(user)
                mCallback?.onChangeMarkerSettings()
            }
        })

        timeToSendData.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
            }

            override fun onStopTrackingTouch(slider: Slider) {
                user.timeToSendData = timeToSendData.value.toInt()
                settingsViewModel.saveUser(user)
            }
        })
    }

    private fun setupEditTextListener() = with(binding) {
        editName.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_DONE || (event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER)) {
                    val imm = requireContext().applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(editName.windowToken, 0)
                    binding.editName.clearFocus()
                    return true
                }
                return false
            }
        })
    }
    interface OnChangeMarkerSettings{
        fun onChangeMarkerSettings()
    }
}