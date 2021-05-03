package com.sagikor.android.jobao.ui.fragments.home

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.LineData
import com.sagikor.android.jobao.R
import kotlinx.android.synthetic.main.line_chart_list_item.view.*

class LineChartAdapter(private var lineChartList: List<LineData>) :
    RecyclerView.Adapter<LineChartAdapter.ChartHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChartHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.line_chart_list_item, parent, false)
        return ChartHolder(view)
    }

    override fun onBindViewHolder(holder: ChartHolder, position: Int) {
        val currentChart = lineChartList[position]
        holder.itemView.line_chart.xAxis.apply {
            setDrawAxisLine(false)
            setDrawGridLines(false)
            setDrawLabels(false)

        }
        holder.itemView.line_chart.apply {
            data = currentChart
            description.isEnabled = false
            setBorderColor(Color.GREEN)
            extraBottomOffset = 15f
            legend.apply {
                textSize = 16f
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                direction = Legend.LegendDirection.RIGHT_TO_LEFT
                form = Legend.LegendForm.CIRCLE
            }
            invalidate()
        }
    }

    override fun getItemCount(): Int {
        return lineChartList.size
    }

    inner class ChartHolder(viewItem: View) : RecyclerView.ViewHolder(viewItem) {}

    fun submitList(lineChartList: List<LineData>) {
        this.lineChartList = lineChartList
        notifyDataSetChanged()
    }


}