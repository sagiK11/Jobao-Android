package com.sagikor.android.jobao.data

import android.util.Log
import androidx.room.*
import com.sagikor.android.jobao.model.Job
import com.sagikor.android.jobao.model.JobStatus
import com.sagikor.android.jobao.util.Converters
import kotlinx.coroutines.flow.Flow

@TypeConverters(Converters::class)
@Dao
interface JobDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addJob(job: Job)

    @Update
    suspend fun updateJob(job: Job)

    @Delete
    suspend fun deleteJob(job: Job)


    fun getJobs(query: String, sortOrder: SortOrder, status: JobStatus): Flow<List<Job>> =
        when (sortOrder) {
            SortOrder.BY_COMPANY -> {
                Log.d("SAGI", "BY_COMPANY")
                getJobsSortedByCompanyName(query, status)
            }
            else ->{
                Log.d("SAGI", "BY_DATE")
                getJobsSortedByJobCreated(query, status)}
        }

        //status can be PENDING
    @Query("SELECT * FROM user_jobs WHERE (status = :status OR status != 0) AND companyName LIKE '%' || :searchQuery || '%' ORDER BY companyName ASC, companyName")
    fun getJobsSortedByCompanyName(searchQuery: String,  status: JobStatus): Flow<List<Job>>

    @Query("SELECT * FROM user_jobs WHERE (status = :status OR status != 0) AND companyName LIKE '%' || :searchQuery || '%' ORDER BY dateApplied DESC, dateApplied")
    fun getJobsSortedByJobCreated(searchQuery: String, status: JobStatus): Flow<List<Job>>

    @Query("DELETE FROM user_jobs")
    suspend fun deleteAll()
}

//original

//@Query("SELECT * FROM user_jobs WHERE (isRejected != :isRejected OR isRejected = 0) AND companyName LIKE '%' || :searchQuery || '%' ORDER BY companyName ASC, companyName")
//fun getJobsSortedByCompanyName(searchQuery: String,  isRejected: Boolean): Flow<List<Job>>
//
//@Query("SELECT * FROM user_jobs WHERE (isRejected != :isRejected OR isRejected = 0) AND companyName LIKE '%' || :searchQuery || '%' ORDER BY dateApplied ASC, dateApplied")
//fun getJobsSortedByJobCreated(searchQuery: String,  isRejected: Boolean): Flow<List<Job>>
