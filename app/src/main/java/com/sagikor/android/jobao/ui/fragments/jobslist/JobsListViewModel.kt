package com.sagikor.android.jobao.ui.fragments.jobslist

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagikor.android.jobao.model.JobStatus
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch


class JobsListViewModel @ViewModelInject constructor(
    @Assisted private val state: SavedStateHandle
) : ViewModel() {
    private val TAG = JobsListViewModel::class.qualifiedName
    private val listEventChannel = Channel<ListEvent>()
    val listEvent = listEventChannel.receiveAsFlow()

    val jobStatus = state.get<JobStatus>("jobStatus")
    private var isFilteredByStatusList: Boolean? = null

    var filterByStatus = state.get<JobStatus>("jobStatus")
        set(value) {
            field = value
            state.set("jobStatus", value)
        }


    fun onCreated() {
        isFilteredByStatusList = filterByStatus != null
        viewModelScope.launch {
            if (isFilteredByStatusList!!) {
                listEventChannel.send(ListEvent.ObserveFilteredByStatusList(filterByStatus))
            } else {
                listEventChannel.send(ListEvent.ObserveAllJobsList)
            }
        }
    }

    sealed class ListEvent {
        data class ObserveFilteredByStatusList(val status: JobStatus?) : ListEvent()
        object ObserveAllJobsList : ListEvent()
    }
}
