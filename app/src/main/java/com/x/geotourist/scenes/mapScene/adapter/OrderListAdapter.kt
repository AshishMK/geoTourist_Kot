package com.x.geotourist.scenes.mapScene.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.x.geotourist.R
import com.x.geotourist.data.local.entity.MarkerEntity
import com.x.geotourist.data.local.entity.TourDataEntity
import com.x.geotourist.databinding.MarkerListItemBinding
import com.x.geotourist.databinding.TourListItemBinding
import timber.log.Timber
import java.util.*

class OrderListAdapter(val activity: Activity, val itemListener: ItemListener) :
    RecyclerView.Adapter<OrderListAdapter.ViewHolder>() {
    var contents: ArrayList<MarkerEntity> = ArrayList<MarkerEntity>()

    interface ItemListener {
        fun onItemClickListener(entity: MarkerEntity, position: Int, view: View)
        fun onItemDeleteClickListener(entity: MarkerEntity, position: Int)
    }

    fun setItems(contents: ArrayList<MarkerEntity>) {
        Timber.v("select  %s", contents.size)
        this.contents.clear()
        this.contents.addAll(contents)
        notifyDataSetChanged()
    }


    inner class ViewHolder(private val listItemBinding: MarkerListItemBinding) :
        RecyclerView.ViewHolder(listItemBinding.root) {

        fun bindTo(viewHolder: ViewHolder, content: MarkerEntity) {
            Timber.v("selects  %s", contents.size)
            listItemBinding.contentViewHolder = viewHolder
            listItemBinding.title = content.title
            listItemBinding.position = adapterPosition
            listItemBinding.order = content.markerOrder.toInt()
            listItemBinding.idTour = content.id.toString()
        }

        fun onItemClick(position: Int,view : View) {
            itemListener.onItemClickListener(contents[position], position, view)
        }


    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.marker_list_item,
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