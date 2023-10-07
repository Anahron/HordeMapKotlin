package ru.newlevel.hordemap.presentatin

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.set
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ru.newlevel.hordemap.databinding.FragmentLoginBinding
import ru.newlevel.hordemap.domain.models.UserDomainModel

class LoginFragment(private val loginVM: LoginVM): Fragment() {
    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val user: UserDomainModel = loginVM.getUser()

        binding.sbTimeToSendData.id = user.timeToSendData
        binding.sbStaticMarkerSize.id = user.staticMarkerSize
        binding.sbUsersMarkerSize.id = user.usersMarkerSize

        if (user.name.isNotEmpty()) {
            binding.editName.setText(user.name)
        } else {
            binding.editName.setText("Аноним")
        }
        binding.tvTimeToSendData.text = user.timeToSendData.toString() + " сек"
    }
}