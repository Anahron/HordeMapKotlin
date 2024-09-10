package ru.newlevel.hordemap.presentation.map

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.databinding.OnMapClickDialogBinding

class OnMapClickInfoDialog(private val dialogResult: (description: String, checkedItem: Int, isLocal: Boolean) -> Unit) :
    DialogFragment(R.layout.on_map_click_dialog) {

    private val binding: OnMapClickDialogBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var checkedRadioButton = 0

        val descriptionEditText =
            binding.descriptionEditText
        val numberPointText =
            binding.descriptionEditTextNumber
        var description = "Маркер"
        dialog?.window?.setBackgroundDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.round_white
            )
        )

        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedRadioButton = binding.radioGroup.findViewById<RadioButton>(checkedId)
            val checkedTag = selectedRadioButton.tag.toString()
            checkedRadioButton = selectedRadioButton.tag.toString().toInt()
            if (selectedRadioButton.tag.toString() == "10")
                numberPointText.visibility = View.VISIBLE
            else
                numberPointText.visibility = View.GONE
            setAlphaToRadioButtons(checkedTag)
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
            dialogResult(description, checkedRadioButton, binding.checkBoxForMe.isChecked)
            dialog?.dismiss()
        }
    }

    private fun setAlphaToRadioButtons(checkedTag: String) {
        for (i in 0 until binding.radioGroup.childCount) {
            val radioButton = binding.radioGroup.getChildAt(i) as? RadioButton
            radioButton?.alpha = if (radioButton?.tag == checkedTag) 1.0f else 0.3f
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        this@OnMapClickInfoDialog.dismissAllowingStateLoss()
        super.onDismiss(dialog)
    }
}