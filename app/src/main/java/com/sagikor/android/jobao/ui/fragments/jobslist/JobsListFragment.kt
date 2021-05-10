package com.sagikor.android.jobao.ui.fragments.jobslist

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.sagikor.android.jobao.R
import com.sagikor.android.jobao.data.SortOrder
import com.sagikor.android.jobao.databinding.FragmentJobsListBinding
import com.sagikor.android.jobao.model.Job
import com.sagikor.android.jobao.model.JobStatus
import com.sagikor.android.jobao.ui.activities.OnScrollListener
import com.sagikor.android.jobao.ui.fragments.jobslist.JobsListViewModel.ListEvent.ObserveFilteredByStatusList
import com.sagikor.android.jobao.util.exhaustive
import com.sagikor.android.jobao.util.onQueryTextChanged
import com.sagikor.android.jobao.viewmodel.ADD_EDIT_REQUEST
import com.sagikor.android.jobao.viewmodel.ADD_EDIT_RESULT
import com.sagikor.android.jobao.viewmodel.JOB_ADDED
import com.sagikor.android.jobao.viewmodel.JobViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_jobs_list.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class JobsListFragment : Fragment(R.layout.fragment_jobs_list), JobAdapter.onItemClickListener {
    private val TAG = JobsListFragment::class.qualifiedName
    private val jobViewModel: JobViewModel by viewModels()
    private val viewModel: JobsListViewModel by viewModels()
    private lateinit var binding: FragmentJobsListBinding
    private var onScrollListener: OnScrollListener? = null
    private lateinit var jobAdapter: JobAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        binding = FragmentJobsListBinding.bind(view)
        viewModel.onCreated()
        jobAdapter = JobAdapter(this)

        initListEventChannel()
        initRecycleViewSettings()
        initItemTouchHelper()
        initFragmentResultListener()
        initJobEventChannel()
        setHasOptionsMenu(true)
        setAdapterDataObserver()
    }

    private fun initListEventChannel() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.listEvent.collect { event ->
                when (event) {
                    is JobsListViewModel.ListEvent.ObserveAllJobsList -> observeAllJobs()
                    is ObserveFilteredByStatusList -> {
                        observeFilteredByStatusJobs(event.status)
                        setHasOptionsMenu(false)
                    }

                }
            }
        }
    }

    private fun observeAllJobs() {
        jobViewModel.filteredJobs.observe(viewLifecycleOwner) { jobsList ->
            submitList(jobsList)
        }
    }

    private fun observeFilteredByStatusJobs(jobStatus: JobStatus?) {
        jobViewModel.filteredByStatus.observe(viewLifecycleOwner) { jobsList ->
            val actionBar = (requireActivity() as AppCompatActivity).supportActionBar
            actionBar?.title = when (jobStatus) {
                JobStatus.ACCEPTED -> getString(R.string.offers_title)
                else -> getString(R.string.active_process_title)
            }
            submitList(jobsList)
        }
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

    private fun initRecycleViewSettings() {
        binding.rvJobsList.apply {
            adapter = jobAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    DividerItemDecoration.VERTICAL
                )
            )
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy < 0) {
                        onScrollListener?.onScrollUp()
                    } else if (dy > 0) {
                        onScrollListener?.onScrollDown()
                    }
                }

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    val minItems = 8
                    if (jobAdapter.itemCount > minItems && !recyclerView.canScrollVertically(1)) {//reached bottom
                        onScrollListener?.onScrollDown()
                    }
                }
            })
        }
    }

    private fun initItemTouchHelper() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                //not supported in app
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val job = jobAdapter.currentList[viewHolder.adapterPosition]
                jobViewModel.onJobSwiped(job)
            }
        }).attachToRecyclerView(rv_jobs_list)

    }

    private fun initJobEventChannel() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            jobViewModel.jobsEvent.collect { event ->
                when (event) {
                    is JobViewModel.JobsEvents.ShowUndoDeleteJobMessage -> {
                        showSnackBarWithAction(event.job, getString(R.string.job_deleted))
                    }
                    is JobViewModel.JobsEvents.NavigateToEditJobScreen -> {
                        val action =
                            JobsListFragmentDirections.actionNavigationApplicationsToAddEditFragment(
                                event.job,
                                getString(R.string.title_edit_job)
                            )
                        findNavController().navigate(action)
                    }
                    is JobViewModel.JobsEvents.ShowJobSavedConfirmationMessage -> {
                        val msg =
                            if (event.message == JOB_ADDED) getString(R.string.add_success) else getString(
                                R.string.edit_success
                            )
                        showSnackBar(msg)
                    }
                }.exhaustive
            }
        }
    }

    private fun showSnackBar(msg: String) {
        Snackbar.make(
            requireView(),
            msg,
            Snackbar.LENGTH_LONG
        ).setAnchorView(R.id.fab).show()
    }

    private fun showSnackBarWithAction(job: Job, text: String) {
        Snackbar.make(
            requireView(),
            text,
            Snackbar.LENGTH_LONG
        ).setAction(getString(R.string.undo)) {
            jobViewModel.onUndoDeleteJob(job)
        }.setAnchorView(R.id.fab).show()
    }

    private fun initFragmentResultListener() {
        setFragmentResultListener(ADD_EDIT_REQUEST) { _, bundle ->
            val result = bundle.getInt(ADD_EDIT_RESULT)
            jobViewModel.onAddEditResult(result)
        }
    }

    private fun setAdapterDataObserver() {
        jobAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                setEmptyListViewVisibility()
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                setEmptyListViewVisibility()
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                setEmptyListViewVisibility()
            }

            fun setEmptyListViewVisibility() {
                empty_list_display_view.visibility = when (jobAdapter.itemCount) {
                    0 -> View.VISIBLE
                    else -> View.GONE
                }
            }
        })
    }

    override fun onItemClick(job: Job) {
        jobViewModel.onJobSelected(job)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.jobs_list_menu, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.onQueryTextChanged {
            jobViewModel.searchQuery.value = it
        }

        viewLifecycleOwner.lifecycleScope.launch {
            menu.findItem(R.id.action_hide_declined_jobs).isChecked =
                jobViewModel.preferencesFlow.first().hideRejected
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sort_by_name -> {
                jobViewModel.onSortOrderSelected(SortOrder.BY_COMPANY)
                true
            }
            R.id.action_sort_by_date -> {
                jobViewModel.onSortOrderSelected(SortOrder.BY_DATE)
                true
            }
            R.id.action_hide_declined_jobs -> {
                item.isChecked = !item.isChecked
                jobViewModel.onHideRejectedSelected(item.isChecked)
                true
            }
            R.id.action_sort_by_status -> {
                jobViewModel.onSortOrderSelected(SortOrder.BY_STATUS)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun submitList(jobsList: List<Job>?) {
        jobAdapter.apply {
            submitList(null)//sending null to scroll to top after sort
            submitList(jobsList)
        }
    }
}