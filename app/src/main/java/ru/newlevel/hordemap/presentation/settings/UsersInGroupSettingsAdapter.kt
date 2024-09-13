package ru.newlevel.hordemap.presentation.settings

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.databinding.ItemUserSettingsBinding
import ru.newlevel.hordemap.domain.models.UserDomainModel
import ru.newlevel.hordemap.presentation.messenger.NameColors

class UsersInGroupSettingsAdapter(private val onImageClick: (String) -> Unit) : RecyclerView.Adapter<UsersInGroupSettingsAdapter.UserViewHolder>() {

    private var usersData: List<UserDomainModel> = ArrayList()

    @SuppressLint("NotifyDataSetChanged")
    fun setMessages(newList: List<UserDomainModel>) {
        usersData = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        return UserViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_user_settings, parent, false))
    }

    override fun getItemCount(): Int {
        return usersData.size
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(usersData[position], onImageClick)
    }


    class UserViewHolder(
        view: View,
    ) : RecyclerView.ViewHolder(view) {

        private val binding = ItemUserSettingsBinding.bind(view)
        fun bind(userData: UserDomainModel, onImageClick: (String) -> Unit) {
            binding.imgUserPhoto.setOnClickListener {
                onImageClick.invoke(userData.profileImageUrl)
            }
            binding.tvUserName.text = userData.name
            NameColors.entries.find { it.id ==userData.selectedMarker }?.let {
                binding.tvUserName.setTextColor(ContextCompat.getColor(itemView.context, it.resourceId))
           }
            Glide.with(itemView.context)
                .load(
                    userData.profileImageUrl
                        .toUri()
                )
                .thumbnail(1f)
                .timeout(30_000)
                .placeholder(R.drawable.img_anonymous).into(binding.imgUserPhoto)
        }
    }
}