package com.almasu.myapplication.fragments

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.almasu.myapplication.adapters.AdapterUsers
import com.almasu.myapplication.databinding.FragmentUsersBinding
import com.almasu.myapplication.models.ModelUsers
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UsersFragment : Fragment() {

    private lateinit var binding: FragmentUsersBinding

    private companion object{

        private const val TAG = "Users_TAG"

    }

    private lateinit var mContext: Context

    private lateinit var progressDialog: ProgressDialog

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var adapterUsers: AdapterUsers

    private lateinit var userArrayList: ArrayList<ModelUsers>

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = FragmentUsersBinding.inflate(layoutInflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressDialog = ProgressDialog(mContext)
        progressDialog.setTitle("Пожалуйста, подождите...")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()

        loadAllUsers("All")

    }

    private fun loadAllUsers(uid: String){
        Log.d(TAG, "loadAllUsers: ")

        userArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                userArrayList.clear()

                for (ds in snapshot.children){
                    try {
                        val modelUsers = ds.getValue(ModelUsers::class.java)
                        if (modelUsers!!.uid == uid){
                            userArrayList.add(modelUsers)
                        }
                    } catch (e: Exception){
                        Log.e(TAG, "onDataChange: ", e)
                    }
                }
                adapterUsers = AdapterUsers(mContext, userArrayList)
                binding.recyclerView.adapter = adapterUsers
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

}