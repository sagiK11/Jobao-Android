package com.sagikor.android.jobao.ui.fragments.dashboard

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.sagikor.android.jobao.R
import com.sagikor.android.jobao.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment(R.layout.fragment_dashboard)  {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentDashboardBinding.bind(view)
    }
}