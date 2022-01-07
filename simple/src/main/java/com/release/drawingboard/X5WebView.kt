package com.release.drawingboard

import android.annotation.SuppressLint
import android.app.Activity
import com.tencent.smtt.sdk.WebChromeClient
import com.tencent.smtt.sdk.WebSettings
import com.tencent.smtt.sdk.WebView
import com.tencent.smtt.sdk.WebViewClient

/**
 * 自定义x5WebView
 * @author yancheng
 * @since 2021/7/29
 */
class X5WebView(ct: Activity, heightBlock: (height: Int) -> Unit = {}) : WebView(ct) {

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
        this.view.isClickable = true
    }

    /**
     * 初始化setting配置
     */
    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebViewSettings() {
        this.settings.apply {
            javaScriptEnabled = true //允许js调用
            javaScriptCanOpenWindowsAutomatically = true //支持通过JS打开新窗口
            databaseEnabled = true //设置可以调用数据库
            domStorageEnabled = true //设置可以dom存储
            loadsImagesAutomatically = true
            allowFileAccess = false //在File域下，能够执行任意的JavaScript代码，同源策略跨域访问能够对私有目录文件进行访问等
            setSupportZoom(false) //支持页面缩放
            builtInZoomControls = false //进行控制缩放
            allowContentAccess = true //是否允许在WebView中访问内容URL（Content Url），默认允许
            setGeolocationEnabled(false) //定位是否可用
            setSupportMultipleWindows(false) //设置WebView是否支持多窗口,如果为true需要实现onCreateWindow(WebView, boolean, boolean, Message)
            loadWithOverviewMode = true //是否允许WebView度超出以概览的方式载入页面，
            defaultTextEncodingName = "GBK"
        }

        this.webChromeClient = chromeClient
        this.webViewClient = WebViewClient()
    }

    override fun onSizeChanged(p0: Int, p1: Int, p2: Int, p3: Int) {
        super.onSizeChanged(p0, p1, p2, p3)
        mHeight = p1
    }
}