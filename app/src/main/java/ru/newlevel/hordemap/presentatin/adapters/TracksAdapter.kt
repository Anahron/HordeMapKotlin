package ru.newlevel.hordemap.presentatin.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.button.MaterialButton
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.databinding.ItemTrackBinding
import ru.newlevel.hordemap.domain.models.TrackItemDomainModel
import kotlin.math.roundToInt


class TracksAdapter : RecyclerView.Adapter<TracksAdapter.TracksViewHolder>() {

    private var tracksData: List<TrackItemDomainModel> = ArrayList()
    private var mCallback: TracksAdapterCallback? = null

    fun attachCallback(callback: TracksAdapterCallback) {
        this.mCallback = callback
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TracksViewHolder {

        return TracksViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_track, parent, false),
            mCallback
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setMessages(newList: List<TrackItemDomainModel>) {
        val diffCallback = TracksDiffCallback(tracksData, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        tracksData = newList
        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemCount(): Int {
        return tracksData.size
    }

    override fun onBindViewHolder(holder: TracksViewHolder, position: Int) {
        holder.itemView.setOnClickListener {
            mCallback?.onTrackRvItemClick(tracksData[position].locations)
        }
        holder.bind(tracksData[position])
    }

    class TracksViewHolder(view: View, private val callback: TracksAdapterCallback?) :
        RecyclerView.ViewHolder(view) {

        private val binding = ItemTrackBinding.bind(view)

        fun bind(trackItemDomainModel: TrackItemDomainModel) = with(binding) {
            tvTrackDate.text = trackItemDomainModel.date
            tvTrackDistance.text = trackItemDomainModel.distance
            tvTrackDuration.text = trackItemDomainModel.duration
            tvTrackTitle.text = trackItemDomainModel.title
            ibFavourite.setBackgroundResource(R.drawable.vector_favourite_off)
            if (trackItemDomainModel.isFavourite)
                ibFavourite.setBackgroundResource(R.drawable.vector_favourite_on)
            else
                ibFavourite.setBackgroundResource(R.drawable.vector_favourite_off)
            ibPopup.setOnClickListener {
                showMenu(it, trackItemDomainModel)
                callback?.menuActive(true)
            }

            ibFavourite.setOnClickListener {
                if (trackItemDomainModel.isFavourite) callback?.setFavourite(false, trackItemDomainModel.sessionId)
                else callback?.setFavourite(true, trackItemDomainModel.sessionId)

            }
        }

        private fun showMenu(v: View, trackItemDomainModel: TrackItemDomainModel) {
            val popupWindow = PopupWindow(v.context)
            val view: View =
                LayoutInflater.from(v.context)
                    .inflate(R.layout.list_popup_window_item, v.rootView as ViewGroup, false)
            popupWindow.contentView = view
            popupWindow.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    v.context,
                    R.drawable.round_white
                )
            )
            popupWindow.elevation = 6f
            popupWindow.isFocusable = true
            popupWindow.showAsDropDown(
                binding.ibPopup,
                -convertDpToPx(v.context, 104),
                -convertDpToPx(v.context, 36)
            )
            view.findViewById<MaterialButton>(R.id.btnRename).setOnClickListener {
                popupWindow.dismiss()
                callback?.renameTrack(trackItemDomainModel.sessionId)
            }
            view.findViewById<MaterialButton>(R.id.btnDelete).setOnClickListener {
                popupWindow.dismiss()
                callback?.deleteTrack(trackItemDomainModel.sessionId)
            }
            popupWindow.setOnDismissListener {
                callback?.menuActive(false)
            }
        }

        private fun convertDpToPx(context: Context, dp: Int): Int {
            val density: Float = context.resources.displayMetrics.density
            return (dp.toFloat() * density).roundToInt()
        }
    }

    class TracksDiffCallback(
        private val oldList: List<TrackItemDomainModel>,
        private val newList: List<TrackItemDomainModel>
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].timestamp == newList[newItemPosition].timestamp
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            Log.e("AAA","areContentsTheSame" + (oldList[oldItemPosition].isFavourite == newList[newItemPosition].isFavourite) + (oldList[oldItemPosition].title == newList[newItemPosition].title))
            return (oldList[oldItemPosition].isFavourite == newList[newItemPosition].isFavourite && oldList[oldItemPosition].title == newList[newItemPosition].title)
        }
    }

    interface TracksAdapterCallback {
        fun onTrackRvItemClick(listLatLng: List<LatLng>)
        fun deleteTrack(sessionId: String)
        fun renameTrack(sessionId: String)
        fun shareTrack()
        fun setFavourite(isFavourite: Boolean, sessionId: String)
        fun menuActive(isActive: Boolean)
    }


}