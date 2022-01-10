package com.release.drawlibrary

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.graphics.*
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import java.lang.Math.pow
import kotlin.math.*


/**
 * 自定义画板
 * @author yancheng
 * @since 2021/8/12
 */
class DrawingBoardView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    constructor(context: Context) : this(context, null)

    enum class PaintModeType {
        PEN,              // 画笔
        ERASER,           // 橡皮擦
        DRAW_LINE,        // 画直线
        DRAW_CIRCLE,      // 画圆形
        DRAW_RECT,        // 画矩形
        DRAW_ARROW,       // 画箭头
        DRAW_TEXT         // 画文本
    }

    /**
     * view的宽度
     */
    var mViewWidth = -1

    /**
     * view的高度
     */
    var mViewHeight = -1

    /**
     * 屏幕宽高
     */
    private var mScreenWidth = -1
    private var mScreenHeight = -1

    /**
     * 初始化画笔
     */
    private lateinit var mPaint: Paint

    /**
     * 初始化文字画笔
     */
    private val mTextPaint by lazy {
        TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 14.dp2pxF
            color = Color.RED
        }
    }

    /**
     * 当前画笔模式类型
     */
    private var mCurPaintModeType = PaintModeType.PEN

    /**
     * 布局是否改变
     */
    private var isSizeChanged = false

    /**
     * 开始位置
     */
    private var startX = 0f
    private var startY = 0f

    /**
     * 原始开始位置
     */
    private var mRawX = 0.0F
    private var mRawY = 0.0F

    /**
     * 记录上次的位置
     */
    private var mLastX = 0.0F
    private var mLastY = 0.0F

    /**
     * 点的路径信息
     */
    private val mPath: Path = Path()

    /**
     * 背景图Bitmap
     */
    private lateinit var mBgBitmap: Bitmap

    /**
     * 背景图Bitmap
     */
    private var mBitmap: Bitmap? = null

    /**
     * 背景画布
     */
    private val mBgCanvas: Canvas = Canvas()

    /**
     * 画笔的颜色
     */
    private var mPaintColor = Color.RED

    /**
     * 画笔的粗细
     */
    private val paintStrokeWidth = 3F

    /**
     * 存储画笔及路径
     */
    private val mPaintList = mutableListOf<PaintDataBean>()

    /**
     * 是否撤销
     */
    private var mIsRevoke = false

    /**
     * 是否背景透明
     */
    private var mIsBgTransparent = true

    /**
     * 画板的背景色
     */
    private var mBgBitmapColor = 0xFFEFE1FB.toInt()

    /**
     * 初始化完成函数
     */
    private lateinit var mOnFinish: () -> Unit

    /**
     * 是否是第一次加载
     */
    private var isFirst = true

    /**
     * 初始化View
     */
    init {
        initPaint()
        initBgBitmap()
    }

    /**
     * 初始化画笔
     */
    private fun initPaint() {
        mPaint = Paint().apply {
            //防抖动
            isDither = true
            //抗锯齿
            isAntiAlias = true
            //设置线帽 圆形线帽
            strokeCap = Paint.Cap.ROUND
            //设置拐角类型
            strokeJoin = Paint.Join.ROUND
            //型拐角的延长线的最大值
            strokeMiter = 1.0f
            //设置样式（效果）
            pathEffect = CornerPathEffect(1f)
            //画笔颜色
            color = mPaintColor
            //设置宽度
            strokeWidth = paintStrokeWidth
            //设置样式
            style = Paint.Style.STROKE
        }

    }

    /**
     * 初始化背景Bitmap
     */
    private fun initBgBitmap() {
        val displayMetrics = resources.displayMetrics
        mScreenWidth = displayMetrics.widthPixels
        mScreenHeight = displayMetrics.heightPixels
        if (mIsBgTransparent) {
            //背景透明
            mBgBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            mBgCanvas.drawColor(Color.TRANSPARENT)
        } else {
            //不透明背景
            mBgBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565)
            // 设置画板的背景色
            mBgBitmap.eraseColor(mBgBitmapColor)
        }
    }

    /**
     * 初始化背景画布
     */
    private fun initCanvas(bgBitmap: Bitmap?) {
        if (bgBitmap != null) {
            mBgBitmap = Bitmap.createScaledBitmap(bgBitmap, mViewWidth, mViewHeight, true)
            //bitmap抗锯齿
            mBgCanvas.drawFilter =
                PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            mBgCanvas.setBitmap(mBgBitmap)
        }
    }

    /**
     * 对宽高进行处理  由于该view的宽高是成比例的，所以只需要确定下来宽度，高度按比例换算就ok
     * @param widthMeasureSpec Int
     * @param heightMeasureSpec Int
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        // 获取测量的宽度
        val width = MeasureSpec.getSize(widthMeasureSpec)
        // 根据测量的宽度换算出高度
        var height = mScreenHeight
        when (MeasureSpec.getMode(heightMeasureSpec)) {
            //match_parent  EXACTLY 精确的
            MeasureSpec.EXACTLY -> {
                height = MeasureSpec.getSize(heightMeasureSpec)
            }
            //wrap_content
            MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED -> {
            }
        }
        Log.d("cyc", "onMeasure: width:$width height:$height")
        setMeasuredDimension(width, height)
    }

    /**
     * 获取到view的实际宽高
     * @param width Int
     * @param height Int
     * @param oldW Int
     * @param oldH Int
     */
    override fun onSizeChanged(width: Int, height: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(width, height, oldW, oldH)
        isSizeChanged = true
        mViewWidth = width
        mViewHeight = height
        Log.d("cyc", "onSizeChanged: width-->$width   height-->$height")
        initCanvas(mBgBitmap)
    }

    /**
     * 绘制
     * @param canvas Canvas
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isFirst) {
            if (::mOnFinish.isInitialized)
                mOnFinish()
            isFirst = false
        }
        canvas.drawBitmap(mBgBitmap, 0F, 0F, null)
        if (mIsRevoke) {
            drawing()
            mIsRevoke = false
        } else {
            when (mCurPaintModeType) {
                PaintModeType.ERASER -> mBgCanvas.drawPath(mPath, mPaint)
                PaintModeType.DRAW_TEXT -> drawing()
                else -> canvas.drawPath(mPath, mPaint)
            }
        }
    }

    /**
     * 画起来
     */
    private fun drawing() {
        mPaintList.forEach {
            if (it.mText.isNotEmpty()) {
                mBgCanvas.translate(it.mOffX, it.mOffY)
                val staticLayout = StaticLayout(
                    it.mText, mTextPaint, it.mWidth,
                    Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false
                )
                staticLayout.draw(mBgCanvas)
                mBgCanvas.translate(-it.mOffX, -it.mOffY)
            } else {
                mBgCanvas.drawPath(it.mPath, it.mPaint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
//        Log.d("cyc","event.action === ${event.action}")
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                //路径移动到当前位置
                mPath.moveTo(event.x, event.y)
                mPaintList.add(PaintDataBean(Paint(mPaint), Path(mPath)))
                mRawX = event.rawX
                mRawY = event.rawY
                startX = event.x
                startY = event.y
                mLastX = event.x
                mLastY = event.y
                //画文本
                if (mCurPaintModeType == PaintModeType.DRAW_TEXT)
                    showTextPop()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                //设置贝塞尔曲线的操作点为起点和终点的一半
                val cX = (event.x + mLastX) / 2
                val cY = (event.y + mLastY) / 2
                val paintData = mPaintList[mPaintList.size - 1]
                when (mCurPaintModeType) {
                    PaintModeType.PEN, PaintModeType.ERASER -> {
                        //画笔/橡皮擦
                        //二次贝塞尔，实现平滑曲线
                        paintData.mPath.quadTo(mLastX, mLastY, cX, cY)
                        mPath.quadTo(mLastX, mLastY, cX, cY)
                    }
                    PaintModeType.DRAW_LINE -> {
                        //画直线
                        mPath.reset()
                        paintData.mPath.reset()
                        paintData.mPath.moveTo(startX, startY)
                        paintData.mPath.lineTo(event.x, event.y)
                        mPath.moveTo(startX, startY)
                        mPath.lineTo(event.x, event.y)
                    }
                    PaintModeType.DRAW_CIRCLE -> {
                        //画椭圆
                        mPath.reset()
                        paintData.mPath.reset()
                        val rectF = RectF(startX, startY, event.x, event.y)
                        paintData.mPath.addOval(rectF, Path.Direction.CCW)
                        mPath.addOval(rectF, Path.Direction.CCW)
                    }
                    PaintModeType.DRAW_RECT -> {
                        //画矩形
                        mPath.reset()
                        paintData.mPath.reset()
                        val rectF = RectF(startX, startY, event.x, event.y)
                        paintData.mPath.addRect(rectF, Path.Direction.CCW)
                        mPath.addRect(rectF, Path.Direction.CCW)
                    }
                    PaintModeType.DRAW_ARROW -> {
                        //画箭头
                        mPath.reset()
                        paintData.mPath.reset()
                        drawArrow(startX, startY, event.x, event.y, paintData.mPath)
                    }
                }
                invalidate()
                mLastX = event.x
                mLastY = event.y
            }
            MotionEvent.ACTION_UP -> {
                val paintData = mPaintList[mPaintList.size - 1]
                if ((mCurPaintModeType == PaintModeType.PEN)) {
                    val cX = (event.x + mLastX) / 2
                    val cY = (event.y + mLastY) / 2
                    //二次贝塞尔，实现平滑曲线
                    paintData.mPath.quadTo(mLastX, mLastY, cX, cY)
                }
                mBgCanvas.drawPath(mPath, paintData.mPaint)
                mPath.reset()
            }
        }
        return super.onTouchEvent(event)
    }

    /**
     * 画箭头
     * @param startX Float
     * @param startY Float
     * @param endX Float
     * @param endY Float
     * @param savePath Path
     */
    private fun drawArrow(startX: Float, startY: Float, endX: Float, endY: Float, savePath: Path) {
        //线当前长度
        val lineLength =
            sqrt(pow(abs(endX - startX).toDouble(), 2.0) + pow(abs(endY - startY).toDouble(), 2.0))
        // 箭头高度
        var H = 0.0
        // 箭头长度
        var L = 0.0
        //防止箭头开始时过大
        if (lineLength < 320) {
            H = lineLength / 4
            L = lineLength / 6
        } else {
            //超过一定线长箭头大小固定
            H = 80.0
            L = 50.0
        }
        // 箭头角度
        val arrawAngle = atan(L / H)

        // 箭头角度
        val arraowLen = sqrt(L * L + H * H)

        val pointXY1: DoubleArray =
            rotateAndGetPoint(endX - startX, endY - startY, arrawAngle, true, arraowLen)
        val pointXY2: DoubleArray =
            rotateAndGetPoint(endX - startX, endY - startY, -arrawAngle, true, arraowLen)
        // 画线
        savePath.moveTo(startX, startY)
        savePath.lineTo(endX, endY)
        savePath.moveTo((endX - pointXY1[0]).toFloat(), (endY - pointXY1[1]).toFloat())
        savePath.lineTo(endX, endY)
        savePath.lineTo((endX - pointXY2[0]).toFloat(), (endY - pointXY2[1]).toFloat())
        mPath.moveTo(startX, startY)
        mPath.lineTo(endX, endY)
        mPath.moveTo((endX - pointXY1[0]).toFloat(), (endY - pointXY1[1]).toFloat())
        mPath.lineTo(endX, endY)
        mPath.lineTo((endX - pointXY2[0]).toFloat(), (endY - pointXY2[1]).toFloat())
    }

    /**
     * 矢量旋转函数，计算末点的位置
     * @param x  x分量
     * @param y  y分量
     * @param ang  旋转角度
     * @param isChLen  是否改变长度
     * @param newLen   箭头长度长度
     * @return    返回末点坐标
     */
    private fun rotateAndGetPoint(x: Float, y: Float, ang: Double, isChLen: Boolean, newLen: Double)
            : DoubleArray {
        val pointXY = DoubleArray(2)
        val vx = x * cos(ang) - y * sin(ang)
        val vy = x * sin(ang) + y * cos(ang)
        if (isChLen) {
            val d = sqrt(vx * vx + vy * vy)
            pointXY[0] = vx / d * newLen
            pointXY[1] = vy / d * newLen
        }
        return pointXY
    }

    private var mTextPopup: PopupWindow? = null
    private var mTextView: EditText? = null

    /**
     * 弹出popupWidnwo输入text
     */
    @SuppressLint("PrivateApi")
    private fun showTextPop() {
        if (null == mTextPopup) {
            mTextView = EditText(context)
            mTextView?.apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    textCursorDrawable =
                        ContextCompat.getDrawable(context, R.drawable.common_color_cursor_red)
                }else{
                    try {
                        val f = TextView::class.java.getDeclaredField("mCursorDrawableRes")
                        f.isAccessible = true
                        f[mTextView] = R.drawable.common_color_cursor_red
                    } catch (ignored: Throwable) {
                    }
                }
                setBackgroundResource(R.drawable.common_edit_bg_line_red)
                textSize = 14f
                setTextColor(Color.RED)
                hint = "请输入文字"
            }
            mTextPopup = PopupWindow(
                mTextView,
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                true
            )
            mTextPopup?.setOnDismissListener {
                if (!TextUtils.isEmpty(mTextView?.text)) {
                    //添加到列表
                    mPaintList.add(
                        PaintDataBean(
                            mPaint,
                            mPath,
                            mTextView?.text.toString(),
                            (width - startX).toInt(),
                            startX,
                            startY
                        )
                    )
                    invalidate()
                }
            }
            //可以让popup显示在软键盘上面
//            mTextPopup?.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        }
        mTextView?.apply {
            setText("")
            requestFocus()
        }
        val imm = context.getSystemService(Service.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS)

        mTextPopup?.showAtLocation(
            this, Gravity.TOP and Gravity.START,
            mRawX.toInt(), mRawY.toInt()
        )
    }

    /*********************************对外提供的方法**************************************/
    /**
     * 设置画板的背景色
     * @param bgBitmapColor Int
     */
    fun setBgColor(@ColorInt bgBitmapColor: Int) {
        mBgBitmapColor = bgBitmapColor
        mBgBitmap.eraseColor(bgBitmapColor)
    }

    /**
     * 设置画板的背景图片
     * @param resId Int
     */
    fun setBgImage(resId: Int) {
        mBgBitmap = BitmapFactory.decodeResource(resources, resId).copy(Bitmap.Config.RGB_565, true)
        Log.i("cyc", "setBgImage背景图片width=$mBgBitmap.width  height=$mBgBitmap.height")
        requestLayout()
        initCanvas(mBgBitmap)
    }

    /**
     * 设置回显的背景
     * @param bitmap Bitmap
     */
    fun setEchoBgBitmap(bitmap: Bitmap) {
        mBgBitmap = bitmap
        mBitmap = bitmap
        requestLayout()
        initCanvas(bitmap)
    }


    /**
     * 清空
     */
    fun clear() {
        mPaintList.clear()
        mBitmap = null
        //清空缓存画板
        mBgCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        if (!mIsBgTransparent)
            mBgCanvas.drawColor(mBgBitmapColor)
        invalidate()
    }

    /**
     * 切换画笔模式类型
     * 画线，圆，矩形，以及箭头
     * @param paintModeType PaintModeType
     *  PEN,              // 画笔
     *  ERASER,           // 橡皮擦
     *  DRAW_LINE,        // 画直线
     *  DRAW_CIRCLE,      // 画圆形
     *  DRAW_RECT,        // 画矩形
     *  DRAW_ARROW,       // 画箭头
     *  DRAW_TEXT         // 画文本
     */
    fun switchPaintModeType(paintModeType: PaintModeType) {
        mCurPaintModeType = paintModeType
        when (mCurPaintModeType) {
            PaintModeType.PEN, PaintModeType.DRAW_ARROW, PaintModeType.DRAW_CIRCLE, PaintModeType.DRAW_LINE, PaintModeType.DRAW_RECT -> {
                mPaint.apply {
                    //设置宽度
                    strokeWidth = paintStrokeWidth
                    //设置样式
                    style = Paint.Style.STROKE
                    //混合的模式
                    xfermode = null
                }
            }
            PaintModeType.ERASER -> {
                mPaint.apply {
                    //设置宽度
                    strokeWidth = 50f
                    //设置样式
                    style = Paint.Style.STROKE
                    if (mIsBgTransparent) {
                        //混合的模式(如果背景是透明的可以用)
                        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
                    } else {
                        //画笔颜色(如果背景不是透明的可以用)
                        color = mBgBitmapColor
                    }
                }
            }
            PaintModeType.DRAW_TEXT -> {
                mPaint.apply {
                    //设置宽度
                    strokeWidth = paintStrokeWidth
                    //设置样式
                    style = Paint.Style.FILL
                    //混合的模式
                    xfermode = null
                }
            }
        }
    }


    /**
     * 撤销数据
     * @param onRevokeFinishListener 无可撤销数据
     */
    fun revoke(onRevokeFinishListener: () -> Unit) {
        if (mPaintList.size > 0) {
            mIsRevoke = true
            val paintData = mPaintList[mPaintList.size - 1]
            mPaintList.remove(paintData)
            //清空缓存画板
            mBgCanvas.drawColor(0, PorterDuff.Mode.CLEAR)
            initCanvas(mBitmap)
            if (!mIsBgTransparent)
                mBgCanvas.drawColor(mBgBitmapColor)
            invalidate()
        } else {
            onRevokeFinishListener()
        }
    }

    /**
     * 获取背景Bitmap
     * @return Bitmap
     */
    fun getBitmap(): Bitmap {
        return mBgBitmap
    }

    /**
     * 获取View初始化完成的标志
     * @param viewLoadFinishListener View初始化完成监听
     */
    fun getViewLoadFinish(viewLoadFinishListener: () -> Unit) {
        mOnFinish = viewLoadFinishListener
    }
}

data class PaintDataBean(
    var mPaint: Paint,          //保存画笔
    var mPath: Path,            //保存路径
    var mText: String = "",     //文本
    var mWidth: Int = 0,        //文本宽度
    var mOffX: Float = 0f,      //偏移量x
    var mOffY: Float = 0f      //偏移量y
)

