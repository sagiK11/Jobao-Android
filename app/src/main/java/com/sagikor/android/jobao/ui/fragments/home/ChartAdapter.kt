package com.sagikor.android.jobao.ui.fragments.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartModel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartType
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartView
import com.github.aachartmodel.aainfographics.aachartcreator.AASeriesElement
import com.sagikor.android.jobao.R
import com.sagikor.android.jobao.databinding.ChartListItemBinding
import com.sagikor.android.jobao.model.Job
import kotlinx.android.synthetic.main.chart_list_item.view.*


class ChartAdapter(var chartsList : List<AAChartModel>)
    : RecyclerView.Adapter<ChartAdapter.ChartHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChartHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chart_list_item,parent,false)
        return ChartHolder(view)
    }

    override fun onBindViewHolder(holder: ChartHolder, position: Int) {
        val currentChart = chartsList[position]
        holder.itemView.chart_recent_activity.aa_drawChartWithChartModel(currentChart)
    }

    override fun getItemCount(): Int {
        return chartsList.size
    }

    inner class ChartHolder(itemView : View) : RecyclerView.ViewHolder(itemView){}

    fun submitList(newList : List<AAChartModel>){
        this.chartsList = newList
        notifyDataSetChanged()
    }
}