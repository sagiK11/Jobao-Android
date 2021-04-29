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
import com.sagikor.android.jobao.util.AppliedViaConverters
import com.sagikor.android.jobao.util.Converters
import com.sagikor.android.jobao.util.SentWithCoverLetterConverters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider
@TypeConverters(Converters::class, AppliedViaConverters::class, SentWithCoverLetterConverters::class)
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
                dao.addJob(
                    Job(
                        "Microsoft1",
                        "Engineer1",

                        JobStatus.ACCEPTED,
                        AppliedVia.REFERENCE,
                        SentWithCoverLetter.NO,
                        "",
                        System.currentTimeMillis()
                    )
                )
                dao.addJob(
                    Job(
                        "Microsoft2",
                        "Engineer2",

                        JobStatus.IN_PROCESS,
                        AppliedVia.EMAIL,
                        SentWithCoverLetter.NO,
                        "",
                        System.currentTimeMillis()
                    )
                )
                dao.addJob(
                    Job(
                        "Microsoft3",
                        "Engineer3",

                        JobStatus.REJECTED,
                        AppliedVia.SITE,
                        SentWithCoverLetter.NO,
                        "7/7/2018",
                        System.currentTimeMillis()
                    )
                )
                dao.addJob(
                    Job(
                        "Microsoft4",
                        "CTO",

                        JobStatus.REJECTED,
                        AppliedVia.SITE,
                        SentWithCoverLetter.NO,
                        "1/1/2018",
                        System.currentTimeMillis()
                    )
                )
                dao.addJob(
                    Job(
                        "Microsoft5",
                        "Student",
                        JobStatus.REJECTED,
                        AppliedVia.SITE,
                        SentWithCoverLetter.NO,
                        "1/1/2021",
                        System.currentTimeMillis()
                    )
                )
                dao.addJob(
                    Job(
                        "Microsoft6",
                        "Manager",

                        JobStatus.PENDING,
                        AppliedVia.SITE,
                        SentWithCoverLetter.NO,
                        "",
                        System.currentTimeMillis()
                    )
                )
                dao.addJob(
                    Job(
                        "Facebook1",
                        "Data Guy",

                        JobStatus.PENDING,
                        AppliedVia.SITE,
                        SentWithCoverLetter.YES,
                        "",
                        System.currentTimeMillis()
                    )
                )
                dao.addJob(
                    Job(
                        "Facebook2",
                        "Spy Guy",

                        JobStatus.REJECTED,
                        AppliedVia.SITE,
                        SentWithCoverLetter.NO,
                        "1/1/2007",
                        System.currentTimeMillis()
                    )
                )
                dao.addJob(
                    Job(
                        "Netflix1",
                        "Engineer1",

                        JobStatus.ACCEPTED,
                        AppliedVia.REFERENCE,
                        SentWithCoverLetter.NO,
                        "",
                        System.currentTimeMillis()
                    )
                )
                dao.addJob(
                    Job(
                        "Netflix2",
                        "Engineer2",

                        JobStatus.IN_PROCESS,
                        AppliedVia.EMAIL,
                        SentWithCoverLetter.NO,
                        "",
                        System.currentTimeMillis()
                    )
                )
                dao.addJob(
                    Job(
                        "Netflix3",
                        "Engineer3",

                        JobStatus.ACCEPTED,
                        AppliedVia.SITE,
                        SentWithCoverLetter.NO,
                        "",
                        System.currentTimeMillis()
                    )
                )
                dao.addJob(
                    Job(
                        "Apple1",
                        "CTO",

                        JobStatus.REJECTED,
                        AppliedVia.SITE,
                        SentWithCoverLetter.NO,
                        "1/5/2014",
                        System.currentTimeMillis()
                    )
                )
                dao.addJob(
                    Job(
                        "Apple2",
                        "Student",

                        JobStatus.REJECTED,
                        AppliedVia.SITE,
                        SentWithCoverLetter.NO,
                        "1/1/2010",
                        System.currentTimeMillis()
                    )
                )
                dao.addJob(
                    Job(
                        "Apple3",
                        "Manager",

                        JobStatus.PENDING,
                        AppliedVia.SITE,
                        SentWithCoverLetter.NO,
                        "",
                        System.currentTimeMillis()
                    )
                )
                dao.addJob(
                    Job(
                        "Apple4",
                        "Data Guy",

                        JobStatus.PENDING,
                        AppliedVia.SITE,
                        SentWithCoverLetter.YES,
                        "",
                        System.currentTimeMillis()
                    )
                )
                dao.addJob(
                    Job(
                        "Google1",
                        "Architect",

                        JobStatus.REJECTED,
                        AppliedVia.SITE,
                        SentWithCoverLetter.NO,
                        "1/2/2021",
                        System.currentTimeMillis()
                    )
                )
                dao.addJob(
                    Job(
                        "Google2",
                        "Manager",

                        JobStatus.PENDING,
                        AppliedVia.SITE,
                        SentWithCoverLetter.NO,
                        "",
                        System.currentTimeMillis()
                    )
                )
                dao.addJob(
                    Job(
                        "Google3",
                        "Data Guy",

                        JobStatus.PENDING,
                        AppliedVia.SITE,
                        SentWithCoverLetter.YES,
                        "",
                        System.currentTimeMillis()
                    )
                )
                dao.addJob(
                    Job(
                        "Dell1",
                        "Hardware Engineer",

                        JobStatus.ACCEPTED,
                        AppliedVia.SITE,
                        SentWithCoverLetter.NO,
                        "",
                        System.currentTimeMillis()
                    )
                )
                dao.addJob(
                    Job(
                        "Dell12",
                        "Software Engineer",

                        JobStatus.ACCEPTED,
                        AppliedVia.REFERENCE,
                        SentWithCoverLetter.NO,
                        "",
                        System.currentTimeMillis()
                    )
                )
                dao.addJob(
                    Job(
                        "Dell13",
                        "Engineer2",

                        JobStatus.REJECTED,
                        AppliedVia.EMAIL,
                        SentWithCoverLetter.NO,
                        "1/1/2020",
                        System.currentTimeMillis()
                    )
                )
                dao.addJob(
                    Job(
                        "Amazon1",
                        "Engineer3",

                        JobStatus.REJECTED,
                        AppliedVia.SITE,
                        SentWithCoverLetter.NO,
                        "5/1/2017",
                        System.currentTimeMillis()
                    )
                )
                dao.addJob(
                    Job(
                        "Amazon2",
                        "CTO",

                        JobStatus.REJECTED,
                        AppliedVia.SITE,
                        SentWithCoverLetter.NO,
                        "5/1/1999",
                        System.currentTimeMillis()
                    )
                )
                dao.addJob(
                    Job(
                        "Amazon3",
                        "Student",

                        JobStatus.REJECTED,
                        AppliedVia.SITE,
                        SentWithCoverLetter.NO,
                        "1/1/2012",
                        System.currentTimeMillis()
                    )
                )
                dao.addJob(
                    Job(
                        "Amazon4",
                        "Manager",

                        JobStatus.PENDING,
                        AppliedVia.SITE,
                        SentWithCoverLetter.NO,
                        "",
                        System.currentTimeMillis()
                    )
                )
                dao.addJob(
                    Job(
                        "Waze",
                        "Data Guy",

                        JobStatus.REJECTED,
                        AppliedVia.SITE,
                        SentWithCoverLetter.YES,
                        "3/4/2014",
                        System.currentTimeMillis()
                    )
                )
                dao.addJob(
                    Job(
                        "Waze2",
                        "Spy Guy",

                        JobStatus.REJECTED,
                        AppliedVia.SITE,
                        SentWithCoverLetter.NO,
                        "1/4/2021",
                        System.currentTimeMillis()
                    )
                )

            }
        }
    }
}
