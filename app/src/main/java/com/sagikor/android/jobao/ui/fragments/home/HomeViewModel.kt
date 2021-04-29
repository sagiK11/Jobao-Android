package com.sagikor.android.jobao.ui.fragments.home

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartView
import com.sagikor.android.jobao.data.JobDao
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class HomeViewModel @ViewModelInject constructor(
) : ViewModel() {

    private val jobsEventChannel = Channel<JobsEvent>()
    val jobEvent = jobsEventChannel.receiveAsFlow()

    fun onAddNewJobClick(){
        viewModelScope.launch {
            jobsEventChannel.send(JobsEvent.NavigateToAddNewJobScreen)
        }
    }

    sealed class JobsEvent{
        object NavigateToAddNewJobScreen : JobsEvent()
    }


}