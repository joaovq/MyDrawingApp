package br.edu.mydrawingapp

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton

class MainActivity : AppCompatActivity() {

    private lateinit var drawingView:DrawingView
    private lateinit var btnBrush:ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        drawingView = findViewById(R.id.drawing_view)
        btnBrush= findViewById(R.id.ib_brush)


        drawingView.setSizeForBrush(20F)
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
            //            TODO: Put the sizes in Enum Class
            drawingView.setSizeForBrush(10F)
            brushDialog.dismiss()
        }
        mediumBtn.setOnClickListener{
            //            TODO: Put the sizes in Enum Class
            drawingView.setSizeForBrush(20F)
            brushDialog.dismiss()
        }
        largeBtn.setOnClickListener{
//            TODO: Put the sizes in Enum Class
            drawingView.setSizeForBrush(30F)
            brushDialog.dismiss()
        }
        brushDialog.show()
    }
}