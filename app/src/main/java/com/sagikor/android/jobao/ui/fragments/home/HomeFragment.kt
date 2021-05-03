package com.sagikor.android.jobao.ui.fragments.home

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
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


@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {
    private val TAG = HomeFragment::class.qualifiedName
    private val jobViewModel: JobViewModel by viewModels()
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var barBarChartAdapter: BarChartAdapter
    private lateinit var pieChartAdapter: PieChartAdapter
    private lateinit var lineChartAdapter: LineChartAdapter
    private lateinit var binding: FragmentHomeBinding
    private var onScrollListener: OnScrollListener? = null
    private val barDataList: MutableList<BarData> = mutableListOf()
    private val pieDataList: MutableList<PieData> = mutableListOf()
    private val lineDataList: MutableList<LineData> = mutableListOf()

    private enum class ChartOrder { FIRST, SECOND, THIRD, FOURTH }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)

        observeApplicationsInfo()
        observeJobList()
        setAdapters()
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

    private fun setAdapters() {
        barBarChartAdapter = BarChartAdapter(barDataList)
        pieChartAdapter = PieChartAdapter(pieDataList)
        lineChartAdapter = LineChartAdapter(lineDataList)

        binding.apply {
            barViewPager.apply {
                adapter = barBarChartAdapter
            }
            pieViewPager.apply {
                adapter = pieChartAdapter
            }
            lineViewPager.apply {
                adapter = lineChartAdapter
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
        observeStatus()
        observeAppliedVia()
        observeDates()
        observeProcessWithAppliedVia()
    }

    private fun observeStatus() {
        jobViewModel.jobs.observe(viewLifecycleOwner) { jobsList ->
            createBarChart(jobsList, ChartOrder.FIRST, getString(R.string.applications_status))
        }
    }

    private fun observeAppliedVia() {
        jobViewModel.jobs.observe(viewLifecycleOwner) { jobsList ->
            createPieChart(
                jobsList,
                ChartOrder.FIRST,
                getString(R.string.applied_via_pie_chart_title)
            )
        }
    }

    private fun observeDates() {
        jobViewModel.jobs.observe(viewLifecycleOwner) { jobsList ->
            createLineChart(jobsList, ChartOrder.FIRST, getString(R.string.dates))
        }

    }

    private fun observeProcessWithAppliedVia() {
        jobViewModel.jobs.observe(viewLifecycleOwner) { jobsList ->
            val filteredJobs = jobsList.filter { job -> job.wasReplied }
            createPieChart(
                filteredJobs,
                ChartOrder.SECOND,
                getString(R.string.applied_via_of_process)
            )
        }
    }

    private fun createLineChart(jobsList: List<Job>, chartOrder: ChartOrder, label: String) {
        //gather data
        val daysList = getLastSevenDaysActivity(jobsList)

        //create chart
        val entries = mutableListOf<Entry>().apply {
            for (i in 0..6) {
                add(Entry(i.toFloat(), daysList[i].second.toFloat()))
            }
        }

        val lineDataSet1 = LineDataSet(entries, label).apply {
            fillColor = ResourcesCompat.getColor(resources, R.color.chart_3, null)
            fillAlpha = 1000
            color = ResourcesCompat.getColor(resources, R.color.chart_3, null)
            setDrawFilled(true)
        }

        val data = LineData(lineDataSet1).apply {
            setDrawValues(false)
        }
        addOrUpdateInLineList(data, chartOrder)
        lineChartAdapter.submitList(lineDataList)
    }


    private fun createPieChart(jobsList: List<Job>, chartOrder: ChartOrder, description: String) {
        //gather data
        val options = getAppliedViaOptionsData(jobsList)

        //create chart
        val entries = mutableListOf<PieEntry>().apply {
            val stringResources = getAppliedViaStringResources()
            for (i in 0..4) {
                add(PieEntry(options[i].toFloat(), stringResources[i]))
            }
        }

        val pieDataSet1 = PieDataSet(entries, description).apply {
            sliceSpace = 3f
            selectionShift = 5f
            colors = getChartColors()
            setDrawValues(false)
        }

        val data = PieData(pieDataSet1).apply {
            setValueTextSize(12f)
            setValueTextColor(Color.BLACK)
        }

        addOrUpdateInPieList(data, chartOrder)
        pieChartAdapter.submitXLabels(getAXisAppliedViaLabels())
        pieChartAdapter.submitList(pieDataList)
    }

    private fun createBarChart(jobsList: List<Job>, chartOrder: ChartOrder, label: String) {
        //gather data
        val status = getSubmissionStatusData(jobsList)

        //create chart
        val xValues = getXAxisStatusLabels()
        val entries = mutableListOf<BarEntry>().apply {
            for (i in 0..3) {
                add(BarEntry(i.toFloat(), status[i]))
            }
        }

        val barDataSet1 = BarDataSet(entries, label).apply {
            colors = getChartColors()
        }

        val data = BarData(barDataSet1).apply {
            setValueFormatter(object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            })
            barWidth = 0.5f
            setValueTextSize(15f)
        }
        addOrUpdateInBarList(data, chartOrder)
        barBarChartAdapter.submitXLabels(xValues)
        barBarChartAdapter.submitList(barDataList)

    }

    private fun addOrUpdateInBarList(
        data: BarData,
        chartOrder: ChartOrder
    ) {
        if (barDataList.size <= chartOrder.ordinal) {
            barDataList.add(data)
        } else {
            barDataList[chartOrder.ordinal] = data
        }
    }

    private fun addOrUpdateInPieList(
        data: PieData,
        chartOrder: ChartOrder
    ) {
        if (pieDataList.size <= chartOrder.ordinal) {
            pieDataList.add(data)
        } else {
            pieDataList[chartOrder.ordinal] = data
        }
    }

    private fun addOrUpdateInLineList(
        data: LineData,
        chartOrder: ChartOrder
    ) {
        if (lineDataList.size <= chartOrder.ordinal) {
            lineDataList.add(data)
        } else {
            lineDataList[chartOrder.ordinal] = data
        }
    }


    private fun getAppliedViaStringResources(): MutableList<String> {
        return arrayListOf(
            getString(R.string.site),
            getString(R.string.email),
            getString(R.string.reference),
            getString(R.string.linkedin),
            getString(R.string.other)
        )
    }

    private fun getAXisAppliedViaLabels(): MutableList<String> {
        return mutableListOf<String>().apply {
            add(getString(R.string.site))
            add(getString(R.string.email))
            add(getString(R.string.reference))
            add(getString(R.string.linkedin))
            add(getString(R.string.other))
        }
    }

    private fun getAppliedViaOptionsData(jobsList: List<Job>): IntArray {
        val appliedViaOptionNo = 5
        val options = IntArray(appliedViaOptionNo)

        for (job in jobsList) {
            when (job.appliedVia) {
                AppliedVia.SITE -> options[AppliedVia.SITE.ordinal]++
                AppliedVia.EMAIL -> options[AppliedVia.EMAIL.ordinal]++
                AppliedVia.REFERENCE -> options[AppliedVia.REFERENCE.ordinal]++
                AppliedVia.LINKEDIN -> options[AppliedVia.LINKEDIN.ordinal]++
                else -> options[AppliedVia.OTHER.ordinal]++
            }
        }
        return options
    }


    private fun getXAxisStatusLabels(): MutableList<String> {
        return mutableListOf<String>().apply {
            add(getString(R.string.pending))
            add(getString(R.string.in_process))
            add(getString(R.string.rejected))
            add(getString(R.string.accepted))
        }
    }

    private fun getChartColors(): MutableList<Int?> {
        return arrayListOf(
            context?.let { ContextCompat.getColor(it, R.color.chart_1) },
            context?.let { ContextCompat.getColor(it, R.color.chart_2) },
            context?.let { ContextCompat.getColor(it, R.color.chart_3) },
            context?.let { ContextCompat.getColor(it, R.color.chart_4) },
            context?.let { ContextCompat.getColor(it, R.color.chart_5) }
        )
    }

    private fun getSubmissionStatusData(jobsList: List<Job>): FloatArray {
        val statusOptionsNo = 4
        val statusData = FloatArray(statusOptionsNo)

        for (job in jobsList) {
            when (job.status) {
                JobStatus.PENDING -> statusData[JobStatus.PENDING.ordinal]++
                JobStatus.IN_PROCESS -> statusData[JobStatus.IN_PROCESS.ordinal]++
                JobStatus.REJECTED -> statusData[JobStatus.REJECTED.ordinal]++
                JobStatus.ACCEPTED -> statusData[JobStatus.ACCEPTED.ordinal]++
            }
        }
        return statusData
    }

    private fun getLastSevenDaysActivity(jobsList: List<Job>): MutableList<Pair<String, Int>> {
        val daysList = mutableListOf<Pair<String, Int>>()
        val calender = Calendar.getInstance()
        calender.time = Date(System.currentTimeMillis())

        for (i in 0..6) {
            daysList.add(initialPair(calender))
            calender.add(Calendar.DAY_OF_MONTH, -1)
        }

        for (job in jobsList) {
            for (i in 0..6) {
                daysList[i] = getUpdatedDayValue(daysList[i], job)
            }
        }
        return daysList
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
}