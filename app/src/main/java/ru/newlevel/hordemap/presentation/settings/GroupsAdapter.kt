package ru.newlevel.hordemap.presentation.settings

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.databinding.ItemGroupBinding

class GroupsAdapter(private val onItemClick: (Int) -> Unit) :
    RecyclerView.Adapter<GroupsAdapter.GroupsViewHolder>() {

    private var groupData: List<GroupInfoModel> = ArrayList()
    private var selectedPosition: Int = RecyclerView.NO_POSITION
    private var  originalGroupData: List<GroupInfoModel> = ArrayList()
    @SuppressLint("NotifyDataSetChanged")
    fun setMessages(newList: List<GroupInfoModel>) {
        groupData = newList
        originalGroupData = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupsViewHolder {
        return GroupsViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_group, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return groupData.size
    }

    override fun onBindViewHolder(holder: GroupsViewHolder, position: Int) {
        holder.bind(groupData[position], position == selectedPosition)
        holder.itemView.setOnClickListener {
            onItemClick(groupData[position].nodeName.toInt())
            updateSelection(position)
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    fun filter(query: String) {
        groupData = if (query.isEmpty()) {
            originalGroupData
        } else {
            groupData.filter {
                it.nodeName.contains(query, ignoreCase = true)
            }
        }
        selectedPosition = RecyclerView.NO_POSITION
        notifyDataSetChanged()
    }
    private fun updateSelection(newPosition: Int) {
        val oldPosition = selectedPosition
        selectedPosition = newPosition
        notifyItemChanged(oldPosition)
        notifyItemChanged(newPosition)
    }
    class GroupsViewHolder(
        view: View,
    ) : RecyclerView.ViewHolder(view) {

        private val binding = ItemGroupBinding.bind(view)
        fun bind(itemData: GroupInfoModel, isSelected: Boolean) {
            binding.tvGroupNumbSym.visibility = View.VISIBLE
            if (itemData.nodeName.toInt() == 0) {
                binding.tvGroupName.text = itemView.context.getText(R.string.group_general_short)
                binding.tvGroupNumbSym.visibility = View.GONE
            } else
                binding.tvGroupName.text = itemData.nodeName
            binding.tvUserCount.text = itemData.childCount.toString()
            itemView.setBackgroundColor(
                if (isSelected)
                    ContextCompat.getColor(itemView.context, R.color.main_green_middle_transparent) // цвет для выделенного элемента
                else
                    ContextCompat.getColor(itemView.context, R.color.white) //  цвет для невыделенного элемента
            )
        }
    }
}