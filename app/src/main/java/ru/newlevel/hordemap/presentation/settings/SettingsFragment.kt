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
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.material.slider.Slider
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.databinding.FragmentSettingsBinding
import ru.newlevel.hordemap.domain.models.UserDomainModel
import ru.newlevel.hordemap.presentation.DisplayLocationUi
import ru.newlevel.hordemap.presentation.sign_in.GoogleAuthUiClient
import kotlin.properties.Delegates

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val settingsViewModel: SettingsViewModel by viewModel()
    private val binding: FragmentSettingsBinding by viewBinding()
    private var checkedRadioButton by Delegates.notNull<Int>()
    private var mCallback: OnChangeMarkerSettings? = null
    private var activityListener: DisplayLocationUi? = null
    fun attachCallback(callback: OnChangeMarkerSettings) {
        this.mCallback = callback
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is DisplayLocationUi) {
            activityListener = context
        } else {
            throw RuntimeException("$context must implement DisplayLocationUi.displayLocationUI")
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUIComponents()
        setupRadioButtonListeners()
        setupEditNameListener()
        setupSeekBarListeners()
        setupEditTextListener()
        setUpLogOutButton()
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
        settingsViewModel.resultData.observe(viewLifecycleOwner) { user ->
            setLayoutParams(user)
            sbTimeToSendData.value = user.timeToSendData.toFloat()
            sbStaticMarkerSize.value = user.staticMarkerSize.toFloat()
            sbUsersMarkerSize.value = user.usersMarkerSize.toFloat()
            tvTimeToSendData.text = " ${user.timeToSendData}${getString(R.string.sec)}"
            checkedRadioButton = user.selectedMarker
            for (i in 0 until radioGroup.childCount) {
                val radioButton = radioGroup.getChildAt(i) as? RadioButton
                radioButton?.alpha =
                    if (radioButton?.tag == checkedRadioButton.toString()) 1.0f else 0.3f
            }
        }
        editName.setText(settingsViewModel.getUserSettings().name)
    }

    private fun setupRadioButtonListeners() = with(binding) {
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val checkedTag = selectedRadioButton.tag.toString()
            settingsViewModel.saveUser(
                UserEntityProvider.userEntity?.copy(
                    selectedMarker = checkedTag.toInt()
                )!!
            )
        }
    }
    private fun setUpLogOutButton(){
        binding.btnSettingsLogOut.setOnClickListener {
            lifecycleScope.launch {
                GoogleAuthUiClient(
                    context = requireContext(),
                    oneTapClient = Identity.getSignInClient(requireContext())
                ).signOut()
                activityListener?.logOut()
            }
        }
    }

    private fun setupEditNameListener() {
        binding.editName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                binding.editName.setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        val inputText = s.toString()
                        if (inputText.length > 2) {
                            UserEntityProvider.userEntity?.copy(
                                name = inputText
                            )?.let {
                                settingsViewModel.saveUser(it)
                            }
                        } else {
                            Toast.makeText(requireContext(), R.string.name_must_be_3, Toast.LENGTH_SHORT).show()
                        }
                    }
                    false
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
                UserEntityProvider.userEntity?.copy(
                    usersMarkerSize = userMarkerSize.value.toInt()
                )?.let {
                    settingsViewModel.saveUser(it)
                }
                mCallback?.onChangeMarkerSettings()
            }
        })
        staticMarkerSize.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
            }

            override fun onStopTrackingTouch(slider: Slider) {
                UserEntityProvider.userEntity?.copy(
                    staticMarkerSize = staticMarkerSize.value.toInt()
                )?.let {
                    settingsViewModel.saveUser(it)
                }
                mCallback?.onChangeMarkerSettings()
            }
        })

        timeToSendData.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
            }

            override fun onStopTrackingTouch(slider: Slider) {
                UserEntityProvider.userEntity?.copy(
                    timeToSendData = timeToSendData.value.toInt()
                )?.let {
                    settingsViewModel.saveUser(it)
                }
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
                    binding.editName.clearFocus()
                    return true
                }
                return false
            }
        })
    }

    interface OnChangeMarkerSettings {
        fun onChangeMarkerSettings()
    }
}