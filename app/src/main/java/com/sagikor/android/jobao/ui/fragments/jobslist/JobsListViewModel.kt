package com.sagikor.android.jobao.ui.fragments.jobslist

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.sagikor.android.jobao.model.JobStatus


class JobsListViewModel @ViewModelInject constructor(
    @Assisted private val state: SavedStateHandle
) : ViewModel() {
    private val TAG = JobsListViewModel::class.qualifiedName
    val jobStatus = state.get<JobStatus>("jobStatus")

    var filterByStatus = state.get<JobStatus>("jobStatus")
        set(value) {
            field = value
            state.set("jobStatus", value)
        }
}
