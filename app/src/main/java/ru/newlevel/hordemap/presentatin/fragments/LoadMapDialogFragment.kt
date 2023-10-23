package ru.newlevel.hordemap.presentatin.fragments

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.app.SelectFilesContract
import ru.newlevel.hordemap.app.makeLongToast
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.databinding.LoadMapDialogBinding
import ru.newlevel.hordemap.presentatin.viewmodels.MapViewModel
import ru.newlevel.hordemap.presentatin.viewmodels.SettingsViewModel

class LoadMapDialogFragment(
    private val mapViewModel: MapViewModel, private val settingsViewModel: SettingsViewModel,
) : Fragment(R.layout.load_map_dialog) {
    private val binding: LoadMapDialogBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var boolean = UserEntityProvider.userEntity?.autoLoad
        if (boolean != null) {
            binding.checkBox.isChecked = boolean
        }

        binding.checkBox.setOnClickListener {
            boolean = binding.checkBox.isChecked
            mapViewModel.setIsAutoLoadMap(boolean!!)
            settingsViewModel.saveAutoLoad(boolean!!)
        }

        binding.btnFromServer.setOnClickListener {
            lifecycleScope.launch {
                makeLongToast("Загрузка началась, подождите...", requireContext())
                var uri: Uri?
                withContext(Dispatchers.IO) {
                    uri = mapViewModel.loadMapFromServer(requireContext().applicationContext)
                }
                if (uri != null)
                    mapViewModel.setUriForMap(uri!!)
                else {
                    makeLongToast("Скачивание завершилось неудачно", requireContext())
                }
            }
        }

        val activityLauncher = registerForActivityResult(SelectFilesContract()) { result ->
            lifecycleScope.launch {
                if (result != null) {
                    mapViewModel.saveGameMapToFile(result)
                    mapViewModel.setUriForMap(result)
                }
            }
        }
        binding.btnFromFiles.setOnClickListener {
            activityLauncher.launch("application/vnd.google-earth.kmz")
        }

        binding.btnLastSaved.setOnClickListener {
            lifecycleScope.launch {
                if (!mapViewModel.loadLastGameMap()) Toast.makeText(
                    requireContext().applicationContext,
                    "Сохраненная карта отсутствует",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        binding.btnCleanMap.setOnClickListener {
            mapViewModel.cleanUriForMap()
        }
    }
}