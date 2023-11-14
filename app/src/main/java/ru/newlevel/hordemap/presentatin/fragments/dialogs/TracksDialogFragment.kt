package ru.newlevel.hordemap.presentatin.fragments.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import by.kirich1409.viewbindingdelegate.viewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.databinding.FragmentTracksDialogBinding
import ru.newlevel.hordemap.presentatin.viewmodels.LocationUpdateViewModel
import com.google.android.gms.maps.model.LatLng
import ru.newlevel.hordemap.domain.models.TrackItemDomainModel

class TracksDialogFragment(
    private val locationUpdateViewModel: LocationUpdateViewModel,
    private val onTrackItemClick: OnTrackItemClick
) :
    DialogFragment(R.layout.fragment_tracks_dialog) {

    private val binding: FragmentTracksDialogBinding by viewBinding()
    private lateinit var currentTrack: TrackItemDomainModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
        initCurrentTrack()

        binding.itemCurrentTrack.setOnClickListener {
            dialog?.dismiss()
            onTrackItemClick.onTrackItemClick(currentTrack.locations)
        }

    }

    private fun initCurrentTrack() = with(binding) {
        CoroutineScope(Dispatchers.IO).launch {
            currentTrack =
                locationUpdateViewModel.getCurrentSessionLocations(UserEntityProvider.sessionId.toString())
            withContext(Dispatchers.Main) {
                tvTrackDate.text = currentTrack.date
                tvTrackDuration.text = currentTrack.duration
                tvTrackDistance.text = currentTrack.distance
            }
        }
    }

    interface OnTrackItemClick {
        fun onTrackItemClick(listLatLng: List<LatLng>)
    }

}