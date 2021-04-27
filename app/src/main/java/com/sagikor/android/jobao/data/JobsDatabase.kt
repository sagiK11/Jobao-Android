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

@Database(entities = [Job::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
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
                        "1/1/1999",
                        JobStatus.ACCEPTED,
                        AppliedVia.REFERENCE,
                        SentWithCoverLetter.NO,
                        "1/1/2000",
                        System.currentTimeMillis()
                    )
                )
                dao.addJob(
                    Job(
                        "Microsoft2",
                        "Engineer2",
                        "2/1/1999",
                        JobStatus.IN_PROCESS,
                        AppliedVia.EMAIL,
                        SentWithCoverLetter.NO,
                        "1/1/2000",
                        System.currentTimeMillis()
                    )
                )
                dao.addJob(
                    Job(
                        "Microsoft3",
                        "Engineer3",
                        "3/1/1999",
                        JobStatus.REJECTED,
                        AppliedVia.SITE,
                        SentWithCoverLetter.NO,
                        "1/1/2000",
                        System.currentTimeMillis()
                    )
                )
                dao.addJob(
                    Job(
                        "Microsoft4",
                        "CTO",
                        "4/1/1999",
                        JobStatus.REJECTED,
                        AppliedVia.SITE,
                        SentWithCoverLetter.NO,
                        "1/1/2000",
                        System.currentTimeMillis()
                    )
                )
                dao.addJob(
                    Job(
                        "Microsoft5",
                        "Student",
                        "5/1/1999",
                        JobStatus.REJECTED,
                        AppliedVia.SITE,
                        SentWithCoverLetter.NO,
                        "1/1/2000",
                        System.currentTimeMillis()
                    )
                )
                dao.addJob(
                    Job(
                        "Microsoft6",
                        "Manager",
                        "6/1/1999",
                        JobStatus.PENDING,
                        AppliedVia.SITE,
                        SentWithCoverLetter.NO,
                        "1/1/2000",
                        System.currentTimeMillis()
                    )
                )
                dao.addJob(
                    Job(
                        "Facebook",
                        "Data Guy",
                        "1/1/1998",
                        JobStatus.PENDING,
                        AppliedVia.SITE,
                        SentWithCoverLetter.YES,
                        "1/1/2000",
                        System.currentTimeMillis()
                    )
                )
                dao.addJob(
                    Job(
                        "Facebook",
                        "Spy Guy",
                        "22/1/1999",
                        JobStatus.REJECTED,
                        AppliedVia.SITE,
                        SentWithCoverLetter.NO,
                        "1/1/2000",
                        System.currentTimeMillis()
                    )
                )

            }
        }
    }

}

//original
//@Database(entities = [Job::class], version = 1, exportSchema = false)
//abstract class UserDatabase : RoomDatabase() {
//
//    abstract fun jobDao(): JobDao
//
//    companion object {
//        @Volatile
//        private var INSTANCE: UserDatabase? = null
//
//        fun getDatabase(context: Context): UserDatabase {
//            val tempInstance = INSTANCE
//            if (tempInstance != null)
//                return tempInstance
//            synchronized(this) {
//                val instance = Room.databaseBuilder(
//                    context.applicationContext,
//                    UserDatabase::class.java,
//
//                    "user_database"
//                ).build()
//                INSTANCE = instance
//                return instance
//            }
//        }
//    }
//}