package com.example.artur

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import kotlin.properties.Delegates


class DrawingView(context: Context, attributes: AttributeSet) : View(context, attributes) {

    // An inner class for custom path with two params as color and stroke size.

    private var drawPath: Path? = null
    private var canvasBitmap: Bitmap? = null
    private var drawPaint: Paint? = null
    private var canvasPaint: Paint? = null
    private var brushSize by Delegates.notNull<Float>()     //lateinit Float variable
    private var brushColor by Delegates.notNull<Int>()    //lateinit Int variable
    private var canvas: Canvas? = null    //Canvas class holds the draw calls to write the bitmap

    private val actionList = ArrayList<Bitmap>()

    init {
        setUpDrawingView()
    }

    private fun setUpDrawingView() {
        drawPaint = Paint()

        setupBasicBrush()

        //Set up paths storage
        drawPath = Path()

        //Set up everything needed for Paint class
        setUpPaintClassVariables()
    }

    private fun setUpPaintClassVariables() {
        drawPaint!!.color = brushColor
        drawPaint!!.style = Paint.Style.STROKE
        drawPaint!!.strokeJoin = Paint.Join.MITER
        drawPaint!!.strokeCap = Paint.Cap.ROUND
        canvasPaint = Paint(Paint.DITHER_FLAG)   //Paint flag that enables smoothing when dithering.
    }

    ///Function is initialized once the view is shown on the screen and
    //Initializes the bitmap and canvas
    override fun onSizeChanged(
        currentWidth: Int,
        currentHeight: Int,
        oldWidth: Int,
        oldHeight: Int
    ) {
        super.onSizeChanged(currentWidth, currentHeight, oldWidth, oldHeight)
        canvasBitmap = createNewBitmap()
        canvas = Canvas(canvasBitmap!!)
    }

    //Function is being called, when the stroke is drawn
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawBitmap(canvasBitmap!!, 0f, 0f, null)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX = event.x
        val touchY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                beginTheLine(touchX, touchY)
            }
            MotionEvent.ACTION_MOVE -> {
                addLine(touchX, touchY)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                drawPath = Path()
                addLastAction(getBitmap())
            }
            else -> return false
        }

        invalidate()    //Used to notify the ViewModel, that the data (paths) has changed
        return true
    }

    private fun addLastAction(bitmap: Bitmap) {
        actionList.add(bitmap)
    }

    fun returnCurrentBitmap(): Bitmap {
        if (actionList.size > 0) {
            return actionList[actionList.size - 1]
        } else {
            return createNewBitmap()
        }
    }

    private fun getBitmap(): Bitmap {
        val returnedBitmap = createNewBitmap()
        val canvas = Canvas(returnedBitmap)
        val bgDrawable: Drawable = background
        bgDrawable.draw(canvas)
        draw(canvas)

        return returnedBitmap
    }

    fun undoOneAction() {
        if (actionList.size > 0) {
            actionList.removeAt(actionList.size - 1)

            canvasBitmap = if (actionList.size > 0) {
                actionList[actionList.size - 1]
            } else {
                createNewBitmap()
            }

            canvas = Canvas(canvasBitmap!!)

            invalidate()
        }
    }

    fun setSizeForBrush(newSize: Float) {
        brushSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            newSize, resources.displayMetrics
        )    //Converts an unpacked complex data value holding a dimension to its final floating point value.
        drawPaint!!.strokeWidth = brushSize
    }

    fun activateEraser() {
        drawPaint!!.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    fun disableEraser() {
        drawPaint!!.xfermode = null
    }

    fun setBrushColor(newColor: String) {
        brushColor = Color.parseColor(newColor)
        drawPaint!!.color = brushColor
    }

    private fun beginTheLine(cordX: Float, cordY: Float) {
        drawPath!!.moveTo(cordX, cordY)    //Set the beginning of the upcoming line
    }

    //Adds a line to the specified point
    private fun addLine(cordX: Float, cordY: Float) {
        drawPath!!.lineTo(cordX, cordY)
        canvas!!.drawPath(drawPath!!, drawPaint!!)
    }

    private fun setupBasicBrush() {
        brushColor = Color.BLACK
        setSizeForBrush(20.toFloat())
    }

    private fun createNewBitmap(): Bitmap {
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    }
}