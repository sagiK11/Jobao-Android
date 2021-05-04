package com.sagikor.android.jobao.util

import androidx.room.TypeConverter
import com.sagikor.android.jobao.model.AppliedVia
import com.sagikor.android.jobao.model.JobStatus
import com.sagikor.android.jobao.model.SentWithCoverLetter

class Converters {
    @TypeConverter
    fun toStatus(value: Int) = enumValues<JobStatus>()[value]

    @TypeConverter
    fun fromStatus(value: JobStatus) = value.ordinal

    @TypeConverter
    fun toAppliedVia(value: Int) = enumValues<AppliedVia>()[value]

    @TypeConverter
    fun fromAppliedVia(value: AppliedVia) = value.ordinal

    @TypeConverter
    fun toSentWithCoverLetter(value: Int) = enumValues<SentWithCoverLetter>()[value]

    @TypeConverter
    fun fromSentWithCoverLetter(value: SentWithCoverLetter) = value.ordinal
}
