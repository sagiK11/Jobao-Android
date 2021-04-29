package com.sagikor.android.jobao.ui.fragments.jobslist

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.sagikor.android.jobao.data.JobDao




class JobsListViewModel @ViewModelInject constructor(
    private val jobDao: JobDao
) : ViewModel() {

}
