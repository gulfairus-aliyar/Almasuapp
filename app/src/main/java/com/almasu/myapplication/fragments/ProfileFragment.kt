package com.almasu.myapplication.fragments

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.almasu.myapplication.R
import com.almasu.myapplication.Utils
import com.almasu.myapplication.activities.AdCreateActivity
import com.almasu.myapplication.activities.ChangePasswordActivity
import com.almasu.myapplication.activities.DeleteAccountActivity
import com.almasu.myapplication.activities.MainActivity
import com.almasu.myapplication.activities.ProfileEditActivity
import com.almasu.myapplication.databinding.FragmentProfileBinding
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding

    internal companion object{
        const val TAG = "Profile_TAG"
    }

    private lateinit var mContext: Context

    private lateinit var progressDialog: ProgressDialog

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(layoutInflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        progressDialog = ProgressDialog(mContext)
        progressDialog.setTitle("Пожалуйста, подождите...")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()

        loadMyInfo()

        binding.logOutCv.setOnClickListener{
            firebaseAuth.signOut()
            startActivity(Intent(mContext, MainActivity::class.java))
            activity?.finishAffinity()
        }

        binding.editProfileCv.setOnClickListener{
            startActivity(Intent(mContext, ProfileEditActivity::class.java))
        }

        binding.changePassCv.setOnClickListener {
            startActivity(Intent(mContext, ChangePasswordActivity::class.java))
        }

        binding.verifyAccCv.setOnClickListener {
            verifyAccount()
        }

        binding.deleteAccCv.setOnClickListener {
            startActivity(Intent(mContext, DeleteAccountActivity::class.java))
        }

        binding.messageIb.setOnClickListener {
            chat()
        }

        binding.createAd.setOnClickListener {
            startActivity(Intent(mContext, AdCreateActivity::class.java))
        }
    }

    private fun loadMyInfo(){
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child("${firebaseAuth.uid}")
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val email = "${snapshot.child("email").value}"
                    val name = "${snapshot.child("name").value}"
                    val phoneCode = "${snapshot.child("phoneCode").value}"
                    val phoneNumber = "${snapshot.child("phoneNumber").value}"
                    val profileImageUrl = "${snapshot.child("profileImageUrl").value}"
                    var timestamp = "${snapshot.child("timestamp").value}"
                    val userType = "${snapshot.child("userType").value}"

                    val phone = phoneCode + phoneNumber

                    if (timestamp == "null"){
                        timestamp = "0"
                    }

                    val formattedDate = Utils.formatTimestampDate(timestamp.toLong())

                    binding.email.text = email
                    binding.username.text = name
                    binding.username1.text = name
                    binding.mobilePhone.text = phone
                    binding.memberSince.text = formattedDate

                    if (userType == "Email"){
                        val isVerified = firebaseAuth.currentUser!!.isEmailVerified
                        if (isVerified){
                            binding.verifyAccCv.visibility = View.GONE
                            binding.verification.text = "Проверено"
                        } else{
                            binding.verifyAccCv.visibility = View.VISIBLE
                            binding.verification.text = "Не проверено"
                        }
                    } else{
                        binding.verifyAccCv.visibility = View.GONE
                        binding.verification.text = "Проверено"
                    }

                    try {
                        Glide.with(mContext)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.ic_person)
                            .into(binding.profileIv)
                    } catch (e: Exception) {
                        Log.e(TAG, "onDataChange: ", e)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun verifyAccount(){
        Log.d(TAG, "verifyAccount: ")

        progressDialog.setMessage("Sending account verification instructions to your email...")
        progressDialog.show()

        firebaseAuth.currentUser!!.sendEmailVerification()
            .addOnSuccessListener {
                Log.d(TAG, "verifyAccount: Successfully sent")
                progressDialog.dismiss()
                Utils.toast(mContext, "Account verification instructions sent to your email...")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "verifyAccount: ", e)
                progressDialog.dismiss()
                Utils.toast(mContext, "Failed to send due to ${e.message}")
            }
    }

    private fun chat(){

    }

}