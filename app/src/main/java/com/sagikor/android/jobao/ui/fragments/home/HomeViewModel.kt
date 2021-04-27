package com.sagikor.android.jobao.ui.fragments.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartView

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text

    private val _recent_activity_chart = MutableLiveData<AAChartView>()

    val recentActivityChart: LiveData<AAChartView> = _recent_activity_chart
}