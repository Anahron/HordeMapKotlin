package ru.newlevel.hordemap.presentatin.fragments.dialogs

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.view.iterator
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.databinding.FragmentTracksDialogBinding
import ru.newlevel.hordemap.domain.models.TrackItemDomainModel
import ru.newlevel.hordemap.presentatin.adapters.TracksAdapter
import ru.newlevel.hordemap.presentatin.viewmodels.SortState
import ru.newlevel.hordemap.presentatin.viewmodels.TrackTransferViewModel
import ru.newlevel.hordemap.presentatin.viewmodels.TracksViewModel
import kotlin.math.roundToInt

class TracksDialogFragment : Fragment(R.layout.fragment_tracks_dialog) {

    private val trackTransferViewModel by viewModel<TrackTransferViewModel>()
    private val tracksViewModel by viewModel<TracksViewModel>()
    private val binding: FragmentTracksDialogBinding by viewBinding()
    private lateinit var currentTrack: TrackItemDomainModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var trackAdapter: TracksAdapter
    private lateinit var alertDialog: AlertDialog
    private var popupItemWindow: PopupWindow? = null
    private val handler = Handler(Looper.getMainLooper())

    private fun initDefault() {
        tracksViewModel.getCurrentSessionLocations(UserEntityProvider.sessionId.toString())
        tracksViewModel.getAllSessionsLocations()
    }

