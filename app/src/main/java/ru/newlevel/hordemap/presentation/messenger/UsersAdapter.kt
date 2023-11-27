package ru.newlevel.hordemap.presentation.messenger

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.databinding.ItemUserBinding
import ru.newlevel.hordemap.domain.models.UserDomainModel

class UsersAdapter : RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

    private var usersData: List<UserDomainModel> = ArrayList()

    @SuppressLint("NotifyDataSetChanged")
    fun setMessages(newList: List<UserDomainModel>) {
        usersData = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        return UserViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false))
    }

    override fun getItemCount(): Int {
        return usersData.size
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(usersData[position])
    }


    class UserViewHolder(
        view: View,
    ) : RecyclerView.ViewHolder(view) {

        private val binding = ItemUserBinding.bind(view)
        fun bind(userData: UserDomainModel) {
            binding.tvUserName.text = userData.name
            NameColors.values().find { it.id ==userData.selectedMarker }?.let {
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