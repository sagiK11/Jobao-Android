package com.sagikor.android.jobao.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.sagikor.android.jobao.R

class SeparatorView(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

    init {
        inflate(context, R.layout.separator_view, this)

        val textView: TextView = findViewById(R.id.text_view)

        val attributes = context.obtainStyledAttributes(attrs, R.styleable.SeparatorView)
        textView.text = attributes.getString(R.styleable.SeparatorView_text)
        attributes.recycle()

    }
}