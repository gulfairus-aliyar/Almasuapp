package com.almasu.myapplication.activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.activity.result.contract.ActivityResultContracts
import com.almasu.myapplication.R
import com.almasu.myapplication.Utils
import com.almasu.myapplication.databinding.ActivityLoginEmailBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import java.lang.Exception

class LoginEmailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginEmailBinding

    private companion object{
        private const val TAG = "LOGIN_TAG"
    }

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    private lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Пожалуйста, подождите...")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.toolbarBackBtn.setOnClickListener{
            onBackPressed()
        }

        binding.noAccountTv.setOnClickListener{
            startActivity(Intent(this, RegisterEmailActivity::class.java))
        }

        binding.forgotPass.setOnClickListener{
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        binding.loginBtn.setOnClickListener{
            validateData()
        }

        binding.loginGoogleBtn.setOnClickListener {
            beginGoogleLogin()
        }

    }

    private fun beginGoogleLogin() {
        Log.d(TAG, "beginGoogleLogin: ")
        val googleSignInClient = mGoogleSignInClient.signInIntent
        googleSignInARL.launch(googleSignInClient)
    }

    private val googleSignInARL = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){ result ->
        Log.d(TAG, "googleSignInARL: ")

        if (result.resultCode == RESULT_OK){
            val data = result.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                val account = task.getResult(ApiException::class.java)
                Log.d(TAG, "googleSignInARL: Account ID: ${account.id}")

                firebaseAuthWithGoogleAccount(account.idToken)
            }
            catch (e: Exception){
                Log.e(TAG, "googleSignInARL: ", e)
                Utils.toast(this, "${e.message}")
            }
        }
        else{
            Utils.toast(this, "Отменено...!")
        }
    }

    private fun firebaseAuthWithGoogleAccount(idToken: String?) {
        Log.d(TAG, "firebaseAuthWithGoogleAccount: idToken: $idToken")

        val credential = GoogleAuthProvider.getCredential(idToken, null)

        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->

                if (authResult.additionalUserInfo!!.isNewUser){
                    Log.d(TAG, "firebaseAuthWithGoogleAccount: Новый пользователь, учетная запись создана...")

                    updateUserInfoDb()
                } else{
                    Log.d(TAG, "firebaseAuthWithGoogleAccount: Существующий пользователь, авторизовался...")
                    startActivity(Intent(this, MainActivity::class.java))
                    finishAffinity()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "firebaseAuthWithGoogleAccount: ", e)
                Utils.toast(this, "${e.message}")
            }
    }
    private fun updateUserInfoDb(){
        Log.d(TAG, "updateUserInfoDb: ")

        progressDialog.setMessage("Сохранение информации пользователя")
        progressDialog.show()

        val timestamp = Utils.getTimestamp()
        val registeredUserEmail = firebaseAuth.currentUser!!.email
        val registeredUserUid = firebaseAuth.uid
        val name = firebaseAuth.currentUser?.displayName

        val hashMap = HashMap<String, Any?>()
        hashMap["name"] = "$name"
        hashMap["phoneCode"] = ""
        hashMap["phoneNumber"] = ""
        hashMap["profileImageUrl"] = ""
        hashMap["userType"] = "Google"
        hashMap["typingTo"] = ""
        hashMap["timestamp"] = timestamp
        hashMap["onlineStatus"] = true
        hashMap["email"] = "$registeredUserEmail"
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
                progressDialog.dismiss()
                Log.e(TAG, "updateUserInfoDb: ", e)
                Utils.toast(
                    this,
                    "Не удалось сохранить информацию пользователя из -за ${e.message}"
                )
            }
    }

    private var email = ""
    private var password = ""

    private fun validateData(){
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()

        Log.d(TAG, "validateData: email: $email")
        Log.d(TAG, "validateData: password: $password")

        //validate data
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            //email pattern is invalid, show error
            binding.emailEt.error = "Неверный формат электронной почты"
            binding.emailEt.requestFocus()
        }
        else if (password.isEmpty()){
            //password is not entered, show error
            binding.passwordEt.error = "Введите пароль"
            binding.passwordEt.requestFocus()
        }
        else{
            //email pattern is valid and password is entered, start login
            loginUser()
        }
    }

    private fun loginUser() {
        Log.d(TAG, "loginUser: ")
        //show progress
        progressDialog.setMessage("Вход в систему")
        progressDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                //User login success
                Log.d(TAG, "loginUser: Вошел в систему ...")
                progressDialog.dismiss()

                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()
            }
            .addOnFailureListener { e ->

                Log.e(TAG, "loginUser: ", e)
                progressDialog.dismiss()

                Utils.toast(this, "Неспособный войти в систему из-за ${e.message}")
            }
    }
}