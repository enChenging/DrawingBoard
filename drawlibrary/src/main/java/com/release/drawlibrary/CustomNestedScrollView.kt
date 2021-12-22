package com.release.drawlibrary

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.core.widget.NestedScrollView

/**
 * 禁止拦截子view 的滑动事件
 * @author yancheng
 * @since 2021/8/12
 */
class CustomNestedScrollView : NestedScrollView {
    private var scrollable = true

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return scrollable && super.onTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return scrollable
    }

    fun setScrollingEnabled(enabled: Boolean) {
        scrollable = enabled
    }
}