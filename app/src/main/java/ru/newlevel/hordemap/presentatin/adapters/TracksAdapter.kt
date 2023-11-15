package ru.newlevel.hordemap.presentatin.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.databinding.ItemTrackBinding
import ru.newlevel.hordemap.domain.models.TrackItemDomainModel


class TracksAdapter : RecyclerView.Adapter<TracksAdapter.TracksViewHolder>(){

    private var tracksData: ArrayList<TrackItemDomainModel> = ArrayList()
    private var mCallback: TracksAdapterCallback? = null

    fun attachCallback(callback: TracksAdapterCallback){
        this.mCallback = callback
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TracksViewHolder {

        return TracksViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_track, parent, false))
    }

    fun setMessages(newList: ArrayList<TrackItemDomainModel>) {
        tracksData = newList
        notifyDataSetChanged()
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

    class TracksViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val binding = ItemTrackBinding.bind(view)

        fun bind(trackItemDomainModel: TrackItemDomainModel) = with(binding) {
            tvTrackDate.text = trackItemDomainModel.date
            tvTrackDistance.text = trackItemDomainModel.distance
            tvTrackDuration.text = trackItemDomainModel.duration
            tvTrackTitle.text = trackItemDomainModel.title
        }
    }
    interface TracksAdapterCallback {
        fun onTrackRvItemClick(listLatLng: List<LatLng>)
    }



}