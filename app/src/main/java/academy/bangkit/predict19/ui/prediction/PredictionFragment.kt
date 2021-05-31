package academy.bangkit.predict19.ui.prediction

import academy.bangkit.predict19.R
import academy.bangkit.predict19.databinding.FragmentPredictionBinding
import android.Manifest
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.io.IOException


class PredictionFragment : Fragment() {

    private var _binding: FragmentPredictionBinding? = null
    private val binding get() = _binding

    private val requestGallery: Int = 1
    private val requestCamera: Int = 2

    private var pickedImgUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentPredictionBinding.inflate(inflater, container, false)

        binding?.btnChooseGallery?.setOnClickListener {
            if (Build.VERSION.SDK_INT >= 22) {
                checkRequestGallery()
            } else {
                openGallery()
            }
        }

        binding?.btnChooseCamera?.setOnClickListener {
            if (Build.VERSION.SDK_INT >= 22) {
                checkRequestCamera()
            } else {
                openCamera()
            }
        }

        return binding?.root
    }

    private fun checkRequestCamera() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (getFromPreference(requireContext())) {
                showSettingAlert()
            } else if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity()
                        , Manifest.permission.CAMERA
                    )
                ) {
                    showAlertDialog()
                } else {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.CAMERA),
                        requestCamera
                    )
                }
            }
        } else if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                Toast.makeText(
                    context,
                    getString(R.string.notif_reqired_permission),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                val requestCodeCamera = 1
                ActivityCompat.requestPermissions(
                    requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    requestCodeCamera
                )
            }
        } else openCamera()

    }

    private fun showAlertDialog() {
        val alertDialog: AlertDialog = AlertDialog.Builder(requireContext()).create()
        alertDialog.setTitle(getString(R.string.label_alert))
        alertDialog.setMessage(getString(R.string.notif_camera_permission))
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.notif_decline)
        ) { dialogInterface, _ -> dialogInterface.dismiss() }
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.notif_accept)
        ) { dialogInterface, _ ->
            dialogInterface.dismiss()
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                requestCamera
            )
        }
        alertDialog.show()
    }


    private fun showSettingAlert() {
        val alertDialog = AlertDialog.Builder(
            requireContext()
        ).create()
        alertDialog.setTitle(getString(R.string.label_alert))
        alertDialog.setMessage(getString(R.string.notif_camera_permission))
        alertDialog.setButton(
            AlertDialog.BUTTON_NEGATIVE, getString(R.string.notif_decline)
        ) { dialogInterface, _ -> dialogInterface.dismiss() }
        alertDialog.setButton(
            AlertDialog.BUTTON_POSITIVE, getString(R.string.label_setting)
        ) { dialogInterface, _ ->
            dialogInterface.dismiss()
            startInstalledAppDetailsActivity(requireActivity())
        }
        alertDialog.show()

    }

    private fun getFromPreference(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences("camera_pref", Context.MODE_PRIVATE)

        return sharedPreferences.getBoolean("ALLOWED", false)

    }

    private fun saveToPreferences(context: Context) {
        val sharedPreferences = context.getSharedPreferences("camera_pref", Context.MODE_PRIVATE)
        val sharedPreferencesEditor = sharedPreferences.edit()
        sharedPreferencesEditor.putBoolean("ALLOWED", true)
        sharedPreferencesEditor.apply()
    }


    private fun checkRequestGallery() {
        if (context?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.READ_EXTERNAL_STORAGE) }
            != PackageManager.PERMISSION_GRANTED){
            if (activity?.let { ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.READ_EXTERNAL_STORAGE) } == true){
                Toast.makeText(context, getString(R.string.notif_reqired_permission), Toast.LENGTH_LONG).show()
            }
            else {
                val requestCodeGallery = 1
                activity?.let { ActivityCompat.requestPermissions(
                    it,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    requestCodeGallery
                ) }
            }
        }
        else
            openGallery()

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResult: IntArray
    ) {
        if (requestCode == requestCamera) {
            for (i in permissions.indices) {
                val permission = permissions[i]
                if (grantResult[i] == PackageManager.PERMISSION_DENIED) {
                    val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                            requireActivity(),
                        permission
                    )
                    if (showRationale) {
                        showAlertDialog()
                    } else {
                        saveToPreferences(requireContext())
                    }
                }
            }
        }
    }


    private fun startInstalledAppDetailsActivity(activity: Activity) {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.data = Uri.parse("package:" + activity.packageName)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        activity.startActivity(intent)
    }


    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
        galleryIntent.type = "image/*"
        startActivityForResult(galleryIntent, requestGallery)
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, requestCamera)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == requestGallery) {
            if (resultCode == RESULT_OK && intent != null) {
                pickedImgUri = intent.data
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(
                            activity?.applicationContext?.contentResolver, pickedImgUri
                    )
                    binding?.ivImageFromChoose?.setImageBitmap(bitmap)

                    val returnCursor: Cursor? = pickedImgUri?.let {
                        context?.contentResolver?.query(
                            it, null, null, null, null)
                    }
                    val nameIndex: Int? = returnCursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    returnCursor?.moveToFirst()
                    binding?.tvImageGallery?.text = nameIndex?.let { returnCursor.getString(it) }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        } else if (requestCode == requestCamera) {
            if (resultCode == RESULT_OK && intent != null) {
                pickedImgUri = intent.data
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(
                        activity?.applicationContext?.contentResolver, pickedImgUri
                    )
                    binding?.ivImageFromChoose?.setImageBitmap(bitmap)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}