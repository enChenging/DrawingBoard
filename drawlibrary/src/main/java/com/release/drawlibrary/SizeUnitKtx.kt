package com.release.drawlibrary

import android.content.res.Resources

/**
 * 尺寸单位换算相关扩展属性
 *
 * @author yancheng
 * @since 2021/11/15
 */

private val displayMetrics = Resources.getSystem().displayMetrics

/**
 * dp 转 px
 */

val Float.dp2px: Int
    get() {
        return (this * (displayMetrics.density) + 0.5f).toInt()
    }

val Int.dp2px: Int
    get() {
        return (this * (displayMetrics.density) + 0.5f).toInt()
    }

val Float.dp2pxF: Float
    get() {
        return this * (displayMetrics.density)
    }

val Int.dp2pxF: Float
    get() {
        return this * (displayMetrics.density)
    }

/**
 * px 转 dp
 */
val Float.px2dp: Int
    get() {
        return (this / (displayMetrics.density) + 0.5f).toInt()
    }

val Int.px2dp: Int
    get() {
        return (this / (displayMetrics.density) + 0.5f).toInt()
    }

val Float.px2dpF: Float
    get() {
        return this / (displayMetrics.density)
    }

val Int.px2dpF: Float
    get() {
        return this / (displayMetrics.density)
    }

/**
 * sp 转 px
 */
val Float.sp2px: Int
    get() {
        return (this * (displayMetrics.scaledDensity) + 0.5f).toInt()
    }

val Int.sp2px: Int
    get() {
        return (this * (displayMetrics.scaledDensity) + 0.5f).toInt()
    }

val Float.sp2pxF: Float
    get() {
        return this * (displayMetrics.scaledDensity)
    }

val Int.sp2pxF: Float
    get() {
        return this * (displayMetrics.scaledDensity)
    }

/**
 * px 转 sp
 */
val Float.px2sp: Int
    get() {
        return (this / (displayMetrics.scaledDensity) + 0.5f).toInt()
    }

val Int.px2sp: Int
    get() {
        return (this / (displayMetrics.scaledDensity) + 0.5f).toInt()
    }

val Float.px2spF: Float
    get() {
        return this / (displayMetrics.scaledDensity)
    }

val Int.px2spF: Float
    get() {
        return this / (displayMetrics.scaledDensity)
    }