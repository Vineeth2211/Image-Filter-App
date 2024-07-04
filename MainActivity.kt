package com.example.imagefilterapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.imagefilter.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var originalBitmap: Bitmap
    private lateinit var filteredBitmap: Bitmap

    companion object {
        private const val REQUEST_SELECT_IMAGE = 1
        private const val REQUEST_WRITE_PERMISSION = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)

        // Load the sample image
        originalBitmap = BitmapFactory.decodeResource(resources, R.drawable.sample_image)
        imageView.setImageBitmap(originalBitmap)

        findViewById<Button>(R.id.buttonSelectImage).setOnClickListener { selectImageFromGallery() }
        findViewById<Button>(R.id.buttonSepia).setOnClickListener { applyFilter(FilterType.SEPIA) }
        findViewById<Button>(R.id.buttonBlackWhite).setOnClickListener { applyFilter(FilterType.BLACK_WHITE) }
        findViewById<Button>(R.id.buttonNegative).setOnClickListener { applyFilter(FilterType.NEGATIVE) }
        findViewById<Button>(R.id.buttonVignette).setOnClickListener { applyFilter(FilterType.VIGNETTE) }
        findViewById<Button>(R.id.buttonDownload).setOnClickListener { saveImage() }

        // Request storage permissions if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_WRITE_PERMISSION)
        }
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_SELECT_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SELECT_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageUri: Uri? = data.data
            if (selectedImageUri != null) {
                val inputStream = contentResolver.openInputStream(selectedImageUri)
                originalBitmap = BitmapFactory.decodeStream(inputStream)
                imageView.setImageBitmap(originalBitmap)
            }
        }
    }

    private fun applyFilter(filterType: FilterType) {
        filteredBitmap = Bitmap.createBitmap(originalBitmap.width, originalBitmap.height, Bitmap.Config.ARGB_8888)
        when (filterType) {
            FilterType.SEPIA -> applySepiaFilter()
            FilterType.BLACK_WHITE -> applyBlackWhiteFilter()
            FilterType.NEGATIVE -> applyNegativeFilter()
            FilterType.VIGNETTE -> applyVignetteFilter()
        }
        imageView.setImageBitmap(filteredBitmap)
    }

    private fun applySepiaFilter() {
        val canvas = Canvas(filteredBitmap)
        val paint = Paint()
        paint.colorFilter = createSepiaColorFilter()
        canvas.drawBitmap(originalBitmap, 0f, 0f, paint)
    }

    private fun applyBlackWhiteFilter() {
        val canvas = Canvas(filteredBitmap)
        val paint = Paint()
        paint.colorFilter = createBlackWhiteColorFilter()
        canvas.drawBitmap(originalBitmap, 0f, 0f, paint)
    }

    private fun applyNegativeFilter() {
        val canvas = Canvas(filteredBitmap)
        val paint = Paint()
        paint.colorFilter = createNegativeColorFilter()
        canvas.drawBitmap(originalBitmap, 0f, 0f, paint)
    }

    private fun applyVignetteFilter() {
        val width = originalBitmap.width
        val height = originalBitmap.height
        val radius = (width / 1.5).toFloat()
        val centerX = width / 2f
        val centerY = height / 2f

        val gradient = RadialGradient(
            centerX, centerY, radius,
            intArrayOf(0x7f000000, 0x00000000),
            floatArrayOf(0.0f, 1.0f),
            Shader.TileMode.CLAMP
        )

        val paint = Paint()
        paint.isAntiAlias = true
        paint.shader = gradient

        filteredBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(filteredBitmap)
        canvas.drawBitmap(originalBitmap, 0f, 0f, null)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }

    private fun createSepiaColorFilter(): ColorMatrixColorFilter {
        val sepiaMatrix = ColorMatrix()
        sepiaMatrix.set(floatArrayOf(
            0.393f, 0.769f, 0.189f, 0f, 0f,
            0.349f, 0.686f, 0.168f, 0f, 0f,
            0.272f, 0.534f, 0.131f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ))
        return ColorMatrixColorFilter(sepiaMatrix)
    }

    private fun createBlackWhiteColorFilter(): ColorMatrixColorFilter {
        val blackWhiteMatrix = ColorMatrix()
        blackWhiteMatrix.setSaturation(0f)
        return ColorMatrixColorFilter(blackWhiteMatrix)
    }

    private fun createNegativeColorFilter(): ColorMatrixColorFilter {
        val negativeMatrix = ColorMatrix(floatArrayOf(
            -1f, 0f, 0f, 0f, 255f,
            0f, -1f, 0f, 0f, 255f,
            0f, 0f, -1f, 0f, 255f,
            0f, 0f, 0f, 1f, 0f
        ))
        return ColorMatrixColorFilter(negativeMatrix)
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

    enum class FilterType {
        SEPIA, BLACK_WHITE, NEGATIVE, VIGNETTE
    }
}
