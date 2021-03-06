package com.sagikor.android.jobao.ui.fragments.addedit

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
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
    private var isInDarkMode = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        binding = FragmentAddEditJobBinding.bind(view)
        initEventChannel()
        setFields()
        bindListeners()
        setOptionalViewsVisibility(View.GONE)
        setOnBackPressDispatcherCallBack()
        setInDarkModeField()

    }

    private fun bindListeners() {
        bindRequiredFieldsListener()
        bindSeparatorListener()
        bindOptionalFieldsListeners()
        bindNoteFieldListener()
        setChipLogic(binding.chipTrain, getString(R.string.next_to_train))
        setChipLogic(binding.chipRemote, getString(R.string.remote_job))
        setChipLogic(binding.chipStartup, getString(R.string.startup))
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
                    R.id.btn_accepted -> JobStatus.ACCEPTED
                    else -> JobStatus.UNPROVIDED
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
                    R.id.btn_other -> AppliedVia.OTHER
                    else -> AppliedVia.UNPROVIDED
                }

            }
            coverLetterGroup.addOnButtonCheckedListener { _, checkedId, _ ->
                viewModel.jobIsCoverLetterSent = when (checkedId) {
                    R.id.btn_cover_letter_positive -> SentWithCoverLetter.YES
                    R.id.btn_cover_letter_negative -> SentWithCoverLetter.NO
                    else -> SentWithCoverLetter.UNPROVIDED
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
            tilNotes.visibility = visibility
            chipGroup.visibility = visibility
            dateCreated.visibility = if (viewModel.job == null) View.INVISIBLE else visibility
            when (visibility) {
                View.GONE -> {
                    setFieldsLayoutToGone()
                }
                View.VISIBLE -> {
                    setFieldsLayoutToVisible()
                }
            }
        }
    }

    private fun setFieldsLayoutToVisible() {
        binding.apply {
            fieldsLayout.transitionToStart()
            requiredFieldsLayout.background =
                getDrawable(R.drawable.required_fields_layer_list)
            optionFieldsLayout.background =
                getDrawable(R.drawable.optional_fields_layer_list)
            separator.drawable.setImageDrawable(getDrawable(R.drawable.ic_baseline_remove_circle_24))
        }
    }

    private fun setFieldsLayoutToGone() {
        binding.apply {
            fieldsLayout.transitionToEnd()
            requiredFieldsLayout.background = null
            optionFieldsLayout.background = null
            separator.drawable.setImageDrawable(
                getDrawable(R.drawable.ic_baseline_add_circle_24)
            )
        }
    }

    private fun getDrawable(source: Int): Drawable? {
        return ContextCompat.getDrawable(requireContext(), source)
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
        val bgColor = when (isInDarkMode) {
            true -> ResourcesCompat.getColor(resources, R.color.teal_700, null)
            else -> ResourcesCompat.getColor(resources, R.color.complementary_1, null)
        }
        val textColor = ResourcesCompat.getColor(resources, R.color.white, null)

        chip.chipBackgroundColor = ColorStateList.valueOf(bgColor)
        chip.setTextColor(ColorStateList.valueOf(textColor))
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
        }
        setOptionalFields()
    }

    private fun setOptionalFields() {
        binding.apply {
            statusGroup.check(
                when (viewModel.jobStatus) {
                    JobStatus.PENDING -> R.id.btn_pending
                    JobStatus.IN_PROCESS -> R.id.btn_in_process
                    JobStatus.REJECTED -> R.id.btn_rejected
                    JobStatus.ACCEPTED -> R.id.btn_accepted
                    JobStatus.UNPROVIDED -> {
                        Log.i(TAG, "setOptionalFields: user didn't provide status")
                    }
                }
            )
            appliedViaGroup.check(
                when (viewModel.jobAppliedVia) {
                    AppliedVia.SITE -> R.id.btn_company_site
                    AppliedVia.EMAIL -> R.id.btn_email
                    AppliedVia.REFERENCE -> R.id.btn_reference
                    AppliedVia.LINKEDIN -> R.id.btn_linkedin
                    AppliedVia.OTHER -> R.id.btn_other
                    AppliedVia.UNPROVIDED -> {
                        Log.i(TAG, "setOptionalFields: user didn't provide how he applied")
                    }
                }
            )
            coverLetterGroup.check(
                when (viewModel.jobIsCoverLetterSent) {
                    SentWithCoverLetter.NO -> R.id.btn_cover_letter_negative
                    SentWithCoverLetter.YES -> R.id.btn_cover_letter_positive
                    SentWithCoverLetter.UNPROVIDED -> {
                        Log.i(TAG, "setOptionalFields: user didn't provide if he sent CL")
                    }
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
                    is AddEditViewModel.AddEditJobEvent.NavigateBack -> findNavController().popBackStack()
                    is AddEditViewModel.AddEditJobEvent.ShowGoBackConfirmationMessage -> showAlertDialog()
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


    private fun setInDarkModeField() {
        isInDarkMode = when (resources.configuration?.uiMode?.and(
            Configuration
                .UI_MODE_NIGHT_MASK
        )) {
            Configuration.UI_MODE_NIGHT_YES -> {
                true
            }
            else -> {
                false
            }
        }
    }
}
