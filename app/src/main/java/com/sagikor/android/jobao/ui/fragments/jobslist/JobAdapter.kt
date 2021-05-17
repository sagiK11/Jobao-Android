package com.sagikor.android.jobao.ui.fragments.jobslist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sagikor.android.jobao.R
import com.sagikor.android.jobao.databinding.JobListItemBinding
import com.sagikor.android.jobao.model.Job
import com.sagikor.android.jobao.model.JobStatus


class JobAdapter(private val listener: OnItemClickListener) :
    ListAdapter<Job, JobAdapter.JobViewHolder>(DiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding = JobListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JobViewHolder(binding)

    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    inner class JobViewHolder(private val binding: JobListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.apply {
                jobListItemRoot.apply {
                    setOnClickListener {
                        if (jobListItemRoot.currentState == jobListItemRoot.startState) {
                            jobListItemRoot.transitionToEnd()
                            cardMotionLayout.transitionToEnd()
                        } else {
                            jobListItemRoot.transitionToStart()
                            cardMotionLayout.transitionToStart()
                        }
                    }
                }
                editOption.setOnClickListener {
                    val pos = adapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        val job = getItem(pos)
                        listener.onItemClick(job)
                    }
                }
                deleteOption.setOnClickListener {
                    val pos = adapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        val job = getItem(pos)
                        listener.onItemDelete(job)
                        jobListItemRoot.transitionToStart()
                        cardMotionLayout.transitionToStart()
                    }
                }
            }
        }

        fun bind(job: Job) {
            val resource = binding.root.context.resources
            val pendingIcon =
                ResourcesCompat.getDrawable(resource, R.drawable.ic_waiting_for_reply, null)
            val rejectIcon =
                ResourcesCompat.getDrawable(resource, R.drawable.ic_baseline_close_24, null)
            val acceptedIcon =
                ResourcesCompat.getDrawable(resource, R.drawable.ic_baseline_done_24, null)
            val inProcessIcon =
                ResourcesCompat.getDrawable(resource, R.drawable.ic_in_process, null)
            binding.apply {
                listItemCompanyName.text = job.companyName
                listItemPositionTitle.text = job.title
                listItemStatus.setImageDrawable(
                    when (job.status) {
                        JobStatus.PENDING -> pendingIcon
                        JobStatus.REJECTED -> rejectIcon
                        JobStatus.IN_PROCESS -> inProcessIcon
                        else -> acceptedIcon
                    }
                )
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(job: Job)
        fun onItemDelete(job: Job)
    }


    class DiffCallback : DiffUtil.ItemCallback<Job>() {
        override fun areItemsTheSame(oldItem: Job, newItem: Job) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Job, newItem: Job) =
            oldItem == newItem
    }

}
