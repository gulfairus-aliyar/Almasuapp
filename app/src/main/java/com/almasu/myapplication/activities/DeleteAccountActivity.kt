package com.almasu.myapplication.activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.almasu.myapplication.Utils
import com.almasu.myapplication.databinding.ActivityDeleteAccountBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DeleteAccountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeleteAccountBinding

    private companion object{
        private const val TAG = "DELETE_ACCOUNT_TAG"
    }

    private lateinit var progressDialog: ProgressDialog

    private lateinit var firebaseAuth: FirebaseAuth
    private var firebaseUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeleteAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Пожалуйста, подождите...")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser!!


        binding.toolbarBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.submitBtn.setOnClickListener {
            deleteAccount()
        }

    }

    private fun deleteAccount(){
        Log.d(TAG, "deleteAccount: ")

        progressDialog.setMessage("Delete User Account")
        progressDialog.show()

        val myUid = firebaseAuth.uid

        firebaseUser!!.delete()
            .addOnSuccessListener {
                Log.d(TAG, "deleteAccount: Account deleted...")

                progressDialog.setMessage("Delete User Ads")

                val refUserAds = FirebaseDatabase.getInstance().getReference("Ads")
                refUserAds.orderByChild("uid").equalTo(myUid)
                    .addValueEventListener(object: ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {

                            for (ds in snapshot.children){
                                ds.ref.removeValue()
                            }
                            progressDialog.setMessage("Deleting User Data")

                            val refUsers = FirebaseDatabase.getInstance().getReference("Users")
                            refUsers.child(myUid!!).removeValue()
                                .addOnSuccessListener {

                                    Log.d(TAG, "onDataChange: User Data deleted")
                                    progressDialog.dismiss()
                                    startMainActivity()
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "onDataChange: ", e)
                                    progressDialog.dismiss()
                                    Utils.toast(
                                        this@DeleteAccountActivity,
                                        "Failed to delete user data due to ${e.message}"
                                    )
                                    startMainActivity()
                                }
                        }
                        override fun onCancelled(error: DatabaseError) {

                        }
                    })
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "deleteAccount: ", e)
                progressDialog.dismiss()
                Utils.toast(this, "Failed to delete account due to ${e.message}")
            }
    }

    private fun startMainActivity(){
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }

    override fun onBackPressed() {
        startMainActivity()
    }
}