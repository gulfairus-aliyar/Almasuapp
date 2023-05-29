package com.almasu.myapplication.activities

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import com.almasu.myapplication.R
import com.almasu.myapplication.Utils
import com.almasu.myapplication.databinding.ActivityProfileEditBinding
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage


class ProfileEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileEditBinding

    private companion object{
        private const val TAG = "PROFILE_EDIT_TAG"
    }

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    private var myUserType = ""

    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Пожалуйста, подождите...")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()
        loadMyInfo()

        binding.toolbarBackBtn.setOnClickListener{
            onBackPressed()
        }

        binding.profileAddImage.setOnClickListener{
            imagePickDialog()
        }

        binding.updateBtn.setOnClickListener {
            validateData()
        }

    }

    private var name = ""
    private var email = ""
    private var phoneCode = ""
    private var phoneNumber = ""

    private fun validateData(){
        name = binding.nameEt.text.toString().trim()
        email = binding.emailEt.text.toString().trim()
        phoneCode = binding.phoneCodeTill.selectedCountryCodeWithPlus
        phoneNumber = binding.mobileNumEt.text.toString().trim()

        if (imageUri == null){
            updateProfileDb(null)
        } else {
            uploadProfileImageStorage()
        }
    }

    private fun uploadProfileImageStorage(){
        Log.d(TAG, "uploadProfileImageStorage: ")
        progressDialog.setMessage("Загрузка изображения профиля пользователя")
        progressDialog.show()

        val filePathAndName = "UserProfile/profile_${firebaseAuth.uid}"

        val ref = FirebaseStorage.getInstance().reference.child(filePathAndName)
        ref.putFile(imageUri!!)
            .addOnProgressListener { snapshot ->
                val progress = 100.0* snapshot.bytesTransferred / snapshot.totalByteCount
                Log.d(TAG, "uploadProfileImageStorage: progress: $progress")
                progressDialog.setMessage("Загрузка изображения профиля. Progress: $progress")
            }
            .addOnSuccessListener { taskSnapshot ->
                Log.d(TAG, "uploadProfileImageStorage: Изображение загружено...")

                val uriTask = taskSnapshot.storage.downloadUrl

                while (!uriTask.isSuccessful);

                val uploadedImageUrl = uriTask.result.toString()
                if (uriTask.isSuccessful){
                    updateProfileDb(uploadedImageUrl)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "uploadProfileImageStorage: ", e)
                progressDialog.dismiss()
                Utils.toast(this, "Не удалось загрузить из-за ${e.message}")
            }
    }

    private fun updateProfileDb(uploadedImageUrl: String?){
        Log.d(TAG, "updateProfileDb: uploadedImageUrl: $uploadedImageUrl")

        progressDialog.setMessage("Обновление информации о пользователе")
        progressDialog.show()

        val hashMap = HashMap<String, Any>()
        hashMap["name"] = "$name"
        if (uploadedImageUrl != null){
            hashMap["profileImageUrl"] = "$uploadedImageUrl"
        }

        if (myUserType.equals("Phone", true)){
            hashMap["email"] = "$email"
        }
        else if (myUserType.equals("Email", true) || myUserType.equals("Google", true)){
            hashMap["phoneCode"] = "$phoneCode"
            hashMap["phoneNumber"] = "$phoneNumber"
        }

        val reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.child("${firebaseAuth.uid}")
            .updateChildren(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "updateProfileDb: Обновлено...")
                progressDialog.dismiss()
                Utils.toast(this, "Обновлено...")

                imageUri = null
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "updateProfileDb: ", e)
                progressDialog.dismiss()
                Utils.toast(this, "Не удалось обновить из-за ${e.message}")
            }
    }

    private fun loadMyInfo(){
        Log.d(TAG, "LoadMyInfo: ")

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child("${firebaseAuth.uid}")
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val email = "${snapshot.child("email").value}"
                    val name = "${snapshot.child("name").value}"
                    val phoneCode = "${snapshot.child("phoneCode").value}"
                    val phoneNumber = "${snapshot.child("phoneNumber").value}"
                    val profileImageUrl = "${snapshot.child("profileImageUrl").value}"
                    var timestamp = "${snapshot.child("timestamp").value}"
                    myUserType = "${snapshot.child("userType").value}"

                    val phone = phoneCode + phoneNumber

                    if (myUserType.equals("Email", true) || myUserType.equals("Google", true)){
                        binding.emailTil.isEnabled = false
                        binding.emailEt.isEnabled = false
                    } else{
                        binding.mobileNumTil.isEnabled = false
                        binding.mobileNumEt.isEnabled = false
                        binding.phoneCodeTill.isEnabled = false
                    }
                    binding.emailEt.setText(email)
                    binding.nameEt.setText(name)
                    binding.mobileNumEt.setText(phoneNumber)

                    try{
                        val phoneCodeInt = phoneCode.replace("+", "").toInt()
                        binding.phoneCodeTill.setCountryForPhoneCode(phoneCodeInt)
                    } catch (e: Exception){
                        Log.e(TAG, "onDataChange: ", e)
                    }

                    try {
                        Glide.with(this@ProfileEditActivity)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.ic_person)
                            .into(binding.profileIv)
                    } catch (e: Exception){
                        Log.e(TAG, "onDataChange: ", e)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun imagePickDialog(){
        val popupMenu = PopupMenu(this, binding.profileAddImage)

        popupMenu.menu.add(Menu.NONE, 1, 1, "Camera")
        popupMenu.menu.add(Menu.NONE, 2, 2, "Gallery")

        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item ->
            val itemId = item.itemId

            if (itemId == 1){
                Log.d(TAG, "imagePickDialog: Камера нажата, проверьте, предоставлено ли разрешение камеры или нет")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    requestCameraPermissions.launch(arrayOf(Manifest.permission.CAMERA))
                } else{
                    requestCameraPermissions.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                }
            } else if (itemId == 2){
                Log.d(TAG, "imagePickDialog: Галерея Нажата, проверьте, предоставлено ли разрешение на хранение или нет")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    pickImageGallery()
                } else{
                    requestStoragePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }

            return@setOnMenuItemClickListener true
        }
    }

    private val requestCameraPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ result ->
            Log.d(TAG, "requestCameraPermissions: result: $result")

            var areAllGranted = true
            for (isGranted in result.values){
                areAllGranted = areAllGranted && isGranted
            }

            if (areAllGranted){
                Log.d(TAG, "requestCameraPermissions: Все предоставлено, например, камера, хранилище")
                pickImageCamera()
            } else{
                Log.d(TAG, "requestCameraPermissions: Отказано все или одно...")
                Utils.toast(this, "Камера или хранилище или оба разрешения запрещены")
            }
        }

    private val requestStoragePermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted ->
            Log.d(TAG, "requestStoragePermission: isGranted $isGranted")

            if (isGranted){
                pickImageGallery()
            }else{
                Utils.toast(this, "Отказано в доступе к хранилищу...")
            }
        }

    private fun pickImageCamera(){
        Log.d(TAG, "pickImageCamera: ")
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_image_title")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_image_description")

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLauncher.launch(intent)
    }

    private val cameraActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->

            if (result.resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "cameraActivityResultLauncher: Image captured: imageUri: $imageUri")

                try{
                    Glide.with(this)
                        .load(imageUri)
                        .placeholder(R.drawable.ic_person)
                        .into(binding.profileIv)
                } catch (e: Exception){
                    Log.e(TAG, "cameraActivityResultLauncher: ", e)
                }
            } else{
                Utils.toast(this, "Отменено!")
            }
        }

    private fun pickImageGallery(){
        Log.d(TAG, "pickImageGallery: ")
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }

    private val galleryActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            if (result.resultCode == Activity.RESULT_OK){
                val data = result.data
                imageUri = data!!.data

                try {
                    Glide.with(this)
                        .load(imageUri)
                        .placeholder(R.drawable.ic_person)
                        .into(binding.profileIv)
                } catch (e: java.lang.Exception){
                    Log.e(TAG, "galleryActivityResultLauncher: ", e)
                }
            }
        }
}