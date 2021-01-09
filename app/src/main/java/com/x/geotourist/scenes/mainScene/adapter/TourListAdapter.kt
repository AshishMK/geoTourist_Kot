package com.x.geotourist.scenes.mainScene.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.x.geotourist.R
import com.x.geotourist.data.local.entity.TourDataEntity
import com.x.geotourist.databinding.TourListItemBinding
import timber.log.Timber
import java.util.*

class TourListAdapter(val activity: Activity, val itemListener: ItemListener) :
    RecyclerView.Adapter<TourListAdapter.ViewHolder>() {
    var contents: ArrayList<TourDataEntity> = ArrayList<TourDataEntity>()

    interface ItemListener {
        fun onItemClickListener(entity: TourDataEntity, position: Int)
        fun onItemDeleteClickListener(entity: TourDataEntity, position: Int)
    }

    fun setItems(contents: ArrayList<TourDataEntity>) {
        Timber.v("select  %s", contents.size)
        this.contents.addAll(contents)
        notifyDataSetChanged()
    }

    fun addItem(content: TourDataEntity) {
        this.contents.add(content)
        //notifyItemRangeInserted(itemCount, contents.size)
        notifyItemInserted(this.contents.size-1);
    }


    inner class ViewHolder(private val listItemBinding: TourListItemBinding) :
        RecyclerView.ViewHolder(listItemBinding.root) {

        fun bindTo(viewHolder: ViewHolder, content: TourDataEntity) {
            Timber.v("selects  %s", contents.size)
            listItemBinding.contentViewHolder = viewHolder
            listItemBinding.title = content.title
            listItemBinding.position = adapterPosition
            listItemBinding.idTour = content.id.toString()
        }

        fun onItemClick(position: Int) {
            itemListener.onItemClickListener(contents[position], position)
        }



    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.tour_list_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return contents.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindTo(holder, contents[position])
    }
}