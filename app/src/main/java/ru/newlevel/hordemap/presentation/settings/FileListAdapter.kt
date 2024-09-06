package ru.newlevel.hordemap.presentation.settings

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.databinding.ItemMapFileBinding

class FileListAdapter(
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<FileListAdapter.FileViewHolder>() {

    private var mapsList: List<Triple<String, String, Long>> = ArrayList()

    @SuppressLint("NotifyDataSetChanged")
    fun setMessages(newList: List<Triple<String, String, Long>>) {
        mapsList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        return FileViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_map_file, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return mapsList.size
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(mapsList[position], onClick)
    }


    class FileViewHolder(view: View, ) : RecyclerView.ViewHolder(view) {

        private val binding = ItemMapFileBinding.bind(view)
        @SuppressLint("SetTextI18n")
        fun bind(mapData: Triple<String, String, Long>, onClick: (String) -> Unit) {
            binding.root.setOnClickListener {
                onClick(mapData.second)
            }
            binding.tvMapTitle.text = mapData.first
            binding.tvFileSize.text = (mapData.third / 1000).toString() + "kb"

        }

    }
}