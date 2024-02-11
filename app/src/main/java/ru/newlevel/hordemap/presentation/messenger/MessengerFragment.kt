package ru.newlevel.hordemap.presentation.messenger

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import android.widget.TextView.VISIBLE
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.button.MaterialButton
import com.jsibbold.zoomage.ZoomageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.app.REQUEST_CODE_CAMERA_PERMISSION
import ru.newlevel.hordemap.app.REQUEST_CODE_WRITE_EXTERNAL_STORAGE
import ru.newlevel.hordemap.app.SelectFilesContract
import ru.newlevel.hordemap.app.TAG
import ru.newlevel.hordemap.app.animateButtonPadding
import ru.newlevel.hordemap.app.animateButtonPaddingReverse
import ru.newlevel.hordemap.app.blinkAndHideShadow
import ru.newlevel.hordemap.app.convertDpToPx
import ru.newlevel.hordemap.app.copyTextInSystem
import ru.newlevel.hordemap.app.createTempImageFile
import ru.newlevel.hordemap.app.getFileNameFromUri
import ru.newlevel.hordemap.app.getFileSizeFromUri
import ru.newlevel.hordemap.app.hasPermission
import ru.newlevel.hordemap.app.hideShadowAnimate
import ru.newlevel.hordemap.app.hideToBottomAnimation
import ru.newlevel.hordemap.app.hideToRight
import ru.newlevel.hordemap.app.loadAnimation
import ru.newlevel.hordemap.app.showAtRight
import ru.newlevel.hordemap.app.showFromBottomAnimation
import ru.newlevel.hordemap.app.showShadowAnimate
import ru.newlevel.hordemap.data.db.MyMessageEntity
import ru.newlevel.hordemap.databinding.FragmentMessengerBinding
import java.io.File

