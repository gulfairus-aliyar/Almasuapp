package com.almasu.myapplication.activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.almasu.myapplication.Utils
import com.almasu.myapplication.databinding.ActivityLoginPhoneBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.FirebaseDatabase
import java.util.concurrent.TimeUnit

class LoginPhoneActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginPhoneBinding

    private companion object{
        private const val TAG = "PHONE_LOGIN_TAG"
    }

    private lateinit var progressDialog: ProgressDialog

    private lateinit var firebaseAuth: FirebaseAuth

    private var forceRefreshingToken: PhoneAuthProvider.ForceResendingToken? = null

    private lateinit var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    private var mVerificationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginPhoneBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.phoneRl.visibility = View.VISIBLE
        binding.OtpInputRl.visibility = View.GONE


        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Пожалуйста, подождите...")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()

        phoneLoginCallBacks()

        binding.toolbarBackBtn.setOnClickListener{
            onBackPressed()
        }

        binding.sendPhoneBtn.setOnClickListener {
            validateData()
        }

        binding.resendOtpTv.setOnClickListener {
            resendVerificationCode(forceRefreshingToken)
        }

        binding.verifyOtpBtn.setOnClickListener {

            val otp = binding.mobileOtpEt.text.toString().trim()
            Log.d(TAG, "onCreate: otp: $otp")

            if (otp.isEmpty()) {
                binding.mobileOtpEt.error = "Введите OTP"
                binding.mobileOtpEt.requestFocus()
            } else if (otp.length < 6) {
                binding.mobileOtpEt.error = "Длина OTP должна состоять из 6 символов"
                binding.mobileOtpEt.requestFocus()
            }else{
                verifyPhoneNumberWithCode(mVerificationId, otp)
            }
        }

    }

    private var phoneCode = ""
    private var phoneNumber = ""
    private var phoneNumberWithCode = ""

    private fun validateData(){

        phoneCode = binding.phoneCodeTill.selectedCountryCodeWithPlus
        phoneNumber = binding.mobileNumEt.text.toString().trim()
        phoneNumberWithCode = phoneCode + phoneNumber

        Log.d(TAG, "validateData: phoneCode: $phoneCode")
        Log.d(TAG, "validateData: phoneNumber: $phoneNumber")
        Log.d(TAG, "validateData: phoneNumberWithCode: $phoneNumberWithCode")

        if (phoneNumber.isEmpty()){
            binding.mobileNumEt.error = "Введите номер телефона"
            binding.mobileNumEt.requestFocus()
        } else{
            startPhoneNumberVerification()
        }
    }

    private fun startPhoneNumberVerification(){
        Log.d(TAG, "startPhoneNumberVerification: ")

        progressDialog.setMessage("Отправка OTP на $phoneNumberWithCode")
        progressDialog.show()

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumberWithCode)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(mCallbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun phoneLoginCallBacks(){
        Log.d(TAG, "phoneLoginCallBacks: ")

        mCallbacks = object: PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(TAG, "onVerificationCompleted: ")

                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.e(TAG, "onVerificationFailed: ", e)

                progressDialog.dismiss()

                Utils.toast(this@LoginPhoneActivity, "${e.message}")
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                Log.d(TAG, "onCodeSent: verificationId: $verificationId")

                mVerificationId = verificationId
                forceRefreshingToken = token

                progressDialog.dismiss()

                binding.phoneRl.visibility = View.GONE
                binding.OtpInputRl.visibility = View.VISIBLE

                Utils.toast(this@LoginPhoneActivity, "OTP отправляется на $phoneNumberWithCode")

                binding.loginPhoneTv.text = "Пожалуйста, введите проверочный код, отправленный на $phoneNumberWithCode"
            }

            override fun onCodeAutoRetrievalTimeOut(p0: String) {

            }
        }
    }


    private fun verifyPhoneNumberWithCode(verificationId: String?, otp: String) {
        Log.d(TAG, "verifyPhoneNumberWithCode: mVerificationId: $verificationId")
        Log.d(TAG, "verifyPhoneNumberWithCode: otp: $otp")

        progressDialog.setMessage("Проверка OTP")
        progressDialog.show()

        val credential = PhoneAuthProvider.getCredential(verificationId!!, otp)
        signInWithPhoneAuthCredential(credential)
    }

    private fun resendVerificationCode(token: PhoneAuthProvider.ForceResendingToken?){
        Log.d(TAG, "resendVerificationCode")

        progressDialog.setMessage("Повторная отправка OTP на $phoneNumberWithCode")
        progressDialog.show()

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumberWithCode)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(mCallbacks)
            .setForceResendingToken(token!!)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        Log.d(TAG, "signInWithPhoneAuthCredential: ")

        progressDialog.setMessage("Вход в систему")
        progressDialog.show()

        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                Log.d(TAG, "signInWithPhoneAuthCredential: Успех")

                if (authResult.additionalUserInfo!!.isNewUser){
                    Log.d(TAG, "signInWithPhoneAuthCredential: Новый пользователь, учетная запись создана")
                    updateUserInfoDb()
                } else {
                    Log.d(TAG, "signInWithPhoneAuthCredential: Существующий пользователь, вошедший в систему")

                    startActivity(Intent(this, MainActivity::class.java))
                    finishAffinity()
                }
            }
            .addOnFailureListener { e ->

                Log.e(TAG, "signInWithPhoneAuthCredential: ", e)
                progressDialog.dismiss()
                Utils.toast(this, "Не удалось войти из-за ${e.message}")
            }
    }

    private fun updateUserInfoDb(){
        Log.d(TAG, "updateUserInfoDb: ")

        progressDialog.setMessage("Сохранение информации о пользователе")
        progressDialog.show()

        val timestamp = Utils.getTimestamp()
        val registeredUserUid = firebaseAuth.uid

        val hashMap = HashMap<String, Any?>()
        hashMap["name"] = ""
        hashMap["phoneCode"] = "$phoneCode"
        hashMap["phoneNumber"] = "$phoneNumber"
        hashMap["profileImageUrl"] = ""
        hashMap["userType"] = "Phone"
        hashMap["typingTo"] = ""
        hashMap["timestamp"] = timestamp
        hashMap["onlineStatus"] = true
        hashMap["email"] = ""
        hashMap["uid"] = "$registeredUserUid"

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(registeredUserUid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "updateUserInfoDb: Информация о пользователе сохранена")
                progressDialog.dismiss()

                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "updateUserInfoDb: ", e)
                progressDialog.dismiss()
                Utils.toast(
                    this,
                    "Не удалось сохранить информацию о пользователе из-за ${e.message}"
                )
            }
    }

}