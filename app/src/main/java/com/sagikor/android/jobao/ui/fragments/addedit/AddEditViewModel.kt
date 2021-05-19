package com.sagikor.android.jobao.ui.fragments.addedit

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagikor.android.jobao.data.JobDao
import com.sagikor.android.jobao.model.AppliedVia
import com.sagikor.android.jobao.model.Job
import com.sagikor.android.jobao.model.JobStatus
import com.sagikor.android.jobao.model.SentWithCoverLetter
import com.sagikor.android.jobao.ui.activities.ADD_JOB_RESULT_OK
import com.sagikor.android.jobao.ui.activities.EDIT_JOB_RESULT_OK
import com.sagikor.android.jobao.ui.activities.GO_BACK_RESULT_OK
import com.sagikor.android.jobao.util.AppExceptions
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch


class AddEditViewModel @ViewModelInject constructor(
    private val jobDao: JobDao,
    @Assisted private val state: SavedStateHandle
) : ViewModel() {
    private var isStateChanged = false
    val job = state.get<Job>(Keys.job)

    private val addEditTaskJobChannel = Channel<AddEditJobEvent>()
    val addEditJobEvent = addEditTaskJobChannel.receiveAsFlow()

    var jobCompany = state.get<String>(Keys.company) ?: job?.companyName ?: ""
        set(value) {
            field = value
            isStateChanged = true
            state.set(Keys.company, value)
        }

    var jobTitle = state.get<String>(Keys.title) ?: job?.title ?: ""
        set(value) {
            field = value
            isStateChanged = true
            state.set(Keys.title, value)
        }


    var jobDeclinedDate = state.get<String>(Keys.declineDate) ?: job?.declinedDate ?: ""
        set(value) {
            field = value
            isStateChanged = true
            state.set(Keys.declineDate, value)
        }

    var jobWasReplied = state.get<Boolean>(Keys.wasReplied) ?: job?.wasReplied ?: false
        set(value) {
            field = value
            isStateChanged = true
            state.set(Keys.wasReplied, value)
        }

    var jobStatus = state.get<JobStatus>(Keys.status) ?: job?.status ?: JobStatus.UNPROVIDED
        set(value) {
            field = value
            isStateChanged = true
            state.set(Keys.status, value)
        }

    var jobAppliedVia = state.get<AppliedVia>(Keys.appliedVia) ?: job?.appliedVia ?: AppliedVia.UNPROVIDED
        set(value) {
            field = value
            isStateChanged = true
            state.set(Keys.appliedVia, value)
        }

    var jobIsCoverLetterSent =
        state.get<SentWithCoverLetter>(Keys.coverLetter) ?: job?.isCoverLetterSent
        ?: SentWithCoverLetter.UNPROVIDED
        set(value) {
            field = value
            isStateChanged = true
            state.set(Keys.coverLetter, value)
        }

    var jobNote = state.get<String>(Keys.note) ?: job?.note ?: ""
        set(value) {
            field = value
            isStateChanged = true
            state.set(Keys.note, value)
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
                status = jobStatus,
                appliedVia = jobAppliedVia,
                wasReplied = jobWasReplied,
                declinedDate = jobDeclinedDate,
                note = jobNote,
                isCoverLetterSent = jobIsCoverLetterSent
            )
            updateJob(updatedJob)
        } else {
            val newJob = Job(
                companyName = jobCompany,
                title = jobTitle,
                status = jobStatus,
                appliedVia = jobAppliedVia,
                wasReplied = jobWasReplied,
                declinedDate = jobDeclinedDate,
                note = jobNote,
                isCoverLetterSent = jobIsCoverLetterSent
            )
            createJob(newJob)
        }
    }

    fun onCancelClick() {
        if (isStateChanged) {
            viewModelScope.launch {
                addEditTaskJobChannel.send(
                    AddEditJobEvent.ShowGoBackConfirmationMessage(GO_BACK_RESULT_OK)
                )
            }
        } else {
            viewModelScope.launch {
                addEditTaskJobChannel.send(AddEditJobEvent.NavigateBack)
            }
        }
    }


    private fun createJob(newJob: Job) {
        viewModelScope.launch {
            jobDao.addJob(newJob)
            addEditTaskJobChannel.send(AddEditJobEvent.NavigateBackWithResult(ADD_JOB_RESULT_OK))
        }
    }


    private fun updateJob(updatedJob: Job) {
        viewModelScope.launch {
            jobDao.updateJob(updatedJob)
            addEditTaskJobChannel.send(AddEditJobEvent.NavigateBackWithResult(EDIT_JOB_RESULT_OK))
        }
    }

    sealed class AddEditJobEvent {
        object NavigateBack : AddEditJobEvent()
        data class NavigateBackWithResult(val result: Int) : AddEditJobEvent()
        data class ShowInvalidInputMessage(val location: AppExceptions.Location) : AddEditJobEvent()
        data class ShowGoBackConfirmationMessage(val result: Int) : AddEditJobEvent()
    }

    object Keys {
        const val job = "job"
        const val company = "jobCompany"
        const val title = "jobTitle"
        const val declineDate = "jobDeclinedDate"
        const val wasReplied = "jobWasReplied"
        const val status = "jobStatus"
        const val appliedVia = "jobAppliedVia"
        const val coverLetter = "jobIsCoverLetterSent"
        const val note = "jobNote"
    }


    private fun validateForm() {
        when {
            jobCompany.isBlank() -> {
                throw AppExceptions.Input(AppExceptions.Location.COMPANY)
            }
            jobTitle.isBlank() -> {
                throw AppExceptions.Input(AppExceptions.Location.TITLE)
            }
        }
    }

}
