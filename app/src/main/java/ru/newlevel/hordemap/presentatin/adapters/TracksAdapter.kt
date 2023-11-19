package ru.newlevel.hordemap.presentatin.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.databinding.ItemTrackBinding
import ru.newlevel.hordemap.domain.models.TrackItemDomainModel


class TracksAdapter : RecyclerView.Adapter<TracksAdapter.TracksViewHolder>() {

    private var tracksData: ArrayList<TrackItemDomainModel> = ArrayList()
    private var mCallback: TracksAdapterCallback? = null

    fun attachCallback(callback: TracksAdapterCallback) {
        this.mCallback = callback
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TracksViewHolder {
        val view = when (viewType) {
            ITEM_CURRENT -> LayoutInflater.from(parent.context)
                .inflate(R.layout.item_track_current, parent, false)

            else -> LayoutInflater.from(parent.context)
                .inflate(R.layout.item_track, parent, false)
        }
        return TracksViewHolder(view, mCallback)
    }

    fun setCurrentTrack(track: TrackItemDomainModel) {
        if (tracksData.isNotEmpty()) {
            tracksData[0] = track
            notifyItemChanged(0)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setTracks(newList: ArrayList<TrackItemDomainModel>) {
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
            mCallback?.onTrackItemClick(tracksData[position].locations)
        }
        holder.bind(tracksData[position], position)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) ITEM_CURRENT else ITEM_ALL
    }

    class TracksViewHolder(view: View, private var callback: TracksAdapterCallback?) :
        RecyclerView.ViewHolder(view) {

        private val binding = ItemTrackBinding.bind(view)

        fun bind(trackItemDomainModel: TrackItemDomainModel, position: Int) = with(binding) {
            tvTrackDate.text = trackItemDomainModel.date
            tvTrackDistance.text = trackItemDomainModel.distance
            tvTrackDuration.text = trackItemDomainModel.duration
            tvTrackTitle.text = trackItemDomainModel.title
            ibFavourite.setImageDrawable(
                AppCompatResources.getDrawable(
                    itemView.context,
                    R.drawable.vector_favourite_off
                )
            )
            if (trackItemDomainModel.isFavourite)
                ibFavourite.setImageDrawable(
                    AppCompatResources.getDrawable(
                        itemView.context,
                        R.drawable.vector_favourite_on
                    )
                )
            else
                ibFavourite.setImageDrawable(
                    AppCompatResources.getDrawable(
                        itemView.context,
                        R.drawable.vector_favourite_off
                    )
                )
            if (position == 0) {
                ibFavourite.visibility = View.GONE
                tvTrackTitle.text = itemView.context.resources.getText(R.string.my_current_track)
            }
            ibPopup.setOnClickListener {
                callback?.onShowMenuClick(it, trackItemDomainModel.sessionId)
            }

            ibFavourite.setOnClickListener {
                if (trackItemDomainModel.isFavourite) callback?.onFavouriteClick(
                    false,
                    trackItemDomainModel.sessionId
                )
                else callback?.onFavouriteClick(true, trackItemDomainModel.sessionId)
            }
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
            return (oldList[oldItemPosition].isFavourite == newList[newItemPosition].isFavourite && oldList[oldItemPosition].title == newList[newItemPosition].title)
        }
    }

    companion object {
        private const val ITEM_CURRENT = 1
        private const val ITEM_ALL = 2
    }

    interface TracksAdapterCallback {
        fun onTrackItemClick(listLatLng: List<LatLng>)
        fun onShowMenuClick(v: View, sessionId: String)
        fun onFavouriteClick(isFavourite: Boolean, sessionId: String)
    }


}