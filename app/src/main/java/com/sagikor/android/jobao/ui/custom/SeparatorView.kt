package com.sagikor.android.jobao.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.motion.widget.MotionLayout
import com.sagikor.android.jobao.R

class SeparatorView(context: Context, attrs: AttributeSet) : MotionLayout(context, attrs) {
    val text: TextView
    var drawable: ImageView

    init {
        inflate(context, R.layout.separator_view, this)

        text = findViewById(R.id.text_view)
        drawable = findViewById(R.id.iv_add_section)

        val attributes = context.obtainStyledAttributes(attrs, R.styleable.SeparatorView)
        text.text = attributes.getString(R.styleable.SeparatorView_text)
        drawable.setImageDrawable(attributes.getDrawable(R.styleable.SeparatorView_drawableSource))
        attributes.recycle()

    }


    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        val action = event?.action
        //if its not a press let super handle
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE || action == MotionEvent.ACTION_CANCEL) {
            return super.onInterceptTouchEvent(event)
        }
        performClick()
        return false
    }


}