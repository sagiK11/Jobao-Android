package com.sagikor.android.jobao.ui.fragments.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartModel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartType
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartView
import com.github.aachartmodel.aainfographics.aachartcreator.AASeriesElement
import com.sagikor.android.jobao.R
import com.sagikor.android.jobao.viewmodel.JobViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_home.view.*

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private val jobViewModel: JobViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        setUpCharts(root)
        return root
    }

    private fun setUpCharts(root: View) {
        // setUpRecentActivityChart(root)
    }

    private fun setUpRecentActivityChart(root: View) {
        val aaChartView = root.findViewById<AAChartView>(R.id.chart_recent_activity)
        val aaChartModel: AAChartModel = AAChartModel()
            .chartType(AAChartType.Line)
            .title(root.context.getString(R.string.recent_activity))
            .subtitle("subtitle")
            .backgroundColor(root.context.getColor(R.color.soft_theme))
            .dataLabelsEnabled(true)
            .series(
                arrayOf(
                    AASeriesElement()
                        .name("Tokyo")
                        .data(
                            arrayOf(
                                7.0,
                                6.9,
                                9.5,
                                14.5,
                                18.2,
                                21.5,
                                25.2,
                                26.5,
                                23.3,
                                18.3,
                                13.9,
                                9.6
                            )
                        ),
                    AASeriesElement()
                        .name("NewYork")
                        .data(
                            arrayOf(
                                0.2,
                                0.8,
                                5.7,
                                11.3,
                                17.0,
                                22.0,
                                24.8,
                                24.1,
                                20.1,
                                14.1,
                                8.6,
                                2.5
                            )
                        ),
                    AASeriesElement()
                        .name("London")
                        .data(
                            arrayOf(
                                0.9,
                                0.6,
                                3.5,
                                8.4,
                                13.5,
                                17.0,
                                18.6,
                                17.9,
                                14.3,
                                9.0,
                                3.9,
                                1.0
                            )
                        ),
                    AASeriesElement()
                        .name("Berlin")
                        .data(
                            arrayOf(
                                3.9,
                                4.2,
                                5.7,
                                8.5,
                                11.9,
                                15.2,
                                17.0,
                                16.6,
                                14.2,
                                10.3,
                                6.6,
                                4.8
                            )
                        )
                )
            )
        aaChartView.aa_drawChartWithChartModel(aaChartModel)
    }
}