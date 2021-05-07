package com.sagikor.android.jobao.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.sagikor.android.jobao.util.Converters
import kotlinx.android.parcel.Parcelize
import java.text.DateFormat

@TypeConverters(Converters::class)
@Parcelize
@Entity(tableName = "user_jobs")
data class Job(
    val companyName: String,
    val title: String,
    @TypeConverters(Converters::class) var status: JobStatus,
    @TypeConverters(Converters::class) var appliedVia: AppliedVia,
    @TypeConverters(Converters::class) var isCoverLetterSent: SentWithCoverLetter,
    var declinedDate: String? = null,
    var wasReplied: Boolean = false,
    var note: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    @PrimaryKey(autoGenerate = true) val id: Int = 0

) : Parcelable {
    val createdAtDateFormat: String
        get() = DateFormat.getDateTimeInstance().format(createdAt)
}

enum class JobStatus { PENDING, IN_PROCESS, REJECTED, ACCEPTED }
enum class AppliedVia { SITE, EMAIL, REFERENCE, LINKEDIN, OTHER }
enum class SentWithCoverLetter { NO, YES, }
