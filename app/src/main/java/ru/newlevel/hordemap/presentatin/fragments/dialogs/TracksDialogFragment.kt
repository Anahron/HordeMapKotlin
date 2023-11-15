package ru.newlevel.hordemap.presentatin.fragments.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MenuRes
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.MenuPopupWindow
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.view.iterator
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.databinding.FragmentTracksDialogBinding
import ru.newlevel.hordemap.presentatin.viewmodels.LocationUpdateViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
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
    private lateinit var backgroundView: View

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

        backgroundView  = View(requireContext())
        backgroundView.setBackgroundColor(Color.BLACK)
        backgroundView.alpha = 0.15f


        setButtonsColors(R.id.btnDate)
        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                setButtonsColors(checkedId)
            }
        }

        binding.ibPopup.setOnClickListener {
            //  showMenu(it)
        }

        btnGoBack.setOnClickListener {
            dialog?.dismiss()
        }

        itemCurrentTrack.setOnClickListener {
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
            if (it != null)
                trackAdapter.setMessages(it)
        }
        trackAdapter.attachCallback(object : TracksAdapter.TracksAdapterCallback {
            override fun onTrackRvItemClick(listLatLng: List<LatLng>) {
                onTrackItemClick.onTrackItemClick(listLatLng)
                dialog?.dismiss()
            }

            override fun deleteTrack(sessionId: String) {
                locationUpdateViewModel.deleteSessionLocations(sessionId = sessionId)
            }

            override fun renameTrack() {

            }

            override fun shareTrack() {

            }

            override fun menuActive(isActive: Boolean) {
                if (isActive) {
                    dialog?.window?.addContentView(backgroundView,ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    ))
                }  else
                    backgroundView.let {
                        (it.parent as? ViewGroup)?.removeView(it)
                    }
            }
        })
    }

    private fun setButtonsColors(checkedId: Int) {
        val defaultColor = ContextCompat.getColor(requireContext(), R.color.slate_800)
        val selectedColor = ContextCompat.getColor(requireContext(), R.color.white)
        when (checkedId) {
            R.id.btnDate -> {
                locationUpdateViewModel.sortByDate()
                setButtonBackgroundTint(R.id.btnDate)
                updateButtonTextColor(R.id.btnDate, selectedColor)
                updateButtonTextColor(R.id.btnDuration, defaultColor)
                updateButtonTextColor(R.id.btnDistance, defaultColor)
            }

            R.id.btnDuration -> {
                locationUpdateViewModel.sortByDuration()
                setButtonBackgroundTint(R.id.btnDuration)
                updateButtonTextColor(R.id.btnDate, defaultColor)
                updateButtonTextColor(R.id.btnDuration, selectedColor)
                updateButtonTextColor(R.id.btnDistance, defaultColor)
            }

            R.id.btnDistance -> {
                locationUpdateViewModel.sortByDistance()
                setButtonBackgroundTint(R.id.btnDistance)
                updateButtonTextColor(R.id.btnDate, defaultColor)
                updateButtonTextColor(R.id.btnDuration, defaultColor)
                updateButtonTextColor(R.id.btnDistance, selectedColor)
            }
        }
    }

    private fun updateButtonTextColor(buttonId: Int, color: Int) {
        val button = binding.root.findViewById<MaterialButton>(buttonId)
        button.setTextColor(color)
    }

    private fun setButtonBackgroundTint(
        buttonId: Int,
    ) {
        for (button in binding.toggleGroup) {
            if (button.id == buttonId) {
                button.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.main_green_dark)
            } else
                button.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.white)
        }
    }

    interface OnTrackItemClick {
        fun onTrackItemClick(listLatLng: List<LatLng>)
    }
}