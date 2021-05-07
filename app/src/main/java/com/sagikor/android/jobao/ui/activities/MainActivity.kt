package com.sagikor.android.jobao.ui.activities


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import com.sagikor.android.jobao.R
import com.sagikor.android.jobao.model.Job
import com.sagikor.android.jobao.ui.fragments.home.HomeFragment
import com.sagikor.android.jobao.ui.fragments.home.HomeFragmentDirections
import com.sagikor.android.jobao.ui.fragments.jobslist.JobsListFragment
import com.sagikor.android.jobao.ui.fragments.jobslist.JobsListFragmentDirections
import com.sagikor.android.jobao.util.appendAttribute
import com.sagikor.android.jobao.viewmodel.JobViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.flow.collect
import java.io.File
import java.io.IOException
import java.util.*


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), OnScrollListener {
    private val TAG = MainActivity::class.qualifiedName
    private val jobViewModel: JobViewModel by viewModels()
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initBottomAppBar()
        initEventChannel()

    }

    private fun initBottomAppBar() {
        bottomAppBar.replaceMenu(R.menu.bottom_app_bar_more)
        bottomAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.send_to_mail -> jobViewModel.onSendToMailClick()
                R.id.rate_us -> jobViewModel.onRateUsClick()

            }
            true
        }

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_applications
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        nav_view.setupWithNavController(navController)
        nav_view.background = null

        setFabListener()
    }

    private fun setFabListener() {
        fab.setOnClickListener {
            navController.navigate(
                when (supportFragmentManager.fragments.last().childFragmentManager.fragments.last()) {
                    is HomeFragment -> HomeFragmentDirections.actionNavigationHomeToAddEditFragment(
                        null,
                        getString(R.string.title_add_job)
                    )
                    is JobsListFragment -> JobsListFragmentDirections.actionNavigationApplicationsToAddEditFragment(
                        null,
                        getString(R.string.title_add_job)
                    )
                    else -> throw RuntimeException("unknown fragment")
                }
            )
        }
        navController.addOnDestinationChangedListener { _, destination, _ ->
            run {
                coordinator.isVisible = destination.id != R.id.navigation_add_edit
            }
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onScrollUp() {
        bottomAppBar.performShow()
    }

    override fun onScrollDown() {
        bottomAppBar.performHide()
    }

    private fun initEventChannel() {
        val appUri = "https://play.google.com/store/apps/details?id=$packageName"
        lifecycleScope.launchWhenStarted {
            jobViewModel.actionEvent.collect { action ->
                when (action) {
                    is JobViewModel.ActionEvent.NavigateToGooglePlayRate -> startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(appUri))
                    )
                    is JobViewModel.ActionEvent.SendToMailSuccess -> sendAttachedFileToMail()
                    is JobViewModel.ActionEvent.SendToMailFail -> popFailToSendEmail()
                }
            }
        }
    }

    private fun popFailToSendEmail() {
        Snackbar.make(
            supportFragmentManager.fragments.last().requireView(),
            getString(R.string.email_send_error),
            Snackbar.LENGTH_LONG
        ).setAnchorView(R.id.nav_view).show()
    }

    private fun sendAttachedFileToMail() {
        val current = supportFragmentManager.fragments.last()
        jobViewModel.allJobs.observe(current.viewLifecycleOwner) { jobsList ->
            val jobsSubmission = getString(R.string.job_submissions) + ".csv"
            val jobsSubmissionsData = getJobsSubmissionData(jobsList)

            try {
                openFileOutput(jobsSubmission, MODE_PRIVATE).apply {
                    write(jobsSubmissionsData.toByteArray())
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            val file = File(filesDir, jobsSubmission)
            val authority = applicationContext.packageName + ".provider"
            val path = FileProvider.getUriForFile(applicationContext, authority, file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.job_submissions))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putExtra(Intent.EXTRA_STREAM, path)
            }
            startActivity(Intent.createChooser(intent, getString(R.string.send_to_mail)))
        }
    }

    private fun getJobsSubmissionData(jobsList: List<Job>): String {
        val stringBuilder = StringBuilder().apply {
            append(getHeaders())
        }
        jobsList.iterator().forEach { job ->
            stringBuilder.apply {
                appendAttribute(job.companyName)
                appendAttribute(job.title)
                appendAttribute(job.wasReplied.toString().toLowerCase(Locale.ENGLISH))
                appendAttribute(job.declinedDate ?: "")
                appendAttribute(job.note ?: "")
                appendAttribute(job.createdAtDateFormat)
                append("\n")
            }
        }
        return stringBuilder.toString()
    }

    private fun getHeaders(): String {
        return StringBuilder().apply {
            appendAttribute(getString(R.string.company_name))
            appendAttribute(getString(R.string.position_title))
            appendAttribute(getString(R.string.had_process))
            appendAttribute(getString(R.string.declined_date))
            appendAttribute(getString(R.string.note))
            appendAttribute(getString(R.string.csv_created_at))
            append("\n")
        }.toString()
    }
}


const val ADD_JOB_RESULT_OK = Activity.RESULT_FIRST_USER
const val EDIT_JOB_RESULT_OK = Activity.RESULT_FIRST_USER + 1
const val GO_BACK_RESULT_OK = Activity.RESULT_FIRST_USER + 2