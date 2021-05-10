package com.sagikor.android.jobao.ui.fragments.home.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.Chart
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
        val currentChartData = lineChartList[position]
        val isDataEmpty = currentChartData.yMax == 0f
        val resources = holder.itemView.context.resources

        holder.itemView.line_chart_description.apply {
            text = currentChartData.dataSetLabels[0] //single label
        }

        holder.itemView.line_chart.xAxis.apply {
            setDrawAxisLine(false)
            setDrawGridLines(false)
            setDrawLabels(false)
        }
        holder.itemView.line_chart.apply {
            setTouchEnabled(false)
            setNoDataText(resources.getString(R.string.no_data_text))
            data = if (!isDataEmpty) currentChartData else null
            description.isEnabled = false
            setBorderColor(Color.GREEN)
            axisLeft.axisMinimum = 0f
            extraBottomOffset = 15f
            axisRight.isEnabled = false
            legend.isEnabled = false
            getPaint(Chart.PAINT_INFO).textSize = 50f
            getPaint(Chart.PAINT_INFO).color =
                ResourcesCompat.getColor(resources, R.color.grayish_red_5, null)
            invalidate()
        }
    }

    override fun getItemCount(): Int {
        return lineChartList.size
    }

    inner class ChartHolder(viewItem: View) : RecyclerView.ViewHolder(viewItem)

    fun submitList(lineChartList: List<LineData>) {
        this.lineChartList = lineChartList
        notifyDataSetChanged()
    }


}