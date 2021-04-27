package com.sagikor.android.jobao.ui.fragments.jobslist

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.sagikor.android.jobao.R
import com.sagikor.android.jobao.viewmodel.JobViewModel
import com.sagikor.android.jobao.util.onQueryTextChanged
import com.sagikor.android.jobao.data.SortOrder
import com.sagikor.android.jobao.databinding.FragmentJobsListBinding
import com.sagikor.android.jobao.model.Job
import com.sagikor.android.jobao.util.exhaustive
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_jobs_list.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class JobsListFragment : Fragment(R.layout.fragment_jobs_list), JobAdapter.onItemClickListener {

    private val jobViewModel: JobViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentJobsListBinding.bind(view)
        val jobAdapter = JobAdapter(this)

        binding.apply {
            rvJobsList.apply {
                adapter = jobAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }
        }
        initItemTouchHelper(jobAdapter, rv_jobs_list)

        jobViewModel.jobs.observe(viewLifecycleOwner) {
            jobAdapter.submitList(it)
        }

        setFragmentResultListener("add_edit_result") { _, bundle ->
            val result = bundle.getInt("add_edit_result")
            jobViewModel.onAddEditResult(result)
        }
        initEventChannel()

        setHasOptionsMenu(true)
    }

    private fun initItemTouchHelper(jobAdapter: JobAdapter, rvJobsList: RecyclerView?) {
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
        }).attachToRecyclerView(rvJobsList)

    }

    private fun initEventChannel() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            jobViewModel.jobsEvent.collect { event ->
                when (event) {
                    is JobViewModel.JobsEvents.ShowUndoDeleteJobMessage -> {
                        Snackbar.make(
                            requireView(),
                            getString(R.string.job_deleted),
                            Snackbar.LENGTH_LONG
                        )
                            .setAction(getString(R.string.undo)) {
                                jobViewModel.onUndoDeleteJob(event.job)
                            }.show()
                    }
                    is JobViewModel.JobsEvents.NavigateToEditJobScreen -> {
                        val action =
                            JobsListFragmentDirections.actionNavigationApplicationsToNavigationAddApplication(
                                event.job,
                                getString(R.string.edit_application)
                            )
                        findNavController().navigate(action)
                    }
                    is JobViewModel.JobsEvents.ShowJobSavedConfirmationMessage -> {
                        val msg =
                            if (event.message == "Job added") getString(R.string.add_success) else getString(
                                R.string.edit_success
                            )
                        Snackbar.make(
                            requireView(),
                            msg,
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }.exhaustive
            }
        }
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

    override fun onItemClick(job: Job) {
        jobViewModel.onJobSelected(job)
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
            else -> super.onOptionsItemSelected(item)
        }
    }
}