package ru.newlevel.hordemap.presentatin.fragments

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.RadioButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.databinding.FragmentLoginBinding
import ru.newlevel.hordemap.domain.models.UserDomainModel
import ru.newlevel.hordemap.presentatin.viewmodels.LoginViewModel
import kotlin.properties.Delegates

class LoginFragment(private val loginVM: LoginViewModel) : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private lateinit var layoutParamsUser: LayoutParams
    private lateinit var layoutParamsStatic: LayoutParams
    private var checkedRadioButton by Delegates.notNull<Int>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        layoutParamsUser = binding.imageUserMarker.layoutParams
        layoutParamsStatic = binding.imageCustomMarker.layoutParams

        setupUIComponents(layoutParamsUser, layoutParamsStatic)
        setupRadioButtonListeners()
        setupSaveButton()
        setupResetButton()
        setupSeekBarListeners()
        setupEditTextListener()
    }

    private fun setupUIComponents(
        layoutParamsUser: LayoutParams,
        layoutParamsStatic: LayoutParams
    ) {
        loginVM.resultData.observe(viewLifecycleOwner) { user ->
            val userName = binding.editName
            val timeToSendData = binding.sbTimeToSendData
            val staticMarkerSize = binding.sbStaticMarkerSize
            val userMarkerSize = binding.sbUsersMarkerSize
            val deviceId = user.deviceID

            timeToSendData.progress = user.timeToSendData
            staticMarkerSize.progress = user.staticMarkerSize
            userMarkerSize.progress = user.usersMarkerSize

            layoutParamsStatic.width = user.staticMarkerSize
            layoutParamsStatic.height = user.staticMarkerSize
            layoutParamsUser.width = user.usersMarkerSize
            layoutParamsUser.height = user.usersMarkerSize

            binding.imageUserMarker.layoutParams = layoutParamsUser
            binding.imageCustomMarker.layoutParams = layoutParamsStatic
            binding.tvDeviceId.text = deviceId
            checkedRadioButton = user.selectedMarker

            if (user.name.isNotEmpty())
                userName.setText(user.name)

            binding.tvTimeToSendData.text = "${user.timeToSendData}${getString(R.string.sec)}"

            for (i in 0 until binding.radioGroup.childCount) {
                val radioButton = binding.radioGroup.getChildAt(i) as? RadioButton
                radioButton?.alpha = if (radioButton?.tag == checkedRadioButton.toString()) 1.0f else 0.3f
            }
        }
        loginVM.getUser()
    }

    private fun setupRadioButtonListeners() {
        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedRadioButton = binding.radioGroup.findViewById<RadioButton>(checkedId)
            val checkedTag = selectedRadioButton.tag.toString()
            checkedRadioButton = checkedTag.toInt()
            for (i in 0 until binding.radioGroup.childCount) {
                val radioButton = binding.radioGroup.getChildAt(i) as? RadioButton
                radioButton?.alpha = if (radioButton?.tag == checkedTag) 1.0f else 0.3f
            }
        }
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            val userName = binding.editName
            val timeToSendData = binding.sbTimeToSendData
            val userMarkerSize = binding.sbUsersMarkerSize
            val staticMarkerSize = binding.sbStaticMarkerSize
            val deviceId = binding.tvDeviceId.text.toString()

            if (userName.text.toString().length < 3)
                Toast.makeText(requireContext().applicationContext, getString(R.string.name_must_be), Toast.LENGTH_LONG).show()
            else {
                loginVM.saveUser(
                    UserDomainModel(
                        userName.text.toString(),
                        timeToSendData.progress,
                        userMarkerSize.progress,
                        staticMarkerSize.progress,
                        checkedRadioButton,
                        deviceId
                    )
                )
            }
        }
    }

    private fun setupResetButton() {
        binding.btnReset.setOnClickListener {
            loginVM.reset()
            loginVM.getUser()
        }
    }

    private fun setupSeekBarListeners() {
        val userMarkerSize = binding.sbUsersMarkerSize
        val staticMarkerSize = binding.sbStaticMarkerSize
        val timeToSendData = binding.sbTimeToSendData

        userMarkerSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                layoutParamsUser.width = progress
                layoutParamsUser.height = progress
                binding.imageUserMarker.layoutParams = layoutParamsUser
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        staticMarkerSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                layoutParamsStatic.height = progress
                layoutParamsStatic.width = progress
                binding.imageCustomMarker.layoutParams = layoutParamsStatic
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        timeToSendData.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
               binding.tvTimeToSendData.text = "$progress ${getString(R.string.sec)}"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupEditTextListener() {
        binding.editName.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_DONE || (event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Скрыть клавиатуру
                    val imm = requireContext().applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(binding.editName.windowToken, 0)
                    return true
                }
                return false
            }
        })
    }
}