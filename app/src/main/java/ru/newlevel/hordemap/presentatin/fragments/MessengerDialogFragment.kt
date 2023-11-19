package ru.newlevel.hordemap.presentatin.fragments

import android.Manifest
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.KeyEvent
import android.view.View
import android.view.View.GONE
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import android.widget.TextView.VISIBLE
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jsibbold.zoomage.ZoomageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.app.SelectFilesContract
import ru.newlevel.hordemap.app.makeShortToast
import ru.newlevel.hordemap.data.storage.models.MessageDataModel
import ru.newlevel.hordemap.databinding.MessagesDialogBinding
import ru.newlevel.hordemap.presentatin.adapters.MessagesAdapter
import ru.newlevel.hordemap.presentatin.fragments.dialogs.SendFileDescriptionDialogFragment
import ru.newlevel.hordemap.presentatin.viewmodels.MessengerViewModel
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessengerDialogFragment : Fragment(R.layout.messages_dialog),
    MessagesAdapter.OnButtonSaveClickListener,
    MessagesAdapter.OnImageClickListener,
    SendFileDescriptionDialogFragment.OnFileDescriptionListener {

    private val binding: MessagesDialogBinding by viewBinding()
    private val messengerViewModel by viewModel<MessengerViewModel>()

    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: MessagesAdapter
    private lateinit var messageLayoutManager: LinearLayoutManager
    private lateinit var file: File
    private lateinit var photoUri: Uri
    private var isDownloadingState = false

    private lateinit var activityLauncher: ActivityResultLauncher<String>
    private lateinit var pickImage: ActivityResultLauncher<String>
    private lateinit var takePicture: ActivityResultLauncher<Uri>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityLauncher = registerForActivityResult(SelectFilesContract()) { uri: Uri? ->
            if (uri != null) {
                val dialogFragment = SendFileDescriptionDialogFragment(
                    uri,
                    getFileNameFromUri(uri),
                    getFileSizeFromUri(uri),
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
                    getFileNameFromUri(uri),
                    getFileSizeFromUri(uri),
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
                        getFileNameFromUri(photoUri),
                        getFileSizeFromUri(photoUri),
                        this, true
                    )
                    dialogFragment.show(childFragmentManager, "photo_description_dialog")
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestWriteExternalStoragePermission()
        setupRecyclerView()
        setupUIComponents()
        setupMessagesUpdates()
        showAnimation()
    }

    private fun showAnimation() {
        val inputLayout = binding.inputLayout
        inputLayout.translationY = 500f
        val animator = ObjectAnimator.ofFloat(inputLayout, "translationY", 0f)
        animator.duration = 500
        animator.start()

    }

    private fun requestWriteExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_CODE_WRITE_EXTERNAL_STORAGE
            )
        }
    }

    private fun handleNewMessages(messages: List<MessageDataModel>) {
        if (messageAdapter.itemCount < messages.size) {
            val onDown =
                recyclerView.canScrollVertically(1) && recyclerView.computeVerticalScrollRange() > recyclerView.height
            messageAdapter.setMessages(messages as ArrayList<MessageDataModel>)
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
                    delay(250)
                messengerViewModel.startMessageUpdate()
                messengerViewModel.messagesLiveData.observe(viewLifecycleOwner) { messages ->
                    handleNewMessages(messages)
                }
                messengerViewModel.progressLiveData.observe(viewLifecycleOwner) { progress ->
                    handleProgressUpdate(progress)
                }
            }
            messengerViewModel.stopMessageUpdate()
        }
    }

    private fun setupUIComponents() {
        setupEditTextMessage()
        setupSendMessageButton()
        setupUploadFileButton()
        setupOpenCameraButton()
        setupCloseMessengerButton()
        setupProgressBar()
        setupScrollDownButton()
    }

    private fun getFileNameFromUri(uri: Uri): String {
        val contentResolver = requireContext().contentResolver
        val cursor = contentResolver.query(uri, null, null, null, null) ?: return ""
        return cursor.use { c ->
            val nameIndex = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) {
                c.moveToFirst()
                c.getString(nameIndex)
            } else {
                ""
            }
        }
    }

    private fun getFileSizeFromUri(uri: Uri): Long {
        val contentResolver = requireContext().contentResolver
        val cursor = contentResolver.query(uri, null, null, null, null)
        return cursor?.use { c ->
            val sizeIndex = c.getColumnIndex(OpenableColumns.SIZE)
            if (sizeIndex != -1) {
                c.moveToFirst()
                c.getLong(sizeIndex)
            } else {
                0
            }
        } ?: 0
    }

    private fun openPhotoButtonClick(context: Context) {
        file = createTempImageFile(context)!!
        photoUri = FileProvider.getUriForFile(
            requireContext(),
            "ru.newlevel.hordemap.app",
            file
        )
        val items = arrayOf("Выбрать из галереи", "Сделать фото")
        MaterialAlertDialogBuilder(context)
            .setTitle("Выберите действие")
            .setItems(items) { _, which ->
                when (which) {
                    0 -> pickImage.launch("image/*") // Выбор из галереи
                    1 -> takePicture.launch(photoUri) // Фотографирование с камеры
                }
            }
            .show()
    }

    private fun createTempImageFile(context: Context): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = context.filesDir
        try {
            return File.createTempFile(imageFileName, ".jpg", storageDir)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
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
            setOnScrollChangeListener { _, _, _, _, _ ->
                if (!recyclerView.canScrollVertically(1) && recyclerView.computeVerticalScrollOffset() > 0) {
                    binding.btnGoDown.visibility = GONE
                }else
                    binding.btnGoDown.visibility = VISIBLE
            }
        }
    }

    private fun setupScrollDownButton() {
        binding.btnGoDown.setOnClickListener {
            recyclerView.smoothScrollToPosition(
                messageAdapter.itemCount.minus(1)
            )
        }
    }

    private fun setupEditTextMessage() {
        binding.editTextMessage.setOnEditorActionListener(OnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val text = binding.editTextMessage.text.toString()
                if (text.isNotEmpty()) messengerViewModel.sendMessage(text)
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
                messengerViewModel.sendMessage(text)
            binding.editTextMessage.setText("")
            binding.editTextMessage.requestFocus()
        }
    }

    private fun setupUploadFileButton() {
        binding.buttonSendFile.setOnClickListener {
            activityLauncher.launch("*/*")
            binding.editTextMessage.requestFocus()
        }
    }

    private fun setupOpenCameraButton() {
        binding.buttonPhoto.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    (context as Activity?)!!,
                    arrayOf(Manifest.permission.CAMERA),
                    REQUEST_CODE_CAMERA_PERMISSION
                )
            } else {
                openPhotoButtonClick(requireContext())
            }
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
            makeShortToast("Дождитесь окончания загрузки", requireContext())
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

    override fun onFileDescriptionReceived(
        description: String,
        photoUri: Uri,
        fileName: String,
        fileSize: Long
    ) {
        messengerViewModel.sendFile(description, photoUri, fileName, fileSize)
    }

    companion object {
        private const val REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 1010
        private const val REQUEST_CODE_CAMERA_PERMISSION = 1011
    }
}