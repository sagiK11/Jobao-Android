package com.sagikor.android.jobao.ui.fragments.home

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartModel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartType
import com.github.aachartmodel.aainfographics.aachartcreator.AASeriesElement
import com.sagikor.android.jobao.R
import com.sagikor.android.jobao.databinding.FragmentHomeBinding
import com.sagikor.android.jobao.model.AppliedVia
import com.sagikor.android.jobao.model.Job
import com.sagikor.android.jobao.model.JobStatus
import com.sagikor.android.jobao.viewmodel.JobViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.coroutines.flow.collect
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {
    private val jobViewModel: JobViewModel by viewModels()
    private val viewModel : HomeViewModel by viewModels()
    private lateinit var chartAdapter: ChartAdapter
    private val chartsList: MutableList<AAChartModel> = ArrayList()

    private enum class ChartOrder { STATUS, DATES, APPLIED_VIA }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentHomeBinding.bind(view)

        observeApplicationsInfo(binding)
        observeJobList(binding)
        chartAdapter = ChartAdapter(chartsList)
        setAdapter(binding)
        initJobsChannel(binding)

    }

    private fun initJobsChannel(binding: FragmentHomeBinding){
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.jobEvent.collect { event ->
                when(event){
                    HomeViewModel.JobsEvent.NavigateToAddNewJobScreen -> {
                        val action = HomeFragmentDirections.actionNavigationHomeToAddEditFragment(null,
                        getString(R.string.title_add_job))
                        findNavController().navigate(action)
                    }
                }

            }
        }

    }

    private fun setAdapter(binding: FragmentHomeBinding) {
        binding.apply {
            viewPager.apply {
                viewPager.adapter = chartAdapter
            }
            fab.setOnClickListener{
                viewModel.onAddNewJobClick()
            }
        }
    }

    private fun observeApplicationsInfo(binding: FragmentHomeBinding) {
        jobViewModel.jobs.observe(viewLifecycleOwner) {
            binding.apply {
                totalApplications.text = it.size.toString()
                totalReplies.text =
                    it.filter { item -> item.status == JobStatus.ACCEPTED || item.status == JobStatus.IN_PROCESS }.size.toString()
            }
        }
    }

    private fun observeJobList(binding: FragmentHomeBinding) {
        val statusChart: AAChartModel = getStatusChart(binding)
        val datesChart: AAChartModel = getDatesChart(binding)
        val appliedViaChart: AAChartModel = getAppliedViaChart(binding)
        chartsList.add(statusChart)
        chartsList.add(datesChart)
        chartsList.add(appliedViaChart)

    }

    private fun getDatesChart(binding: FragmentHomeBinding): AAChartModel {
        val aaDatesChart: AAChartModel = AAChartModel()
            .chartType(AAChartType.Area)
            .title(binding.root.context.getString(R.string.dates))
            .backgroundColor(binding.root.context.getColor(R.color.soft_theme))
            .dataLabelsEnabled(true)

        //get last 7 days
        val calender = Calendar.getInstance()
//        calender.time = Date(System.currentTimeMillis())
//        val daysList = mutableListOf<Triple<String, String, Int>>()
//        for (i in 0..6) {
//            daysList.add(initialTriple(calender))
//            calender.add(Calendar.DAY_OF_MONTH, -1)
//        }

        jobViewModel.jobs.observe(viewLifecycleOwner) { jobsList ->
//            for (job in jobsList) {
//                for (i in 0..6) {
//                    daysList[i] = getUpdatedDayValue(daysList[i], job)
//                }
//            }
//            val seriesArray = mutableListOf<AASeriesElement>()
//            for (i in 0..6) {
//                seriesArray.add(getSeries(daysList[i].third, daysList[i].first))
//            }
            //aaDatesChart.series(seriesArray.toTypedArray())
        }
        return aaDatesChart
    }

//    private fun initialTriple(cal: Calendar): Triple<String, String, Int> {
//        return Triple<String, String, Int>(
//            SimpleDateFormat("dd").format(cal.timeInMillis),
//            SimpleDateFormat("d/M/yyyy").format(cal.timeInMillis),
//            0
//        )
//    }

//    private fun getUpdatedDayValue(
//        day: Triple<String, String, Int>,
//        job: Job
//    ): Triple<String, String, Int> {
//        return if (job.dateApplied == day.second) {
//            day.copy(third = day.third + 1)
//        } else {
//            day
//        }
//
//    }

    private fun getAppliedViaChart(binding: FragmentHomeBinding): AAChartModel {
        val appliedViaOptionNo = 3
        val options = IntArray(appliedViaOptionNo)
        val aaAppliedViaChart: AAChartModel = AAChartModel()
            .chartType(AAChartType.Pie)
            .title(binding.root.context.getString(R.string.applied_via))
            .backgroundColor(binding.root.context.getColor(R.color.soft_theme))

        jobViewModel.jobs.observe(viewLifecycleOwner) { jobsList ->
            for (job in jobsList) {
                when (job.appliedVia) {
                    AppliedVia.SITE -> options[AppliedVia.SITE.ordinal]++
                    AppliedVia.EMAIL -> options[AppliedVia.EMAIL.ordinal]++
                    AppliedVia.REFERENCE -> options[AppliedVia.REFERENCE.ordinal]++
                }
            }
            aaAppliedViaChart
                .series(
                    arrayOf(
                        AASeriesElement()
                            .name(getString(R.string.applied_via))
                            .data(
                                arrayOf(
                                    options[AppliedVia.SITE.ordinal],
                                    options[AppliedVia.EMAIL.ordinal],
                                    options[AppliedVia.REFERENCE.ordinal]
                                )
                            ),
                    )
                )
            chartsList[ChartOrder.APPLIED_VIA.ordinal] = aaAppliedViaChart
            chartAdapter.submitList(chartsList)
        }
        return aaAppliedViaChart
    }

    private fun getStatusChart(binding: FragmentHomeBinding): AAChartModel {
        val statusOptionsNo = 4
        val status = IntArray(statusOptionsNo)
        val aaStatusChart: AAChartModel = AAChartModel()
            .chartType(AAChartType.Column)
            .title(binding.root.context.getString(R.string.applications_status))
            .dataLabelsEnabled(true)

        jobViewModel.jobs.observe(viewLifecycleOwner) { jobsList ->
            for (job in jobsList) {
                when (job.status) {
                    JobStatus.PENDING -> status[JobStatus.PENDING.ordinal]++
                    JobStatus.IN_PROCESS -> status[JobStatus.IN_PROCESS.ordinal]++
                    JobStatus.REJECTED -> status[JobStatus.REJECTED.ordinal]++
                    JobStatus.ACCEPTED -> status[JobStatus.ACCEPTED.ordinal]++
                }
            }
            aaStatusChart
                .series(
                    arrayOf(
                        getSeries(status[JobStatus.REJECTED.ordinal], getString(R.string.rejected)),
                        getSeries(status[JobStatus.PENDING.ordinal], getString(R.string.pending)),
                        getSeries(status[JobStatus.ACCEPTED.ordinal], getString(R.string.accepted)),
                        getSeries(
                            status[JobStatus.IN_PROCESS.ordinal],
                            getString(R.string.in_process)
                        )

                    )
                )
            chartsList[ChartOrder.STATUS.ordinal] = aaStatusChart
            chartAdapter.submitList(chartsList)
        }
        return aaStatusChart
    }

    private fun getSeries(element: Int, label: String): AASeriesElement {
        return AASeriesElement()
            .name(label)
            .data(
                arrayOf(
                    element
                )
            )
    }
}