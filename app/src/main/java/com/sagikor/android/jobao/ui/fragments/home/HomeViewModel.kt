package com.sagikor.android.jobao.ui.fragments.home

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class HomeViewModel @ViewModelInject constructor(
) : ViewModel() {

    private val jobsEventChannel = Channel<JobsEvent>()
    val jobEvent = jobsEventChannel.receiveAsFlow()

    sealed class JobsEvent {
        object NavigateToAddNewJobScreen : JobsEvent()
    }


}