package ru.newlevel.hordemap.presentation.tracks

import android.content.Context
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
import kotlin.math.roundToInt

class TracksFragment : Fragment(R.layout.fragment_tracks_dialog) {

    private val trackTransferViewModel by viewModel<TrackTransferViewModel>()
    private val tracksViewModel by viewModel<TracksViewModel>()
    private val binding: FragmentTracksDialogBinding by viewBinding()
    private lateinit var recyclerView: RecyclerView
    private lateinit var trackAdapter: TracksAdapter
    private lateinit var alertDialog: AlertDialog
    private var popupItemWindow: PopupWindow? = null
    private val handler = Handler(Looper.getMainLooper())

    private fun initDefault() {
        tracksViewModel.getAllSessionsLocations()
    }

    private fun setupUIComponents() {
        trackAdapter = TracksAdapter()
        recyclerView = binding.rvTracks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = trackAdapter
            itemAnimator?.apply {
                addDuration = 200
                moveDuration = 250
                changeDuration = 1
            }
        }
    }

    private fun setupClickListeners() = with(binding) {
        ibPopupMain.setOnClickListener {
            showMainPopupMenu(it)
        }

        btnGoBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
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

        tracksViewModel.currentTrack.observe(viewLifecycleOwner){
            trackAdapter.setCurrentTrack(it)
        }
        tracksViewModel.trackItemAll.observe(viewLifecycleOwner) {
            if (it != null) {
                val newlist = it.toMutableList()
                tracksViewModel.currentTrack.value?.let { it1 -> newlist.add(0, it1) }
                trackAdapter.setTracks(newlist as ArrayList)
            }
        }

        trackAdapter.attachCallback(
            object : TracksAdapter.TracksAdapterCallback {
                override fun onTrackItemClick(listLatLng: List<LatLng>) {
                    trackTransferViewModel.setTrack(listLatLng)
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }

                override fun onShowMenuClick(v: View, sessionId: String) {
                    if (sessionId == UserEntityProvider.sessionId.toString())
                        showCurrentItemPopupMenu(v)
                    else
                        showItemMenu(v, sessionId)
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
            }
        currentItemPopupMenu.contentView?.findViewById<MaterialButton>(R.id.btnSaveCurrentTrack)
            ?.setOnClickListener {
                val currentDistance = tracksViewModel.currentTrack.value?.distanceMeters
                if (currentDistance != null) {
                    if (currentDistance > 300) {
                        CoroutineScope(Dispatchers.IO).launch {
                            tracksViewModel.saveCurrentTrack(UserEntityProvider.sessionId.toString())
                        }
                    } else
                        Toast.makeText(
                            requireContext(),
                            R.string.minimal_distance_should_be,
                            Toast.LENGTH_LONG
                        ).show()
                }
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
        handler.postDelayed({
            recyclerView.scrollToPosition(0)
        }, 500)
    }
}