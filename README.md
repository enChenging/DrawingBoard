DrawingBoard
-

[![](https://jitpack.io/v/enChenging/DrawingBoard.svg)](https://jitpack.io/#enChenging/DrawingBoard)

	
## 用法

>Android Studio

将其添加到存储库build.gradle中
```xml
allprojects {
    repositories {
      	...
        maven{url 'https://jitpack.io'}
    }
}
```
 在build.gradle文件中的dependencies下添加引用：
	
```kotlin
implementation 'com.github.enChenging:DrawingBoard:1.0.1'
```
详细使用见工程里的[simple](https://github.com/enChenging/DrawingBoard/tree/master/simple)

代码使用：
```kotlin
    /**
     * 设置回显的背景
     * @param bitmap Bitmap
     */
    fun setEchoBgBitmap(bitmap: Bitmap)
    
    /**
     * 清空
     */
    fun clear()
    
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
    fun switchPaintModeType(paintModeType: PaintModeType)
    
    /**
     * 撤销数据
     * @param onRevokeFinishListener 无可撤销数据
     */
    fun revoke(onRevokeFinishListener: () -> Unit)
    
    /**
     * 获取背景Bitmap
     * @return Bitmap
     */
    fun getBitmap(): Bitmap
    
    /**
     * 获取View初始化完成的标志
     * @param viewLoadFinishListener View初始化完成监听
     */
    fun getViewLoadFinish(viewLoadFinishListener: () -> Unit)
    
    /**
     * 设置画板的背景色
     * @param bgBitmapColor Int
     */
    fun setBgColor(@ColorInt bgBitmapColor: Int)
    
    /**
     * 设置画板的背景图片
     * @param resId Int
     */
    fun setBgImage(resId: Int)

```


## 混淆

```java
#drawlibrary
-dontwarn com.release.drawlibrary.**
-keep class com.release.drawlibrary.**{*;}

```

声明
-
本控件用作分享与学习。





