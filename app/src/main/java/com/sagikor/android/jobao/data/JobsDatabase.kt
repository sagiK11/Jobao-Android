package com.sagikor.android.jobao.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sagikor.android.jobao.di.ApplicationScope
import com.sagikor.android.jobao.model.AppliedVia
import com.sagikor.android.jobao.model.Job
import com.sagikor.android.jobao.model.JobStatus
import com.sagikor.android.jobao.model.SentWithCoverLetter
import com.sagikor.android.jobao.util.Converters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@TypeConverters(
    Converters::class
)
@Database(entities = [Job::class], version = 1, exportSchema = false)
abstract class JobsDatabase : RoomDatabase() {

    abstract fun jobDao(): JobDao

    class Callback @Inject constructor(
        private val database: Provider<JobsDatabase>,
        @ApplicationScope private val applicationScope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            val dao = database.get().jobDao()
            //mock data
            applicationScope.launch {
                //addMockData(dao)
            }
        }

        private suspend fun addMockData(dao: JobDao) {
            dao.addJob(
                Job( 
                    "Microsoft",
                    "Software Engineer",

                    JobStatus.ACCEPTED,
                    AppliedVia.REFERENCE,
                    SentWithCoverLetter.NO,
                    "",
                    true,
                    "",
                    System.currentTimeMillis()
                )
            )
            dao.addJob(
                Job(
                    "Apple",
                    "Designer",

                    JobStatus.IN_PROCESS,
                    AppliedVia.EMAIL,
                    SentWithCoverLetter.NO,
                    "",
                    true,
                    "",
                    System.currentTimeMillis()
                )
            )
            dao.addJob(
                Job(
                    "Netflix",
                    "Backend developer",

                    JobStatus.REJECTED,
                    AppliedVia.SITE,
                    SentWithCoverLetter.NO,
                    "7/7/2018",
                    false,
                    "",
                    System.currentTimeMillis()
                )
            )
            dao.addJob(
                Job(
                    "Netflix",
                    "Actor",
                    JobStatus.REJECTED,
                    AppliedVia.SITE,
                    SentWithCoverLetter.NO,
                    "1/1/2018",
                    false,
                    "",
                    System.currentTimeMillis()
                )
            )
            dao.addJob(
                Job(
                    "Google",
                    "Mobile developer",
                    JobStatus.REJECTED,
                    AppliedVia.SITE,
                    SentWithCoverLetter.NO,
                    "1/1/2021",
                    true,
                    "",
                    System.currentTimeMillis()
                )
            )
            dao.addJob(
                Job(
                    "Linkedin",
                    "Frontend developer",
                    JobStatus.PENDING,
                    AppliedVia.SITE,
                    SentWithCoverLetter.NO,
                    "",
                    false,
                    "",
                    System.currentTimeMillis()
                )
            )
            dao.addJob(
                Job(
                    "Yahoo",
                    "Database architect",
                    JobStatus.PENDING,
                    AppliedVia.SITE,
                    SentWithCoverLetter.YES,
                    "",
                    false,
                    "",
                    System.currentTimeMillis()
                )
            )
            dao.addJob(
                Job(
                    "Facebook",
                    "UX designer",
                    JobStatus.REJECTED,
                    AppliedVia.SITE,
                    SentWithCoverLetter.NO,
                    "",
                    false,
                    "",
                    System.currentTimeMillis()
                )
            )
            dao.addJob(
                Job(
                    "Amazon",
                    "Warehouse manager",
                    JobStatus.ACCEPTED,
                    AppliedVia.REFERENCE,
                    SentWithCoverLetter.NO,
                    "",
                    true,
                    "",
                    System.currentTimeMillis()
                )
            )
            dao.addJob(
                Job(
                    "Deloitte",
                    "Accountant",
                    JobStatus.IN_PROCESS,
                    AppliedVia.EMAIL,
                    SentWithCoverLetter.NO,
                    "",
                    true,
                    "",
                    System.currentTimeMillis()
                )
            )
            dao.addJob(
                Job(
                    "EY",
                    "Accountant intern",
                    JobStatus.ACCEPTED,
                    AppliedVia.SITE,
                    SentWithCoverLetter.NO,
                    "",
                    true,
                    "",
                    System.currentTimeMillis()
                )
            )
            dao.addJob(
                Job(
                    "Apple",
                    "OS architect",
                    JobStatus.REJECTED,
                    AppliedVia.SITE,
                    SentWithCoverLetter.NO,
                    "",
                    false,
                    "",
                    System.currentTimeMillis()
                )
            )
            //from here its a duplicate
            dao.addJob(
                Job(
                    "Microsoft",
                    "Software Engineer",

                    JobStatus.ACCEPTED,
                    AppliedVia.REFERENCE,
                    SentWithCoverLetter.NO,
                    "",
                    true,
                    "",
                    System.currentTimeMillis()
                )
            )
            dao.addJob(
                Job(
                    "Apple",
                    "Designer",

                    JobStatus.IN_PROCESS,
                    AppliedVia.EMAIL,
                    SentWithCoverLetter.NO,
                    "",
                    true,
                    "",
                    System.currentTimeMillis()
                )
            )
            dao.addJob(
                Job(
                    "Netflix",
                    "Backend developer",

                    JobStatus.REJECTED,
                    AppliedVia.SITE,
                    SentWithCoverLetter.NO,
                    "7/7/2018",
                    false,
                    "",
                    System.currentTimeMillis()
                )
            )
            dao.addJob(
                Job(
                    "Netflix",
                    "Actor",
                    JobStatus.REJECTED,
                    AppliedVia.SITE,
                    SentWithCoverLetter.NO,
                    "1/1/2018",
                    false,
                    "",
                    System.currentTimeMillis()
                )
            )
            dao.addJob(
                Job(
                    "Google",
                    "Mobile developer",
                    JobStatus.REJECTED,
                    AppliedVia.SITE,
                    SentWithCoverLetter.NO,
                    "1/1/2021",
                    true,
                    "",
                    System.currentTimeMillis()
                )
            )
            dao.addJob(
                Job(
                    "Linkedin",
                    "Frontend developer",
                    JobStatus.PENDING,
                    AppliedVia.SITE,
                    SentWithCoverLetter.NO,
                    "",
                    false,
                    "",
                    System.currentTimeMillis()
                )
            )
            dao.addJob(
                Job(
                    "Yahoo",
                    "Database architect",
                    JobStatus.PENDING,
                    AppliedVia.SITE,
                    SentWithCoverLetter.YES,
                    "",
                    false,
                    "",
                    System.currentTimeMillis()
                )
            )
            dao.addJob(
                Job(
                    "Facebook",
                    "UX designer",
                    JobStatus.REJECTED,
                    AppliedVia.SITE,
                    SentWithCoverLetter.NO,
                    "",
                    false,
                    "",
                    System.currentTimeMillis()
                )
            )
            dao.addJob(
                Job(
                    "Amazon",
                    "Warehouse manager",
                    JobStatus.ACCEPTED,
                    AppliedVia.REFERENCE,
                    SentWithCoverLetter.NO,
                    "",
                    true,
                    "",
                    System.currentTimeMillis()
                )
            )
            dao.addJob(
                Job(
                    "Deloitte",
                    "Accountant",
                    JobStatus.IN_PROCESS,
                    AppliedVia.EMAIL,
                    SentWithCoverLetter.NO,
                    "",
                    true,
                    "",
                    System.currentTimeMillis()
                )
            )
            dao.addJob(
                Job(
                    "EY",
                    "Accountant intern",
                    JobStatus.ACCEPTED,
                    AppliedVia.SITE,
                    SentWithCoverLetter.NO,
                    "",
                    true,
                    "",
                    System.currentTimeMillis()
                )
            )
            dao.addJob(
                Job(
                    "Apple",
                    "OS architect",
                    JobStatus.REJECTED,
                    AppliedVia.SITE,
                    SentWithCoverLetter.NO,
                    "",
                    false,
                    "",
                    System.currentTimeMillis()
                )
            )
        }
    }
}
