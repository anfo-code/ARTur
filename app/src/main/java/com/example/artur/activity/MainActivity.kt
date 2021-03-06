package com.example.artur.activity

import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.get
import com.example.artur.R
import com.example.artur.databinding.ActivityMainBinding
import com.example.artur.dialogs.BrushDialog
import com.example.artur.utils.BitmapFromGalleryGetter
import com.example.artur.utils.BitmapSaver
import com.example.artur.utils.Constants
import com.example.artur.utils.Permissions

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMainBinding
    private var currentColorImageButtonSelected: ImageButton? = null
    private lateinit var bitmapFromGalleryGetter: BitmapFromGalleryGetter

    //TODO finish MainActivity functionality

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        bitmapFromGalleryGetter = BitmapFromGalleryGetter(this, binding)

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        turnOffDarkMode()

        setBasicBrushSettings()

        binding.ibBrush.setOnClickListener(this)
        binding.ibGallery.setOnClickListener(this)
        binding.ibSave.setOnClickListener(this)
        binding.ibUndo.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.ib_brush -> {
                showChangeBrushSizeDialog()
            }
            R.id.ib_gallery -> {
                initiateChoosingPhotoFromGallery()
            }
            R.id.ib_undo -> {
                binding.drawingView.undoOneAction()
            }
            R.id.ib_save -> {
                saveBitmapToStorage()
            }
        }
    }

    private fun showChangeBrushSizeDialog() {
        BrushDialog(this, binding).createBrushDialog()
    }

    private fun initiateChoosingPhotoFromGallery() {
        if (Permissions(this, this).checkPermissions()) {
            bitmapFromGalleryGetter.getPhotoFromGallery()
        }
    }

    private fun saveBitmapToStorage() {
        if (Permissions(this, this).checkPermissions()) {
            val bitmapSaver = BitmapSaver(binding.drawingView, this)
            bitmapSaver.savePhotoToStorage()

            bitmapSaver.sendToast()
        }
    }

    private fun setBasicBrushSettings() {
        //Set the first element of llPaintColors as the basic one
        val basicColorImageButton = binding.llPaintColors[0] as ImageButton

        //Make a selection of the basic color
        setColorButtonClicked(basicColorImageButton)

        currentColorImageButtonSelected = basicColorImageButton
    }

    fun changeClickedPaintBorder(view: View) {
        if (view !== currentColorImageButtonSelected) {
            val imageButton = view as ImageButton

            setColorButtonUnClicked(currentColorImageButtonSelected as ImageButton)

            currentColorImageButtonSelected = if (imageButton.tag.toString() == Constants.ERASER) {
                binding.drawingView.activateEraser()
                setColorButtonClicked(imageButton)

                imageButton
            } else {
                binding.drawingView.disableEraser()
                setColorButtonClicked(imageButton)
                setBrushColor(imageButton)

                imageButton
            }
        }
    }

    private fun setBrushColor(imageButton: ImageButton) {
        val colorTag = imageButton.tag.toString()
        binding.drawingView.setBrushColor(colorTag)
    }

    private fun setColorButtonClicked(imageButton: ImageButton) {
        imageButton.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.pallet_pressed)
        )
    }

    private fun setColorButtonUnClicked(imageButton: ImageButton) {
        imageButton.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.pallet_normal)
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_AND_WRITE_PERMISSIONS_GRANTED) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission was granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    this,
                    "Oops! You have just declined the permission! \n" +
                            "To allow it now, you need to allow the permission in settings of your phone...",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun turnOffDarkMode() {
        val appSettingPrefs: SharedPreferences = getSharedPreferences("AppSettingPrefs", 0)
        val sharedPreferencesEditor: SharedPreferences.Editor = appSettingPrefs.edit()

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        sharedPreferencesEditor.putBoolean("NightMode", true)
        sharedPreferencesEditor.apply()
    }
}