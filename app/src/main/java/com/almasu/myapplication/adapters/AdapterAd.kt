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
import com.almasu.myapplication.FilterAd
import com.almasu.myapplication.R
import com.almasu.myapplication.Utils
import com.almasu.myapplication.activities.RequestActivity
import com.almasu.myapplication.databinding.RowAdsListBinding
import com.almasu.myapplication.models.ModelAd
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdapterAd : RecyclerView.Adapter<AdapterAd.HolderAd>, Filterable{

    private lateinit var binding: RowAdsListBinding

    private companion object{
        private const val TAG = "ADAPTER_AD_TAG"
    }

    private var context: Context
    var adArrayList: ArrayList<ModelAd>
    private var filterList: ArrayList<ModelAd>

    private var filter: FilterAd? = null

    private var firebaseAuth: FirebaseAuth

    constructor(context: Context, adArrayList: ArrayList<ModelAd>) {
        this.context = context
        this.adArrayList = adArrayList
        this.filterList = adArrayList

        firebaseAuth = FirebaseAuth.getInstance()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderAd {

        binding = RowAdsListBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderAd(binding.root)
    }

    override fun onBindViewHolder(holder: HolderAd, position: Int) {
        val modelAd = adArrayList[position]
        val uid = modelAd.uid
        val nameService = modelAd.nameService
        val category = modelAd.category
        val address = modelAd.address
        val username = modelAd.username
        val profileImageUrl = modelAd.profileImageUrl
        val timestamp = modelAd.timestamp
        val formattedDate = Utils.formatTimestampDate(timestamp)

        loadAdFirstImage(modelAd, holder)

        holder.nameServiceTv.text = nameService
        holder.skills.text = category
        holder.locationTv.text = address
        holder.dateTv.text = formattedDate
        holder.username.text = username
        holder.itemView.setOnClickListener { v ->
           val intent = Intent(context, RequestActivity::class.java)
            intent.putExtra("uid", uid)
            context.startActivity(intent)
        }

    }

    private fun loadAdFirstImage(modelAd: ModelAd, holder: HolderAd){
        Log.d(TAG, "loadAdFirstImage: ")

        val adId = modelAd.id

        val reference = FirebaseDatabase.getInstance().getReference("Ads")
        reference.child(adId).child("Images").limitToFirst(1)
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

                    for (ds in snapshot.children){
                        val imageUrl = "${ds.child("imageUrl").value}"
                        Log.d(TAG, "onDataChange: imageUrl: $imageUrl")

                        try {
                            Glide.with(context)
                                .load(imageUrl)
                                .placeholder(R.drawable.image)
                                .into(holder.imageIv)
                        }catch (e: Exception) {
                            Log.e(TAG, "onDataChange: ", e)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    override fun getItemCount(): Int {
        return adArrayList.size
    }

    inner class HolderAd(itemView: View) : RecyclerView.ViewHolder(itemView){
        var imageIv = binding.imageIv
        var nameServiceTv = binding.nameServiceTv
        var locationTv = binding.locationTv
        var dateTv = binding.dateTv
        var skills = binding.skills
        var profileIv = binding.profileIv
        var username = binding.username
    }

    override fun getFilter(): Filter {

        if (filter == null){
            filter = FilterAd(this, filterList)
        }
        return filter as FilterAd
    }
}
