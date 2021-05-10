package com.sagikor.android.jobao.ui.fragments.home.adapters

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.sagikor.android.jobao.R
import kotlinx.android.synthetic.main.bar_chart_list_item.view.*


class BarChartAdapter(private var chartsList: List<BarData>) :
    RecyclerView.Adapter<BarChartAdapter.ChartHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChartHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.bar_chart_list_item, parent, false)
        return ChartHolder(view)
    }

    override fun onBindViewHolder(holder: ChartHolder, position: Int) {
        val currentChartData = chartsList[position]
        val isCoverLetterChart = currentChartData.dataSetCount >= 2

        when (isCoverLetterChart) {
            true -> onBindCoverLetterChart(holder, currentChartData)
            else -> onBindStatusChart(holder, currentChartData)
        }
    }

    private fun onBindStatusChart(holder: ChartHolder, currentChartData: BarData) {
        val context = holder.itemView.context
        val resources = context.resources
        val isDataEmpty = currentChartData.yMax == 0f
        val chartTitle = holder.itemView.bar_chart_label_0
        val chart = holder.itemView.bar_chart

        //text color based on light/dark theme
        val dynamicColor = getTextColorBasedOnCurrentTheme(resources, context)

        setChartTitle(chartTitle, resources, currentChartData)

        chart.apply {
            setTouchEnabled(false)
            setNoDataText(resources.getString(R.string.no_data_text))
            data = if (!isDataEmpty) currentChartData else null
            axisLeft.axisMinimum = 0f
            description.isEnabled = false
            extraBottomOffset = 15f
            legend.isEnabled = false
            axisRight.isEnabled = false
            getPaint(Chart.PAINT_INFO).textSize = 50f
            getPaint(Chart.PAINT_INFO).color =
                ContextCompat.getColor(context, R.color.grayish_red_5)
        }
        chart.xAxis.apply {
            textSize = 13f
            setDrawAxisLine(false)
            setDrawGridLines(false)
            position = XAxis.XAxisPosition.BOTTOM
            textColor = dynamicColor
            valueFormatter = IndexAxisValueFormatter(currentChartData.dataSets[0].stackLabels)
            granularity = 1f
        }
        chart.invalidate()
    }

    private fun onBindCoverLetterChart(holder: ChartHolder, currentChartData: BarData) {
        val context = holder.itemView.context
        val resources = context.resources
        val isDataEmpty = currentChartData.yMax == 0f
        val chartTitle = holder.itemView.bar_chart_label_0
        val chart = holder.itemView.bar_chart

        //text color based on light/dark theme
        val dynamicColor = getTextColorBasedOnCurrentTheme(resources, context)

        setChartTitle(chartTitle, resources, currentChartData)

        //chart settings
        val barSpace = 0.1f// amount of space between each "stick"
        val groupSpace = 0.66f// how close the data set will be
        chart.apply {
            setNoDataText(resources.getString(R.string.no_data_text))
            data = if (!isDataEmpty) currentChartData else null
            axisLeft.axisMinimum = 0f
            description.isEnabled = false
            extraBottomOffset = 15f
            legend.isEnabled = false
            axisRight.isEnabled = false
            getPaint(Chart.PAINT_INFO).textSize = 50f
            getPaint(Chart.PAINT_INFO).color =
                ContextCompat.getColor(context, R.color.grayish_red_5)
            if (!isDataEmpty)
                groupBars(-0.5f, groupSpace, barSpace)
            legend.isEnabled = true
            legend.direction = Legend.LegendDirection.RIGHT_TO_LEFT
            legend.textSize = 15f
            extraBottomOffset = 10f
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            currentChartData.barWidth = 0.2f
        }
        chart.xAxis.apply {
            textSize = 13f
            setDrawAxisLine(false)
            setDrawGridLines(false)
            position = XAxis.XAxisPosition.BOTTOM
            textColor = dynamicColor
            valueFormatter = IndexAxisValueFormatter(currentChartData.dataSets[0].stackLabels)
            granularity = 0.6f
        }
        chart.invalidate()
    }

    private fun setChartTitle(title: TextView, resources: Resources, currentChartData: BarData) {
        val isCoverLetterChart = currentChartData.dataSetCount >= 2
        title.apply {
            text = if (isCoverLetterChart) {
                resources.getString(R.string.is_cover_letter_sent_chart_title)
            } else {
                currentChartData.dataSetLabels[0] //single label
            }
        }
    }

    private fun getTextColorBasedOnCurrentTheme(resources: Resources, context: Context): Int {
        return when (resources.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                ContextCompat.getColor(context, R.color.white)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                ContextCompat.getColor(context, R.color.black)
            }
            else -> {
                ContextCompat.getColor(context, R.color.black)
            }
        }
    }

    override fun getItemCount(): Int {
        return chartsList.size
    }

    inner class ChartHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    fun submitList(newList: List<BarData>) {
        this.chartsList = newList
        notifyDataSetChanged()
    }

}