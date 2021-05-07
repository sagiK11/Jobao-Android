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
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.sagikor.android.jobao.R
import com.sagikor.android.jobao.databinding.FragmentHomeBinding
import com.sagikor.android.jobao.model.AppliedVia
import com.sagikor.android.jobao.model.Job
import com.sagikor.android.jobao.model.JobStatus
import com.sagikor.android.jobao.model.SentWithCoverLetter
import com.sagikor.android.jobao.ui.activities.OnScrollListener
import com.sagikor.android.jobao.ui.fragments.home.adapters.BarChartAdapter
import com.sagikor.android.jobao.ui.fragments.home.adapters.LineChartAdapter
import com.sagikor.android.jobao.ui.fragments.home.adapters.PieChartAdapter
import com.sagikor.android.jobao.viewmodel.JobViewModel
import dagger.hilt.android.AndroidEntryPoint
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

    private enum class CoverLetterOptions {
        SENT_AND_REPLIED, SENT_AND_NOT_REPLIED,
        NOT_SENT_AND_REPLIED, NOT_SENT_AND_NOT_REPLIED
    }


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
                if (scrollY < oldScrollY) {
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
        jobViewModel.allJobs.observe(viewLifecycleOwner) {
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
        observeStatusAttribute()
        observeAppliedViaAttribute()
        observeDatesAttribute()
        observeProcessWithAppliedViaAttribute()
        observeIsCoverLetterSentAttribute()
    }

    private fun observeStatusAttribute() {
        jobViewModel.allJobs.observe(viewLifecycleOwner) { jobsList ->
            createStatusBarChart(
                jobsList,
                ChartOrder.FIRST,
                getString(R.string.applications_status)
            )
        }
    }

    private fun observeAppliedViaAttribute() {
        jobViewModel.allJobs.observe(viewLifecycleOwner) { jobsList ->
            createPieChart(
                jobsList,
                ChartOrder.FIRST,
                getString(R.string.applied_via_pie_chart_title)
            )
        }
    }

    private fun observeDatesAttribute() {
        jobViewModel.allJobs.observe(viewLifecycleOwner) { jobsList ->
            createLineChart(jobsList, ChartOrder.FIRST, getString(R.string.dates))
        }

    }

    private fun observeIsCoverLetterSentAttribute() {
        jobViewModel.allJobs.observe(viewLifecycleOwner) { jobsList ->
            createCoverLetterBarChart(jobsList, ChartOrder.SECOND)

        }
    }


    private fun observeProcessWithAppliedViaAttribute() {
        jobViewModel.allJobs.observe(viewLifecycleOwner) { jobsList ->
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

        //create chart data
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
            setDrawCircleHole(false)
            color = ResourcesCompat.getColor(resources, R.color.chart_1, null)
            setCircleColor(ResourcesCompat.getColor(resources, R.color.chart_1, null))
            lineWidth = 1.5f;
            mode = LineDataSet.Mode.CUBIC_BEZIER
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

        //create chart data
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

    private fun createStatusBarChart(jobsList: List<Job>, chartOrder: ChartOrder, label: String) {
        //gather data
        val status = getSubmissionStatusData(jobsList)

        //create chart data
        val xValues = getXAxisStatusLabels()
        val entries = mutableListOf<BarEntry>().apply {
            for (i in 0..3) {
                add(BarEntry(i.toFloat(), status[i]))
            }
        }

        val barDataSet = BarDataSet(entries, label).apply {
            colors = getChartColors()
            stackLabels = xValues.toTypedArray()
        }

        val data = BarData(barDataSet).apply {
            setValueFormatter(object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            })
            barWidth = 0.6f
            setValueTextSize(0f)
        }
        addOrUpdateInBarList(data, chartOrder)
        barBarChartAdapter.submitList(barDataList)
    }

    private fun createCoverLetterBarChart(
        jobsList: List<Job>,
        chartOrder: ChartOrder,
    ) {
        //gather data
        val processData = getProcessData(jobsList)

        //create chart data
        val xValues = getXAxisCoverLetterLabels()

        val coverLetterSentEntries = mutableListOf<BarEntry>().apply {
            add(BarEntry(0f, processData[CoverLetterOptions.SENT_AND_REPLIED.ordinal]))
            add(BarEntry(1f, processData[CoverLetterOptions.SENT_AND_NOT_REPLIED.ordinal]))
        }

        val coverLetterSentDataSet = BarDataSet(
            coverLetterSentEntries,
            getString(R.string.cover_letter_sent)
        ).apply {
            color = ContextCompat.getColor(requireContext(), R.color.chart_2)
            stackLabels = xValues.toTypedArray()
        }

        val coverLetterNotSentEntries = mutableListOf<BarEntry>().apply {
            add(BarEntry(0f, processData[CoverLetterOptions.NOT_SENT_AND_REPLIED.ordinal]))
            add(BarEntry(1f, processData[CoverLetterOptions.NOT_SENT_AND_NOT_REPLIED.ordinal]))
        }

        val coverLetterNotSentDataSet =
            BarDataSet(
                coverLetterNotSentEntries, getString(R.string.cover_letter_not_sent)
            ).apply {
                stackLabels = xValues.toTypedArray()
                color = ContextCompat.getColor(requireContext(), R.color.chart_3)
            }

        val data = BarData(coverLetterSentDataSet, coverLetterNotSentDataSet).apply {
            setValueFormatter(object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            })
            barWidth = 0.1f
            setValueTextSize(0f)
        }

        addOrUpdateInBarList(data, chartOrder)
        barBarChartAdapter.submitList(barDataList)
    }


    private fun getXAxisCoverLetterLabels(): MutableList<String> {
        return mutableListOf<String>().apply {
            add(getString(R.string.user_had_process))
            add(getString(R.string.user_did_not_had_process))
        }
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

        jobsList.iterator().forEach { job ->
            when (job.status) {
                JobStatus.PENDING -> statusData[JobStatus.PENDING.ordinal]++
                JobStatus.IN_PROCESS -> statusData[JobStatus.IN_PROCESS.ordinal]++
                JobStatus.REJECTED -> statusData[JobStatus.REJECTED.ordinal]++
                JobStatus.ACCEPTED -> statusData[JobStatus.ACCEPTED.ordinal]++
            }
        }
        return statusData
    }


    private fun getProcessData(jobsList: List<Job>): FloatArray {
        val statusOptionsNo = 4
        val optionsData = FloatArray(statusOptionsNo)

        jobsList.iterator().forEach { job ->
            if (job.isCoverLetterSent == SentWithCoverLetter.YES && job.wasReplied)
                optionsData[CoverLetterOptions.SENT_AND_REPLIED.ordinal]++
            else if (job.isCoverLetterSent == SentWithCoverLetter.YES && !job.wasReplied) {
                optionsData[CoverLetterOptions.SENT_AND_NOT_REPLIED.ordinal]++
            } else if (job.isCoverLetterSent == SentWithCoverLetter.NO && job.wasReplied)
                optionsData[CoverLetterOptions.NOT_SENT_AND_REPLIED.ordinal]++
            else {
                optionsData[CoverLetterOptions.NOT_SENT_AND_NOT_REPLIED.ordinal]++
            }
        }
        return optionsData
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