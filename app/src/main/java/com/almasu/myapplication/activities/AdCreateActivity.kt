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
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import com.almasu.myapplication.R
import com.almasu.myapplication.Utils
import com.almasu.myapplication.adapters.AdapterImagePicked
import com.almasu.myapplication.databinding.ActivityAdCreateBinding
import com.almasu.myapplication.models.ModelImagePicked
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class AdCreateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdCreateBinding

    companion object{
        internal const val TAG = "ADD_CREATE_TAG"
    }

    private lateinit var progressDialog: ProgressDialog

    private lateinit var firebaseAuth: FirebaseAuth

    private var imageUri: Uri? = null

    private lateinit var imagePickedArrayList: ArrayList<ModelImagePicked>

    private lateinit var adapterImagePicked: AdapterImagePicked

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Пожалуйста, подождите...")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()

        val adapterCategories = ArrayAdapter(this, R.layout.row_category_act, Utils.categories)
        binding.categoryAct.setAdapter(adapterCategories)

        imagePickedArrayList = ArrayList()
        loadImages()

        binding.toolbarBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.toolbarAdImageBtn.setOnClickListener {
            showImagePickOptions()
        }

        binding.locationAct.setOnClickListener {
            val intent = Intent(this, LocationPickerActivity::class.java)
            locationPickerActivityResultLauncher.launch(intent)
        }

        binding.postAdBtn.setOnClickListener {
            validateData()
        }
    }

    private val locationPickerActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            Log.d(TAG, "locationPickerActivityResultLauncher: ")

            if (result.resultCode == Activity.RESULT_OK){
                val data = result.data
                
                if (data != null){
                    latitude = data.getDoubleExtra("latitude", 0.0)
                    longitude = data.getDoubleExtra("longitude", 0.0)
                    address = data.getStringExtra("address") ?: ""

                    Log.d(TAG, "locationPickerActivityResultLauncher: latitude: $latitude")
                    Log.d(TAG, "locationPickerActivityResultLauncher: longitude: $longitude")
                    Log.d(TAG, "locationPickerActivityResultLauncher: address: $address")
                    
                    binding.locationAct.setText(address)
                }
            } else {
                Log.d(TAG, "locationPickerActivityResultLauncher: cancelled")
                Utils.toast(this, "Cancelled")
            }
        }

    private fun loadImages(){
        Log.d(TAG, "loadImages: ")

        adapterImagePicked = AdapterImagePicked(this, imagePickedArrayList)

        binding.imageRv.adapter = adapterImagePicked
    }

    private fun showImagePickOptions(){
        Log.d(TAG, "showImagePickOptions: ")

        val popupMenu = PopupMenu(this, binding.toolbarAdImageBtn)

        popupMenu.menu.add(Menu.NONE, 1, 1, "Camera")
        popupMenu.menu.add(Menu.NONE, 2, 2, "Gallery")

        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item ->
            val itemId = item.itemId

            if (itemId == 1) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    val cameraPermissions = arrayOf(Manifest.permission.CAMERA)
                    requestCameraPermission.launch(cameraPermissions)
                } else {
                    val cameraPermissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    requestCameraPermission.launch(cameraPermissions)
                }
            } else if (itemId == 2) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    pickImageGallery()
                } else{
                    val storagePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE
                    requestStoragePermission.launch(storagePermission)
                }
            }

            true
        }
    }

    private val requestStoragePermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {isGranted ->
        Log.d(TAG, "requestStoragePermission: isGranted: $isGranted")

        if (isGranted) {
            pickImageGallery()
        } else{
            Utils.toast(this, "Отказано в доступе к хранилищу...")
        }
    }

    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        Log.d(TAG, "requestCameraPermission: $result")

        var areAllGranted = true
        for (isGranted in result.values){
            areAllGranted = areAllGranted && isGranted
        }

        if (areAllGranted){
            pickImageCamera()
        } else {
            Utils.toast(
                this,
                "Отказано в доступе к камере или хранилищу или к обоим разрешениям..."
            )
        }
    }

    private fun pickImageGallery(){
        Log.d(TAG, "pickImageGallery: ")

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }

    private fun pickImageCamera(){
        Log.d(TAG, "pickImageCamera: ")
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE, "TEMP_IMAGE_TITLE")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "TEMP_IMAGE_DESCRIPTION")

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLauncher.launch(intent)
    }

    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(TAG, "galleryActivityResultLauncher: ")

        if (result.resultCode == Activity.RESULT_OK){
            val data = result.data
            imageUri = data!!.data
            Log.d(TAG, "galleryActivityResultLauncher: imageUri: $imageUri")
            val timestamp = "${Utils.getTimestamp()}"
            val modelImagePicked = ModelImagePicked(timestamp, imageUri, null, false)
            imagePickedArrayList.add(modelImagePicked)
            loadImages()
        } else{
            Utils.toast(this, "Отменено...!")
        }
    }

    private val cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(TAG, "cameraActivityResultLauncher: ")

        if (result.resultCode == Activity.RESULT_OK){
            Log.d(TAG, "cameraActivityResultLauncher: imageUri: $imageUri")
            val timestamp = "${Utils.getTimestamp()}"
            val modelImagePicked = ModelImagePicked(timestamp, imageUri, null, false)
            imagePickedArrayList.add(modelImagePicked)
            loadImages()
        } else{
            Utils.toast(this, "Отменено...!")
        }
    }

    private var nameService = ""
    private var category = ""
    private var address = ""
    private var description = ""
    private var latitude = 0.0
    private var longitude = 0.0

    private fun validateData(){
        Log.d(TAG, "validateData: ")

        nameService = binding.nameServiceEt.text.toString().trim()
        category = binding.categoryAct.text.toString().trim()
        address = binding.locationAct.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()


        if (nameService.isEmpty()){
            binding.nameServiceEt.error = "Enter Name Service"
            binding.nameServiceEt.requestFocus()
        } else if (category.isEmpty()){
            binding.categoryAct.error = "Choose Category"
            binding.categoryAct.requestFocus()
        } else if (description.isEmpty()){
            binding.descriptionEt.error = "Enter Description"
            binding.descriptionEt.requestFocus()
        } else{
            postAd()
        }
    }

    private fun postAd(){
        Log.d(TAG, "postAd: ")
        progressDialog.setMessage("Publishing Ad")
        progressDialog.show()

        val timestamp = Utils.getTimestamp()
        val refAds = FirebaseDatabase.getInstance().getReference("Ads")
        val keyId = refAds.push().key

        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "$keyId"
        hashMap["uid"] = "${firebaseAuth.uid}"
        hashMap["nameService"] = "$nameService"
        hashMap["category"] = "$category"
        hashMap["address"] = "$address"
        hashMap["description"] = "$description"
        hashMap["status"] = "${Utils.AD_STATUS_AVAILABLE}"
        hashMap["timestamp"] = timestamp
        hashMap["latitude"] = latitude
        hashMap["longitude"] = longitude

        refAds.child(keyId!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "postAd: Ad Published")
                uploadImagesStorage(keyId)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "postAd: ", e)
                progressDialog.dismiss()
                Utils.toast(this, "Failed due to ${e.message}")
            }


    }

    private fun uploadImagesStorage(adId: String){
        for (i in imagePickedArrayList.indices){
            val modelImagePicked = imagePickedArrayList[i]
            val imageName = modelImagePicked.id
            val filePathAndName = "Ads/$imageName"
            val imageIndexForProcess = i + 1

            val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
            storageReference.putFile(modelImagePicked.imageUri!!)
                .addOnProgressListener { snapshot ->
                    val process = 100.0 *snapshot.bytesTransferred / snapshot.totalByteCount
                    Log.d(TAG, "uploadImagesStorage: progress: $process")
                    val message = "Uploading $imageIndexForProcess of ${imagePickedArrayList.size} images... Progress ${process.toInt()}"
                    Log.d(TAG, "uploadImagesStorage: message: $message")

                    progressDialog.setMessage(message)
                    progressDialog.show()
                }
                .addOnSuccessListener { taskSnapshot ->
                    Log.d(TAG, "uploadImagesStorage: onSuccess")

                    val uriTask = taskSnapshot.storage.downloadUrl
                    while (!uriTask.isSuccessful);
                    val uploadedImageUrl = uriTask.result
                    if (uriTask.isSuccessful){
                        val hashMap = HashMap<String, Any>()
                        hashMap["id"] = "${modelImagePicked.id}"
                        hashMap["imageUrl"] = "$uploadedImageUrl"

                        val ref = FirebaseDatabase.getInstance().getReference("Ads")
                        ref.child(adId).child("Images")
                            .child(imageName)
                            .updateChildren(hashMap)
                    }

                    progressDialog.dismiss()
                }
                .addOnFailureListener{ e ->
                    Log.e(TAG, "uploadImagesStorage: ", e)
                    progressDialog.dismiss()
                }
        }
    }
}