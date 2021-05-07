package com.sagikor.android.jobao.ui.fragments.home.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.data.PieData
import com.sagikor.android.jobao.R
import kotlinx.android.synthetic.main.pie_chart_list_item.view.*

class PieChartAdapter(private var pieChartsList: List<PieData>) :
    RecyclerView.Adapter<PieChartAdapter.ChartHolder>() {

    private lateinit var xValues: MutableList<String>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChartHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.pie_chart_list_item, parent, false)
        return ChartHolder(view)
    }

    override fun onBindViewHolder(holder: ChartHolder, position: Int) {
        val currentDataChart = pieChartsList[position]
        val resources = holder.itemView.context.resources
        val isDataEmpty = currentDataChart.yMax == 0f

        holder.itemView.chart_description.text = currentDataChart.dataSet.label
        holder.itemView.pie_chart.apply {
            data = if (!isDataEmpty) currentDataChart else null
            holeRadius = 5f
            //setTouchEnabled(false)
            setDrawEntryLabels(false)
            description.isEnabled = false
            legend.isEnabled = false
            setNoDataText(resources.getString(R.string.no_data_text))
            getPaint(Chart.PAINT_INFO).textSize = 50f
            getPaint(Chart.PAINT_INFO).color =
                ResourcesCompat.getColor(resources, R.color.grayish_red_5, null)
            invalidate()
        }
        val entries = holder.itemView.pie_chart.legend.entries
        holder.itemView.chart_legend.apply {
            if (!isDataEmpty) {
                label_0.text = entries[0].label
                label_1.text = entries[1].label
                label_2.text = entries[2].label
                label_3.text = entries[3].label
                label_4.text = entries[4].label
                label_0_color.setBackgroundColor(entries[0].formColor)
                label_1_color.setBackgroundColor(entries[1].formColor)
                label_2_color.setBackgroundColor(entries[2].formColor)
                label_3_color.setBackgroundColor(entries[3].formColor)
                label_4_color.setBackgroundColor(entries[4].formColor)
            } else {
                isVisible = false
            }
        }
    }

    override fun getItemCount(): Int {
        return pieChartsList.size
    }

    inner class ChartHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    fun submitList(newList: List<PieData>) {
        this.pieChartsList = newList
        notifyDataSetChanged()
    }

    fun submitXLabels(xValues: MutableList<String>) {
        this.xValues = xValues
    }
}