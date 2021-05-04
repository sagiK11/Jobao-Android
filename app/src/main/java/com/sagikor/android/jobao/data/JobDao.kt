package com.sagikor.android.jobao.data

import androidx.room.*
import com.sagikor.android.jobao.model.Job
import com.sagikor.android.jobao.model.JobStatus
import com.sagikor.android.jobao.util.Converters
import kotlinx.coroutines.flow.Flow

@Dao
@TypeConverters(Converters::class)
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
                getJobsSortedByCompanyName(query, status)
            }
            SortOrder.BY_STATUS -> {
                getJobsSortedByJobStatus(query, status)
            }
            else -> {
                getJobsSortedByJobCreated(query, status)
            }
        }

    //                0           1         2         3
    //status can be PENDING, IN_PROCESS, REJECTED, ACCEPTED
    @Query("SELECT * FROM user_jobs WHERE ( status = :status OR status != 2) AND companyName LIKE '%' || :searchQuery || '%' ORDER BY companyName ASC, companyName")
    fun getJobsSortedByCompanyName(searchQuery: String, status: JobStatus): Flow<List<Job>>

    @Query("SELECT * FROM user_jobs WHERE (status = :status OR status != 2) AND companyName LIKE '%' || :searchQuery || '%' ORDER BY createdAt DESC, createdAt")
    fun getJobsSortedByJobCreated(searchQuery: String, status: JobStatus): Flow<List<Job>>

    @Query("SELECT * FROM user_jobs WHERE (status = :status OR status != 2) AND companyName LIKE '%' || :searchQuery || '%' ORDER BY status DESC")
    fun getJobsSortedByJobStatus(searchQuery: String, status: JobStatus): Flow<List<Job>>

    @Query("DELETE FROM user_jobs")
    suspend fun deleteAll()
}

