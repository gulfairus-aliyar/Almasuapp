package com.almasu.myapplication.activities

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.almasu.myapplication.R
import com.almasu.myapplication.Utils
import com.almasu.myapplication.databinding.ActivityRequestBinding
import com.almasu.myapplication.fragments.ProfileFragment
import com.almasu.myapplication.fragments.UsersFragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RequestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRequestBinding

    companion object{
        internal const val TAG = "REQUEST_TAG"
    }

    private lateinit var progressDialog: ProgressDialog

    private lateinit var firebaseAuth: FirebaseAuth

    private var requestId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestId = intent.getStringExtra("requestId")!!

        loadRequestDetails()

//        loadMyInfo()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Пожалуйста, подождите...")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.toolbarBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.respond.setOnClickListener {
            startActivity(Intent(this, UsersFragment::class.java))
        }
    }

    private fun loadRequestDetails(){
        //Requests > requestId > Details
        val ref = FirebaseDatabase.getInstance().getReference("Ads")
        ref.child(requestId).child("Images").limitToFirst(1)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get data
                    val address = "${snapshot.child("address").value}"
                    val category = "${snapshot.child("category").value}"
                    val description = "${snapshot.child("description").value}"
                    val nameService = "${snapshot.child("nameService").value}"
                    var timestamp = "${snapshot.child("timestamp").value}"

                    if (timestamp == "null"){
                        timestamp = "0"
                    }
                    //format date
                    val date = Utils.formatTimestampDate(timestamp.toLong())

                    //set data
                    binding.descriptionTv.text = description
                    binding.nameServiceTv.text = nameService
                    binding.locationTv.text = address
                    binding.skills.text = category
                    binding.dateTv.text = date
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

//    private fun loadMyInfo(){
//        val ref = FirebaseDatabase.getInstance().getReference("Users")
//        ref.child("${firebaseAuth.uid}")
//            .addValueEventListener(object : ValueEventListener{
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    val name = "${snapshot.child("name").value}"
//                    val profileImageUrl = "${snapshot.child("profileImageUrl").value}"
//
//                    binding.username.text = name
//
//                    try {
//                        Glide.with(this@RequestActivity)
//                            .load(profileImageUrl)
//                            .placeholder(R.drawable.ic_person)
//                            .into(binding.profileIv)
//                    } catch (e: Exception) {
//                        Log.e(ProfileFragment.TAG, "onDataChange: ", e)
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//
//                }
//
//            })
//    }

}