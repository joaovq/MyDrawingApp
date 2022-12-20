package br.edu.mydrawingapp

import android.app.Dialog
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import androidx.core.view.get

class MainActivity : AppCompatActivity(){

    private lateinit var drawingView:DrawingView
    private lateinit var btnBrush:ImageButton
    private lateinit var mImageButtonCurrentPaint:ImageButton
    private lateinit var linearLayoutPaintColors:LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        drawingView = findViewById(R.id.drawing_view)
        btnBrush= findViewById(R.id.ib_brush)

        linearLayoutPaintColors = findViewById(R.id.ll_paint_colors)

        mImageButtonCurrentPaint = linearLayoutPaintColors[0] as ImageButton
        mImageButtonCurrentPaint.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.pallet_pressed
            )
        )


//        Standard Brush size
        drawingView.setSizeForBrush(BrushSize.MEDIUM.value)
        btnBrush.setOnClickListener{
            showBrushSizeChooserDialog()
        }
    }

    private fun showBrushSizeChooserDialog(){
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        brushDialog.setTitle("Brush size:  ")
        val smallBtn = brushDialog.findViewById<ImageButton>(R.id.ib_small_brush)
        val mediumBtn = brushDialog.findViewById<ImageButton>(R.id.ib_medium_brush)
        val largeBtn = brushDialog.findViewById<ImageButton>(R.id.ib_large_brush)
        smallBtn.setOnClickListener{
            drawingView.setSizeForBrush(BrushSize.SMALL.value)
            brushDialog.dismiss()
        }
        mediumBtn.setOnClickListener{
            drawingView.setSizeForBrush(BrushSize.MEDIUM.value)
            brushDialog.dismiss()
        }
        largeBtn.setOnClickListener{
            drawingView.setSizeForBrush(BrushSize.LARGE.value)
            brushDialog.dismiss()
        }
        brushDialog.show()
    }

    fun paintClicked(view: View){
        var imageButton:ImageButton? = null
        val colorTag: String?
        if(view !== mImageButtonCurrentPaint){
            imageButton = view as ImageButton
            colorTag = imageButton.tag.toString()
        }else{
            colorTag = view.tag.toString()
        }
        drawingView.setColor(colorTag)
        imageButton?.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.pallet_pressed
            )
        )
        mImageButtonCurrentPaint.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.pallet_normal)
        )
        mImageButtonCurrentPaint = view
    }

}