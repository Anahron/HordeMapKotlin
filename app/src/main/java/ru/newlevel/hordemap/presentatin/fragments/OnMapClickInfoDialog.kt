package ru.newlevel.hordemap.presentatin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.fragment.app.DialogFragment
import com.google.android.gms.maps.model.LatLng
import ru.newlevel.hordemap.databinding.OnMapClickDialogBinding
import ru.newlevel.hordemap.presentatin.viewmodels.MapViewModel

class OnMapClickInfoDialog(private val mapViewModel: MapViewModel, private val latLng: LatLng) :
    DialogFragment() {

    private lateinit var binding: OnMapClickDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = OnMapClickDialogBinding.inflate(inflater, container, false)

        var checkedRadioButton = 0

        val descriptionEditText =
            binding.descriptionEditText
        val numberPointText =
            binding.descriptionEditTextNumber
        var description = "Маркер"

        // Установка OnClickListener для каждой ImageView
        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedRadioButton = binding.radioGroup.findViewById<RadioButton>(checkedId)
            val checkedTag = selectedRadioButton.tag.toString()
            checkedRadioButton = selectedRadioButton.tag.toString().toInt()
            if (selectedRadioButton.tag.toString() == "10")
                numberPointText.visibility = View.VISIBLE
            else
                numberPointText.visibility = View.GONE
            for (i in 0 until binding.radioGroup.childCount) {
                val radioButton = binding.radioGroup.getChildAt(i) as? RadioButton
                radioButton?.alpha = if (radioButton?.tag == checkedTag) 1.0f else 0.3f
            }
        }

        binding.btnCancel.setOnClickListener {
            dialog?.dismiss()
        }

        binding.btnSave.setOnClickListener {
            if (checkedRadioButton == 10) {
                if (numberPointText.text.toString().isNotEmpty()
                ) checkedRadioButton += numberPointText.text.toString().toInt()
            }

            if (descriptionEditText.text.toString().isNotEmpty()) {
                description = descriptionEditText.text.toString()
            }
            mapViewModel.sendMarker(latLng,description,checkedRadioButton)
            dialog?.dismiss()
        }
        return binding.root
    }
}