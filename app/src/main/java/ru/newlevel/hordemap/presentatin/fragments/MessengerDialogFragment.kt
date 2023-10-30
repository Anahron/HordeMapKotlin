package ru.newlevel.hordemap.presentatin.fragments

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jsibbold.zoomage.ZoomageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.app.SelectFilesContract
import ru.newlevel.hordemap.app.makeShortToast
import ru.newlevel.hordemap.data.storage.models.MessageDataModel
import ru.newlevel.hordemap.databinding.MessagesDialogBinding
import ru.newlevel.hordemap.presentatin.MessagesAdapter
import ru.newlevel.hordemap.presentatin.viewmodels.MessengerViewModel
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MessengerDialogFragment : DialogFragment(R.layout.messages_dialog),
    MessagesAdapter.OnButtonSaveClickListener, MessagesAdapter.OnImageClickListener {

    private val binding: MessagesDialogBinding by viewBinding()
    private val messengerViewModel by viewModel<MessengerViewModel>()

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MessagesAdapter
    private lateinit var file: File
    private lateinit var uri: Uri

    private lateinit var activityLauncher: ActivityResultLauncher<String>
    private lateinit var pickImage: ActivityResultLauncher<String>
    private lateinit var takePicture: ActivityResultLauncher<Uri>

    private var isDownloadingState = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityLauncher = registerForActivityResult(SelectFilesContract()) { result ->
            lifecycleScope.launch {
                if (result != null) {
                    messengerViewModel.sendFile(
                        result, getFileNameFromUri(result), getFileSizeFromUri(result)
                    )
                }
            }
        }
        pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            lifecycleScope.launch {
                if (uri != null) {
                    messengerViewModel.sendFile(
                        uri, getFileNameFromUri(uri), getFileSizeFromUri(uri)
                    )
                }
            }
        }
        takePicture =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess: Boolean ->
                if (isSuccess) {
                    messengerViewModel.sendFile(
                        uri, getFileNameFromUri(uri), getFileSizeFromUri(uri)
                    )
                }
            }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )

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

        setupRecyclerView()
        setupNewMessageAnnounces()
        createProgressBar()
        setupScrollDownButton()
        setupEditTextMessage()
        setupSendMessageButton()
        setupUploadFileButton()
        setupOpenCameraButton()
        setupCloseMessengerButton()

        messengerViewModel.startMessageUpdate()

        messengerViewModel.messagesLiveData.observe(this) { messages ->
            if (adapter.itemCount < messages.size) {
                val onDown =
                    recyclerView.canScrollVertically(1) && recyclerView.computeVerticalScrollRange() > recyclerView.height
                adapter.setMessages(messages as ArrayList<MessageDataModel>)
                if (onDown) {
                    binding.newMessage.visibility = View.VISIBLE
                } else {
                    recyclerView.scrollToPosition(adapter.itemCount - 1)
                }
            }
        }

        messengerViewModel.progressLiveData.observe(this) { progress ->
            if (progress < 1000) {
                isDownloadingState = true
                binding.progressBar.visibility = View.VISIBLE
                binding.progressBar.setProgress(progress, true)
                binding.progressText.visibility = View.VISIBLE
            } else {
                isDownloadingState = false
                binding.progressBar.visibility = View.GONE
                binding.progressText.visibility = View.GONE
            }
        }

        recyclerView.setOnScrollChangeListener { _, _, _, _, _ ->
            if (!recyclerView.canScrollVertically(1) && recyclerView.computeVerticalScrollOffset() > 0) {
                binding.newMessage.visibility = View.GONE
            }
        }
        //  слушатель размера экрана для прокрутки элементов при открытии клавиатуры
        binding.activityRoot.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect()
            binding.activityRoot.getWindowVisibleDisplayFrame(r)
            val screenHeight = binding.activityRoot.rootView.height
            val keypadHeight = r.bottom - screenHeight
            // если высота клавиатуры больше 15% от экрана, считаем клавиатуру открытой
            if (keypadHeight > screenHeight * 0.15) {
                if (adapter.itemCount > 0) recyclerView.smoothScrollToPosition(
                    adapter.itemCount - 1
                )
            }
        }

        dialog?.setOnDismissListener {
            messengerViewModel.stopMessageUpdate()
            this@MessengerDialogFragment.dismissAllowingStateLoss()
        }
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        val contentResolver = requireContext().contentResolver
        val cursor = contentResolver.query(uri, null, null, null, null)

        return cursor?.use { c ->
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
        uri = FileProvider.getUriForFile(
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
                    1 -> takePicture.launch(uri) // Фотографирование с камеры
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
        recyclerView = binding.recyclerViewMessages
        binding.recyclerViewMessages.layoutManager = LinearLayoutManager(requireContext())
        adapter = MessagesAdapter(this, this)
        binding.recyclerViewMessages.adapter = adapter
    }

    private fun setupScrollDownButton() {
        binding.goDown.setOnClickListener {
            recyclerView.scrollToPosition(
                adapter.itemCount.minus(1)
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
        }) // отпрака по нажатию энтер на клавиатуре
    }

    private fun setupSendMessageButton() { // Отправка сообщения
        binding.buttonSend.scaleType = ImageView.ScaleType.CENTER_INSIDE
        binding.buttonSend.setOnClickListener {
            val text = binding.editTextMessage.text.toString()
            if (text.isNotEmpty()) messengerViewModel.sendMessage(text)
            binding.editTextMessage.setText("")
            binding.editTextMessage.requestFocus()
        }
    }

    private fun setupUploadFileButton() { // Отправка файла
        binding.buttonSendFile.setOnClickListener {
            activityLauncher.launch("*/*")
            binding.editTextMessage.requestFocus()
        }
    }

    private fun setupOpenCameraButton() {
        binding.buttonPhoto.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    context!!, Manifest.permission.CAMERA
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
        binding.closeMassager.setOnClickListener { dialog?.dismiss() }
    }

    private fun createProgressBar() {
        binding.progressText.visibility = View.INVISIBLE
        binding.progressBar.visibility = View.INVISIBLE
    }

    private fun setupNewMessageAnnounces() {
        binding.newMessage.setOnClickListener {
            recyclerView.smoothScrollToPosition(adapter.itemCount - 1)
            binding.newMessage.visibility = View.GONE
        }
    }

    override fun onPause() {
        super.onPause()
        messengerViewModel.stopMessageUpdate()
    }

    override fun onResume() {
        super.onResume()
        messengerViewModel.startMessageUpdate()
    }

    companion object {
        private const val REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 1010
        private const val REQUEST_CODE_CAMERA_PERMISSION = 1011
    }

    override fun onButtonSaveClick(uri: String, fileName: String) {
        if (!isDownloadingState) {
            isDownloadingState = true
            runBlocking {
                launch(Dispatchers.IO) {
                    messengerViewModel.downloadFile(requireContext(), Uri.parse(uri), fileName)
                }
            }
        } else {
            makeShortToast("Дождитесь окончания загрузки", requireContext())
            return
        }
    }

    override fun onImageClick(url: String) {
            val dialog = Dialog(
                requireContext(),
                android.R.style.Theme_Black_NoTitleBar_Fullscreen
            )
            dialog.setContentView(R.layout.fragment_full_screen_image)
            val imageView =
                dialog.findViewById<ZoomageView>(R.id.myZoomageView)
            Glide.with(requireContext())
                .load(url)
                .into(imageView)
            dialog.show()
    }
}