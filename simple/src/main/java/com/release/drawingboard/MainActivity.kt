package com.release.drawingboard

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.release.drawlibrary.CustomNestedScrollView
import com.release.drawlibrary.DrawingBoardView

/**
 * 自定义画板 功能展示
 * @author yancheng
 * @since 2021/12/21
 */
open class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var mFirst = true
    private lateinit var mBottomLayout: CustomNestedScrollView
    private lateinit var mDrawBoardView: DrawingBoardView
    private lateinit var mContainerFl: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mContainerFl = findViewById(R.id.vContainerFl)
        mDrawBoardView = findViewById(R.id.vDrawBoardView)
        mBottomLayout = findViewById(R.id.vBottomLayout)
        initWebView()


        findViewById<TextView>(R.id.vDeleteTv).setOnClickListener(this)
        findViewById<TextView>(R.id.vPaintBrushTv).setOnClickListener(this)
        findViewById<TextView>(R.id.vEraserTv).setOnClickListener(this)
        findViewById<TextView>(R.id.vLineTv).setOnClickListener(this)
        findViewById<TextView>(R.id.vRectTv).setOnClickListener(this)
        findViewById<TextView>(R.id.vCircleTv).setOnClickListener(this)
        findViewById<TextView>(R.id.vArrowTv).setOnClickListener(this)
        findViewById<TextView>(R.id.vTextTv).setOnClickListener(this)
        findViewById<TextView>(R.id.vWebViewTv).setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.vDeleteTv ->
                mDrawBoardView.clear()
            R.id.vPaintBrushTv ->
                switchPaint(DrawingBoardView.PaintModeType.PEN)
            R.id.vEraserTv ->
                switchPaint(DrawingBoardView.PaintModeType.ERASER)
            R.id.vLineTv ->
                switchPaint(DrawingBoardView.PaintModeType.DRAW_LINE)
            R.id.vRectTv ->
                switchPaint(DrawingBoardView.PaintModeType.DRAW_RECT)
            R.id.vCircleTv ->
                switchPaint(DrawingBoardView.PaintModeType.DRAW_CIRCLE)
            R.id.vArrowTv ->
                switchPaint(DrawingBoardView.PaintModeType.DRAW_ARROW)
            R.id.vTextTv ->
                switchPaint(DrawingBoardView.PaintModeType.DRAW_TEXT)
            R.id.vWebViewTv ->
                mBottomLayout.setScrollingEnabled(true)
        }
    }

    private fun switchPaint(paintModeType: DrawingBoardView.PaintModeType) {
        mBottomLayout.setScrollingEnabled(false)
        mDrawBoardView.switchPaintModeType(paintModeType)
    }

    private fun initWebView() {
        X5WebView(this) {
            if (it > 0 && mFirst) {
                mDrawBoardView.layoutParams = ConstraintLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, it
                )
                mFirst = false
            }
        }.let {
            it.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            it.addJavascriptInterface(WebControl(this, it), "webControl")//与js进行交互
            mContainerFl.addView(it)
            it.loadUrl("https://baijiahao.baidu.com/s?id=1717482400814402987&wfr=spider&for=pc")
        }
    }
}