class MessengerFragment : Fragment(R.layout.fragment_messenger),
    OnMessageItemClickListener,
    SendFileDescriptionDialogFragment.OnFileDescriptionListener {

    private val binding: FragmentMessengerBinding by viewBinding()
    private val messengerViewModel by viewModel<MessengerViewModel>()

    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mMessageAdapter: MessagesAdapter
    private lateinit var mMessageLayoutManager: LinearLayoutManager
    private lateinit var mUsersRecyclerView: RecyclerView
    private lateinit var mUsersRecyclerViewAdapter: UsersAdapter
    private lateinit var mUserLayoutManager: LinearLayoutManager
    private lateinit var mUsersPopupMenu: PopupWindow
    private lateinit var mBottomSheetBehavior: BottomSheetBehavior<*>
    private lateinit var mActivityLauncher: ActivityResultLauncher<String>
    private lateinit var pickImage: ActivityResultLauncher<String>
    private lateinit var takePicture: ActivityResultLauncher<Uri>
    private lateinit var viewBehavior: View
    private var file: File? = null
    private lateinit var photoUri: Uri
    private var isDownloadingState = false
    private var isPopUpShow = false
    private var editMessageId: Long? = null
    private var replyMessageId: Long? = null


    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUIComponents()
        requestWriteExternalStoragePermission()
        setupMessagesUpdates()
        binding.inputLayout.showFromBottomAnimation()
        createActivityRegisters()
    }

    private fun createActivityRegisters() {
        mActivityLauncher = registerForActivityResult(SelectFilesContract()) { uri: Uri? ->
            if (uri != null) {
                binding.shadow.showShadowAnimate()
                val dialogFragment = SendFileDescriptionDialogFragment(
                    uri,
                    requireContext().getFileNameFromUri(uri),
                    requireContext().getFileSizeFromUri(uri),
                    this,
                    false
                )
                dialogFragment.show(childFragmentManager, "file_description_dialog")
            }
        }
        pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                binding.shadow.showShadowAnimate()
                val dialogFragment = SendFileDescriptionDialogFragment(
                    uri,
                    requireContext().getFileNameFromUri(uri),
                    requireContext().getFileSizeFromUri(uri),
                    this,
                    true
                )
                dialogFragment.show(childFragmentManager, "pick_image_description_dialog")
            }
        }
        takePicture =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess: Boolean ->
                if (isSuccess) {
                    binding.shadow.showShadowAnimate()
                    val dialogFragment = SendFileDescriptionDialogFragment(
                        photoUri,
                        requireContext().getFileNameFromUri(photoUri),
                        requireContext().getFileSizeFromUri(photoUri),
                        this, true
                    )
                    dialogFragment.show(childFragmentManager, "photo_description_dialog")
                }
            }
    }

    private fun setupUIComponents() {
        setupUsersRecyclerView()
        setupRecyclerView()
        setupUsersCountButton()
        setupBottomSheetBehavior()
        setupEditTextMessage()
        setupSendMessageButton()
        setupAttachBtn()
        setupCloseMessengerButton()
        setupScrollDownButton()
        setupBottomBehaviorListeners()
        setUpCloseReplyButton()
    }

    private fun setupUsersCountButton() {
        binding.ibUsers.setOnClickListener {
            showMainPopupMenu(it)
        }
    }

    private fun setupUsersRecyclerView() {
        mUsersPopupMenu = PopupWindow(requireContext())
        mUsersPopupMenu.contentView = layoutInflater.inflate(
            R.layout.popup_users,
            binding.ibUsers.rootView as ViewGroup,
            false
        )
        mUsersPopupMenu.setBackgroundDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.round_white
            )
        )
        mUsersPopupMenu.height = requireContext().convertDpToPx(200)
        mUsersPopupMenu.isFocusable = false
        mUsersPopupMenu.isOutsideTouchable = true
        mUsersPopupMenu.elevation = 18f
        mUsersPopupMenu.contentView?.findViewById<RecyclerView>(R.id.rvUsersCount)?.let {
            mUsersRecyclerView = it
        }
        mUsersRecyclerViewAdapter = UsersAdapter {
            onImageClick(it)
        }
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
        mUsersPopupMenu.setOnDismissListener {
            CoroutineScope(Dispatchers.Main).launch {
                delay(300)
                isPopUpShow = false
            }
        }
    }

    private fun showMainPopupMenu(itemDotsView: View) {
        isPopUpShow = true
        binding.shadow.showShadowAnimate()
        mUsersPopupMenu.showAsDropDown(
            itemDotsView,
            -requireContext().convertDpToPx(64),
            0
        )
        mUsersPopupMenu.setOnDismissListener {
            CoroutineScope(Dispatchers.Main).launch {
                delay(300)
                isPopUpShow = false
            }
            binding.shadow.hideShadowAnimate()
        }
    }

    private fun handleNewMessages(messages: List<MyMessageEntity>) {
        val onDown = !mRecyclerView.canScrollVertically(1)
                && mRecyclerView.computeVerticalScrollRange() > mRecyclerView.height
        mMessageAdapter.setMessages(messages as ArrayList<MyMessageEntity>)
        if (onDown) {
            mRecyclerView.scrollToPosition(mMessageAdapter.itemCount - 1)
        }
    }

    private fun setupMessagesUpdates() {
        val lifecycle = viewLifecycleOwner.lifecycle
        lifecycle.coroutineScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                messengerViewModel.usersProfileDataFlow.collect { profiles ->
                    mUsersRecyclerViewAdapter.setMessages(profiles)
                    binding.tvUsersCount.text = profiles.size.toString()
                }
            }
            Log.e(TAG, " messengerViewModel.usersProfileLiveData.collect stop")
        }
        lifecycle.coroutineScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                if (mMessageAdapter.itemCount < 1)
                    delay(350)
                messengerViewModel.messagesDataFlow.collect { messages ->
                    handleNewMessages(messages)
                }
            }
            Log.e(TAG, "messengerViewModel.messagesLiveData.collect stop")
        }
    }

    private fun setupBottomSheetBehavior() {
        viewBehavior = binding.root.findViewById(R.id.bottom_sheet)
        mBottomSheetBehavior = BottomSheetBehavior.from(viewBehavior)
        mBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        viewBehavior.setOnClickListener {
            if (mBottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                mBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }
    }

    private fun setupRecyclerView() {
        mMessageAdapter = MessagesAdapter(this, requireContext())
        mMessageLayoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
            initialPrefetchItemCount = 30
        }
        mRecyclerView = binding.recyclerViewMessages.apply {
            layoutManager = mMessageLayoutManager
            adapter = mMessageAdapter
            setHasFixedSize(false)
            isNestedScrollingEnabled = false
            setOnScrollChangeListener { _, _, _, _, _ ->
                if (!mRecyclerView.canScrollVertically(1) && (mRecyclerView.computeVerticalScrollRange() > mRecyclerView.height))
                    binding.btnGoDown.hideToRight(500f)
                else
                    binding.btnGoDown.showAtRight(500f)
            }
        }
    }

    private fun setupScrollDownButton() {
        binding.btnGoDown.translationX = 500f
        binding.btnGoDown.setOnClickListener {
            mRecyclerView.smoothScrollToPosition(
                mMessageAdapter.itemCount.minus(1)
            )
        }
    }

    private fun setupEditTextMessage() {
        binding.editTextMessage.addTextChangedListener {
            val string = binding.editTextMessage.text.toString()
            if (string.isEmpty()) {
                binding.buttonSendFile.visibility = VISIBLE
                binding.buttonSend.visibility = GONE
            } else {
                binding.buttonSendFile.visibility = GONE
                binding.buttonSend.visibility = VISIBLE
            }
        }
        binding.editTextMessage.setOnEditorActionListener(OnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val text = binding.editTextMessage.text.toString()
                if (text.isNotEmpty()) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        messengerViewModel.sendMessage(text, replyId = replyMessageId, editMessage = editMessageId)
                    }
                }
                binding.editTextMessage.setText("")
                binding.editTextMessage.requestFocus()
                closeBottomInfoWindow()
                return@OnEditorActionListener true
            }
            false
        })
    }

    private fun setupSendMessageButton() {
        binding.buttonSend.setOnClickListener {
            val text = binding.editTextMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                lifecycleScope.launch(Dispatchers.IO) {
                    messengerViewModel.sendMessage(text, replyId = replyMessageId, editMessage = editMessageId)
                }
            }
            closeBottomInfoWindow()
            binding.editTextMessage.setText("")
            binding.editTextMessage.requestFocus()
        }
    }

    private fun setupAttachBtn() {
        binding.buttonSendFile.setOnClickListener {
            if (!requireContext().hasPermission(Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(
                    (context as Activity?)!!,
                    arrayOf(Manifest.permission.CAMERA),
                    REQUEST_CODE_CAMERA_PERMISSION
                )
            } else {
                mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    private fun setupBottomBehaviorListeners() {
        viewBehavior.findViewById<ImageButton>(R.id.btn_bottom_photo).setOnClickListener {
            file = requireContext().createTempImageFile()
            file?.let {
                photoUri = FileProvider.getUriForFile(
                    requireContext(),
                    "ru.newlevel.hordemap.app",
                    it
                )
            }
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            takePicture.launch(photoUri)
        }
        viewBehavior.findViewById<ImageButton>(R.id.btn_bottom_gallery).setOnClickListener {
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            pickImage.launch("image/*")
        }
        viewBehavior.findViewById<ImageButton>(R.id.btn_bottom_file).setOnClickListener {
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            mActivityLauncher.launch("*/*")
        }

    }

    private fun setupCloseMessengerButton() {
        binding.btnGoBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onButtonSaveClick(uri: String, fileName: String) {
        if (!isDownloadingState) {
            lifecycleScope.launch {
                messengerViewModel.downloadFile(requireContext(), Uri.parse(uri), fileName).onFailure {
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(
                requireContext(),
                requireContext().resources.getString(R.string.wait_download),
                Toast.LENGTH_LONG
            )
                .show()
            return
        }
    }

    override fun onImageClick(url: String) {
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

    private fun setUpCloseReplyButton() {
        binding.btnReplyClose.setOnClickListener {
            closeBottomInfoWindow()
        }
    }

    private fun closeBottomInfoWindow() {
        binding.recyclerViewMessages.animateButtonPaddingReverse()
        binding.rootLinearReply.hideToBottomAnimation()
        editMessageId = null
        replyMessageId = null
        binding.replyTextMessage.text = ""
        binding.replyName.text = ""
        binding.editTextMessage.setText("")
    }

    private fun showOutMessagePopupMenu(itemView: View, message: MyMessageEntity, x: Float, y: Float) {
        isPopUpShow = true
        binding.shadow.showShadowAnimate()
        val mainPopupMenu = PopupWindow(requireContext())
        mainPopupMenu.inputMethodMode = PopupWindow.INPUT_METHOD_NEEDED
        mainPopupMenu.contentView = layoutInflater.inflate(
            R.layout.popup_message_out,
            itemView.rootView as ViewGroup,
            false
        )
        mainPopupMenu.setBackgroundDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.round_white
            )
        )
        mainPopupMenu.elevation = 18f
        mainPopupMenu.isFocusable = false
        mainPopupMenu.isOutsideTouchable = true
        mainPopupMenu.contentView?.findViewById<MaterialButton>(R.id.btnDeleteMessage)
            ?.setOnClickListener {
                mainPopupMenu.dismiss()
                CoroutineScope(Dispatchers.IO).launch {
                    messengerViewModel.deleteMessage(message)
                }
            }
        mainPopupMenu.contentView?.findViewById<MaterialButton>(R.id.btnEditMessage)
            ?.setOnClickListener {
                mainPopupMenu.dismiss()
                showEditWindow(message)
                binding.editTextMessage.setText(message.message)
            }
        mainPopupMenu.contentView?.findViewById<MaterialButton>(R.id.btnReplyMessage)
            ?.setOnClickListener {
                mainPopupMenu.dismiss()
                showReplyWindow(message)
            }
        mainPopupMenu.showAtLocation(itemView, Gravity.NO_GRAVITY, x.toInt(), y.toInt())
        mainPopupMenu.setOnDismissListener {
            binding.shadow.hideShadowAnimate()
            CoroutineScope(Dispatchers.Main).launch {
                delay(300)
                isPopUpShow = false
            }

        }
    }

    private fun showInMessagePopupMenu(itemView: View, message: MyMessageEntity, x: Float, y: Float) {
        isPopUpShow = true
        binding.shadow.showShadowAnimate()
        val mainPopupMenu = PopupWindow(requireContext())
        mainPopupMenu.inputMethodMode = PopupWindow.INPUT_METHOD_NEEDED
        mainPopupMenu.contentView = layoutInflater.inflate(
            R.layout.popup_message_in,
            itemView.rootView as ViewGroup,
            false
        )
        mainPopupMenu.setBackgroundDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.round_white
            )
        )
        mainPopupMenu.elevation = 18f
        mainPopupMenu.isFocusable = false
        mainPopupMenu.isOutsideTouchable = true
        mainPopupMenu.contentView?.findViewById<MaterialButton>(R.id.btnCopyMessage)?.setOnClickListener {
            mainPopupMenu.dismiss()
            requireContext().copyTextInSystem(message.message)
        }
        mainPopupMenu.contentView?.findViewById<MaterialButton>(R.id.btnReplyMessage)
            ?.setOnClickListener {
                mainPopupMenu.dismiss()
                showReplyWindow(message)
            }
        mainPopupMenu.showAtLocation(itemView, Gravity.NO_GRAVITY, x.toInt(), y.toInt())
        mainPopupMenu.setOnDismissListener {
            binding.shadow.hideShadowAnimate()
            CoroutineScope(Dispatchers.Main).launch {
                delay(300)
                isPopUpShow = false
            }
        }
    }

    private fun showReplyWindow(message: MyMessageEntity) {
        replyMessageId = message.timestamp
        editMessageId = null
        binding.recyclerViewMessages.animateButtonPadding()
        binding.rootLinearReply.showFromBottomAnimation()
        binding.replyTextMessage.text = message.message
        val userName = requireContext().getString(R.string.reply_to) + " ${message.userName}"
        binding.replyName.text = userName
        NameColors.values().find { it.id == message.selectedMarker }?.let {
            binding.replyName.setTextColor(ContextCompat.getColor(requireContext(), it.resourceId))
        }
    }

    private fun showEditWindow(message: MyMessageEntity) {
        replyMessageId = if (message.replyOn > 0L) message.replyOn else null
        editMessageId = message.timestamp
        binding.recyclerViewMessages.animateButtonPadding()
        binding.rootLinearReply.showFromBottomAnimation()
        binding.replyTextMessage.text = message.message
        binding.replyName.text = requireContext().getText(R.string.editing)
        binding.replyTextMessage.tag = message.timestamp
        NameColors.values().find { it.id == message.selectedMarker }?.let {
            binding.replyName.setTextColor(ContextCompat.getColor(requireContext(), it.resourceId))
        }
    }

    override fun onItemClick(message: MyMessageEntity, itemView: View, x: Float, y: Float, isInMessage: Boolean) {
        if (mBottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED)
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        else {
            if (isInMessage && !isPopUpShow)
                showInMessagePopupMenu(message = message, itemView = itemView, x = x, y = y)
            else if (!isPopUpShow)
                showOutMessagePopupMenu(message = message, itemView = itemView, x = x, y = y)
        }
    }

    override fun onReplyClick(message: MyMessageEntity) {
        if (mBottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED)
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        else {
            val position = mMessageAdapter.getPosition(message)
            mRecyclerView.smoothScrollToPosition(position - 1)
            CoroutineScope(Dispatchers.Main).launch {
                delay(250)
                mMessageLayoutManager.findViewByPosition(position)?.findViewById<FrameLayout>(R.id.rootFrame)
                    ?.blinkAndHideShadow()
            }
        }
    }

    private fun requestWriteExternalStoragePermission() {
        if (!requireContext().hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_CODE_WRITE_EXTERNAL_STORAGE
            )
        }
    }

    override fun onFileDescriptionDialogDismiss() {
        binding.shadow.hideShadowAnimate()
    }

    override fun onFileDescriptionReceived(description: String, photoUri: Uri, fileName: String, fileSize: Long) {
        binding.shadow.hideShadowAnimate()
        lifecycleScope.launch {
            binding.imgLoading.visibility = View.VISIBLE
            val anim = binding.imgLoading.loadAnimation()
            isDownloadingState = true
            messengerViewModel.sendFile(description, photoUri, fileName, fileSize).onFailure {
                Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
            }
            if (this@MessengerFragment.isAdded) {
                isDownloadingState = false
                anim.cancel()
                binding.imgLoading.visibility = GONE
            }
        }
    }
}