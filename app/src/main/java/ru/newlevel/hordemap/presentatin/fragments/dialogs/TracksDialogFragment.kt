package ru.newlevel.hordemap.presentatin.fragments.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.databinding.FragmentTracksDialogBinding
import ru.newlevel.hordemap.presentatin.viewmodels.LocationUpdateViewModel
import com.google.android.gms.maps.model.LatLng
import ru.newlevel.hordemap.domain.models.TrackItemDomainModel
import ru.newlevel.hordemap.presentatin.adapters.TracksAdapter

class TracksDialogFragment(
    private val locationUpdateViewModel: LocationUpdateViewModel,
    private val onTrackItemClick: OnTrackItemClick
) :
    DialogFragment(R.layout.fragment_tracks_dialog) {

    private val binding: FragmentTracksDialogBinding by viewBinding()
    private lateinit var currentTrack: TrackItemDomainModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var trackAdapter: TracksAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )

        locationUpdateViewModel.getCurrentSessionLocations(UserEntityProvider.sessionId.toString())
        locationUpdateViewModel.getAllSessionsLocations()
        trackAdapter = TracksAdapter()
        recyclerView = rvTracks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = trackAdapter
        }


        binding.itemCurrentTrack.setOnClickListener {
            dialog?.dismiss()
            onTrackItemClick.onTrackItemClick(currentTrack.locations)
        }

        locationUpdateViewModel.trackItemCurrent.observe(this@TracksDialogFragment) {
            if (it != null) {
                currentTrack = it
                tvTrackDate.text = it.date
                tvTrackDuration.text = it.duration
                tvTrackDistance.text = it.distance
            }
        }

        locationUpdateViewModel.trackItemAll.observe(this@TracksDialogFragment) {
            trackAdapter.setMessages(it as ArrayList<TrackItemDomainModel>)
        }
        trackAdapter.attachCallback(object : TracksAdapter.TracksAdapterCallback {
            override fun onTrackRvItemClick(listLatLng: List<LatLng>) {
                onTrackItemClick.onTrackItemClick(listLatLng)
                dialog?.dismiss()
            }
        })
    }

    interface OnTrackItemClick {
        fun onTrackItemClick(listLatLng: List<LatLng>)
    }
}