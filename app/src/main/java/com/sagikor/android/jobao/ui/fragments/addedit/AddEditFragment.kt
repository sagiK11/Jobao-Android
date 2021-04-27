package com.sagikor.android.jobao.ui.fragments.addedit

import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.sagikor.android.jobao.R
import com.sagikor.android.jobao.databinding.FragmentAddEditJobBinding
import com.sagikor.android.jobao.model.AppliedVia
import com.sagikor.android.jobao.model.JobStatus
import com.sagikor.android.jobao.model.SentWithCoverLetter
import com.sagikor.android.jobao.ui.activities.ADD_JOB_RESULT_OK
import com.sagikor.android.jobao.util.AppExceptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_add_edit_job.view.*
import kotlinx.coroutines.flow.collect
import java.util.*


@AndroidEntryPoint
class AddEditFragment : Fragment(R.layout.fragment_add_edit_job) {

    private val viewModel: AddEditViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentAddEditJobBinding.bind(view)

        binding.apply {
            setFields(this)
            bindListeners(this)
        }
        initSpinners(binding)
        initEventChannel(binding)
    }

    private fun bindListeners(fragmentAddEditJobBinding: FragmentAddEditJobBinding) {
        val calender = Calendar.getInstance()
        val year = calender.get(Calendar.YEAR)
        val month = calender.get(Calendar.MONTH)
        val day = calender.get(Calendar.DAY_OF_MONTH)
        fragmentAddEditJobBinding.edCompanyName.addTextChangedListener {
            viewModel.jobCompany = it.toString()
        }
        fragmentAddEditJobBinding.edPositionTitle.addTextChangedListener {
            viewModel.jobTitle = it.toString()
        }
        fragmentAddEditJobBinding.edDateApplied.addTextChangedListener {
            viewModel.jobDateApplied = it.toString()
        }
        fragmentAddEditJobBinding.edDateApplied.setOnClickListener {
            val datePicker = DatePickerDialog(requireContext(), { view, year, month, day ->
                fragmentAddEditJobBinding.edDateApplied.setText(
                    getString(
                        R.string.date,
                        day,
                        month,
                        year
                    )
                )
            }, year, month, day)
            datePicker.show()
        }

        fragmentAddEditJobBinding.btnAddApplication.setOnClickListener {
            viewModel.onSaveClick()
        }

    }

    private fun setFields(fragmentAddEditJobBinding: FragmentAddEditJobBinding) {
        val isInEditMode = viewModel.job != null
        fragmentAddEditJobBinding.edCompanyName.setText(viewModel.jobCompany)
        fragmentAddEditJobBinding.edPositionTitle.setText(viewModel.jobTitle)
        fragmentAddEditJobBinding.edDateApplied.setText(viewModel.jobDateApplied)
        fragmentAddEditJobBinding.spinnerStatus.setSelection(viewModel.jobStatus.ordinal)
        fragmentAddEditJobBinding.spinnerAppliedVia.setSelection(viewModel.jobAppliedVia.ordinal)
        fragmentAddEditJobBinding.spinnerSentCoverLetter.setSelection(viewModel.jobIsCoverLetterSent.ordinal)
        fragmentAddEditJobBinding.dateCreated.isVisible = isInEditMode
        fragmentAddEditJobBinding.dateCreated.text =
            getString(R.string.created_at, "${viewModel.job?.createdAtDateFormat}")

        val casted = requireActivity() as AppCompatActivity
        casted.supportActionBar?.title
        if (isInEditMode) {
            fragmentAddEditJobBinding.btnAddApplication.text = getString(R.string.edit_application)
            casted.supportActionBar?.title = getString(R.string.title_edit_job)
        } else {
            fragmentAddEditJobBinding.btnAddApplication.text = getString(R.string.add_application)
            casted.supportActionBar?.title = getString(R.string.title_add_job)
        }


    }

    private fun initEventChannel(fragmentAddEditJobBinding: FragmentAddEditJobBinding) {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.addEditJobEvent.collect { event ->

                when (event) {
                    is AddEditViewModel.AddEditJobEvent.NavigateBackWithResult -> {
                        fragmentAddEditJobBinding.edCompanyName.clearFocus()
                        setFragmentResult(
                            "add_edit_request",
                            bundleOf("add_edit_result" to event.result)
                        )
                        findNavController().popBackStack()
                    }
                    is AddEditViewModel.AddEditJobEvent.ShowOperationSuccess -> {
                        val msg = when (event.result) {
                            ADD_JOB_RESULT_OK -> getString(R.string.add_success)
                            else -> getString(R.string.edit_success)
                        }
                        Snackbar.make(
                            requireView(),
                            msg,
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    is AddEditViewModel.AddEditJobEvent.ShowInvalidInputMessage -> {
                        val error = when (event.location) {
                            AppExceptions.Location.COMPANY -> getString(R.string.company_view_error)
                            AppExceptions.Location.TITLE -> getString(R.string.title_view_error)
                            AppExceptions.Location.DATE_APPLIED -> getString(R.string.date_view_error)
                        }
                        Snackbar.make(
                            requireView(),
                            error,
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    private fun initSpinners(binding: FragmentAddEditJobBinding) {
        setSpinnersText(
            binding.root.spinner_applied_via,
            R.array.applied_via_array,
            AppliedViaListener()
        )
        setSpinnersText(
            binding.root.spinner_sent_cover_letter,
            R.array.sent_with_cover_letter_array,
            IsCoverLetterSentListener()
        )
        setSpinnersText(
            binding.root.spinner_status,
            R.array.status_array,
            StatusListener()
        )
    }

    private fun setSpinnersText(
        spinner: Spinner,
        array: Int,
        listener: AdapterView.OnItemSelectedListener
    ) {
        ArrayAdapter.createFromResource(
            requireContext(),
            array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter

        }
        spinner.onItemSelectedListener = listener
    }

    inner class AppliedViaListener : Activity(), AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
            val item = parent.getItemAtPosition(pos).toString()
            val via = when (item) {
                parent.context.getString(R.string.site) -> AppliedVia.SITE
                parent.context.getString(R.string.email) -> AppliedVia.EMAIL
                else -> AppliedVia.REFERENCE
            }
            viewModel.jobAppliedVia = via
        }

        override fun onNothingSelected(parent: AdapterView<*>) {}
    }

    inner class IsCoverLetterSentListener : Activity(), AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
            val item = parent.getItemAtPosition(pos).toString()
            val answer = when (item) {
                parent.context.getString(R.string.yes) -> SentWithCoverLetter.YES
                else -> SentWithCoverLetter.NO
            }
            viewModel.jobIsCoverLetterSent = answer
        }

        override fun onNothingSelected(parent: AdapterView<*>) {}
    }

    inner class StatusListener : Activity(), AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
            val item = parent.getItemAtPosition(pos).toString()
            val jobStatus = when (item) {
                parent.context.getString(R.string.pending) -> JobStatus.PENDING
                parent.context.getString(R.string.in_process) -> JobStatus.IN_PROCESS
                parent.context.getString(R.string.accepted) -> JobStatus.ACCEPTED
                else -> JobStatus.REJECTED
            }
            viewModel.jobStatus = jobStatus
        }

        override fun onNothingSelected(parent: AdapterView<*>) {}
    }
}
