package com.sagikor.android.jobao.ui.fragments.jobslist

import android.app.Application
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.sagikor.android.jobao.data.JobDao




class JobsListViewModel @ViewModelInject constructor(
    private val jobDao: JobDao
) : ViewModel() {

   // val jobs = jobDao.getJobs().asLiveData()
}
//class JobsListViewModel(application: Application) : AndroidViewModel(application) {

//    private val repository: JobRepository
//    val applicationsData : LiveData<List<Job>>
//
//        private val _text = MutableLiveData<String>().apply {
//        value = "This is notifications Fragment"
//    }
//    val text: LiveData<String> = _text
//
//    init {
//        val jobDao = UserDatabase.getDatabase(application).jobDao()
//        repository = JobRepository(jobDao)
//        applicationsData = repository.allJobs
//    }
//}

//original
//class JobsListViewModel(application: Application) : AndroidViewModel(application) {
//
//    private val repository: JobRepository
//    val applicationsData : LiveData<List<Job>>
//
//    private val _text = MutableLiveData<String>().apply {
//        value = "This is notifications Fragment"
//    }
//    val text: LiveData<String> = _text
//
//    init {
//        val jobDao = UserDatabase.getDatabase(application).jobDao()
//        repository = JobRepository(jobDao)
//        applicationsData = repository.allJobs
//    }
//}