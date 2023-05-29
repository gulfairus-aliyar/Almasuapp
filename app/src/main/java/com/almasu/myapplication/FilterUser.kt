package com.almasu.myapplication

import android.widget.Filter
import com.almasu.myapplication.adapters.AdapterUsers
import com.almasu.myapplication.models.ModelUsers
import java.util.Locale

class FilterUser(
    private val adapter: AdapterUsers,

    private val filterList: ArrayList<ModelUsers>
): Filter() {

    override fun performFiltering(constrant: CharSequence?): FilterResults {

        var constrant = constrant
        val results = FilterResults()

        if (!constrant.isNullOrEmpty()){
            constrant = constrant.toString().uppercase(Locale.getDefault())

            val filteredModels = ArrayList<ModelUsers>()
            for (i in filterList.indices){
                if (filterList[i].name.uppercase(Locale.getDefault()).contains(constrant) ||
                    filterList[i].email.uppercase(Locale.getDefault()).contains(constrant)
                ) {
                    filteredModels.add(filterList[i])
                }
            }
            results.count = filteredModels.size
            results.values = filteredModels
        }else{
            results.count = filterList.size
            results.values = filterList
        }
        return results
    }

    override fun publishResults(constrant: CharSequence?, results: FilterResults) {

        adapter.userArrayList = results.values as ArrayList<ModelUsers>

        adapter.notifyDataSetChanged()
    }


}