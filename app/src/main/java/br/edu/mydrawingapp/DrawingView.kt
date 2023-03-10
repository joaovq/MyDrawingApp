package br.edu.mydrawingapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View


class DrawingView(
    context:Context,
    attrs:AttributeSet
) :View(context, attrs) {


    private var mDrawPath: CustomPath? =
        null // An variable of CustomPath inner class to use it further.
    private var mCanvasBitmap: Bitmap? = null // An instance of the Bitmap.

    private var mDrawPaint: Paint? =
        null // The Paint class holds the style and color information about how to draw geometries, text and bitmaps.
    private var mCanvasPaint: Paint? = null // Instance of canvas paint view.

    private var mBrushSize: Float = 0.toFloat() // A variable for stroke/brush size to draw on the canvas.

    // A variable to hold a color of the stroke.
    private var color = Color.BLACK

    /**
     * A variable for canvas which will be initialized later and used.
     *
     *The Canvas class holds the "draw" calls. To draw something, you need 4 basic components: A Bitmap to hold the pixels, a Canvas to host
     * the draw calls (writing into the bitmap), a drawing primitive (e.g. Rect,
     * Path, text, Bitmap), and a paint (to describe the colors and styles for the
     * drawing)
     */

    private var mCanvarBitmap: Bitmap? = null
    private var canvas: Canvas? = null
    private val mPaths = ArrayList<CustomPath>()
    private val mUndoPaths = ArrayList<CustomPath>()

    init {
        setUpDrawing()
    }

//    Click no botão undo
    fun onClickUndo(){
//        Verificando se os caminho for maior que 0
        if (mPaths.size>0){
//            Adiciona ao Array de mUndoPaths o elemento removido do mPaths
            mUndoPaths.add(mPaths.removeAt(mPaths.size-1))
            /**Se não utilizassemos o método invalidate(), precisariamos utilizar o método
            *onDraw novamente
            *
            *Este método inválida a view e onDraw será chamado posteriormente, se a view for
            vísivel*/
            invalidate()
//            Ou seja, precisariamos desenhar as alterações novamente
        }
    }

    private fun setUpDrawing() {
        mDrawPaint = Paint()
        mDrawPath = CustomPath(color= color,
                               brushThickness = mBrushSize
                                )
        mDrawPaint?.let {
          with(it){
              this@with.color = color
//              Geometria da linha vai ser no estilo de linha
              style = Paint.Style.STROKE
//              Trata o caminho da linha
              strokeJoin = Paint.Join.ROUND
//              Arredonda o final da linha
              strokeCap= Paint.Cap.ROUND
          }
        }
//        Paint.DITHER_FLAG habilita o dither
        mCanvasPaint = Paint(Paint.DITHER_FLAG)
//        mBrushSize = 20F
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvarBitmap = Bitmap.createBitmap(w,h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(mCanvarBitmap!!)
    }
//     TODO:Change Canvas to Canvas? if fails
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(mCanvarBitmap!!, 0f, 0f, mCanvasPaint)

        for(path in mPaths){
            mDrawPaint?.let {
                with(path){
                    it.strokeWidth = this@with.brushThickness
                    it.color = this@with.color
                    canvas.drawPath(this@with, it)
                }
            }
        }

        if(!mDrawPath!!.isEmpty){
            mDrawPaint?.let {
                with(mDrawPath!!){
                    it.strokeWidth = this@with.brushThickness
                    it.color = this@with.color
                }
            }
//            Cria um canvas
            canvas.drawPath(mDrawPath!!, mDrawPaint!!)
        }
    }

    fun setSizeForBrush(newSize: Float){
//        Aplica as dimensões do app, para as dimensões do celular
        mBrushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
            newSize,
             resources.displayMetrics
        )
//      atribui o tamanho da linha
        mDrawPaint!!.strokeWidth = mBrushSize
    }

    fun setColor(newColor: String){
        color = Color.parseColor(newColor)
        mDrawPaint!!.color = color
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y

        when(event?.action){
            MotionEvent.ACTION_DOWN->{
                mDrawPath!!.color = color
                mDrawPath!!.brushThickness = mBrushSize
//              .reset() :  Clear any lines and curves from the path, making it empty.
//                This does NOT change the fill-type setting.
                mDrawPath!!.reset()
//                Instead of asserting, we can do the null check
                if (touchX != null && touchY!=null){
                    mDrawPath!!.moveTo(touchX, touchY)
                }
            }
            MotionEvent.ACTION_MOVE->{
//                Aciona na movimentação do dedo na tela
                if (touchY != null) {
                    if (touchX != null) {
//                        Faz uma linha de touchX a touchY
                        mDrawPath!!.lineTo(touchX, touchY)
                    }
                }
            }
            MotionEvent.ACTION_UP->{
//                Aciona quando existe a retirada do dedo da tela
//                Adiciona o caminho feito no action move
                mPaths.add(mDrawPath!!)
                mDrawPath = CustomPath(color, mBrushSize)
            }
            else->return false
        }
        invalidate()
        return true
    }

    internal inner class CustomPath(var color: Int,
                                    var brushThickness:Float
                                    ): Path()

}