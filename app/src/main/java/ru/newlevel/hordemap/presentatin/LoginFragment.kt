package ru.newlevel.hordemap.presentatin

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.RadioButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import ru.newlevel.hordemap.databinding.FragmentLoginBinding
import ru.newlevel.hordemap.domain.models.UserDomainModel


const val NAME_MUST_BE = "Имя должно быть длиннее 3-х букв"
const val SEC = " сек"

class LoginFragment(private val loginVM: LoginViewModel) : Fragment() {
    private lateinit var binding: FragmentLoginBinding


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

        loginVM.getUser()

        val userName = binding.editName
        val timeToSendData = binding.sbTimeToSendData
        val staticMarkerSize = binding.sbStaticMarkerSize
        val userMarkerSize = binding.sbUsersMarkerSize
        var checkedRadioButton = 0
        val tvTimeToSend = binding.tvTimeToSendData

        val layoutParamsUser = binding.imageUserMarker.layoutParams

        val layoutParamsStatic = binding.imageCustomMarker.layoutParams

        loginVM.resultData.observe(viewLifecycleOwner) { user ->
            timeToSendData.progress = user.timeToSendData
            staticMarkerSize.progress = user.staticMarkerSize
            userMarkerSize.progress = user.usersMarkerSize
            checkedRadioButton = user.selectedMarker
            layoutParamsStatic.width = user.staticMarkerSize
            layoutParamsStatic.height = user.staticMarkerSize
            layoutParamsUser.width = user.usersMarkerSize
            layoutParamsUser.height = user.usersMarkerSize
            binding.imageUserMarker.layoutParams = layoutParamsUser
            binding.imageCustomMarker.layoutParams = layoutParamsStatic
            if (user.name.isNotEmpty())
                userName.setText(user.name)
            tvTimeToSend.text = user.timeToSendData.toString() + SEC
            for (i in 0 until binding.radioGroup.childCount) {
                val radioButton = binding.radioGroup.getChildAt(i) as? RadioButton
                if (radioButton != null) {
                    radioButton.alpha =
                        if (radioButton.tag == checkedRadioButton.toString()) 1.0f else 0.3f
                }

            }
        }

        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedRadioButton = binding.radioGroup.findViewById<RadioButton>(checkedId)
            checkedRadioButton = Integer.valueOf(selectedRadioButton.tag.toString())
            for (i in 0 until binding.radioGroup.childCount) {
                val radioButton = binding.radioGroup.getChildAt(i) as? RadioButton
                radioButton?.alpha = if (radioButton == selectedRadioButton) 1.0f else 0.3f
            }
        }

        binding.btnSave.setOnClickListener {
            if (userName.text.toString().length < 3)
                Toast.makeText(
                    requireContext(),
                    NAME_MUST_BE,
                    Toast.LENGTH_LONG
                ).show()
            else {
                loginVM.saveUser(
                    UserDomainModel(
                        userName.text.toString(),
                        timeToSendData.progress,
                        userMarkerSize.progress,
                        staticMarkerSize.progress,
                        checkedRadioButton
                    )
                )
                loginVM.loginSuccess(userName.text.toString())
            }
        }

        binding.btnReset.setOnClickListener {
            loginVM.reset()
            loginVM.getUser()
        }

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

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
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

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        timeToSendData.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                tvTimeToSend.text = progress.toString() + SEC
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        binding.editName.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_DONE || event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                    // Скрыть клавиатуру
                    val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow( binding.editName.windowToken, 0)
                    return true
                }
                return false
            }
        })
    }
}
