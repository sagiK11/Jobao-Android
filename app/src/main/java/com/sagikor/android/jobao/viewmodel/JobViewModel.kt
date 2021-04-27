package com.sagikor.android.jobao.viewmodel


import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.sagikor.android.jobao.data.JobDao
import com.sagikor.android.jobao.data.PreferencesHandler
import com.sagikor.android.jobao.data.SortOrder
import com.sagikor.android.jobao.model.Job
import com.sagikor.android.jobao.model.JobStatus
import com.sagikor.android.jobao.ui.activities.ADD_JOB_RESULT_OK
import com.sagikor.android.jobao.ui.activities.EDIT_JOB_RESULT_OK
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

private const val TAG = "JobViewModel"

class JobViewModel @ViewModelInject constructor(
    private val jobDao: JobDao,
    private val preferencesHandler: PreferencesHandler,
    @Assisted private val state: SavedStateHandle
) : ViewModel() {
    private val jobsEventsChannel = Channel<JobsEvents>()
    val jobsEvent = jobsEventsChannel.receiveAsFlow()

    val searchQuery = state.getLiveData("searchQuery", "")
    val preferencesFlow = preferencesHandler.preferenceFlow

    private val jobsFlow = combine(
        searchQuery.asFlow(),
        preferencesFlow
    ) { query, filteredPreferences ->
        Pair(query, filteredPreferences)
    }.flatMapLatest { (query, filteredPreferences) ->
        jobDao.getJobs(
            query, filteredPreferences.sortOrder, when (filteredPreferences.hideRejected) {
                true -> JobStatus.REJECTED
                else -> JobStatus.PENDING
            }
        )
    }

    //original
//    query, filteredPreferences.sortOrder, when (filteredPreferences.hideRejected) {
//        true -> JobStatus.PENDING
//        else -> JobStatus.REJECTED
//    }

    val jobs = jobsFlow.asLiveData()

    fun onSortOrderSelected(sortOrder: SortOrder) {
        viewModelScope.launch {
            preferencesHandler.updateSortOrder(sortOrder)
        }
    }

    fun onHideRejectedSelected(hideRejected: Boolean) {
        viewModelScope.launch {
            preferencesHandler.updateHideRejected(hideRejected)
        }
    }

    fun onJobSelected(job: Job) {
        viewModelScope.launch {
            jobsEventsChannel.send(JobsEvents.NavigateToEditJobScreen(job))
        }
    }

    fun onJobSwiped(job: Job) {
        viewModelScope.launch {
            jobDao.deleteJob(job)
            jobsEventsChannel.send(JobsEvents.ShowUndoDeleteJobMessage(job))
        }
    }

    fun onUndoDeleteJob(job: Job) {
        viewModelScope.launch {
            jobDao.addJob(job)
        }
    }

    fun onAddEditResult(result: Int) {
        when (result) {
            ADD_JOB_RESULT_OK -> showJobSavedConfirmation("Job added")
            EDIT_JOB_RESULT_OK -> showJobSavedConfirmation("Job updated")
        }
    }

    private fun showJobSavedConfirmation(text: String) {
        viewModelScope.launch {
            jobsEventsChannel.send(JobsEvents.ShowJobSavedConfirmationMessage(text))
        }
    }

    sealed class JobsEvents {
        data class NavigateToEditJobScreen(val job: Job) : JobsEvents()
        data class ShowUndoDeleteJobMessage(val job: Job) : JobsEvents()
        data class ShowJobSavedConfirmationMessage(val message: String) : JobsEvents()
    }
}
