package com.example.demo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraCaptureActivity : AppCompatActivity() {
    private lateinit var previewView: PreviewView
    private lateinit var photoPreview: ImageView
    private lateinit var confirmButton: Button
    private lateinit var cancelButton: Button
    private lateinit var captureButton: ImageButton
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private var capturedImageFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_capture)

        previewView = findViewById(R.id.previewView)
        photoPreview = findViewById(R.id.photoPreview)
        confirmButton = findViewById(R.id.confirmButton)
        cancelButton = findViewById(R.id.cancelButton)
        captureButton = findViewById(R.id.captureButton)
        cameraExecutor = Executors.newSingleThreadExecutor()

        // 设置返回按钮点击事件
        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        // 设置拍照按钮点击事件
        captureButton.setOnClickListener {
            takePicture()
        }

        // 设置确定按钮点击事件
        confirmButton.setOnClickListener {
            try {
                // 确保capturedImageFile已正确初始化且文件存在
                if (capturedImageFile != null && capturedImageFile!!.exists()) {
                    // 跳转到ImageClipActivity并传递图片路径
                    val intent = Intent(this, ImageClipActivity::class.java)
                    intent.putExtra("image_path", capturedImageFile!!.absolutePath)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "图片文件不存在", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "跳转失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // 设置取消按钮点击事件
        cancelButton.setOnClickListener {
            cancelCapture()
        }

        // 检查相机权限
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({ 
            // 获取相机提供者
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // 创建预览用例
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            // 创建图像捕获用例
            imageCapture = ImageCapture.Builder()
                .build()

            // 选择后置摄像头
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // 解绑之前的所有用例
                cameraProvider.unbindAll()
                
                // 绑定预览和图像捕获用例到相机
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture
                )

            } catch (exc: Exception) {
                Log.e(TAG, "相机启动失败: ${exc.message}")
                Toast.makeText(this, "相机启动失败: ${exc.message}", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePicture() {
        // 确保图像捕获对象已初始化
        val imageCapture = imageCapture ?: return

        // 创建用于保存照片的临时文件
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_$timeStamp.jpg"
        val storageDir = getExternalFilesDir(null)
        capturedImageFile = File(storageDir, imageFileName)

        // 创建输出选项对象
        val outputOptions = ImageCapture.OutputFileOptions.Builder(capturedImageFile!!).build()

        // 执行拍照操作
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    // 拍照成功，显示预览和按钮
                    runOnUiThread {
                        showPreviewAndButtons()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    // 拍照失败
                    Log.e(TAG, "拍照失败: ${exception.message}")
                    runOnUiThread {
                        Toast.makeText(
                            this@CameraCaptureActivity,
                            "拍照失败: ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        )
    }

    private fun showPreviewAndButtons() {
        // 加载捕获的图像到预览视图，并处理旋转问题
        val bitmap = getRotatedBitmap(capturedImageFile?.absolutePath)
        photoPreview.setImageBitmap(bitmap)
        
        // 显示预览视图和按钮，隐藏拍照按钮
        photoPreview.visibility = ImageView.VISIBLE
        confirmButton.visibility = Button.VISIBLE
        cancelButton.visibility = Button.VISIBLE
        captureButton.visibility = ImageButton.GONE
    }

    /**
     * 根据EXIF信息获取正确旋转角度的Bitmap
     */
    private fun getRotatedBitmap(imagePath: String?): Bitmap? {
        if (imagePath == null) return null

        try {
            val bitmap = BitmapFactory.decodeFile(imagePath)
            val exif = ExifInterface(imagePath)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
            
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            }
            
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: Exception) {
            Log.e(TAG, "获取旋转后的Bitmap失败: ${e.message}")
            return BitmapFactory.decodeFile(imagePath)
        }
    }

    private fun cancelCapture() {
        // 删除临时文件
        capturedImageFile?.delete()
        capturedImageFile = null
        // 重置界面
        resetToCaptureMode()
    }

    private fun resetToCaptureMode() {
        // 隐藏预览视图和按钮，显示拍照按钮
        photoPreview.visibility = ImageView.GONE
        confirmButton.visibility = Button.GONE
        cancelButton.visibility = Button.GONE
        captureButton.visibility = ImageButton.VISIBLE
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "需要相机权限才能使用此功能",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 如果有未处理的照片，删除它
        capturedImageFile?.delete()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraCapture"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}