    private fun setupUIComponents() {
        trackAdapter = TracksAdapter()
        recyclerView = binding.rvTracks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = trackAdapter
        }
    }

    private fun setupClickListeners() = with(binding) {
        ibPopupMain.setOnClickListener {
            showMainPopupMenu(it)
        }
        ibPopupCurrentItem.setOnClickListener {
            showCurrentItemPopupMenu(it)
        }

        btnGoBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        itemCurrentTrack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
            trackTransferViewModel.setTrack(currentTrack.locations)
        }

        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                tracksViewModel.setCheckedSortButton(checkedId)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)
        setupUIComponents()
        initDefault()
        setupClickListeners()

        tracksViewModel.trackSortState.observe(viewLifecycleOwner) {
            CoroutineScope(Dispatchers.IO).launch {
                sortTracks(it)
            }
        }
        tracksViewModel.trackItemCurrent.observe(viewLifecycleOwner) {
            if (it != null) {
                currentTrack = it
                tvTrackDate.text = it.date
                tvTrackDuration.text = it.duration
                tvTrackDistance.text = it.distance
            }
        }

        tracksViewModel.trackItemAll.observe(viewLifecycleOwner) { it ->
            if (it != null) {
                trackAdapter.setMessages(it)
                handler.postDelayed({
                    recyclerView.scrollToPosition(0)
                }, 500)
            }
        }


        trackAdapter.attachCallback(
            object : TracksAdapter.TracksAdapterCallback {
                override fun onTrackItemClick(listLatLng: List<LatLng>) {
                    trackTransferViewModel.setTrack(listLatLng)
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }

                override fun onShowMenuClick(v: View, sessionId: String) {
                    this@TracksDialogFragment.showItemMenu(v, sessionId)
                }

                override fun onFavouriteClick(isFavourite: Boolean, sessionId: String) {
                    setFavouriteItem(isFavourite, sessionId)

                }
            })
    }


    private fun setFavouriteItem(isFavourite: Boolean, sessionId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val job = launch {
                tracksViewModel.setFavouriteTrackForSession(sessionId, isFavourite)
            }
            job.join()
            sortTracks(tracksViewModel.trackSortState.value ?: SortState.DATA_SORT)
        }
    }

    private suspend fun sortTracks(sortState: SortState?) {
        withContext(Dispatchers.Main) {
            when (sortState) {
                SortState.DISTANCE_SORT -> {
                    tracksViewModel.sortByDistance()
                    setupSegmentButtons(R.id.btnDistance)
                }

                SortState.DURATION_SORT -> {
                    tracksViewModel.sortByDuration()
                    setupSegmentButtons(R.id.btnDuration)
                }

                else -> {
                    tracksViewModel.sortByDate()
                    setupSegmentButtons(R.id.btnDate)
                }
            }
        }
    }

    private fun setupItemMenu(viewGroup: ViewGroup) {
        popupItemWindow = PopupWindow(requireContext())
        popupItemWindow?.contentView = layoutInflater.inflate(
            R.layout.popup_track_item,
            viewGroup,
            false
        )
        popupItemWindow?.setBackgroundDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.round_white
            )
        )
        popupItemWindow?.elevation = 18f
        popupItemWindow?.isFocusable = true
    }

    private fun showMainPopupMenu(itemDotsView: View) {
        val mainPopupMenu = PopupWindow(requireContext())
        mainPopupMenu.contentView = layoutInflater.inflate(
            R.layout.popup_track_main,
            itemDotsView.rootView as ViewGroup,
            false
        )
        mainPopupMenu.setBackgroundDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.round_white
            )
        )
        mainPopupMenu.elevation = 18f
        mainPopupMenu.isFocusable = true
        mainPopupMenu.contentView?.findViewById<MaterialButton>(R.id.btnDeleteAllTracks)
            ?.setOnClickListener {
                mainPopupMenu.dismiss()
                CoroutineScope(Dispatchers.IO).launch {
                    tracksViewModel.deleteAllTracks()
                }
            }
        mainPopupMenu.showAsDropDown(
            itemDotsView,
            -convertDpToPx(requireContext(), 135),
            -convertDpToPx(requireContext(), 40)
        )
    }

    private fun showCurrentItemPopupMenu(itemDotsView: View) {
        val currentItemPopupMenu = PopupWindow(requireContext())
        currentItemPopupMenu.contentView = layoutInflater.inflate(
            R.layout.popup_track_current,
            itemDotsView.rootView as ViewGroup,
            false
        )
        currentItemPopupMenu.setBackgroundDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.round_white
            )
        )
        currentItemPopupMenu.elevation = 18f
        currentItemPopupMenu.isFocusable = true
        currentItemPopupMenu.contentView?.findViewById<MaterialButton>(R.id.btnCleanCurrentTrack)
            ?.setOnClickListener {
                currentItemPopupMenu.dismiss()
                tracksViewModel.deleteSessionLocations(sessionId = UserEntityProvider.sessionId.toString())
                initDefault()
            }
        currentItemPopupMenu.contentView?.findViewById<MaterialButton>(R.id.btnSaveCurrentTrack)
            ?.setOnClickListener {
                if (tracksViewModel.trackItemCurrent.value?.distanceMeters!! > 300) {
                    CoroutineScope(Dispatchers.IO).launch {
                        tracksViewModel.saveCurrentTrack(UserEntityProvider.sessionId.toString())
                    }
                } else
                    Toast.makeText(
                        requireContext(),
                        "Минимальная дистанция должна быть больше 300 метров",
                        Toast.LENGTH_LONG
                    ).show()
                currentItemPopupMenu.dismiss()
            }
        currentItemPopupMenu.showAsDropDown(
            itemDotsView,
            -convertDpToPx(requireContext(), 104),
            -convertDpToPx(requireContext(), 36)
        )
    }


    private fun showItemMenu(itemDotsView: View, sessionId: String) {
        if (popupItemWindow == null) {
            setupItemMenu(itemDotsView.rootView as ViewGroup)
        }
        popupItemWindow?.contentView?.findViewById<MaterialButton>(R.id.btnRename)
            ?.setOnClickListener {
                popupItemWindow?.dismiss()
                showInputDialog(requireContext(), onConfirm = { enteredText ->
                    tracksViewModel.renameTrackNameForSession(
                        sessionId = sessionId,
                        newTrackName = enteredText
                    )
                })
            }
        popupItemWindow?.contentView?.findViewById<MaterialButton>(R.id.btnDelete)
            ?.setOnClickListener {
                popupItemWindow?.dismiss()
                tracksViewModel.deleteSessionLocations(sessionId = sessionId)
            }

        popupItemWindow?.showAsDropDown(
            itemDotsView,
            -convertDpToPx(requireContext(), 104),
            -convertDpToPx(requireContext(), 36)
        )
    }

    private fun convertDpToPx(context: Context, dp: Int): Int {
        val density: Float = context.resources.displayMetrics.density
        return (dp.toFloat() * density).roundToInt()
    }

    private fun showInputDialog(context: Context, onConfirm: (String) -> Unit) {
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
}