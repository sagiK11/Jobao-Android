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


class JobViewModel @ViewModelInject constructor(
    private val jobDao: JobDao,
    private val preferencesHandler: PreferencesHandler,
    @Assisted private val state: SavedStateHandle
) : ViewModel() {

    private val TAG = JobViewModel::class.qualifiedName

    private val jobsEventsChannel = Channel<JobsEvents>()
    val jobsEvent = jobsEventsChannel.receiveAsFlow()

    private val actionEventChannel = Channel<ActionEvent>()
    val actionEvent = actionEventChannel.receiveAsFlow()

    val searchQuery = state.getLiveData("searchQuery", "")
    val preferencesFlow = preferencesHandler.preferenceFlow
    val jobStatus = state.getLiveData("jobStatus", JobStatus.IN_PROCESS)

    private val jobsFlow = combine(
        searchQuery.asFlow(),
        preferencesFlow
    ) { query, filteredPreferences ->
        Pair(query, filteredPreferences)
    }.flatMapLatest { (query, filteredPreferences) ->
        jobDao.getFilteredJobs(
            query, filteredPreferences.sortOrder, when (filteredPreferences.hideRejected) {
                true -> JobStatus.PENDING
                else -> JobStatus.REJECTED
            }
        )
    }

    val filteredJobs = jobsFlow.asLiveData()

    private val allJobsFlow = jobDao.getAllJobs()
    val allJobs = allJobsFlow.asLiveData()

    private val filteredByStatusFlow = jobStatus.asFlow().flatMapLatest { jobStatus ->
        jobDao.getFilteredJobByStatus(jobStatus)
    }
    val filteredByStatus = filteredByStatusFlow.asLiveData()

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

    fun onJobDelete(job: Job) {
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
            ADD_JOB_RESULT_OK -> showJobSavedConfirmation(JOB_ADDED)
            EDIT_JOB_RESULT_OK -> showJobSavedConfirmation(JOB_EDITED)
        }
    }

    private fun showJobSavedConfirmation(text: String) {
        viewModelScope.launch {
            jobsEventsChannel.send(JobsEvents.ShowJobSavedConfirmationMessage(text))
        }
    }

    fun onRateUsClick() {
        viewModelScope.launch {
            actionEventChannel.send(ActionEvent.NavigateToGooglePlayRate)
        }
    }

    fun onSendToMailClick() {
        viewModelScope.launch {
            actionEventChannel.send(ActionEvent.SendToMailSuccess)
        }
    }

    sealed class JobsEvents {
        data class NavigateToEditJobScreen(val job: Job) : JobsEvents()
        data class ShowUndoDeleteJobMessage(val job: Job) : JobsEvents()
        data class ShowJobSavedConfirmationMessage(val message: String) : JobsEvents()
    }

    sealed class ActionEvent {
        object NavigateToGooglePlayRate : ActionEvent()
        object SendToMailSuccess : ActionEvent()
        object SendToMailFail : ActionEvent()
    }
}

const val JOB_ADDED = "job_added"
const val JOB_EDITED = "job_edited"
const val ADD_EDIT_REQUEST = "add_edit_request"
const val ADD_EDIT_RESULT = "add_edit_result"
