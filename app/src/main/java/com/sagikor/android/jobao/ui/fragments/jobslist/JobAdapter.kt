package com.sagikor.android.jobao.ui.fragments.jobslist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sagikor.android.jobao.R
import com.sagikor.android.jobao.databinding.JobListItemBinding
import com.sagikor.android.jobao.model.Job
import com.sagikor.android.jobao.model.JobStatus


class JobAdapter(private val listener: onItemClickListener) : ListAdapter<Job, JobAdapter.JobViewHolder>(DiffCallback()) {


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
                root.setOnClickListener{
                    val pos = adapterPosition
                    if(pos != RecyclerView.NO_POSITION){
                        val job = getItem(pos)
                        listener.onItemClick(job)
                    }
                }

            }
        }

        fun bind(job: Job) {
            val pendingIcon = binding.root.context.getDrawable(R.drawable.ic_round_hourglass_top_24)
            val rejectIcon = binding.root.context.getDrawable(R.drawable.ic_baseline_close_24)
            val acceptedIcon = binding.root.context.getDrawable(R.drawable.ic_baseline_done_24)
            val inProcessIcon = binding.root.context.getDrawable(R.drawable.ic_baseline_sync_24)
            binding.apply {
                listItemId.text = job.id.toString()
                listItemCompanyName.text = job.companyName
                listItemPositionTitle.text = job.title
                listItemStatus.setImageDrawable(when(job.status){
                    JobStatus.PENDING -> pendingIcon
                    JobStatus.REJECTED-> rejectIcon
                    JobStatus.IN_PROCESS -> inProcessIcon
                    else  -> acceptedIcon
                })
            }
        }
    }

    interface onItemClickListener{
        fun onItemClick(job : Job)
        //fun onCheckBoxSelect(job : Job, isChecked : Boolean)
    }


    class DiffCallback : DiffUtil.ItemCallback<Job>() {
        override fun areItemsTheSame(oldItem: Job, newItem: Job) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Job, newItem: Job) =
            oldItem == newItem
    }


}

//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
//        return JobViewHolder(LayoutInflater.from(parent.context).inflate(
//            R.layout.job_list_item,parent,false))
//    }
//
//    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
//        val currentItem = applicationsList[position]
//        val hourGlassIcon = holder.itemView.context.getDrawable(R.drawable.ic_round_hourglass_top_24)
//        holder.itemView.list_item_id.text = currentItem.id.toString()
//        holder.itemView.list_item_company_name.text = currentItem.companyName
//        holder.itemView.list_item_position_title.text =currentItem.title
//        holder.itemView.list_item_status.setImageDrawable(hourGlassIcon)
//    }
//
//    override fun getItemCount(): Int {
//        return this.applicationsList.size
//    }
//
//    fun submitList(jobList: List<Job>){
//        this.applicationsList = jobList
//        notifyDataSetChanged()
//    }


//original
//private var applicationsList  = emptyList<Job>()
//class MyViewHolder(itemView : View): RecyclerView.ViewHolder(itemView){
//
//}
//
//override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
//    return MyViewHolder(LayoutInflater.from(parent.context).inflate(
//        R.layout.application_list_item,parent,false))
//}
//
//override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
//    val currentItem = applicationsList[position]
//    val hourGlassIcon = holder.itemView.context.getDrawable(R.drawable.ic_round_hourglass_top_24)
//    holder.itemView.list_item_id.text = currentItem.id.toString()
//    holder.itemView.list_item_company_name.text = currentItem.companyName
//    holder.itemView.list_item_position_title.text =currentItem.title
//    holder.itemView.list_item_status.setImageDrawable(hourGlassIcon)
//}
//
//override fun getItemCount(): Int {
//    return this.applicationsList.size
//}
//
//fun submitList(jobList: List<Job>){
//    this.applicationsList = jobList
//    notifyDataSetChanged()
//}