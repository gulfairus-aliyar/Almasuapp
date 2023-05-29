package com.almasu.myapplication.fragments

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import com.almasu.myapplication.R
import com.almasu.myapplication.Utils
import com.almasu.myapplication.activities.LocationPickerActivity
import com.almasu.myapplication.activities.RequestActivity
import com.almasu.myapplication.adapters.AdapterAd
import com.almasu.myapplication.databinding.FragmentHomeBinding
import com.almasu.myapplication.models.ModelAd
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.collections.ArrayList

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private companion object{

        private const val TAG = "Home_TAG"

        private const val MAX_DISTANCE_TO_LOAD_ADS_KM = 10
    }

    private lateinit var mContext: Context

    private lateinit var progressDialog: ProgressDialog

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var adArrayList: ArrayList<ModelAd>

    private lateinit var adapterAd: AdapterAd

    private lateinit var locationSp: SharedPreferences

    private var currentLatitude = 0.0
    private var currentLongitude = 0.0
    private var currentAddress = ""

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)

        return binding.root
        
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationSp = mContext.getSharedPreferences("LOCATION_SP", Context.MODE_PRIVATE)

        currentLatitude = locationSp.getFloat("CURRENT_LATITUDE", 0.0f).toDouble()
        currentLongitude = locationSp.getFloat("CURRENT_LONGITUDE", 0.0f).toDouble()
        currentAddress = locationSp.getString("CURRENT_ADDRESS", "")!!

        if (currentLatitude != 0.0 && currentLongitude != 0.0){

            binding.locationSelectTv.text = currentAddress
        }

        progressDialog = ProgressDialog(mContext)
        progressDialog.setTitle("Пожалуйста, подождите...")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()

        loadMyInfo()

        loadAds("All")

        binding.searchEt.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence, p1: Int, p2: Int, p3: Int) {
                Log.d(TAG, "onTextChanged: Query: $s")

                try {
                    val query = s.toString()
                    adapterAd.filter.filter(query)
                }catch (e: Exception){
                    Log.e(TAG, "onTextChanged: ", e)
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        binding.profileIv.setOnClickListener {
            startActivity(Intent(mContext, ProfileFragment::class.java))
        }

        binding.locationSelectTv.setOnClickListener {
            val intent = Intent(mContext, LocationPickerActivity::class.java)
            locationPickerActivityResult.launch(intent)
        }

    }

    private val locationPickerActivityResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->

        if (result.resultCode == Activity.RESULT_OK){
            Log.d(TAG, "locationPickerActivityResult: RESULT_OK")
            val data = result.data

            if (data != null){
                Log.d(TAG, "locationPickerActivityResult: Location picked!")

                currentLatitude = data.getDoubleExtra("latitude", 0.0).toDouble()
                currentLongitude = data.getDoubleExtra("longitude", 0.0).toDouble()
                currentAddress = data.getStringExtra("address").toString()

                locationSp.edit()
                    .putFloat("CURRENT_LATITUDE", ("" + currentLatitude).toFloat())
                    .putFloat("CURRENT_LONGITUDE", ("" + currentLongitude).toFloat())
                    .putString("CURRENT_ADDRESS", currentAddress)
                    .apply()

                binding.locationSelectTv.text = currentAddress

                loadAds("All")

            }
        } else {
            Log.d(TAG, "locationPickerActivityResult: Cancelled!")
            Utils.toast(mContext, "Cancelled!")
        }


    }


    private fun loadMyInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child("${firebaseAuth.uid}")
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = "${snapshot.child("name").value}"
                    val profileImageUrl = "${snapshot.child("profileImageUrl").value}"

                    binding.username.text = name

                    try {
                        Glide.with(mContext)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.ic_person)
                            .into(binding.profileIv)
                    } catch (e: Exception) {
                        Log.e(ProfileFragment.TAG, "onDataChange: ", e)
                    }
                }
                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun loadAds(category: String) {
        Log.d(TAG, "loadAds: ")

        adArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Ads")
        ref.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                adArrayList.clear()

                for (ds in snapshot.children){
                    try {
                        val modelAd = ds.getValue(ModelAd::class.java)

                        val distance = calculateDistanceKm(
                            modelAd?.latitude ?: 0.0,
                            modelAd?.longitude ?: 0.0
                        )
                        Log.d(TAG, "onDataChange: distance: $distance")

                        if (category == "All"){
                            if (distance <= MAX_DISTANCE_TO_LOAD_ADS_KM){
                                adArrayList.add(modelAd!!)
                            }
                        }else{
                            if (modelAd!!.category == category){
                                if (distance <= MAX_DISTANCE_TO_LOAD_ADS_KM){
                                    adArrayList.add(modelAd)
                                }
                            }
                        }
                    } catch (e: Exception){

                        Log.e(TAG, "onDataChange: ", e)
                    }
                }

                adapterAd = AdapterAd(mContext, adArrayList)
                binding.adsRv.adapter = adapterAd
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }
    private fun calculateDistanceKm(adLatitude: Double, adLongitude: Double): Double {
        Log.d(TAG, "calculateDistanceKm: currentLatitude: $currentLatitude")
        Log.d(TAG, "calculateDistanceKm: currentLongitude: $currentLongitude")
        Log.d(TAG, "calculateDistanceKm: adLatitude: $adLatitude")
        Log.d(TAG, "calculateDistanceKm: adLongitude:$adLongitude")

        val startPoint = Location(LocationManager.NETWORK_PROVIDER)
        startPoint.latitude = currentLatitude
        startPoint.longitude = currentLongitude

        val endPoint = Location(LocationManager.NETWORK_PROVIDER)
        endPoint.latitude = adLatitude
        endPoint.longitude = adLongitude

        val distanceInMeters = startPoint.distanceTo(endPoint).toDouble()
        return distanceInMeters / 1000
    }
}