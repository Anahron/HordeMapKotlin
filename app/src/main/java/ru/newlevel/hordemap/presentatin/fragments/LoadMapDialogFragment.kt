package ru.newlevel.hordemap.presentatin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import ru.newlevel.hordemap.databinding.LoadMapDialogBinding
import ru.newlevel.hordemap.presentatin.viewmodels.LoginViewModel
import ru.newlevel.hordemap.presentatin.viewmodels.MapViewModel

class LoadMapDialogFragment(
    private val mapViewModel: MapViewModel,
    private val loginViewModel: LoginViewModel,
    private val mapFragment: MapFragment
) : DialogFragment() {
    private lateinit var binding: LoadMapDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = LoadMapDialogBinding.inflate(inflater, container, false)
        var boolean = mapViewModel.isAutoLoadMap.value
        if (boolean != null) {
            binding.checkBox.isChecked = boolean
        }

        binding.checkBox.setOnClickListener {
            boolean = binding.checkBox.isChecked
        }

        binding.btnFromServer.setOnClickListener {
            lifecycleScope.launch {
                Toast.makeText(
                    requireContext().applicationContext,
                    "Загрузка началась, подождите...",
                    Toast.LENGTH_LONG
                ).show()
                if (!mapViewModel.loadMapFromServer(requireContext().applicationContext)) {
                    Toast.makeText(
                        requireContext().applicationContext,
                        "Неудачно",
                        Toast.LENGTH_LONG
                    ).show()
                    dialog?.dismiss()
                } else {
                    Toast.makeText(
                        requireContext().applicationContext, "Карта загружена", Toast.LENGTH_LONG
                    ).show()
                    dialog?.dismiss()
                }
            }
            dialog?.hide()
        }

        binding.btnFromFiles.setOnClickListener {
            mapViewModel.loadGameMapFromFiles(mapFragment)
            dialog?.dismiss()
        }

        binding.btnLastSaved.setOnClickListener {
            lifecycleScope.launch {
                if (!mapViewModel.loadLastGameMap()) Toast.makeText(
                    requireContext().applicationContext,
                    "Сохраненная карта отсутствует",
                    Toast.LENGTH_LONG
                ).show()
                dialog?.dismiss()
            }
        }
        dialog?.setOnDismissListener {
            boolean?.let { it1 -> mapViewModel.setIsAutoLoadMap(it1) }
            boolean?.let { it1 -> loginViewModel.saveAutoLoad(it1) }
        }

        return binding.root
    }
}