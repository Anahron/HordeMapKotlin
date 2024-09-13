package ru.newlevel.hordemap.presentation.messenger.userGroup

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import com.jsibbold.zoomage.ZoomageView
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinComponent
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.databinding.UsersInGroupDialogBinding
import ru.newlevel.hordemap.domain.models.UserDomainModel
import ru.newlevel.hordemap.presentation.sign_in.UserData

class UserListDialogFragment : DialogFragment(R.layout.users_in_group_dialog), KoinComponent {

    private lateinit var mUsersInGroupRecyclerView: RecyclerView
    private lateinit var mUsersInGroupRecyclerViewAdapter: UsersInGroupAdapter
    private lateinit var mUserInGroupLayoutManager: LinearLayoutManager
    private lateinit var userDetailLayout: View // Макет для показа данных пользователя
    private val usersGroupViewModel by viewModel<UserGroupViewModel>()


    private val binding: UsersInGroupDialogBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit = with(binding) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.setLayout(
            (requireContext().resources.displayMetrics.widthPixels * 0.8).toInt(),
            (requireContext().resources.displayMetrics.heightPixels * 0.8).toInt() // 80% высоты экрана
        )
        setupRecyclerView()
        setupUsersUpdates()
        val text = requireContext().getText(R.string.group_is).toString() + " " +if (UserEntityProvider.userEntity.userGroup == 0) requireContext().getString(R.string.default_group)else UserEntityProvider.userEntity.userGroup.toString()
        binding.tvGroupName.text = text
        cancelButtonSetup()
    }

    private fun cancelButtonSetup(){
        binding.btnCancel.setOnClickListener {
            dialog?.dismiss()
        }
    }
    private fun setupUsersUpdates() {
        val lifecycle = viewLifecycleOwner.lifecycle
        lifecycle.coroutineScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                usersGroupViewModel.usersProfileDataFlow.collect { profiles ->
                    mUsersInGroupRecyclerViewAdapter.setMessages(profiles)
                    val text = profiles.size.toString() + " " + requireContext().getString(R.string.members)
                    binding.tvUsersCount.text = text
                }

            }
        }
    }

    private fun onImageClick(url: String) {
        if (url.isNotEmpty()) {
            val dialog = Dialog(
                requireContext(),
                android.R.style.Theme_DeviceDefault_NoActionBar
            )
            dialog.setContentView(R.layout.fragment_full_screen_image)
            val imageView =
                dialog.findViewById<ZoomageView>(R.id.myZoomageView)
            dialog.findViewById<ImageView>(R.id.close_massager).setOnClickListener {
                dialog.dismiss()
            }
            Glide.with(requireContext())
                .load(url)
                .into(imageView)
            dialog.show()
        }
    }
    private fun setupRecyclerView() {
        mUsersInGroupRecyclerView = binding.rvUsersCount
        mUsersInGroupRecyclerViewAdapter = UsersInGroupAdapter {
            onImageClick(it)
        }
        mUserInGroupLayoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = false
            initialPrefetchItemCount = 30
        }
        mUsersInGroupRecyclerView.isVerticalScrollBarEnabled = true
        mUsersInGroupRecyclerView.apply {
            layoutManager = mUserInGroupLayoutManager
            adapter = mUsersInGroupRecyclerViewAdapter
            setHasFixedSize(true)
            isNestedScrollingEnabled = true
        }
    }

    private fun showUserDetails(user: UserDomainModel) {

//        mUsersInGroupRecyclerView.visibility = View.GONE
//        userDetailLayout.visibility = View.VISIBLE
//
//        // Обновляем макет данными пользователя
//        userDetailLayout.findViewById<TextView>(R.id.user_name).text = user.name
//        userDetailLayout.findViewById<TextView>(R.id.user_email).text = user.email
//
//        // Кнопка "Назад", чтобы вернуться к списку пользователей
//        userDetailLayout.findViewById<Button>(R.id.back_button).setOnClickListener {
//            userDetailLayout.visibility = View.GONE
//            mUsersInGroupRecyclerView.visibility = View.VISIBLE
    }
}