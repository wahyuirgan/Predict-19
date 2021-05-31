package academy.bangkit.predict19.ui.register

import academy.bangkit.predict19.R
import academy.bangkit.predict19.data.User
import academy.bangkit.predict19.databinding.ActivityRegisterBinding
import academy.bangkit.predict19.ui.login.LoginActivity
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.common.base.Strings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern


@RequiresApi(Build.VERSION_CODES.P)
class RegisterActivity : AppCompatActivity() {

    private var _binding: ActivityRegisterBinding? = null
    private val binding get() = _binding
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private lateinit var name: String
    private lateinit var email: String
    private lateinit var dateBirth: String
    private lateinit var password: String
    private lateinit var rePassword: String

    private val pickedRequestCode: Int = 1
    private val imageRequestCode: Int = 1
    private var pickedImgUri: Uri? = null
    private var cropImgUri: Uri? = null

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.btnImagePlus?.setOnClickListener {
            if (Build.VERSION.SDK_INT >= 22){
                checkAndRequestForPermission()
            } else {
                openGallery()
            }

        }

        val materialDateBuilder =
            MaterialDatePicker.Builder.datePicker()

        materialDateBuilder.setTitleText("Select a Date")

        val materialDatePicker = materialDateBuilder.build()

        binding?.btnDateSignup?.setOnClickListener {
            binding?.tvDateSignup?.error = null
            materialDatePicker.show(supportFragmentManager, "Material Date Picker")
        }

        materialDatePicker.addOnPositiveButtonClickListener {
            if (Locale.getDefault().country == "US") {
                val changeDate = changeDateFormat(materialDatePicker.headerText)
                binding?.tvDateSignup?.text = changeDate
            } else if (Locale.getDefault().country == "ID") {

                val changeDate = changeDateFormatIndonesian(materialDatePicker.headerText)

                binding?.tvDateSignup?.text = changeDate
            }

        }

        binding?.btnRegister?.setOnClickListener {
            binding?.btnRegister?.isEnabled = false
            binding?.tvHaveAccount?.isEnabled = false
            binding?.mainProgressbarRegister?.visibility = View.VISIBLE

            name = binding?.etNameSignup?.text.toString()
            email = binding?.etEmailSignup?.text.toString()
            dateBirth = binding?.tvDateSignup?.text.toString()
            password = binding?.etPassSignup?.text.toString()
            rePassword = binding?.etRePassSignup?.text.toString()

            val emailPattern = Pattern.compile("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")

            when {
                pickedImgUri == null -> {
                    Toast.makeText(this, getString(R.string.notif_image_profile_not_picked), Toast.LENGTH_SHORT).show()
                    binding?.btnRegister?.isEnabled = true
                    binding?.tvHaveAccount?.isEnabled = true
                    binding?.mainProgressbarRegister?.visibility = View.GONE
                }
                name.isEmpty() -> {
                    binding?.etNameSignup?.error = getString(R.string.notif_name_empty)
                    binding?.btnRegister?.isEnabled = true
                    binding?.tvHaveAccount?.isEnabled = true
                    binding?.mainProgressbarRegister?.visibility = View.GONE
                }
                dateBirth.isEmpty() -> {
                    binding?.tvDateSignup?.error = getString(R.string.notif_date_birth_empty)
                    binding?.btnRegister?.isEnabled = true
                    binding?.tvHaveAccount?.isEnabled = true
                    binding?.mainProgressbarRegister?.visibility = View.GONE
                }
                email.isEmpty() -> {
                    binding?.etEmailSignup?.error = getString(R.string.notif_email_empty)
                    binding?.btnRegister?.isEnabled = true
                    binding?.tvHaveAccount?.isEnabled = true
                    binding?.mainProgressbarRegister?.visibility = View.GONE
                }
                !emailPattern.matcher(email).matches() -> {
                    binding?.etEmailSignup?.error = getString(R.string.notif_email_invalid)
                    binding?.btnRegister?.isEnabled = true
                    binding?.tvHaveAccount?.isEnabled = true
                    binding?.mainProgressbarRegister?.visibility = View.GONE
                }
                password.isEmpty() -> {
                    binding?.etPassSignup?.error = getString(R.string.notif_pass_empty)
                    binding?.btnRegister?.isEnabled = true
                    binding?.tvHaveAccount?.isEnabled = true
                    binding?.mainProgressbarRegister?.visibility = View.GONE
                }
                rePassword.isEmpty() -> {
                    binding?.etRePassSignup?.error = getString(R.string.notif_pass_empty)
                    binding?.btnRegister?.isEnabled = true
                    binding?.tvHaveAccount?.isEnabled = true
                    binding?.mainProgressbarRegister?.visibility = View.GONE
                }
                password.length < 8 -> {
                    binding?.etPassSignup?.error = getString(R.string.notif_pass_less_than_eight)
                    binding?.btnRegister?.isEnabled = true
                    binding?.tvHaveAccount?.isEnabled = true
                    binding?.mainProgressbarRegister?.visibility = View.GONE
                }
                password != rePassword -> {
                    binding?.etRePassSignup?.error = getString(R.string.notif_re_pass_not_same)
                    binding?.btnRegister?.isEnabled = true
                    binding?.tvHaveAccount?.isEnabled = true
                    binding?.mainProgressbarRegister?.visibility = View.GONE
                }
                binding?.cbAgreement?.isChecked == false -> {
                    Toast.makeText(this, getString(R.string.notif_agreement_not_checked), Toast.LENGTH_SHORT).show()
                    binding?.btnRegister?.isEnabled = true
                    binding?.tvHaveAccount?.isEnabled = true
                    binding?.mainProgressbarRegister?.visibility = View.GONE
                }
                else -> {
                    createUserAccount(email, name, password)
                }
            }
        }

