package com.release.drawingboard

import android.annotation.SuppressLint
import android.app.Activity
import com.tencent.smtt.sdk.WebChromeClient
import com.tencent.smtt.sdk.WebSettings
import com.tencent.smtt.sdk.WebView

/**
 * 自定义x5WebView
 * @author yancheng
 * @since 2021/7/29
 */
class X5WebView(val ct: Activity, heightBlock: (height: Int) -> Unit = {}) : WebView(ct) {

    private val mHeightBlock = heightBlock

    private var mHeight = 0

    /**
     * 进度条动态改变
     */
    private val chromeClient: WebChromeClient = object : WebChromeClient() {
        override fun onProgressChanged(webView: WebView, progress: Int) {
            super.onProgressChanged(webView, progress)
            if (progress == 100) {
                mHeightBlock(mHeight)
            }
        }
    }

    init {
        initWebViewSettings()
        this.webChromeClient = chromeClient
        this.view.isClickable = true
    }

    companion object {

        const val MIXED_CONTENT_NEVER_ALLOW = 0

        const val MIXED_CONTENT_ALWAYS_ALLOW = 1

        const val MIXED_CONTENT_COMPATIBILITY_MODE = 2
    }

    /**
     * 初始化setting配置
     */
    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebViewSettings() {
        val webSetting = this.settings
        webSetting.javaScriptEnabled = true //允许js调用
        webSetting.javaScriptCanOpenWindowsAutomatically = true //支持通过JS打开新窗口
        webSetting.allowFileAccess = true //在File域下，能够执行任意的JavaScript代码，同源策略跨域访问能够对私有目录文件进行访问等
        webSetting.layoutAlgorithm =
            WebSettings.LayoutAlgorithm.NARROW_COLUMNS //控制页面的布局(使所有列的宽度不超过屏幕宽度)
        webSetting.setSupportZoom(true) //支持页面缩放
        webSetting.builtInZoomControls = true //进行控制缩放
        webSetting.allowContentAccess = true //是否允许在WebView中访问内容URL（Content Url），默认允许
        webSetting.useWideViewPort = true //设置缩放密度
        webSetting.setSupportMultipleWindows(false) //设置WebView是否支持多窗口,如果为true需要实现onCreateWindow(WebView, boolean, boolean, Message)
        //两者都可以
        webSetting.mixedContentMode = MIXED_CONTENT_COMPATIBILITY_MODE //设置安全的来源
        webSetting.setAppCacheEnabled(true) //设置应用缓存
        webSetting.domStorageEnabled = true //DOM存储API是否可用
        webSetting.setGeolocationEnabled(true) //定位是否可用
        webSetting.loadWithOverviewMode = true //是否允许WebView度超出以概览的方式载入页面，
        webSetting.setAppCacheMaxSize(5 * 1024 * 1024) //设置应用缓存内容的最大值 5MB
        webSetting.pluginState = WebSettings.PluginState.ON_DEMAND //设置是否支持插件
        webSetting.cacheMode = WebSettings.LOAD_NO_CACHE //重写使用缓存的方式
        webSetting.setAllowUniversalAccessFromFileURLs(true) //是否允许运行在一个file schema URL环境下的JavaScript访问来自其他任何来源的内容
        webSetting.setAllowFileAccessFromFileURLs(true) //是否允许运行在一个URL环境
    }

    override fun onSizeChanged(p0: Int, p1: Int, p2: Int, p3: Int) {
        super.onSizeChanged(p0, p1, p2, p3)
        mHeight = p1
    }
}