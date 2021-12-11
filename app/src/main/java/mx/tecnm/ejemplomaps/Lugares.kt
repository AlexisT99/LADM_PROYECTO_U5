package mx.tecnm.ejemplomaps

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import mx.tecnm.ejemplomaps.databinding.ActivityLugaresBinding
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.widget.Toast
import com.google.firebase.storage.FirebaseStorage
import java.io.File


class Lugares : AppCompatActivity() {
    private lateinit var b: ActivityLugaresBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityLugaresBinding.inflate(layoutInflater)
        setContentView(b.root)

        val intent = intent
        if (intent!=null){
            val value = intent.getStringArrayExtra("Lugar")
            b.txtTitulo.text = value!![0]

            val progressBar = ProgressDialog(this)
            progressBar.setMessage("Recuperando imagenes")
            progressBar.setCancelable(false)
            progressBar.show()


            val storagimg1 = FirebaseStorage.getInstance().reference.child("${value[1]}.jpg")
            val storagimg2 = FirebaseStorage.getInstance().reference.child("${value[2]}.jpg")
            val storagimg3 = FirebaseStorage.getInstance().reference.child("${value[3]}.jpg")

            val localfile1 = File.createTempFile("temp1","jpg")
            storagimg1.getFile(localfile1).addOnSuccessListener {
                val bitmap = BitmapFactory.decodeFile(localfile1.absolutePath)
                flipperImage(bitmap)
            }.addOnFailureListener{
                Toast.makeText(this,"Error al recuperar las imagenes ${it} 1",Toast.LENGTH_LONG).show()
            }

            val localfile2 = File.createTempFile("temp2","jpg")
            storagimg2.getFile(localfile2).addOnSuccessListener {
                val bitmap = BitmapFactory.decodeFile(localfile2.absolutePath)
                flipperImage(bitmap)
            }.addOnFailureListener{
                Toast.makeText(this,"Error al recuperar las imagenes ${it} 2",Toast.LENGTH_LONG).show()
            }

            val localfile3 = File.createTempFile("temp3","jpg")
            storagimg3.getFile(localfile3).addOnSuccessListener {
                val bitmap = BitmapFactory.decodeFile(localfile3.absolutePath)
                flipperImage(bitmap)
                if(progressBar.isShowing)progressBar.dismiss()
            }.addOnFailureListener{
                Toast.makeText(this,"Error al recuperar las imagenes ${it} 3",Toast.LENGTH_LONG).show()
            }
            if(progressBar.isShowing)progressBar.dismiss()
            b.txtDescripcion.text = value!![4]
            b.viewfliper.startFlipping()
        }

    }

    fun flipperImage(image:Bitmap){
        var imageView = ImageView(this)
        var ob = BitmapDrawable(resources, image)
        imageView.background = ob
        b.viewfliper.addView(imageView)
        b.viewfliper.flipInterval = 3000
        b.viewfliper.isAutoStart = true
        b.viewfliper.setInAnimation(this,android.R.anim.slide_out_right)
        b.viewfliper.setOutAnimation(this,android.R.anim.slide_in_left)
    }
}