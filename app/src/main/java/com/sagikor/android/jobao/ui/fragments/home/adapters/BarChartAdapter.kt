package com.sagikor.android.jobao.ui.fragments.home.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.sagikor.android.jobao.R
import kotlinx.android.synthetic.main.bar_chart_list_item.view.*


class BarChartAdapter(private var chartsList : List<BarData>)
    : RecyclerView.Adapter<BarChartAdapter.ChartHolder>(){

    private lateinit var xValues: MutableList<String>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChartHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.bar_chart_list_item,parent,false)
        return ChartHolder(view)
    }

    override fun onBindViewHolder(holder: ChartHolder, position: Int) {
        val currentChart = chartsList[position]
        holder.itemView.bar_chart_label_0.apply {
            text = currentChart.dataSetLabels[0] //single label
        }
        holder.itemView.bar_chart.xAxis.apply {
            textSize = 12f
            setDrawAxisLine(false)
            setDrawGridLines(false)
            setPosition(XAxis.XAxisPosition.BOTTOM)
            valueFormatter = IndexAxisValueFormatter(xValues)
            granularity = 1f
        }
        holder.itemView.bar_chart.apply {
            data = currentChart
            description.isEnabled =false
            extraBottomOffset = 15f
            legend.isEnabled =false
            invalidate()
        }

    }

    override fun getItemCount(): Int {
        return chartsList.size
    }

    inner class ChartHolder(itemView : View) : RecyclerView.ViewHolder(itemView){}

    fun submitList(newList : List<BarData>){
        this.chartsList = newList
        notifyDataSetChanged()
    }

    fun submitXLabels(xValues: MutableList<String>) {
        this.xValues = xValues
    }


}