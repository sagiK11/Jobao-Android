package com.sagikor.android.jobao.ui.fragments.addedit

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.OnBackPressedCallback
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
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
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class AddEditFragment : Fragment(R.layout.fragment_add_edit_job) {

    private val TAG = AddEditFragment::class.qualifiedName
    private val viewModel: AddEditViewModel by viewModels()
    private lateinit var binding: FragmentAddEditJobBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAddEditJobBinding.bind(view)
        initSpinners()
        initEventChannel()
        setFields()
        bindListeners()
        setOptionalViewsVisibility(View.GONE)
        setOnBackPressDispatcherCallBack()
    }


    private fun bindListeners() {
        binding.edCompanyName.addTextChangedListener { text ->
            viewModel.jobCompany = text.toString()
        }
        binding.edPositionTitle.addTextChangedListener { text ->
            viewModel.jobTitle = text.toString()
        }
        binding.edNotes.addTextChangedListener { text ->
            viewModel.jobNote = text.toString()
        }
        binding.separator.setOnClickListener {
            val isViewsVisible = binding.spinnerStatus.isVisible
            setOptionalViewsVisibility(
                when (isViewsVisible) {
                    true -> View.GONE
                    else -> View.VISIBLE
                }
            )
        }
        setChipLogic(binding.chipTrain, getString(R.string.next_to_train))
        setChipLogic(binding.chipRemote, getString(R.string.remote_job))
        setChipLogic(binding.chipStratup, getString(R.string.startup))
        setChipLogic(binding.chipPartTime, getString(R.string.part_time))
        setChipLogic(binding.chipLongSubmission, getString(R.string.long_submission))
        binding.btnAddApplication.setOnClickListener {
            viewModel.onSaveClick()
        }
        binding.btnCancelApplication.setOnClickListener {
            showAlertDialog()
        }

    }

    private fun showAlertDialog() {
        val alertDialog = AlertDialog.Builder(binding.root.context)
        alertDialog.setTitle(getString(R.string.cancel_title))
        alertDialog.setMessage(getString(R.string.cancel_message))
        alertDialog.setPositiveButton(R.string.yes) { _, _ ->
            findNavController().popBackStack()
        }
        alertDialog.setNegativeButton(R.string.cancel) { _, _ ->
            //do nothing
        }
        alertDialog.show()
    }

    private fun setOptionalViewsVisibility(visibility: Int) {
        binding.spinnerStatus.visibility = visibility
        binding.tvStatus.visibility = visibility
        binding.spinnerAppliedVia.visibility = visibility
        binding.tvAppliedVia.visibility = visibility
        binding.spinnerSentCoverLetter.visibility = visibility
        binding.tvSentWithCoverLetter.visibility = visibility
        binding.separator.drawable.setImageDrawable(
            when (visibility) {
                View.GONE -> ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_baseline_add_circle_24,
                    null
                )
                else -> ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_baseline_remove_circle_24,
                    null
                )
            }
        )
    }


    private fun setChipLogic(chip: Chip, text: String) {
        chip.setOnCheckedChangeListener { _, isChecked ->
            val editText = binding.edNotes
            if (isChecked)
                editText.append("$text, ")
            else {
                editText.setText(editText.text.toString().replace("$text, ", ""))
            }
        }
    }

    private fun setFields() {
        val isInEditMode = viewModel.job != null
        binding.edCompanyName.setText(viewModel.jobCompany)
        binding.edPositionTitle.setText(viewModel.jobTitle)
        binding.spinnerStatus.setSelection(viewModel.jobStatus.ordinal)
        binding.spinnerAppliedVia.setSelection(viewModel.jobAppliedVia.ordinal)
        binding.spinnerSentCoverLetter.setSelection(viewModel.jobIsCoverLetterSent.ordinal)
        binding.edNotes.setText(viewModel.jobNote)
        binding.dateCreated.isVisible = isInEditMode
        binding.dateCreated.text =
            getString(R.string.created_at, "${viewModel.job?.createdAtDateFormat}")

        if (isInEditMode) {
            binding.btnAddApplication.text = getString(R.string.btn_save)
        } else {
            binding.btnAddApplication.text = getString(R.string.btn_save)
        }


    }

    private fun initEventChannel() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.addEditJobEvent.collect { event ->

                when (event) {
                    is AddEditViewModel.AddEditJobEvent.NavigateBackWithResult -> {
                        binding.edCompanyName.clearFocus()
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
                        ).setAnchorView(R.id.nav_view).show()
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
                        ).setAnchorView(R.id.nav_view).show()
                    }
                }
            }
        }
    }

    private fun initSpinners() {
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
            if (jobStatus == JobStatus.REJECTED)
                viewModel.jobDeclinedDate =
                    SimpleDateFormat(
                        "d/M/yyyy",
                        Locale.ENGLISH
                    ).format(Date(System.currentTimeMillis()))
            else {
                viewModel.jobDeclinedDate = ""
            }
            if (jobStatus == JobStatus.IN_PROCESS) {
                viewModel.jobWasReplied = true
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>) {}
    }

    private fun setOnBackPressDispatcherCallBack() {
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    showAlertDialog()
                }
            })
    }


}