        binding?.tvHaveAccount?.setOnClickListener {
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
        }

    }

    private fun createUserAccount(email: String, name: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(
            this
        ) { task ->
            if (task.isSuccessful) {

                dateBirth = binding?.tvDateSignup?.text.toString()

                Toast.makeText(
                    this@RegisterActivity,
                    getString(R.string.notif_account_created),
                    Toast.LENGTH_LONG
                ).show()
                updateUserInfo(name,
                    cropImgUri, firebaseAuth.currentUser
                )

                    val user = User(
                        name,
                        dateBirth,
                        email
                    )

                val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference
                databaseReference.child("users").child(firebaseAuth.uid.toString()).setValue(user)

            } else {
                Toast.makeText(
                    this@RegisterActivity,
                    getString(R.string.notif_account_created_fail).plus(" ").plus(task.exception?.message),
                    Toast.LENGTH_SHORT
                ).show()
                binding?.btnRegister?.isEnabled = true
                binding?.tvHaveAccount?.isEnabled = true
                binding?.mainProgressbarRegister?.visibility = View.GONE
            }
        }
    }

    private fun updateUserInfo(name: String, pickedImgUri: Uri?, currentUser: FirebaseUser?) {
        val storageReference: StorageReference = FirebaseStorage.getInstance().reference
        val imageFile: StorageReference? =
            pickedImgUri?.lastPathSegment?.let { storageReference.child("image_profile").child(it) }
        imageFile?.putFile(pickedImgUri)?.addOnSuccessListener {
            imageFile.downloadUrl.addOnSuccessListener { uri ->
                val profileChangeRequest = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .setPhotoUri(uri)
                    .build()
                currentUser?.updateProfile(profileChangeRequest)
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            sendEmailVerification()
                            firebaseAuth.signOut()
                            Toast.makeText(
                                this@RegisterActivity,
                                getString(R.string.notif_register_success),
                                Toast.LENGTH_LONG
                            ).show()
                            binding?.btnRegister?.isEnabled = true
                            binding?.tvHaveAccount?.isEnabled = true
                            binding?.mainProgressbarRegister?.visibility = View.GONE
                            startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                        }
                    }
            }
        }
    }

    private fun sendEmailVerification() {
        val firebaseUser = firebaseAuth.currentUser
        firebaseUser?.sendEmailVerification()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(
                    this@RegisterActivity,
                    getString(R.string.notif_email_verify_send),
                    Toast.LENGTH_SHORT
                ).show()
                firebaseAuth.signOut()
                finishAffinity()
                binding?.btnRegister?.isEnabled = true
                binding?.tvHaveAccount?.isEnabled = true
                binding?.mainProgressbarRegister?.visibility = View.GONE
            } else {
                Toast.makeText(
                    this@RegisterActivity,
                    getString(R.string.notif_email_verify_unsend),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
        galleryIntent.type = "image/*"
        startActivityForResult(galleryIntent, imageRequestCode)
    }

    private fun checkAndRequestForPermission() {
        if (ContextCompat.checkSelfPermission(
                this@RegisterActivity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this@RegisterActivity,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                Toast.makeText(
                    this@RegisterActivity,
                    getString(R.string.notif_permission),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                ActivityCompat.requestPermissions(
                    this@RegisterActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    pickedRequestCode
                )
            }
        } else openGallery()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (resultCode == RESULT_OK && requestCode == imageRequestCode && intent != null) {
            pickedImgUri = intent.data
            CropImage.activity(pickedImgUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .setCropShape(CropImageView.CropShape.OVAL)
                .setBorderLineColor(Color.RED)
                .setGuidelinesColor(Color.GREEN)
                .setBorderLineThickness(resources.getDimensionPixelSize(R.dimen.dimension_3dp).toFloat())
                .start(this)
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result: CropImage.ActivityResult? = CropImage.getActivityResult(intent)
            if (resultCode == RESULT_OK) {
                cropImgUri = result?.uriContent
                binding?.ivProfileSignup?.setImageURI(cropImgUri)
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                result?.error
            }
        }
    }

    private fun changeDateFormat(
        dateString: String
    ): String {
        var result = ""
        if (Strings.isNullOrEmpty(dateString)) {
            return result
        }
        val formatterOld = SimpleDateFormat("MMM dd, yyy", Locale.US)
        val formatterNew = SimpleDateFormat("dd LLLL yyy", Locale.US)
        var date: Date? = null
        try {
            date = formatterOld.parse(dateString)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        if (date != null) {
            result = formatterNew.format(date)
        }
        return result
    }

    private fun changeDateFormatIndonesian(
        dateString: String
    ): String {
        var result = ""
        if (Strings.isNullOrEmpty(dateString)) {
            return result
        }
        val formatterOld = SimpleDateFormat("dd MMM yyy", Locale.getDefault())
        val formatterNew = SimpleDateFormat("dd LLLL yyy", Locale.getDefault())
        var date: Date? = null
        try {
            date = formatterOld.parse(dateString)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        if (date != null) {
            result = formatterNew.format(date)
        }
        return result
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}