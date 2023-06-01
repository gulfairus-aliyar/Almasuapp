package com.almasu.myapplication.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.almasu.myapplication.FilterUser
import com.almasu.myapplication.R
import com.almasu.myapplication.activities.MessageActivity
import com.almasu.myapplication.databinding.RowUsersBinding
import com.almasu.myapplication.fragments.ProfileFragment
import com.almasu.myapplication.fragments.UsersFragment
import com.almasu.myapplication.models.ModelUsers
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdapterUsers: RecyclerView.Adapter<AdapterUsers.UserHolder>, Filterable {

    private lateinit var binding: RowUsersBinding

    private companion object{
        private const val TAG = "ADAPTER_USERS_TAG"
    }

    private var context: Context

    var userArrayList: ArrayList<ModelUsers>

    private var filterList: ArrayList<ModelUsers>

    private var filter: FilterUser? = null

    private var firebaseAuth: FirebaseAuth

    //constructor
    constructor(
        context: Context,
        userArrayList: ArrayList<ModelUsers>

    ) : super() {
        this.context = context
        this.userArrayList = userArrayList
        this.filterList = userArrayList

        firebaseAuth = FirebaseAuth.getInstance()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserHolder {
        binding = RowUsersBinding.inflate(LayoutInflater.from(context), parent, false)

        return UserHolder(binding.root)
    }

    override fun onBindViewHolder(holder: UserHolder, position: Int) {
        //Get data, Set data, Handle clicks etc
        val modelUsers = userArrayList[position]
        val uid = modelUsers.uid
        val userName = modelUsers.name
        val userEmail = modelUsers.email
        val userImage = modelUsers.profileImageUrl
        val timestamp = modelUsers.timestamp

        holder.username.text = userName
        holder.emailTv.text = userEmail

        try {
            Glide.with(context)
                .load(userImage)
                .placeholder(R.drawable.ic_person)
                .into(binding.imageIv)
        } catch (e: Exception) {
            Log.e(ProfileFragment.TAG, "onDataChange: ", e)
        }
        holder.itemView.setOnClickListener {
            val intent = Intent(context, MessageActivity::class.java)
            intent.putExtra("uid", uid) // will be used to load request details
            intent.putExtra("name", userName)
            intent.putExtra("profileImage", userImage)
            context.startActivity(intent)
        }
        

    }

    override fun getItemCount(): Int {
        return userArrayList.size //number of items in list
    }

    //ViewHolder class to hold/init UI view for row_users.xml
    inner class UserHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var imageIv = binding.imageIv
        var username = binding.userNameTv
        var emailTv = binding.emailTv

    }

    override fun getFilter(): Filter {
        if (filter == null){
            filter = FilterUser(this, filterList)
        }
        return filter as FilterUser
    }


}