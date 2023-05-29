package com.almasu.myapplication.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.almasu.myapplication.R
import com.almasu.myapplication.databinding.RowImagesPickedBinding
import com.almasu.myapplication.models.ModelImagePicked
import com.bumptech.glide.Glide
import java.lang.Exception
import java.util.*

class AdapterImagePicked(
    private val context: Context,
    private val imagePickedArrayList: ArrayList<ModelImagePicked>
) : RecyclerView.Adapter<AdapterImagePicked.HolderImagePicked>() {

    private lateinit var binding: RowImagesPickedBinding

    private companion object{
        private const val TAG = "IMAGES_TAG"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderImagePicked {
        binding = RowImagesPickedBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderImagePicked(binding.root)
    }

    override fun onBindViewHolder(holder: HolderImagePicked, position: Int) {
        val model = imagePickedArrayList[position]
        val imageUri = model.imageUri
        Log.d(TAG, "onBindViewHolder: imageUri $imageUri")

        try {
            Glide.with(context)
                .load(imageUri)
                .placeholder(R.drawable.image)
                .into(holder.imageIv)
        }catch (e: Exception) {
            Log.e(TAG, "onBindViewHolder: ", e)
        }

        holder.closeBtn.setOnClickListener {
            imagePickedArrayList.remove(model)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return imagePickedArrayList.size
    }

    inner class HolderImagePicked(itemView: View) : RecyclerView.ViewHolder(itemView){

        var imageIv = binding.imageIv
        var closeBtn = binding.closeBtn
    }



}