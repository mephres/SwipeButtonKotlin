package me.kdv.swipebuttonadapter.adapter.swipe_helper

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.os.Build
import android.view.View

@SuppressLint("ViewConstructor")
class SwipeButton(
    context: Context,
    private val text: String,
    private val imageResId: Int,
    private val buttonBackgroundColor: Int,
    private val textColor: Int,
    private val imageColor: Int,
    private val buttonTextSize: Int,
    private val buttonCornerRadius: Int,
    private val buttonWidth: Int
) : View(context) {

    private constructor(builder: Builder) : this(
        builder.context,
        builder.text,
        builder.imageResId,
        builder.buttonBackgroundColor,
        builder.textColor,
        builder.imageColor,
        builder.buttonTextSize,
        builder.buttonCornerRadius,
        builder.buttonWidth
    )

    private var pos = 0
    private var clickRegion: RectF? = null

    var onSwipeButtonClickListener: ((Int) -> Unit)? = null

    companion object {
        inline fun build(context: Context, block: Builder.() -> Unit) = Builder(context).apply(block).build()
    }

    class Builder(val context: Context) {
        var text = ""
        var imageResId = 0
        var buttonBackgroundColor = Color.WHITE
        var textColor = Color.BLACK
        var imageColor = Color.BLACK
        var buttonTextSize = 14
        var buttonCornerRadius = 8
        var buttonWidth = 200

        fun build() = SwipeButton(this)
    }

    fun onClick(x: Float, y: Float): Boolean {
        clickRegion?.let {
            if (it.contains(x, y)) {
                onSwipeButtonClickListener?.invoke(pos)
                return true
            }
        }
        return false
    }

    fun draw(c: Canvas, rect: RectF, pos: Int) {
        val p = Paint()
        p.color = buttonBackgroundColor
        c.drawRoundRect(rect, buttonCornerRadius.toFloat(), buttonCornerRadius.toFloat(), p)
        drawImage(imageResId, c, rect, p)
        drawText(text, c, rect, p)
        clickRegion = rect
        this.pos = pos
    }

    private fun drawImage(imageResId: Int, c: Canvas, button: RectF, p: Paint) {
        if (imageResId == 0) {
            return
        }
        var bitmap = drawableToBitmap(imageResId)
        val top: Float
        if (text.isNotEmpty()) {
            bitmap = Bitmap.createScaledBitmap(bitmap, 50, 50, false)
            top = button.centerY() - bitmap.width / 2 - 25
        } else {
            bitmap = Bitmap.createScaledBitmap(bitmap, 96, 96, false)
            top = button.centerY() - bitmap.width / 2
        }
        c.drawBitmap(bitmap, button.centerX() - bitmap.width / 2, top, p)
    }

    private fun drawText(text: String, c: Canvas, button: RectF, p: Paint) {
        val textSize = Resources.getSystem().displayMetrics.density * buttonTextSize
        p.color = textColor
        p.isAntiAlias = true
        p.textSize = textSize
        p.isFakeBoldText = true
        p.textScaleX = Math.abs(button.left - button.right) / buttonWidth
        val textWidth = p.measureText(text)
        val topOffset = if (imageResId > 0) 30 else 0
        c.drawText(
            text,
            button.centerX() - textWidth / 2,
            button.centerY() + textSize / 2 + topOffset,
            p
        )
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun drawableToBitmap(vectorDrawableId: Int): Bitmap {
        val bitmap: Bitmap?
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            val vectorDrawable = context.applicationContext.getDrawable(vectorDrawableId)!!
            vectorDrawable.setTint(imageColor)
            bitmap = Bitmap.createBitmap(
                vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
            vectorDrawable.draw(canvas)
        } else {
            bitmap =
                BitmapFactory.decodeResource(context.applicationContext.resources, vectorDrawableId)
        }
        return bitmap
    }

}