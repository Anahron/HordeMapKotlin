package ru.newlevel.hordemap.presentation.messenger

import android.Manifest
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
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
import com.jsibbold.zoomage.ZoomageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.app.REQUEST_CODE_CAMERA_PERMISSION
import ru.newlevel.hordemap.app.REQUEST_CODE_WRITE_EXTERNAL_STORAGE
import ru.newlevel.hordemap.app.SelectFilesContract
import ru.newlevel.hordemap.app.TAG
import ru.newlevel.hordemap.app.convertDpToPx
import ru.newlevel.hordemap.app.createTempImageFile
import ru.newlevel.hordemap.app.getFileNameFromUri
import ru.newlevel.hordemap.app.getFileSizeFromUri
import ru.newlevel.hordemap.app.hasPermission
import ru.newlevel.hordemap.app.hideShadowAnimate
import ru.newlevel.hordemap.app.showShadowAnimate
import ru.newlevel.hordemap.data.storage.models.MessageDataModel
import ru.newlevel.hordemap.databinding.FragmentMessengerBinding
import java.io.File

class MessengerFragment : Fragment(R.layout.fragment_messenger),
    MessagesAdapter.OnButtonSaveClickListener,
    MessagesAdapter.OnImageClickListener,
    SendFileDescriptionDialogFragment.OnFileDescriptionListener {

    private val binding: FragmentMessengerBinding by viewBinding()
    private val messengerViewModel by viewModel<MessengerViewModel>()

    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: MessagesAdapter
    private lateinit var messageLayoutManager: LinearLayoutManager
    private lateinit var usersRecyclerView: RecyclerView
    private lateinit var usersRecyclerViewAdapter: UsersAdapter
    private lateinit var userLayoutManager: LinearLayoutManager
    private lateinit var usersPopupMenu: PopupWindow
    private var file: File? = null
    private lateinit var photoUri: Uri
    private var isDownloadingState = false
    private lateinit var mBottomSheetBehavior: BottomSheetBehavior<*>
    private lateinit var activityLauncher: ActivityResultLauncher<String>
    private lateinit var pickImage: ActivityResultLauncher<String>
    private lateinit var takePicture: ActivityResultLauncher<Uri>
    private lateinit var viewBehavior: View

    private fun createActivityRegisters() {
        activityLauncher = registerForActivityResult(SelectFilesContract()) { uri: Uri? ->
            if (uri != null) {
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUIComponents()
        requestWriteExternalStoragePermission()
        setupRecyclerView()
        setupMessagesUpdates()
        showInputTextAnimation()
        createActivityRegisters()
        setupUsersRecyclerView()
        setupUsersCountButton()
    }
    private fun setupUIComponents() {
        setupBottomSheetBehavior()
        setupEditTextMessage()
        setupSendMessageButton()
        setupAttachBtn()
        setupCloseMessengerButton()
        setupProgressBar()
        setupScrollDownButton()
        setupBottomBehaviorListeners()
    }

    private fun setupUsersCountButton() {
        binding.tvUsersCount.setOnClickListener {
            showMainPopupMenu(it)
        }
    }

    private fun setupUsersRecyclerView() {
        usersPopupMenu = PopupWindow(requireContext())
        usersPopupMenu.contentView = layoutInflater.inflate(
            R.layout.popup_users,
            binding.tvUsersCount.rootView as ViewGroup,
            false
        )
        usersPopupMenu.setBackgroundDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.round_white
            )
        )
        usersPopupMenu.height = requireContext().convertDpToPx(200)
        usersPopupMenu.elevation = 18f
        usersPopupMenu.isFocusable = true
        usersPopupMenu.contentView?.findViewById<RecyclerView>(R.id.rvUsersCount)?.let {
            usersRecyclerView = it
        }
        usersRecyclerViewAdapter = UsersAdapter()
        userLayoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = false
            initialPrefetchItemCount = 30
        }
        usersRecyclerView.apply {
            layoutManager = userLayoutManager
            adapter = usersRecyclerViewAdapter
            setHasFixedSize(true)
            isNestedScrollingEnabled = false
        }
    }

    private fun showMainPopupMenu(itemDotsView: View) {
        binding.shadow.showShadowAnimate()
        usersPopupMenu.showAsDropDown(
            itemDotsView,
            -requireContext().convertDpToPx(104),
            0
        )
        usersPopupMenu.setOnDismissListener {
            binding.shadow.hideShadowAnimate()
        }
    }

    private fun handleNewMessages(messages: List<MessageDataModel>) {
        if (messageAdapter.itemCount < messages.size) {
            val onDown = !recyclerView.canScrollVertically(1)
                    //&& recyclerView.computeVerticalScrollRange() > recyclerView.height
            messageAdapter.setMessages(messages as ArrayList<MessageDataModel>)
            Log.e(TAG, "onDown = " + onDown)
            if (!onDown) {
                recyclerView.scrollToPosition(messageAdapter.itemCount - 1)
            }
        }
    }

    private fun handleProgressUpdate(progress: Int) {
        if (progress < 1000) {
            isDownloadingState = true
            binding.progressBar.visibility = VISIBLE
            binding.progressBar.setProgress(progress, true)
            binding.progressText.visibility = VISIBLE
        } else {
            isDownloadingState = false
            binding.progressBar.visibility = GONE
            binding.progressText.visibility = GONE
        }
    }

    private fun setupMessagesUpdates() {
        val lifecycle = viewLifecycleOwner.lifecycle
        lifecycle.coroutineScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                if (messageAdapter.itemCount < 1)
                    delay(350)
                messengerViewModel.startMessageUpdate()
                messengerViewModel.messagesLiveData.observe(viewLifecycleOwner) { messages ->
                    handleNewMessages(messages)
                }
                messengerViewModel.progressLiveData.observe(viewLifecycleOwner) { progress ->
                    handleProgressUpdate(progress)
                }
                messengerViewModel.usersProfileLiveData.observe(viewLifecycleOwner) { profiles ->
                    usersRecyclerViewAdapter.setMessages(profiles)
                    binding.tvUsersCount.text = profiles.size.toString()
                }
            }
            Log.e(
                TAG,
                " messengerViewModel.stopMessageUpdate()"
            )
            messengerViewModel.stopMessageUpdate()
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
        messageAdapter = MessagesAdapter(this, this)
        messageLayoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
            initialPrefetchItemCount = 30
        }
        recyclerView = binding.recyclerViewMessages.apply {
            layoutManager = messageLayoutManager
            adapter = messageAdapter
            setHasFixedSize(false)
            isNestedScrollingEnabled = false
            setOnScrollChangeListener { _, _, _, _, _ ->
                if (!recyclerView.canScrollVertically(1)
                 //   && recyclerView.computeVerticalScrollRange() > recyclerView.height
                    ) {
                    if (binding.btnGoDown.translationX != 500F) {
                        showOrHideDownBtn(false)
                    }
                } else {
                    if (binding.btnGoDown.translationX == 500F) {
                        showOrHideDownBtn(true)
                        binding.btnGoDown.visibility = VISIBLE
                    }
                }
            }
        }
    }

    private fun showOrHideDownBtn(isNeedToShow: Boolean) {
        val btnGoDown = binding.btnGoDown
        if (isNeedToShow) {
            btnGoDown.translationX = 500f
            val animator = ObjectAnimator.ofFloat(btnGoDown, "translationX", 0f)
            animator.duration = 500
            animator.start()
        } else {
            btnGoDown.translationX = 0f
            val animator = ObjectAnimator.ofFloat(btnGoDown, "translationX", 500f)
            animator.duration = 500
            animator.start()
        }
    }

    private fun setupScrollDownButton() {
        binding.btnGoDown.translationX = 500f
        binding.btnGoDown.setOnClickListener {
            recyclerView.smoothScrollToPosition(
                messageAdapter.itemCount.minus(1)
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
                if (text.isNotEmpty())
                    lifecycleScope.launch(Dispatchers.IO) {
                        messengerViewModel.sendMessage(text)
                    }
                binding.editTextMessage.setText("")
                binding.editTextMessage.requestFocus()
                return@OnEditorActionListener true
            }
            false
        })
    }

    private fun setupSendMessageButton() {
        binding.buttonSend.setOnClickListener {
            val text = binding.editTextMessage.text.toString().trim()
            if (text.isNotEmpty())
                lifecycleScope.launch(Dispatchers.IO) {
                    messengerViewModel.sendMessage(text)
                }
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
            takePicture.launch(photoUri) // Фотографирование с камеры
        }
        viewBehavior.findViewById<ImageButton>(R.id.btn_bottom_gallery).setOnClickListener {
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            pickImage.launch("image/*") // Выбор из галереи
        }
        viewBehavior.findViewById<ImageButton>(R.id.btn_bottom_file).setOnClickListener {
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            activityLauncher.launch("*/*")
        }

    }

    private fun setupCloseMessengerButton() {
        binding.btnGoBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupProgressBar() {
        binding.progressText.visibility = View.INVISIBLE
        binding.progressBar.visibility = View.INVISIBLE
    }

    override fun onButtonSaveClick(uri: String, fileName: String) {
        if (!isDownloadingState) {
            isDownloadingState = true
            lifecycleScope.launch(Dispatchers.IO) {
                messengerViewModel.downloadFile(requireContext(), Uri.parse(uri), fileName)
            }
        } else {
            Toast.makeText(requireContext(), "${R.string.wait_download}", Toast.LENGTH_LONG).show()
            return
        }
    }

    override fun onImageClick(url: String) {
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

    private fun showInputTextAnimation() {
        val inputLayout = binding.inputLayout
        inputLayout.translationY = requireContext().convertDpToPx(55).toFloat()
        val animator = ObjectAnimator.ofFloat(inputLayout, "translationY", 0f)
        animator.duration = 300
        animator.start()
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

    override fun onFileDescriptionReceived(description: String, photoUri: Uri, fileName: String, fileSize: Long) {
        messengerViewModel.sendFile(description, photoUri, fileName, fileSize)
    }
}