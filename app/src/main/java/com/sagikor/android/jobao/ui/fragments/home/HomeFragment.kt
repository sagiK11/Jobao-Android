package com.sagikor.android.jobao.ui.fragments.home

import android.content.Context
import android.os.Bundle
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
import com.sagikor.android.jobao.ui.activities.OnScrollListener
import com.sagikor.android.jobao.viewmodel.JobViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.coroutines.flow.collect
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {
    private val TAG = HomeFragment::class.qualifiedName
    private val jobViewModel: JobViewModel by viewModels()
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var chartAdapter: ChartAdapter
    private val chartsList: MutableList<AAChartModel> = ArrayList()
    private lateinit var binding: FragmentHomeBinding
    private var onScrollListener: OnScrollListener? = null

    private enum class ChartOrder { STATUS, DATES, APPLIED_VIA }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)

        observeApplicationsInfo()
        observeJobList()
        chartAdapter = ChartAdapter(chartsList)
        setAdapter()
        initJobsChannel()
        initScrollListener()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnScrollListener) {
            onScrollListener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        onScrollListener?.onScrollUp()
        onScrollListener = null
    }

    private fun initScrollListener() {
        binding.apply {
            homeScrollView.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
                if (scrollY < oldScrollY || scrollY == 0) {
                    onScrollListener?.onScrollUp()
                } else {
                    onScrollListener?.onScrollDown()
                }
            }
        }

    }

    private fun initJobsChannel() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.jobEvent.collect { event ->
                when (event) {
                    HomeViewModel.JobsEvent.NavigateToAddNewJobScreen -> {
                        val action = HomeFragmentDirections.actionNavigationHomeToAddEditFragment(
                            null,
                            getString(R.string.title_add_job)
                        )
                        findNavController().navigate(action)
                    }
                }

            }
        }

    }

    private fun setAdapter() {
        binding.apply {
            viewPager.apply {
                viewPager.adapter = chartAdapter
            }
        }
    }

    private fun observeApplicationsInfo() {
        jobViewModel.jobs.observe(viewLifecycleOwner) {
            binding.apply {
                totalApplications.text = getString(R.string.total_application, it.size.toString())
                totalPending.text = getString(
                    R.string.total_pending,
                    it.filter { job -> job.status == JobStatus.PENDING }.size.toString()
                )
                totalProcesses.text = getString(
                    R.string.total_processes,
                    it.filter { job -> job.wasReplied }.size.toString()
                )
                totalActiveProcesses.text = getString(
                    R.string.active_processes,
                    it.filter { job -> job.status == JobStatus.IN_PROCESS }.size.toString()
                )
                totalOffers.text = getString(
                    R.string.total_offers,
                    it.filter { job -> job.status == JobStatus.ACCEPTED }.size.toString()
                )
            }
        }
    }

    private fun observeJobList() {
        val statusChart: AAChartModel = getStatusChart()
        val datesChart: AAChartModel = getDatesChart()
        val appliedViaChart: AAChartModel = getAppliedViaChart()
        chartsList.add(statusChart)
        chartsList.add(datesChart)
        chartsList.add(appliedViaChart)

    }

    private fun getDatesChart(): AAChartModel {
        val aaDatesChart: AAChartModel = AAChartModel()
            .chartType(AAChartType.Areaspline)
            .title(binding.root.context.getString(R.string.dates))
            .dataLabelsEnabled(true).tooltipEnabled(false)

        //get last 7 days
        val calender = Calendar.getInstance()
        calender.time = Date(System.currentTimeMillis())
        val daysList = mutableListOf<Pair<String, Int>>()
        for (i in 0..6) {
            daysList.add(initialPair(calender))
            calender.add(Calendar.DAY_OF_MONTH, -1)
        }

        jobViewModel.jobs.observe(viewLifecycleOwner) { jobsList ->
            for (job in jobsList) {
                for (i in 0..6) {
                    daysList[i] = getUpdatedDayValue(daysList[i], job)
                }
            }
            val seriesArray = mutableListOf<Int>()
            for (i in 0..6) {
                seriesArray.add(daysList[i].second)
            }
            aaDatesChart.series(
                arrayOf(
                    AASeriesElement()
                        .name(getString(R.string.activity))
                        .data(seriesArray.toTypedArray()),
                )
            )
        }
        return aaDatesChart
    }

    private fun initialPair(cal: Calendar): Pair<String, Int> {
        return Pair<String, Int>(
            SimpleDateFormat("d", Locale.ENGLISH).format(cal.timeInMillis),
            0
        )
    }

    private fun getUpdatedDayValue(
        day: Pair<String, Int>,
        job: Job
    ): Pair<String, Int> {
        return if (SimpleDateFormat("d", Locale.ENGLISH).format(job.createdAt) == day.first) {
            day.copy(second = day.second + 1)
        } else {
            day
        }

    }

    private fun getAppliedViaChart(): AAChartModel {
        val appliedViaOptionNo = 3
        val options = IntArray(appliedViaOptionNo)
        val aaAppliedViaChart: AAChartModel = AAChartModel()
            .chartType(AAChartType.Pie)
            .title(binding.root.context.getString(R.string.applied_via)).tooltipEnabled(false)


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

    private fun getStatusChart(): AAChartModel {
        val statusOptionsNo = 4
        val status = IntArray(statusOptionsNo)
        val aaStatusChart: AAChartModel = AAChartModel()
            .chartType(AAChartType.Column)
            .title(binding.root.context.getString(R.string.applications_status))
            .dataLabelsEnabled(true).tooltipEnabled(false)

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