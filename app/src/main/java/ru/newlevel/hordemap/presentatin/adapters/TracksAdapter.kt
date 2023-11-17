package ru.newlevel.hordemap.presentatin.adapters

import android.annotation.SuppressLint
import android.util.Log
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
            mCallback?.onTrackItemClick(tracksData[position].locations)
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
            Log.e(
                "AAA",
                "areContentsTheSame" + (oldList[oldItemPosition].isFavourite == newList[newItemPosition].isFavourite) + (oldList[oldItemPosition].title == newList[newItemPosition].title)
            )
            return (oldList[oldItemPosition].isFavourite == newList[newItemPosition].isFavourite && oldList[oldItemPosition].title == newList[newItemPosition].title)
        }
    }

    interface TracksAdapterCallback {
        fun onTrackItemClick(listLatLng: List<LatLng>)
        fun onShowMenuClick(v: View, sessionId: String)
        fun onFavouriteClick(isFavourite: Boolean, sessionId: String)
    }


}