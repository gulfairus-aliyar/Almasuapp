package com.almasu.myapplication.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.almasu.myapplication.fragments.ChatFragment
import com.almasu.myapplication.fragments.HomeFragment
import com.almasu.myapplication.fragments.ProfileFragment
import com.almasu.myapplication.R
import com.almasu.myapplication.fragments.UsersFragment
import com.almasu.myapplication.Utils
import com.almasu.myapplication.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    //view binding or respective layout i.e activity_main.xml
    private lateinit var binding: ActivityMainBinding

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //activity_main.xml = ActivityMainBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        firebaseAuth = FirebaseAuth.getInstance()
        if (firebaseAuth.currentUser == null){
            startLoginOptions()
        }

        showHomeFragment()

        //handle bottomNv item clicks to navigate between fragments
        binding.bottomNv.setOnItemSelectedListener {item ->

            when(item.itemId){
                R.id.menu_home -> {
                    //Home item clicked, show HomeFragment

                    showHomeFragment()
                    true
                }
                R.id.menu_chat -> {
                    //Chats item clicked, show ChatsFragment

                    if (firebaseAuth.currentUser == null){
                        Utils.toast(this, "Требуется вход")
                        startLoginOptions()
                        false
                    } else{
                        showChatFragment()
                        true
                    }
                }

                R.id.menu_users -> {
                    if (firebaseAuth.currentUser == null){
                        Utils.toast(this, "Требуется вход")
                        startLoginOptions()

                        false
                    } else{
                        showSearchFragment()
                        true
                    }
                }
                R.id.menu_Profile -> {

                    if (firebaseAuth.currentUser == null){
                        Utils.toast(this, "Требуется вход")
                        startLoginOptions()

                        false
                    } else{
                        showProfileFragment()
                        true
                    }
                }
                else -> {
                    false
                }
            }
        }

        binding.exchangeFab.setOnClickListener {
            startActivity(Intent(this, AdCreateActivity::class.java))
        }
    }

    private fun showHomeFragment(){

        val fragment = HomeFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFl.id, fragment, "HomeFragment")
        fragmentTransaction.commit()
    }

    private fun showChatFragment(){

        val fragment = ChatFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFl.id, fragment, "ChatFragment")
        fragmentTransaction.commit()
    }

    private fun showSearchFragment(){

        val fragment = UsersFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFl.id, fragment, "SearchFragment")
        fragmentTransaction.commit()
    }

    private fun showProfileFragment(){

        val fragment = ProfileFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFl.id, fragment, "ProfileFragment")
        fragmentTransaction.commit()
    }

    private fun startLoginOptions(){
        startActivity(Intent(this, LoginOptionsActivity::class.java))
    }
}