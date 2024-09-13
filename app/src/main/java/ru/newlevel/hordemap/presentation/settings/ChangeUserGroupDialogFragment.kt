package ru.newlevel.hordemap.presentation.settings

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.app.DEFAULT_GROUP
import ru.newlevel.hordemap.app.TAG
import ru.newlevel.hordemap.databinding.ChangeUserGroupDialogBinding

class ChangeUserGroupDialogFragment(private val onConfirm: (Int) -> Unit) :
    DialogFragment(R.layout.change_user_group_dialog) {

    private val binding: ChangeUserGroupDialogBinding by viewBinding()
    private val changeGroupViewModel by viewModel<ChangeGroupViewModel>()

    private lateinit var mUsersRecyclerView: RecyclerView
    private lateinit var mUsersRecyclerViewAdapter: UsersInGroupSettingsAdapter
    private lateinit var mUserLayoutManager: LinearLayoutManager
    private lateinit var mGroupRecyclerView: RecyclerView
    private lateinit var mGroupRecyclerViewAdapter: GroupsAdapter
    private lateinit var mGroupLayoutManager: LinearLayoutManager
    private var groups: List<GroupInfoModel> = emptyList()
    private var selectedGroup = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        setupConfirmBtn()
        setupResetBtn()
        setupCancelBtn()
        setupEditText()
        setupUsersRecyclerView()
        setupGroupRecyclerView()
        setupRequests()
    }

    override fun onDismiss(dialog: DialogInterface) {
        onConfirm(-1)
        super.onDismiss(dialog)
    }

    private fun setupRequests() {
        lifecycle.coroutineScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                try {
                    groups = changeGroupViewModel.getGroups()
                    mGroupRecyclerViewAdapter.setMessages(groups)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun setupGroupRecyclerView() = with(binding) {
        mGroupRecyclerView = rvGroups
        mGroupRecyclerViewAdapter = GroupsAdapter {
            onItemClick(it)
        }
        mGroupLayoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = false
            initialPrefetchItemCount = 30
        }
        mGroupRecyclerView.apply {
            layoutManager = mGroupLayoutManager
            adapter = mGroupRecyclerViewAdapter
            setHasFixedSize(true)
            isNestedScrollingEnabled = false
        }
        Log.e(TAG, " messengerViewModel.usersProfileLiveData.collect stop")
    }

    private fun onItemClick(groupNumber: Int) {
        selectedGroup = groupNumber
        binding.btnUserGroupConfirm.isEnabled = true
        if (binding.btnUserGroupConfirm.isEnabled)
            binding.btnUserGroupConfirm.alpha = 1f
        else
            binding.btnUserGroupConfirm.alpha = 0.4f
        lifecycle.coroutineScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                try {
                    val usersInGroup = changeGroupViewModel.getUsersInGroup(groupNumber)
                    mUsersRecyclerViewAdapter.setMessages(usersInGroup)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun setupUsersRecyclerView() = with(binding) {
        mUsersRecyclerView = rvUsersInGroup
        mUsersRecyclerViewAdapter = UsersInGroupSettingsAdapter {  }
        mUserLayoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = false
            initialPrefetchItemCount = 30
        }
        mUsersRecyclerView.apply {
            layoutManager = mUserLayoutManager
            adapter = mUsersRecyclerViewAdapter
            setHasFixedSize(true)
            isNestedScrollingEnabled = false
        }
    }

    private fun setupEditText() = with(binding) {
        etGroupNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                btnUserGroupConfirm.isEnabled =
                    (s.toString().trim().isNotEmpty() && s.toString().isDigitsOnly())
                if (btnUserGroupConfirm.isEnabled) {
                    selectedGroup = s.toString().toInt()
                    mGroupRecyclerViewAdapter.filter(s.toString())
                    btnUserGroupConfirm.alpha = 1f
                    if (mGroupRecyclerViewAdapter.itemCount == 0)
                        mUsersRecyclerViewAdapter.setMessages(emptyList())
                } else {
                    btnUserGroupConfirm.alpha = 0.4f
                    mGroupRecyclerViewAdapter.filter("")
                }
            }

            override fun afterTextChanged(s: Editable) {
                etGroupNumber.setOnEditorActionListener { _, actionId, event ->
                    if (actionId == EditorInfo.IME_ACTION_DONE || (event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER)) {
                        val imm =
                            requireContext().applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(etGroupNumber.windowToken, 0)
                        etGroupNumber.clearFocus()
                        val inputText = s.toString()
                        if (inputText.isNotEmpty() && inputText.isDigitsOnly() && (inputText.toInt() in 1..99)) {
                            btnUserGroupConfirm.isEnabled = true
                            Log.e(TAG, "  selectedGroup = inputText.toInt() = " + inputText.toInt())
                            selectedGroup = inputText.toInt()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                R.string.group_number_must_be_0_10,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    true
                }
            }
        })
    }

    private fun setupCancelBtn() = with(binding) {
        btnUserGroupCancel.setOnClickListener {
            onConfirm(-1)
            dismiss()
        }
    }

    private fun setupResetBtn() = with(binding) {
        btnResetUserGroup.findViewById<AppCompatButton>(R.id.btnResetUserGroup)
            .setOnClickListener {
                onConfirm(DEFAULT_GROUP)
                dismiss()
            }
    }

    private fun setupConfirmBtn() = with(binding) {
        btnUserGroupConfirm.isEnabled = false
        btnUserGroupConfirm.alpha = 0.4f
        btnUserGroupConfirm.setOnClickListener {
            Log.e(TAG, "  onConfirm(selectedGroup) = " + selectedGroup)
            onConfirm(selectedGroup)
            dismiss()
        }
    }
}