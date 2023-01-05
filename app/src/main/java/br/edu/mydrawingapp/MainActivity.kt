package br.edu.mydrawingapp

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity(){


    private lateinit var drawingView:DrawingView
    private lateinit var btnBrush:ImageButton
    private lateinit var mImageButtonCurrentPaint:ImageButton
    private lateinit var linearLayoutPaintColors:LinearLayout
    private lateinit var btnGallery:ImageButton
    private lateinit var btnUndo:ImageButton
    private lateinit var btnSave:ImageButton
    private lateinit var customProgressDialog: Dialog

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
                       "Permission granted, now you can read for storage files",
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
//        Esconder a ActionBar
//        supportActionBar?.hide()

        drawingView = findViewById(R.id.drawing_view)
        btnBrush= findViewById(R.id.ib_brush)

        linearLayoutPaintColors = findViewById(R.id.ll_paint_colors)
        btnGallery = findViewById(R.id.ib_gallery)
        btnUndo = findViewById(R.id.ib_undo)
        btnSave = findViewById(R.id.ib_save)

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

        btnUndo.setOnClickListener{
            drawingView.onClickUndo()
        }

        btnSave.setOnClickListener{
            if(isReadStorageAllowed()) {
                showProgressDialog()
                val flDrawingView:FrameLayout = findViewById(R.id.fl_drawing_view_container)
                lifecycleScope.launch{
                    delay(1000)
                    val bitmapFromView:Bitmap = getBitmapFromView(flDrawingView)
                    saveBitmapFile(bitmapFromView)
                }
            }
            else{
                requestStoragePermission()
            }
        }
    }

    private fun isReadStorageAllowed():Boolean{
        val result = ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_EXTERNAL_STORAGE)

        return result == PackageManager.PERMISSION_GRANTED
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
            requestPermission.launch(arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ))
        }
    }

    private suspend fun saveBitmapFile(mBitmap:Bitmap?):String{
        var result = ""
        withContext(Dispatchers.IO){
            mBitmap?.let {
                try {
                    val bytes = ByteArrayOutputStream()
                    mBitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)

//                    instanciando um arquivo com um nome diferente de caminho para cada salvamento
//                    Estamos utilizando o System.currentTimeMillis() para ser mais aleatório
//                    File.separator é o separador padrão do sistema, dependente do sistema
                    val file = File(externalCacheDir?.absoluteFile.toString()
                            + File.separator + "KidsDrawingApp_"+ System.currentTimeMillis()/1000+ ".png"
                    )
                    val outputStream = FileOutputStream(file)
                    outputStream.write(bytes.toByteArray())
                    outputStream.close()

                    result = file.absolutePath

                    runOnUiThread{
                        cancelProgressDialog()
                        if (result.isNotEmpty()){
                            val toastCompleteSaveFile = Toast.makeText(
                                this@MainActivity,
                                "File saved sucessfully: $result",
                                Toast.LENGTH_LONG
                            )
                            toastCompleteSaveFile.show()
                            shareImage(result)
                        }else{
                            val toastWrongSaveFile = Toast.makeText(
                                this@MainActivity,
                                "Something went wrong while saving the file",
                                Toast.LENGTH_LONG
                            )
                            toastWrongSaveFile.show()
                        }
//                        Cancelando o dialogo de carregamento
                    }
                }
                catch (e:Exception){
                    result = ""
                    e.printStackTrace()
                }
            }
        }
        return result
    }

    //    Images are Bitmap and Bitmap can be storage.
//    Pegando os Bitmaps e juntando com a imagem e salvando na galeria
    private fun getBitmapFromView(view: View): Bitmap {
        val resultBitmapFromView = Bitmap.createBitmap(
            view.width,
            view.height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(resultBitmapFromView)

        val bgDrawable = view.background

        if (bgDrawable != null){
            bgDrawable.draw(canvas)
        }
        else{
            canvas.drawColor(Color.WHITE)
        }

        view.draw(canvas)

        return resultBitmapFromView
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
    private fun showProgressDialog(){
        customProgressDialog = Dialog(this@MainActivity)

        /*Seta a tela através de um layout resource
        * O resource layout vai ser inflado, adicionando acima de todas as views da tela*/
        customProgressDialog.setContentView(R.layout.dialog_custom_progress)
        customProgressDialog.setCancelable(false)
//          Inicia o Dialogo e mostra na tela
        customProgressDialog.show()
    }

    private fun cancelProgressDialog(){
        customProgressDialog.dismiss()
    }

    private fun showBrushSizeChooserDialog(){
        val brushDialog = Dialog(this)
//        /*Seta a tela através de um layout resource
//        * O resource layout vai ser inflado, adicionando acima de todas as views da tela*/
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
//            Pega a tag pela cor em formato de string
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

    private fun shareImage(result: String){
//        Provê um caminho para as aplicações
//        Provê uma interface para scannear a media
//        Passamos o contexto, o caminho
        MediaScannerConnection.scanFile(this, arrayOf(result), null,
            ){
//            Atraves desse caminho, será escaneado para enviar para o lugar desejado
            path, uri->
            /*Um Intent no sistema operacional
            Android é um mecanismo de software
            que permite aos usuários coordenar
             funções de diferentes atividades para realizar uma tarefa.*/
            val shareIntent = Intent()
//          A coordenação da ação de mandar e compartilhar com alguém
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
            shareIntent.type = "image/png"
//            iniciando uma atividade de Chooser. Escolher para aonde vai mandar

            /*Convenience function for creating a ACTION_CHOOSER Intent.
                Builds a new ACTION_CHOOSER Intent that wraps the given target intent,
                also optionally supplying a title.
                If the target intent has specified*/
            startActivity(Intent.createChooser(shareIntent,"Share for friends"))
        }

    }
}