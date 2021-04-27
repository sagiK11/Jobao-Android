package com.sagikor.android.jobao.ui.fragments.addedit

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.sagikor.android.jobao.data.JobDao
import com.sagikor.android.jobao.model.AppliedVia
import com.sagikor.android.jobao.model.Job
import com.sagikor.android.jobao.model.JobStatus
import com.sagikor.android.jobao.model.SentWithCoverLetter
import com.sagikor.android.jobao.ui.activities.ADD_JOB_RESULT_OK
import com.sagikor.android.jobao.ui.activities.EDIT_JOB_RESULT_OK
import com.sagikor.android.jobao.util.AppExceptions
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch


class AddEditViewModel @ViewModelInject constructor(
    private val jobDao: JobDao,
    @Assisted private val state: SavedStateHandle
) : ViewModel() {

    val job = state.get<Job>("job")
    private val addEditTaskJobChannel = Channel<AddEditJobEvent>()
    val addEditJobEvent = addEditTaskJobChannel.receiveAsFlow()

    var jobCompany = state.get<String>("jobCompany") ?: job?.companyName ?: ""
        set(value) {
            field = value
            state.set("jobCompany", value)
        }

    var jobTitle = state.get<String>("jobTitle") ?: job?.title ?: ""
        set(value) {
            field = value
            state.set("jobTitle", value)
        }

    var jobDateApplied = state.get<String>("jobDateApplied") ?: job?.dateApplied ?: ""
        set(value) {
            field = value
            state.set("jobDateApplied", value)
        }

    var jobStatus = state.get<JobStatus>("jobStatus") ?: job?.status ?: JobStatus.PENDING
        set(value) {
            field = value
            state.set("jobStatus", value)
        }

    var jobAppliedVia = state.get<AppliedVia>("jobAppliedVia") ?: job?.appliedVia ?: AppliedVia.SITE
        set(value) {
            field = value
            state.set("jobAppliedVia", value)
        }

    var jobIsCoverLetterSent =
        state.get<SentWithCoverLetter>("jobIsCoverLetterSent") ?: job?.isCoverLetterSent
        ?: SentWithCoverLetter.NO
        set(value) {
            field = value
            state.set("jobIsCoverLetterSent", value)
        }

    fun onSaveClick() {
        try {
            validateForm()
        } catch (e: AppExceptions.Input) {
            viewModelScope.launch {
                addEditTaskJobChannel.send(AddEditJobEvent.ShowInvalidInputMessage(e.location))
            }
            return
        }
        if (job != null) {
            val updatedJob = job.copy(
                companyName = jobCompany,
                title = jobTitle,
                dateApplied = jobDateApplied,
                status = jobStatus,
                appliedVia = jobAppliedVia,
                isCoverLetterSent = jobIsCoverLetterSent
            )
            updateJob(updatedJob)
        } else {
            val newJob = Job(
                companyName = jobCompany,
                title = jobTitle,
                dateApplied = jobDateApplied,
                status = jobStatus,
                appliedVia = jobAppliedVia,
                isCoverLetterSent = jobIsCoverLetterSent
            )
            createJob(newJob)
        }
    }


    private fun createJob(newJob: Job) {
        viewModelScope.launch {
            jobDao.addJob(newJob)
            addEditTaskJobChannel.send(AddEditJobEvent.ShowOperationSuccess(ADD_JOB_RESULT_OK))
        }
    }


    private fun updateJob(updatedJob: Job) {
        viewModelScope.launch {
            jobDao.updateJob(updatedJob)
            addEditTaskJobChannel.send(AddEditJobEvent.NavigateBackWithResult(EDIT_JOB_RESULT_OK))
            addEditTaskJobChannel.send(AddEditJobEvent.ShowOperationSuccess(EDIT_JOB_RESULT_OK))
        }
    }

    sealed class AddEditJobEvent {
        data class ShowOperationSuccess(val result: Int) : AddEditJobEvent()
        data class NavigateBackWithResult(val result: Int) : AddEditJobEvent()
        data class ShowInvalidInputMessage(val location: AppExceptions.Location) : AddEditJobEvent()
    }


    private fun validateForm() {
        when {
            jobCompany.isBlank() -> {
                throw AppExceptions.Input(AppExceptions.Location.COMPANY)
            }
            jobTitle.isBlank() -> {
                throw AppExceptions.Input(AppExceptions.Location.TITLE)
            }
            jobDateApplied.isBlank() -> {
                throw AppExceptions.Input(AppExceptions.Location.DATE_APPLIED)
            }
        }
    }

}
