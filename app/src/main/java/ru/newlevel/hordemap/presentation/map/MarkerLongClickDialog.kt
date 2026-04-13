package ru.newlevel.hordemap.presentation.map


import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.android.gms.maps.model.LatLng
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.data.db.MarkerEntity
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.presentation.map.utils.GaussKrugerConverter
import ru.newlevel.hordemap.presentation.views.CustomMapDialogDrawable


class MarkerLongClickDialog(
    private val point: Point,
    private val marker: MarkerEntity,
    private val onDelete: () -> Unit,
    private val onShowDistance: (LatLng) -> Unit
) : DialogFragment() {

    @SuppressLint("DefaultLocale")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        //   dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setDimAmount(0f)
        dialog.setContentView(R.layout.popup_marker_click)
        dialog.findViewById<TextView>(R.id.tvMarkerTitle).text = marker.title
        val linearCoordinates = dialog.findViewById<LinearLayout>(R.id.linear_coordinates)
        val tvLatitude = dialog.findViewById<TextView>(R.id.tvLatitude)
        val tvLongitude = dialog.findViewById<TextView>(R.id.tvLongitude)

        val linearCoordinatesGauss = dialog.findViewById<LinearLayout>(R.id.linear_coordinates_gauss)
        val tvGaussLeft = dialog.findViewById<TextView>(R.id.tvGaussLeft)
        val tvGaussRight = dialog.findViewById<TextView>(R.id.tvGaussRight)

        dialog.findViewById<LinearLayout>(R.id.root).apply {
            background = CustomMapDialogDrawable(requireContext())
            invalidate()
            requestLayout()
        }

        dialog.findViewById<TextView>(R.id.btnDeleteMarker).setOnClickListener {
            onDelete()
            dismiss()
        }

        dialog.findViewById<TextView>(R.id.btnMarkerShowDistance).setOnClickListener {
            onShowDistance(LatLng(marker.latitude, marker.longitude))
            dismiss()
        }

        if (UserEntityProvider.userEntity.showCoordinates) {
            linearCoordinates.visibility = View.VISIBLE
            tvLatitude.text = String.format("%.5f° N", marker.latitude)
            tvLongitude.text = String.format("%.5f° E", marker.longitude)
        } else {
            linearCoordinates.visibility = View.GONE
        }
        if (UserEntityProvider.userEntity.showGaussCoordinates) {
            linearCoordinatesGauss.visibility = View.VISIBLE
            val (x, y) = GaussKrugerConverter().wgs84ToSK42(
                marker.latitude,
                marker.longitude
            )
            tvGaussLeft.text = "X: $x"
            tvGaussRight.text = "Y: $y"
        } else {
            linearCoordinatesGauss.visibility = View.GONE
        }


        return dialog
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val params = dialog?.window?.attributes
        params?.gravity = Gravity.TOP or Gravity.START
        params?.x = point.x
        params?.y = point.y
        dialog?.window?.attributes = params
    }
}