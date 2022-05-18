package com.example.cameraandgallery

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.cameraandgallery.databinding.ActivityMainBinding
import java.io.IOException
import java.text.SimpleDateFormat

class MainActivity : BaseActivity() {

    private val PERMISSION_STORAGE = 199;
    private val PERMISSION_CAMERA = 200;

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    var realUri: Uri? = null

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        //퍼미션 요청
        requirePermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_STORAGE)
    }

    override fun permissionGranted(requestCode: Int) {
        when(requestCode){
            PERMISSION_STORAGE -> setViews()
            PERMISSION_CAMERA -> openCamera()
        }
    }
    override fun permissionDenied(requestCode: Int) {
        when(requestCode){
            PERMISSION_STORAGE -> {
                Toast.makeText(baseContext, "저장소 권한이 있어야 기능을 사용할수 있습니다", Toast.LENGTH_LONG).show()
                finish()
            }
            PERMISSION_CAMERA -> {
                Toast.makeText(baseContext, "권한이 있어야 카메라를 사용할수 있습니다", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setViews() {
        binding.buttonCamera.setOnClickListener{
            requirePermissions(arrayOf(Manifest.permission.CAMERA), PERMISSION_CAMERA)
        }
        binding.buttonGallery.setOnClickListener{
            openGallery()
        }
    }

    private fun openGallery() {
      val intent = Intent(Intent.ACTION_PICK)
        intent.type = MediaStore.Images.Media.CONTENT_TYPE
        galleryResultLauncher.launch(intent)
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        createImageUri(newFileName(), "image/jpg")?.let { uri ->
            realUri = uri
            intent.putExtra(MediaStore.EXTRA_OUTPUT, realUri)
            cameraResultLauncher.launch(intent)
        }
//        cameraResultLauncher.launch (intent)
    }

    @SuppressLint("SimpleDateFormat")
    private fun newFileName(): String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss")
        val filename = sdf.format(System.currentTimeMillis())
        return "$filename.jpg"
    }

    private fun createImageUri(filename : String, mimeType : String): Uri? {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        values.put(MediaStore.Images.Media.MIME_TYPE,mimeType)
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    }

    //Intent Result
    private val cameraResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
//            val intent = result.data
//
//            if (intent?.extras?.get("data") != null){
//                val bitmap = intent.extras?.get("data") as Bitmap
//                binding.imagePreview.setImageBitmap(bitmap)
//            }
            realUri?.let { uri ->
                val bitmap = loadBitmap(uri)
                binding.imagePreview.setImageBitmap(bitmap)
                realUri = null
            }
        }
    }

    private fun loadBitmap(uri: Uri): Bitmap? {
        var image : Bitmap? = null

        try {
            image = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
                val source : ImageDecoder.Source = ImageDecoder.createSource(this.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            }else {
                MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
            }
        }catch (e : IOException){
            e.printStackTrace()
        }
        return image
    }

    private val galleryResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data.let { uri ->
                binding.imagePreview.setImageURI(uri)
            }
        }
    }
}