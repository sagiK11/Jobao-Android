package com.sagikor.android.jobao.ui.fragments.addedit

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.sagikor.android.jobao.R
import com.sagikor.android.jobao.databinding.FragmentAddEditJobBinding
import com.sagikor.android.jobao.model.AppliedVia
import com.sagikor.android.jobao.model.JobStatus
import com.sagikor.android.jobao.model.SentWithCoverLetter
import com.sagikor.android.jobao.util.AppExceptions
import com.sagikor.android.jobao.viewmodel.ADD_EDIT_REQUEST
import com.sagikor.android.jobao.viewmodel.ADD_EDIT_RESULT
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_add_edit_job.*
import kotlinx.coroutines.flow.collect


@AndroidEntryPoint
class AddEditFragment : Fragment(R.layout.fragment_add_edit_job) {

    private val TAG = AddEditFragment::class.qualifiedName
    private val viewModel: AddEditViewModel by viewModels()
    private lateinit var binding: FragmentAddEditJobBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAddEditJobBinding.bind(view)
        initEventChannel()
        setFields()
        bindListeners()
        setOptionalViewsVisibility(View.GONE)
        setOnBackPressDispatcherCallBack()
    }

    private fun bindListeners() {
        bindRequiredFieldsListener()
        bindSeparatorListener()
        bindOptionalFieldsListeners()
        bindNoteFieldListener()
        setChipLogic(binding.chipTrain, getString(R.string.next_to_train))
        setChipLogic(binding.chipRemote, getString(R.string.remote_job))
        setChipLogic(binding.chipStratup, getString(R.string.startup))
        setChipLogic(binding.chipPartTime, getString(R.string.part_time))
        setChipLogic(binding.chipLongSubmission, getString(R.string.long_submission))
        bindSaveCancelButtonsListeners()
    }

    private fun bindSaveCancelButtonsListeners() {
        binding.apply {
            btnAddApplication.setOnClickListener {
                viewModel.onSaveClick()
            }
            btnCancelApplication.setOnClickListener {
                viewModel.onCancelClick()
            }
        }
    }

    private fun bindNoteFieldListener() {
        binding.apply {
            edNotes.addTextChangedListener { text ->
                // comma in note breaks the csv generator
                val safeText = text.toString().replace(",", ".")
                viewModel.jobNote = safeText
            }
        }
    }

    private fun bindOptionalFieldsListeners() {
        binding.apply {
            statusGroup.addOnButtonCheckedListener { _, checkedId, _ ->
                viewModel.jobStatus = when (checkedId) {
                    R.id.btn_pending -> JobStatus.PENDING
                    R.id.btn_in_process -> JobStatus.IN_PROCESS
                    R.id.btn_rejected -> JobStatus.REJECTED
                    else -> JobStatus.ACCEPTED
                }
                if (viewModel.jobStatus == JobStatus.ACCEPTED ||
                    viewModel.jobStatus == JobStatus.IN_PROCESS
                ) {
                    viewModel.jobWasReplied = true
                }
            }
            appliedViaGroup.addOnButtonCheckedListener { _, checkedId, _ ->
                viewModel.jobAppliedVia = when (checkedId) {
                    R.id.btn_company_site -> AppliedVia.SITE
                    R.id.btn_email -> AppliedVia.EMAIL
                    R.id.btn_reference -> AppliedVia.REFERENCE
                    R.id.btn_linkedin -> AppliedVia.LINKEDIN
                    else -> AppliedVia.OTHER
                }

            }
            coverLetterGroup.addOnButtonCheckedListener { _, checkedId, _ ->
                viewModel.jobIsCoverLetterSent = when (checkedId) {
                    R.id.btn_cover_letter_positive -> SentWithCoverLetter.YES
                    else -> SentWithCoverLetter.NO
                }
            }
        }
    }

    private fun bindSeparatorListener() {
        binding.apply {
            separator.setOnClickListener {
                val isViewsVisible = binding.tvStatus.isVisible
                setOptionalViewsVisibility(
                    when (isViewsVisible) {
                        true -> View.GONE
                        else -> View.VISIBLE
                    }
                )
            }
        }
    }

    private fun bindRequiredFieldsListener() {
        binding.apply {
            edCompanyName.addTextChangedListener { text ->
                viewModel.jobCompany = text.toString()
            }
            edPositionTitle.addTextChangedListener { text ->
                viewModel.jobTitle = text.toString()
            }
        }
    }

    private fun showAlertDialog() {
        AlertDialog.Builder(binding.root.context).apply {
            setTitle(getString(R.string.cancel_title))
            setMessage(getString(R.string.cancel_message))
            setPositiveButton(R.string.yes) { _, _ ->
                findNavController().popBackStack()
            }
            setNegativeButton(R.string.cancel) { _, _ ->
                //do nothing
            }
            show()
        }
    }

    private fun setOptionalViewsVisibility(visibility: Int) {
        binding.apply {
            tvStatus.visibility = visibility
            statusGroup.visibility = visibility
            tvAppliedVia.visibility = visibility
            appliedViaGroup.visibility = visibility
            tvSentWithCoverLetter.visibility = visibility
            coverLetterGroup.visibility = visibility
            separator.drawable.setImageDrawable(
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

    }

    private fun setChipLogic(chip: Chip, text: String) {
        chip.setOnCheckedChangeListener { _, isChecked ->
            val editText = binding.edNotes
            if (isChecked)
                editText.append("$text. ")
            else {
                editText.setText(editText.text.toString().replace("$text. ", ""))
            }
        }
        //light / night mode adjustments
        val resources = requireContext().resources
        val color = when (resources.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                ResourcesCompat.getColor(resources, R.color.teal_700, null)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                ResourcesCompat.getColor(resources, R.color.grayish_red_3, null)
            }
            else -> {
                ResourcesCompat.getColor(resources, R.color.grayish_red_3, null)
            }
        }

        chip.chipBackgroundColor = ColorStateList.valueOf(color)
    }

    private fun setFields() {
        val isInEditMode = viewModel.job != null
        val createdAt = "${viewModel.job?.createdAtDateFormat}"
        binding.apply {
            edCompanyName.setText(viewModel.jobCompany)
            edPositionTitle.setText(viewModel.jobTitle)
            edNotes.setText(viewModel.jobNote)
            dateCreated.visibility = if (isInEditMode) View.VISIBLE else View.INVISIBLE
            dateCreated.text = getString(R.string.created_at, createdAt)
            btnAddApplication.text = getString(R.string.btn_save)
        }
        setOptionalFields()
    }

    private fun setOptionalFields() {
        binding.apply {
            statusGroup.check(
                when (viewModel.jobStatus) {
                    JobStatus.PENDING -> R.id.btn_pending
                    JobStatus.IN_PROCESS -> R.id.btn_in_process
                    JobStatus.REJECTED -> R.id.btn_reference
                    JobStatus.ACCEPTED -> R.id.btn_accepted
                }
            )
            appliedViaGroup.check(
                when (viewModel.jobAppliedVia) {
                    AppliedVia.SITE -> R.id.btn_company_site
                    AppliedVia.EMAIL -> R.id.btn_email
                    AppliedVia.REFERENCE -> R.id.btn_reference
                    AppliedVia.LINKEDIN -> R.id.btn_linkedin
                    AppliedVia.OTHER -> R.id.btn_other
                }
            )
            coverLetterGroup.check(
                when (viewModel.jobIsCoverLetterSent) {
                    SentWithCoverLetter.NO -> R.id.btn_cover_letter_negative
                    SentWithCoverLetter.YES -> R.id.btn_cover_letter_positive
                }
            )
        }
    }

    private fun initEventChannel() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.addEditJobEvent.collect { event ->

                when (event) {
                    is AddEditViewModel.AddEditJobEvent.NavigateBackWithResult -> {
                        binding.edCompanyName.clearFocus()
                        setFragmentResult(
                            ADD_EDIT_REQUEST,
                            bundleOf(ADD_EDIT_RESULT to event.result)
                        )
                        findNavController().navigate(R.id.navigation_applications)
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
                        ).setAnchorView(buttons_layout).show()
                    }
                    else -> showAlertDialog()
                }
            }
        }
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
