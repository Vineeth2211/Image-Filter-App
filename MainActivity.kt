package com.example.imagefilterapp

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.imagefilter.R
import com.squareup.picasso.Picasso
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var imageView: ImageView
    lateinit var originalBitmap: Bitmap
    lateinit var filteredBitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)

        // Load the sample image
        originalBitmap = BitmapFactory.decodeResource(resources, R.drawable.sample_image)
        imageView.setImageBitmap(originalBitmap)

        findViewById<Button>(R.id.buttonSepia).setOnClickListener { applySepiaFilter() }
        findViewById<Button>(R.id.buttonBlackWhite).setOnClickListener { applyBlackWhiteFilter() }
        findViewById<Button>(R.id.buttonNegative).setOnClickListener { applyNegativeFilter() }
        findViewById<Button>(R.id.buttonGreyscale).setOnClickListener { applyGreyscaleFilter() }
        findViewById<Button>(R.id.buttonDownload).setOnClickListener { saveImage() }

        // Request storage permissions if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }
    }

    private fun applySepiaFilter() {
        filteredBitmap = Bitmap.createBitmap(originalBitmap.width, originalBitmap.height, Bitmap.Config.ARGB_8888)
        for (x in 0 until originalBitmap.width) {
            for (y in 0 until originalBitmap.height) {
                val pixel = originalBitmap.getPixel(x, y)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)
                val tr = (0.393 * r + 0.769 * g + 0.189 * b).toInt().coerceIn(0, 255)
                val tg = (0.349 * r + 0.686 * g + 0.168 * b).toInt().coerceIn(0, 255)
                val tb = (0.272 * r + 0.534 * g + 0.131 * b).toInt().coerceIn(0, 255)
                filteredBitmap.setPixel(x, y, Color.rgb(tr, tg, tb))
            }
        }
        imageView.setImageBitmap(filteredBitmap)
    }



    private fun applyBlackWhiteFilter() {
        filteredBitmap = Bitmap.createBitmap(originalBitmap.width, originalBitmap.height, Bitmap.Config.ARGB_8888)
        for (x in 0 until originalBitmap.width) {
            for (y in 0 until originalBitmap.height) {
                val pixel = originalBitmap.getPixel(x, y)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)
                val grey = (r + g + b) / 3
                filteredBitmap.setPixel(x, y, Color.rgb(grey, grey, grey))
            }
        }
        imageView.setImageBitmap(filteredBitmap)
    }

    private fun applyNegativeFilter() {
        filteredBitmap = Bitmap.createBitmap(originalBitmap.width, originalBitmap.height, Bitmap.Config.ARGB_8888)
        for (x in 0 until originalBitmap.width) {
            for (y in 0 until originalBitmap.height) {
                val pixel = originalBitmap.getPixel(x, y)
                val r = 255 - Color.red(pixel)
                val g = 255 - Color.green(pixel)
                val b = 255 - Color.blue(pixel)
                filteredBitmap.setPixel(x, y, Color.rgb(r, g, b))
            }
        }
        imageView.setImageBitmap(filteredBitmap)
    }

    private fun applyGreyscaleFilter() {
        filteredBitmap = Bitmap.createBitmap(originalBitmap.width, originalBitmap.height, Bitmap.Config.ARGB_8888)
        val cm = ColorMatrix()
        cm.setSaturation(0f)
        val paint = android.graphics.Paint()
        paint.colorFilter = ColorMatrixColorFilter(cm)

        val canvas = android.graphics.Canvas(filteredBitmap)
        canvas.drawBitmap(originalBitmap, 0f, 0f, paint)
        imageView.setImageBitmap(filteredBitmap)
    }

    private fun saveImage() {
        if (!this::filteredBitmap.isInitialized) {
            Toast.makeText(this, "Please apply a filter first.", Toast.LENGTH_SHORT).show()
            return
        }

        val file = createImageFile()
        if (file != null) {
            try {
                val out = FileOutputStream(file)
                filteredBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.flush()
                out.close()
                Toast.makeText(this, "Image saved: ${file.absolutePath}", Toast.LENGTH_LONG).show()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, "Failed to save image.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Failed to create file.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createImageFile(): File? {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return try {
            File.createTempFile(
                "filtered_image_${UUID.randomUUID()}",
                ".png",
                storageDir
            )
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}
