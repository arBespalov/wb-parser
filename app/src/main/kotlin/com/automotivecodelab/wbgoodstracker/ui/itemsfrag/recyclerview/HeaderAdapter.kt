package com.automotivecodelab.wbgoodstracker.ui.itemsfrag.recyclerview

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.automotivecodelab.wbgoodstracker.R
import com.automotivecodelab.wbgoodstracker.databinding.AdRecyclerviewItemBinding
import com.automotivecodelab.wbgoodstracker.domain.models.Ad
import com.squareup.picasso.Picasso

class HeaderAdapter: RecyclerView.Adapter<HeaderAdapter.AdViewHolder>() {

    private var ad: Ad? = null

    fun setAd(ad: Ad) {
        val isAdWasPreviouslySet = this.ad != null
        this.ad = ad
        if (isAdWasPreviouslySet)
            notifyItemChanged(0)
        else
            notifyItemInserted(0)
    }

    fun removeAd() {
        val isAdWasPreviouslySet = this.ad != null
        ad = null
        if (isAdWasPreviouslySet) notifyItemRemoved(0)
    }

    inner class AdViewHolder(
        val binding: AdRecyclerviewItemBinding
    ): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdViewHolder {
        return AdViewHolder(
            DataBindingUtil.inflate(
                /* inflater = */ LayoutInflater.from(parent.context),
                /* layoutId = */ R.layout.ad_recyclerview_item,
                /* parent = */ parent,
                /* attachToParent = */ false
            )
        )
    }

    override fun getItemCount(): Int {
        return if (ad != null) 1 else 0
    }

    override fun onBindViewHolder(holder: AdViewHolder, position: Int) {
        holder.binding.root.setOnClickListener { view ->
            val url = ad!!.url
            val webpage= Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW, webpage)
            view.context.startActivity(intent)
        }
        Picasso.get()
            .load(ad!!.imgUrl)
            .fit()
            .centerCrop()
            .error(R.drawable.ic_baseline_error_outline_24)
            .into(holder.binding.imageView)
    }
}