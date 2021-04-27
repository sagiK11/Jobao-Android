package com.sagikor.android.jobao.util

import java.lang.Exception

class AppExceptions {
    enum class Location {
        COMPANY, TITLE, DATE_APPLIED
    }

    class Input(val location: Location) : Exception()
}

