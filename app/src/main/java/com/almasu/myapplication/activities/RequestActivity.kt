package com.almasu.myapplication.activities

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.almasu.myapplication.R
import com.almasu.myapplication.databinding.ActivityRequestBinding
import com.almasu.myapplication.fragments.ProfileFragment
import com.google.firebase.auth.FirebaseAuth

class RequestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRequestBinding

    companion object{
        internal const val TAG = "REQUEST_TAG"
    }

    private lateinit var progressDialog: ProgressDialog

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Пожалуйста, подождите...")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.toolbarBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.respond.setOnClickListener {
            startActivity(Intent(this, MessageActivity::class.java))
        }

        binding.toolbarShareBtn.setOnClickListener {

        }
    }
}