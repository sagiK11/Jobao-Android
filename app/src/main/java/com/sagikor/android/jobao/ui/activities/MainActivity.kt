package com.sagikor.android.jobao.ui.activities

import android.app.ActionBar
import android.app.Activity
import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.inputmethod.InputMethodManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.sagikor.android.jobao.R
import com.sagikor.android.jobao.ui.fragments.addedit.AddEditFragment
import com.sagikor.android.jobao.ui.fragments.home.HomeFragment
import com.sagikor.android.jobao.ui.fragments.home.HomeFragmentDirections
import com.sagikor.android.jobao.ui.fragments.jobslist.JobsListFragment
import com.sagikor.android.jobao.ui.fragments.jobslist.JobsListFragmentDirections
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.RuntimeException

internal const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), OnScrollListener {

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        navView.background = null
        navView.menu.getItem(1).isEnabled = false

        navController = findNavController(R.id.nav_host_fragment)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_dashboard,
                R.id.navigation_applications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        setFabListener()

    }

    private fun setFabListener() {
        fab.setOnClickListener {
            val current =
                supportFragmentManager.fragments.last().childFragmentManager.fragments.last()
            val action = when (current) {
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
            navController.navigate(action)
        }
        navController.addOnDestinationChangedListener { _, destination, _ ->
            run {
                fab.isEnabled = destination.id != R.id.navigation_add_edit
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

}

const val ADD_JOB_RESULT_OK = Activity.RESULT_FIRST_USER
const val EDIT_JOB_RESULT_OK = Activity.RESULT_FIRST_USER + 1