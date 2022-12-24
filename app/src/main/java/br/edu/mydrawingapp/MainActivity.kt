package br.edu.mydrawingapp

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.get

class MainActivity : AppCompatActivity(){


    private lateinit var drawingView:DrawingView
    private lateinit var btnBrush:ImageButton
    private lateinit var mImageButtonCurrentPaint:ImageButton
    private lateinit var linearLayoutPaintColors:LinearLayout
    private lateinit var btnGallery:ImageButton
//  Com o laucher de gallery, iniciamos uma activity para o result das permissões
    private val openGalleryLaucher:ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){
        result ->
//      Confere se o resultado das permissões foi ok e se existem dados no resultado da permissão
        if (result.resultCode == RESULT_OK && result.data != null){
            val imageBackground: ImageView = findViewById(R.id.iv_background)
//          setando uma imagem pro background do image view do FrameLayout
            imageBackground.setImageURI(result.data?.data)
        }
    }
    private var requestPermission:ActivityResultLauncher<Array<String>> = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()){
            permissions ->
       permissions.entries.forEach{
           permission->
           val permissionName = permission.key
           val isGranted = permission.value
           if (isGranted){
               Toast.makeText(
                   applicationContext,
                   "Permission granted, now you can read for  storage files",
                   Toast.LENGTH_SHORT
               ).show()
//            Intent da ação de colocar e lançando a abertura da galeria do celular
               val pickIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
               openGalleryLaucher.launch(pickIntent)
           }else{
               if (permissionName == Manifest.permission.READ_EXTERNAL_STORAGE){
                   Toast.makeText(
                       applicationContext,
                       "Permission denied for read storage files",
                       Toast.LENGTH_SHORT
                   ).show()
               }
           }
       }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        drawingView = findViewById(R.id.drawing_view)
        btnBrush= findViewById(R.id.ib_brush)

        linearLayoutPaintColors = findViewById(R.id.ll_paint_colors)
        btnGallery = findViewById(R.id.ib_gallery)

        mImageButtonCurrentPaint = linearLayoutPaintColors[0] as ImageButton
        mImageButtonCurrentPaint.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.pallet_pressed
            )
        )

//        ImageButton for set Gallery
        btnGallery.setOnClickListener{
            requestStoragePermission()
        }


//        Standard Brush size
        drawingView.setSizeForBrush(BrushSize.MEDIUM.value)
        btnBrush.setOnClickListener{
            showBrushSizeChooserDialog()
        }
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
        ) {
            showRationaleDialog(
                "DrawingApp",
                "Put photos in app cannot be used because read external file access is denied"
            )
        } else {
            requestPermission.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
        }
    }

    private fun showRationaleDialog(title: String, message: String) {
//        Utilizando um builder
        val alertDialog = AlertDialog.Builder(this)

//        Setando as propriedades
        alertDialog
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false).setPositiveButton("Cancel"){
                dialog,_->
                dialog.dismiss()
            }
//      Criando  e mostrando o dialogo de alerta
        alertDialog.create().show()

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