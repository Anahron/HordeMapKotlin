package ru.newlevel.hordemap

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.newlevel.hordemap.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment(R.layout.fragment_setting) {

    private val binding by viewBinding<FragmentSettingsBinding>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)

    }
}