package ru.newlevel.hordemap.presentatin.fragments.dialogs

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.PopupWindow
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.view.iterator
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.databinding.FragmentTracksDialogBinding
import ru.newlevel.hordemap.domain.models.TrackItemDomainModel
import ru.newlevel.hordemap.presentatin.adapters.TracksAdapter
import ru.newlevel.hordemap.presentatin.viewmodels.LocationUpdateViewModel
import ru.newlevel.hordemap.presentatin.viewmodels.SortState
import kotlin.math.roundToInt

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
    private lateinit var alertDialog: AlertDialog
    private var popupItemWindow: PopupWindow? = null

    private fun initDefault() {
        locationUpdateViewModel.getCurrentSessionLocations(UserEntityProvider.sessionId.toString())
        if (locationUpdateViewModel.trackItemAll.value == null)
            locationUpdateViewModel.getAllSessionsLocations()
    }

    private fun setupUIComponents() {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
        trackAdapter = TracksAdapter()
        recyclerView = binding.rvTracks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = trackAdapter
        }
        backgroundView = View(requireContext())
        backgroundView.setBackgroundColor(Color.BLACK)
        backgroundView.alpha = 0.0F
        dialog?.window?.addContentView(
            backgroundView, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
    }

    private fun setupClickListeners() = with(binding) {
        ibPopup.setOnClickListener {
            showCurrentItemMenu(it)
        }

        btnGoBack.setOnClickListener {
            dialog?.dismiss()
        }

        itemCurrentTrack.setOnClickListener {
            dialog?.dismiss()
            onTrackItemClick.onTrackItemClick(currentTrack.locations)
        }

        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                locationUpdateViewModel.setCheckedSortButton(checkedId)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)
        setupUIComponents()
        initDefault()
        setupClickListeners()

        locationUpdateViewModel.trackSortState.observe(this@TracksDialogFragment) {
            sortTracks(it)
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
            if (it != null) {
                trackAdapter.setMessages(it)
                recyclerView.scrollToPosition(0)
            }
        }

        trackAdapter.attachCallback(object : TracksAdapter.TracksAdapterCallback {
            override fun onTrackItemClick(listLatLng: List<LatLng>) {
                onTrackItemClick.onTrackItemClick(listLatLng)
                dialog?.dismiss()
            }

            override fun onShowMenuClick(v: View, sessionId: String) {
                this@TracksDialogFragment.showItemMenu(v, sessionId)
            }

            override fun onFavouriteClick(isFavourite: Boolean, sessionId: String) {
                CoroutineScope(Dispatchers.IO).launch {
                    setFavouriteItem(isFavourite, sessionId)
                }
            }
        })
    }

    private suspend fun setFavouriteItem(isFavourite: Boolean, sessionId: String) {
        coroutineScope {
            val job = launch {
                locationUpdateViewModel.setFavouriteTrackForSession(
                    sessionId,
                    isFavourite
                )
            }
            job.join()
            withContext(Dispatchers.Main) {
                locationUpdateViewModel.trackSortState.value?.let { sortTracks(it) }
            }
        }
    }

    private fun sortTracks(sortState: SortState) {
        when (sortState) {
            SortState.DISTANCE_SORT -> {
                setupSegmentButtons(R.id.btnDistance)
                locationUpdateViewModel.sortByDistance()
            }

            SortState.DURATION_SORT -> {
                setupSegmentButtons(R.id.btnDuration)
                locationUpdateViewModel.sortByDuration()
            }

            else -> {
                setupSegmentButtons(R.id.btnDate)
                locationUpdateViewModel.sortByDate()
            }
        }
    }

    private fun setupItemMenu(viewGroup: ViewGroup) {
        popupItemWindow = PopupWindow(requireContext())
        popupItemWindow?.contentView = layoutInflater.inflate(
            R.layout.list_popup_window_item,
            viewGroup,
            false
        )
        popupItemWindow?.setBackgroundDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.round_white
            )
        )
        popupItemWindow?.elevation = 8f
        popupItemWindow?.isFocusable = true
        popupItemWindow?.setOnDismissListener {
            hideBackgroundShadow()
        }
    }

    private fun showItemMenu(itemDotsView: View, sessionId: String) {
        if (popupItemWindow == null) {
            setupItemMenu(itemDotsView.rootView as ViewGroup)
        }
        popupItemWindow?.contentView?.findViewById<MaterialButton>(R.id.btnRename)
            ?.setOnClickListener {
                popupItemWindow?.dismiss()
                showInputDialog(requireContext(), onConfirm = { enteredText ->
                    locationUpdateViewModel.renameTrackNameForSession(
                        sessionId = sessionId,
                        newTrackName = enteredText
                    )
                })
            }
        popupItemWindow?.contentView?.findViewById<MaterialButton>(R.id.btnDelete)
            ?.setOnClickListener {
                popupItemWindow?.dismiss()
                locationUpdateViewModel.deleteSessionLocations(sessionId = sessionId)
            }
        popupItemWindow?.showAsDropDown(
            itemDotsView,
            -convertDpToPx(requireContext(), 104),
            -convertDpToPx(requireContext(), 36)
        )
        showBackgroundShadow()
    }

    private fun showCurrentItemMenu(itemDotsView: View) {
        if (popupItemWindow == null) {
            setupItemMenu(itemDotsView.rootView as ViewGroup)
        }
        popupItemWindow?.contentView?.findViewById<MaterialButton>(R.id.btnRename)
            ?.setOnClickListener {
                popupItemWindow?.dismiss()
                showInputDialog(requireContext(), onConfirm = { enteredText ->
                    locationUpdateViewModel.renameTrackNameForSession(
                        sessionId = UserEntityProvider.sessionId.toString(),
                        newTrackName = enteredText
                    )
                })
            }
        popupItemWindow?.contentView?.findViewById<MaterialButton>(R.id.btnDelete)
            ?.setOnClickListener {
                popupItemWindow?.dismiss()
                locationUpdateViewModel.deleteSessionLocations(sessionId = UserEntityProvider.sessionId.toString())
                locationUpdateViewModel.getCurrentSessionLocations(UserEntityProvider.sessionId.toString())
            }
        popupItemWindow?.showAsDropDown(
            itemDotsView,
            -convertDpToPx(requireContext(), 104),
            -convertDpToPx(requireContext(), 36)
        )
        showBackgroundShadow()
    }


    private fun convertDpToPx(context: Context, dp: Int): Int {
        val density: Float = context.resources.displayMetrics.density
        return (dp.toFloat() * density).roundToInt()
    }

    private fun showBackgroundShadow() {
        val fadeInAnimation = ObjectAnimator.ofFloat(backgroundView, "alpha", 0f, 0.15f)
        fadeInAnimation.duration = 200
        fadeInAnimation.start()
    }

    private fun hideBackgroundShadow() {
        val fadeOutAnimation =
            ObjectAnimator.ofFloat(backgroundView, "alpha", backgroundView.alpha, 0f)
        fadeOutAnimation.duration = 200
        fadeOutAnimation.start()
    }

    private fun showInputDialog(context: Context, onConfirm: (String) -> Unit) {
        showBackgroundShadow()

        val customLayout = View.inflate(context, R.layout.rename_track_dialog, null)
        val editText = customLayout.findViewById<EditText>(R.id.description_edit_text)
        alertDialog = AlertDialog.Builder(context)
            .setView(customLayout)
            .create()
        customLayout.findViewById<AppCompatButton>(R.id.btn_save).setOnClickListener {
            onConfirm(editText.text.toString())
            alertDialog.dismiss()
        }
        customLayout.findViewById<AppCompatButton>(R.id.btn_cancel).setOnClickListener {
            alertDialog.dismiss()
        }
        alertDialog.window?.setBackgroundDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.round_white
            )
        )
        alertDialog.show()
        alertDialog.setOnDismissListener {
            hideBackgroundShadow()
        }
    }

    private fun setupSegmentButtons(checkedId: Int) {
        val defaultColor = ContextCompat.getColor(requireContext(), R.color.slate_800)
        val selectedColor = ContextCompat.getColor(requireContext(), R.color.white)
        for (button in binding.toggleGroup) {
            if (button.id == checkedId) {
                binding.root.findViewById<MaterialButton>(button.id).setTextColor(selectedColor)
                button.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.main_green_dark)
            } else {
                button.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.white)
                binding.root.findViewById<MaterialButton>(button.id).setTextColor(defaultColor)
            }
        }
    }

    interface OnTrackItemClick {
        fun onTrackItemClick(listLatLng: List<LatLng>)
    }
}