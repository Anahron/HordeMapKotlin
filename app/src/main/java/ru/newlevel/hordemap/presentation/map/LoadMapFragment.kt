package ru.newlevel.hordemap.presentation.map

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.app.GPX_EXTENSION
import ru.newlevel.hordemap.app.KMZ_EXTENSION
import ru.newlevel.hordemap.app.SelectFilesContract
import ru.newlevel.hordemap.app.getFileNameFromUri
import ru.newlevel.hordemap.app.makeLongToast
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.databinding.LoadMapDialogBinding
import ru.newlevel.hordemap.presentation.settings.SettingsViewModel

class LoadMapFragment(
    private val mapViewModel: MapViewModel,
) : Fragment(R.layout.load_map_dialog) {

    private val settingsViewModel: SettingsViewModel by viewModel()
    private val binding: LoadMapDialogBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)
        var boolean = UserEntityProvider.userEntity?.autoLoad
        if (boolean != null) {
            checkBox.isChecked = boolean
        }

        checkBox.setOnClickListener {
            boolean = checkBox.isChecked
            mapViewModel.setIsAutoLoadMap(boolean!!)
            settingsViewModel.saveAutoLoad(boolean!!)
        }

        btnFromServer.setOnClickListener {
            lifecycleScope.launch {
                makeLongToast("Загрузка началась, подождите...", requireContext())
                val uri = mapViewModel.loadMapFromServer(requireContext().applicationContext)
                if (uri != null) {
                    mapViewModel.setUriForMap(uri)
                } else {
                    makeLongToast("Скачивание завершилось неудачно", requireContext())
                }
            }
        }

        val activityLauncher = registerForActivityResult(SelectFilesContract()) { result ->
            result?.let {
                lifecycleScope.launch {
                    val mimeType = requireContext().getFileNameFromUri(it)
                    Log.e("AAA",  "registerForActivityResult(SelectFilesContract())  " + mimeType.toString())
                    when {
                        mimeType?.endsWith(KMZ_EXTENSION) == true -> {
                            mapViewModel.setUriForMap(mapViewModel.saveGameMapToFile(it, KMZ_EXTENSION))
                        }

                        mimeType?.endsWith(GPX_EXTENSION) == true -> {
                            mapViewModel.setUriForMap(mapViewModel.saveGameMapToFile(it, GPX_EXTENSION))
                        }

                        else -> Toast.makeText(
                            requireContext().applicationContext,
                            "Неверный формат файла",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        btnFromFiles.setOnClickListener {
            activityLauncher.launch("application/*")
        }

        btnLastSaved.setOnClickListener {
            lifecycleScope.launch {
                if (!mapViewModel.loadLastGameMap())
                    Toast.makeText(
                        requireContext().applicationContext,
                        "Сохраненная карта отсутствует",
                        Toast.LENGTH_LONG
                    ).show()
            }
        }
        btnCleanMap.setOnClickListener {
            boolean = false
            checkBox.isChecked = false
            mapViewModel.setIsAutoLoadMap(boolean!!)
            settingsViewModel.saveAutoLoad(boolean!!)
            mapViewModel.cleanUriForMap()
        }
    }
}