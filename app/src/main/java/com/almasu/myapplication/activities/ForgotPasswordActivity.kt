package com.almasu.myapplication.activities

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import com.almasu.myapplication.Utils
import com.almasu.myapplication.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding

    private companion object{
        private const val TAG = "FORGOT PASSWORD"
    }

    private lateinit var progressDialog: ProgressDialog

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("пожалуйста, подождите")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.toolbarBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.submitBtn.setOnClickListener {
            validateData()
        }
    }

    private var email = ""
    private fun validateData(){
        email = binding.emailEt.text.toString().trim()
        Log.d(TAG, "validateData: email: $email")

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.emailEt.error = "Неверный шаблон электронной почты!"
            binding.emailEt.requestFocus()
        } else {
            sendPasswordRecoveryInstructions()
        }
    }

    private fun sendPasswordRecoveryInstructions(){
        Log.d(TAG, "sendPasswordRecoveryInstructions: ")

        progressDialog.setMessage("Отправка инструкций по сбросу пароля на $email")
        progressDialog.show()

        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Utils.toast(this, "Инструкции по сбросу пароля отправлены на $email")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "sendPasswordRecoveryInstructions: ", e)
                progressDialog.dismiss()
                Utils.toast(this, "Не удалось отправить из-за ${e.message}")
            }
    }
}
