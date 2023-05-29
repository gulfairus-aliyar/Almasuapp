package com.almasu.myapplication.activities

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.almasu.myapplication.Utils
import com.almasu.myapplication.databinding.ActivityChangePasswordBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangePasswordBinding

    private companion object{
        private const val TAG = "CHANGE_PASSWORD_TAG"
    }

    private lateinit var progressDialog: ProgressDialog

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
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
            validateData()
        }
    }

    private var currentPassword = ""
    private var newPassword = ""
    private var confirmNewPassword = ""

    private fun validateData(){

        currentPassword = binding.currentPassEt.text.toString().trim()
        newPassword = binding.newPassEt.text.toString().trim()
        confirmNewPassword = binding.confirmNewPassEt.text.toString().trim()

        Log.d(TAG, "validateData: currentPassword: $currentPassword")
        Log.d(TAG, "validateData: newPassword: $newPassword")
        Log.d(TAG, "validateData: confirmNewPassword: $confirmNewPassword")

        if (currentPassword.isEmpty()) {
            binding.currentPassEt.error = "Введи свой текущий пароль!"
            binding.currentPassEt.requestFocus()
        } else if (newPassword.isEmpty()) {
            binding.newPassEt.error = "Введите новый пароль!"
            binding.newPassEt.requestFocus()
        } else if (confirmNewPassword.isEmpty()) {
            binding.confirmNewPassEt.error = "Введите подтвердите новый пароль!"
            binding.confirmNewPassEt.requestFocus()
        } else if (newPassword != confirmNewPassword) {
            binding.confirmNewPassEt.error = "Пароль не подходит"
            binding.confirmNewPassEt.requestFocus()
        } else {
            authenticateUserForUpdatePassword()
        }
    }

    private fun authenticateUserForUpdatePassword(){

        progressDialog.setMessage("Authenticating User")
        progressDialog.show()

        val authCredential = EmailAuthProvider.getCredential(firebaseUser.email.toString(), currentPassword)
        firebaseUser.reauthenticate(authCredential)
            .addOnSuccessListener {
                Log.d(TAG, "authenticateUserForUpdatePassword: Успех аутентификации")
                updatePassword()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "authenticateUserForUpdatePassword: ", e)
                progressDialog.dismiss()
                Utils.toast(this, "Не удалось пройти аутентификацию из-за ${e.message}")
            }

    }

    private fun updatePassword(){

        progressDialog.setMessage("Changing Password")
        progressDialog.show()

        firebaseUser.updatePassword(newPassword)
            .addOnSuccessListener {
                Log.d(TAG, "updatePassword: Пароль обновлен...")
                progressDialog.dismiss()
                Utils.toast(this, "Пароль обновлен...")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "updatePassword: ", e)
                progressDialog.dismiss()
                Utils.toast(this, "Не удалось обновить пароль из-за ${e.message}")
            }
    }
}