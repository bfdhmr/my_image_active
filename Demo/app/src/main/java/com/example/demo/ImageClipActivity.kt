package com.example.demo

import android.content.Intent
import android.graphics.*
import android.media.ExifInterface
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import android.content.ContentValues

class ImageClipActivity : AppCompatActivity() {
    private lateinit var imagePreview: AppCompatImageView
    private lateinit var backButton: ImageButton
    private lateinit var saveButton: Button
    private lateinit var rotateButton: LinearLayout
    private lateinit var brightnessButton: LinearLayout
    private lateinit var brightnessSlider: SeekBar
    private lateinit var toolsNavBar: ConstraintLayout
    private lateinit var brightnessLabel: TextView

    // 添加对比度相关变量
    private lateinit var contrastButton: LinearLayout
    private lateinit var contrastSlider: SeekBar
    private lateinit var contrastLabel: TextView
    private var contrast = 0 // 对比度值范围：-100 到 100，默认为0（原始对比度）

    // 旋转选项按钮
    private var rotate90Button: Button? = null
    private var rotate180Button: Button? = null
    private var rotate270Button: Button? = null
    private var rotate360Button: Button? = null
    private var rotationOptionsLayout: LinearLayout? = null

    private var currentImagePath: String? = null
    private var bitmap: Bitmap? = null
    private var rotation = 0
    private var brightness = 0 // 亮度值范围：-100 到 100，默认为0（原始亮度）

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.image_clip)

            // 初始化视图组件
            initViews()

            // 设置按钮点击事件
            setButtonListeners()

            // 加载图片
            loadImage()
        } catch (e: Exception) {
Log.e("ImageClipActivity", "onCreate error", e)
            Toast.makeText(this, "初始化失败: ${e.message}", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    // 修改initViews方法，添加对比度滑块和标签的初始化
    private fun initViews() {
        try {
            // 逐个初始化视图组件，增加错误处理
            try {
                imagePreview = findViewById(R.id.imagePreview)
                Log.d("ImageClipActivity", "找到imagePreview")
            } catch (e: Exception) {
                Log.e("ImageClipActivity", "找不到imagePreview", e)
throw RuntimeException("找不到imagePreview视图")
            }

            try {
                backButton = findViewById(R.id.backButton)
                Log.d("ImageClipActivity", "找到backButton")
            } catch (e: Exception) {
                Log.e("ImageClipActivity", "找不到backButton", e)
                throw RuntimeException("找不到backButton视图")
            }

try {
                saveButton = findViewById(R.id.saveButton)
                Log.d("ImageClipActivity", "找到saveButton")
            } catch (e: Exception) {
                Log.e("ImageClipActivity", "找不到saveButton", e)
                throw RuntimeException("找不到saveButton视图")
            }

            try {
rotateButton = findViewById(R.id.rotateButton)
                Log.d("ImageClipActivity", "找到rotateButton")
            } catch (e: Exception) {
                Log.e("ImageClipActivity", "找不到rotateButton", e)
                throw RuntimeException("找不到rotateButton视图")
            }

            try {
                brightnessButton = findViewById(R.id.brightnessButton)
                Log.d("ImageClipActivity", "找到brightnessButton")
            } catch (e: Exception) {
                Log.e("ImageClipActivity", "找不到brightnessButton", e)
                throw RuntimeException("找不到brightnessButton视图")
            }

            try {
                contrastButton = findViewById(R.id.contrastButton)
                Log.d("ImageClipActivity", "找到contrastButton")
            } catch (e: Exception) {
                Log.e("ImageClipActivity", "找不到contrastButton", e)
                throw RuntimeException("找不到contrastButton视图")
            }

            try {
                toolsNavBar = findViewById(R.id.toolsNavBar)
                // 设置toolsNavBar背景为透明
                toolsNavBar.setBackgroundColor(Color.TRANSPARENT)
                Log.d("ImageClipActivity", "找到toolsNavBar并设置透明背景")
            } catch (e: Exception) {
                Log.e("ImageClipActivity", "找不到toolsNavBar", e)
                throw RuntimeException("找不到toolsNavBar视图")
            }

            // 初始化亮度滑块
            brightnessSlider = SeekBar(this)
            brightnessSlider.id = View.generateViewId()

            // 创建亮度文本标签
            brightnessLabel = TextView(this)
            brightnessLabel.id = View.generateViewId()
            brightnessLabel.text = "亮度"
            brightnessLabel.textSize = 16f
            brightnessLabel.setTextColor(Color.BLACK)
            brightnessLabel.gravity = Gravity.CENTER_HORIZONTAL

            val labelParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )
            labelParams.topMargin = 20
            brightnessLabel.layoutParams = labelParams
            brightnessLabel.visibility = View.GONE

            val params = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )
            params.topMargin = 10  // 调整为相对于文本的边距
            params.leftMargin = 20
            params.rightMargin = 20
            brightnessSlider.layoutParams = params

            brightnessSlider.max = 200
            brightnessSlider.progress = 100
brightnessSlider.visibility = View.GONE

            // 初始化对比度滑块
            contrastSlider = SeekBar(this)
            contrastSlider.id = View.generateViewId()

            // 创建对比度文本标签
            contrastLabel = TextView(this)
            contrastLabel.id = View.generateViewId()
            contrastLabel.text = "对比度"
            contrastLabel.textSize = 16f
            contrastLabel.setTextColor(Color.BLACK)
            contrastLabel.gravity = Gravity.CENTER_HORIZONTAL

            val contrastLabelParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )
            contrastLabelParams.topMargin = 20
            contrastLabel.layoutParams = contrastLabelParams
            contrastLabel.visibility = View.GONE

            val contrastParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )
            contrastParams.topMargin = 10  // 调整为相对于文本的边距
            contrastParams.leftMargin = 20
            contrastParams.rightMargin = 20
            contrastSlider.layoutParams = contrastParams

            contrastSlider.max = 200
            contrastSlider.progress = 100
            contrastSlider.visibility = View.GONE

            try {
                // 添加亮度和对比度控件
                toolsNavBar.addView(brightnessLabel)
                toolsNavBar.addView(brightnessSlider)
                toolsNavBar.addView(contrastLabel)
                toolsNavBar.addView(contrastSlider)
                Log.d("ImageClipActivity", "成功添加亮度和对比度控件")
            } catch (e: Exception) {
                Log.e("ImageClipActivity", "添加控件失败", e)
                throw RuntimeException("添加控件失败")
            }

            // 修改亮度按钮点击事件，同步显示/隐藏亮度文本
            brightnessButton.setOnClickListener {
                // 显示亮度滑块时隐藏旋转选项
                if (rotationOptionsLayout?.visibility == View.VISIBLE) {
                    rotationOptionsLayout?.visibility = View.GONE
                }

                // 隐藏对比度控件
                contrastSlider.visibility = View.GONE
                contrastLabel.visibility = View.GONE

                // 同步切换亮度文本和滑块的可见性
                val isVisible = brightnessSlider.visibility == View.VISIBLE
                brightnessSlider.visibility = if (isVisible) View.GONE else View.VISIBLE
                brightnessLabel.visibility = brightnessSlider.visibility
            }

            // 设置亮度滑块监听器
            brightnessSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    brightness = progress - 100
                    applyBrightnessAdjustment()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })

            // 设置对比度滑块监听器
            contrastSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    contrast = progress - 100
                    applyContrastAdjustment()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })

            Log.d("ImageClipActivity", "视图初始化成功")
        } catch (e: Exception) {
            Log.e("ImageClipActivity", "initViews error", e)
            Toast.makeText(this, "初始化视图失败: ${e.message}", Toast.LENGTH_SHORT).show()
            throw e
        }
    }

    private fun setButtonListeners() {
        backButton.setOnClickListener { finish() }
        saveButton.setOnClickListener { saveImage() }

        // 旋转按钮点击事件 - 显示/隐藏旋转选项
        rotateButton.setOnClickListener {
            toggleRotationOptions()
        }

        brightnessButton.setOnClickListener {
            // 显示亮度滑块时隐藏其他控件
            if (rotationOptionsLayout?.visibility == View.VISIBLE) {
                rotationOptionsLayout?.visibility = View.GONE
            }
            if (contrastSlider.visibility == View.VISIBLE) {
                contrastSlider.visibility = View.GONE
                contrastLabel.visibility = View.GONE
            }

            // 同步切换亮度文本和滑块的可见性
            brightnessSlider.visibility =
                if (brightnessSlider.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            brightnessLabel.visibility = brightnessSlider.visibility
        }

        // 添加对比度按钮点击事件
        contrastButton.setOnClickListener {
            // 显示对比度滑块时隐藏其他控件
            if (rotationOptionsLayout?.visibility == View.VISIBLE) {
                rotationOptionsLayout?.visibility = View.GONE
            }
            if (brightnessSlider.visibility == View.VISIBLE) {
                brightnessSlider.visibility = View.GONE
                brightnessLabel.visibility = View.GONE
            }

            // 同步切换对比度文本和滑块的可见性
            contrastSlider.visibility =
                if (contrastSlider.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            contrastLabel.visibility = contrastSlider.visibility
        }
    }

    // 修改toggleRotationOptions方法，隐藏对比度滑块
    private fun toggleRotationOptions() {
        try {
            // 如果旋转选项布局还未创建，则创建
            if (rotationOptionsLayout == null) {
                createRotationOptions()
            }

            // 切换显示状态
            if (rotationOptionsLayout?.visibility == View.VISIBLE) {
                rotationOptionsLayout?.visibility = View.GONE
                // 隐藏时隐藏亮度和对比度滑块
                brightnessSlider.visibility = View.GONE
                brightnessLabel.visibility = View.GONE
                contrastSlider.visibility = View.GONE
                contrastLabel.visibility = View.GONE
            } else {
                // 显示旋转选项时隐藏亮度和对比度滑块
                brightnessSlider.visibility = View.GONE
                brightnessLabel.visibility = View.GONE
                contrastSlider.visibility = View.GONE
                contrastLabel.visibility = View.GONE
                rotationOptionsLayout?.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
Log.e("ImageClipActivity", "toggleRotationOptions error", e)
            Toast.makeText(this, "切换旋转选项失败", Toast.LENGTH_SHORT).show()
        }
    }

    // 创建旋转选项按钮
    private fun createRotationOptions() {
        rotationOptionsLayout = LinearLayout(this)
        rotationOptionsLayout?.orientation = LinearLayout.HORIZONTAL
        rotationOptionsLayout?.gravity = Gravity.CENTER
        rotationOptionsLayout?.setBackgroundColor(Color.argb(200, 0, 0, 0))
    
        val layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            160
        )
        rotationOptionsLayout?.layoutParams = layoutParams
    
        // 创建旋转90度按钮
        rotate90Button = Button(this)
        rotate90Button?.setBackgroundResource(android.R.color.transparent)
        // 设置内边距，确保图片完全显示
        rotate90Button?.setPadding(20, 20, 20, 20)
        // 使用较小的图标尺寸
        val rotate90Drawable = resources.getDrawable(R.drawable.ic_rotate_90, theme)
        rotate90Drawable?.setBounds(0, 0, 60, 60) // 设置图标大小为60x60像素
        rotate90Button?.setCompoundDrawables(null, rotate90Drawable, null, null)
        rotate90Button?.setTextColor(Color.TRANSPARENT) // 隐藏文本   
        val buttonParams = LinearLayout.LayoutParams(120, 120) // 减小按钮尺寸
        buttonParams.setMargins(15, 0, 15, 0)
        rotate90Button?.layoutParams = buttonParams
        rotate90Button?.setOnClickListener {
            rotation = (rotation + 90) % 360
            applyRotation()
        }
        
        // 创建左右镜像翻转按钮
        rotate180Button = Button(this)
        rotate180Button?.setBackgroundResource(android.R.color.transparent)
        // 设置内边距
        rotate180Button?.setPadding(20, 20, 20, 20)
        // 使用较小的图标尺寸
        val rotate180Drawable = resources.getDrawable(R.drawable.ic_rotate_180, theme)
        rotate180Drawable?.setBounds(0, 0, 60, 60)
        rotate180Button?.setCompoundDrawables(null, rotate180Drawable, null, null)
        rotate180Button?.setTextColor(Color.TRANSPARENT) // 隐藏文本
        rotate180Button?.layoutParams = buttonParams
        rotate180Button?.setOnClickListener {
            // 执行左右镜像翻转
            flipImageHorizontally()
        }
    
        // 创建上下镜像翻转按钮
        rotate360Button = Button(this)
        rotate360Button?.setBackgroundResource(android.R.color.transparent)
        // 设置内边距
        rotate360Button?.setPadding(20, 20, 20, 20)
        // 使用较小的图标尺寸
        val rotate360Drawable = resources.getDrawable(R.drawable.ic_rotate_360, theme)
        rotate360Drawable?.setBounds(0, 0, 60, 60)
        rotate360Button?.setCompoundDrawables(null, rotate360Drawable, null, null)
        rotate360Button?.setTextColor(Color.TRANSPARENT) // 隐藏文本
        rotate360Button?.layoutParams = buttonParams
        rotate360Button?.setOnClickListener {
            // 执行上下镜像翻转
            flipImageVertically()
        }
    
        // 创建旋转270度按钮
        rotate270Button = Button(this)
        rotate270Button?.setBackgroundResource(android.R.color.transparent)
        // 设置内边距
        rotate270Button?.setPadding(20, 20, 20, 20)
        // 使用较小的图标尺寸
        val rotate270Drawable = resources.getDrawable(R.drawable.ic_rotate_270, theme)
        rotate270Drawable?.setBounds(0, 0, 60, 60)
        rotate270Button?.setCompoundDrawables(null, rotate270Drawable, null, null)
        rotate270Button?.setTextColor(Color.TRANSPARENT) // 隐藏文本
        rotate270Button?.layoutParams = buttonParams
        rotate270Button?.setOnClickListener {
            rotation = (rotation + 270) % 360
            applyRotation()
        }
    
        // 添加按钮到布局
        rotationOptionsLayout?.addView(rotate90Button)
        rotationOptionsLayout?.addView(rotate180Button)
        rotationOptionsLayout?.addView(rotate360Button)
        rotationOptionsLayout?.addView(rotate270Button)
    
        // 添加布局到toolsNavBar
        toolsNavBar.addView(rotationOptionsLayout)
        rotationOptionsLayout?.visibility = View.GONE
    }

// 添加左右镜像功能方法
    private fun flipImageHorizontally() {
        try {
            if (bitmap == null) return

            // 创建一个水平翻转的矩阵
            val matrix = Matrix()
            matrix.postScale(-1f, 1f) // x轴缩放-1表示水平翻转

            // 创建翻转后的bitmap
            val flippedBitmap = Bitmap.createBitmap(
                bitmap!!,
                0,
                0,
                bitmap!!.width,
                bitmap!!.height,
                matrix,
                true
            )

            bitmap = flippedBitmap
            imagePreview.setImageBitmap(bitmap)

            Log.d("ImageClipActivity", "图片水平镜像成功")
        } catch (e: Exception) {
            Log.e("ImageClipActivity", "图片水平镜像失败", e)
            Toast.makeText(this, "镜像失败", Toast.LENGTH_SHORT).show()
        }
    }

    // 添加上下镜像翻转方法
    private fun flipImageVertically() {
        try {
            if (bitmap == null) return

            // 创建一个垂直翻转的矩阵
            val matrix = Matrix()
            matrix.postScale(1f, -1f) // y轴缩放-1表示垂直翻转

            // 创建翻转后的bitmap
            val flippedBitmap = Bitmap.createBitmap(
                bitmap!!,
                0,
                0,
                bitmap!!.width,
                bitmap!!.height,
                matrix,
                true
            )

            // 更新bitmap并显示
            bitmap = flippedBitmap
            imagePreview.setImageBitmap(bitmap)

            Log.d("ImageClipActivity", "图片垂直镜像成功")
} catch (e: Exception) {
            Log.e("ImageClipActivity", "图片垂直镜像失败", e)
            Toast.makeText(this, "镜像失败", Toast.LENGTH_SHORT).show()
        }
    }

    // 加载图片
    private fun loadImage() {
        try {
            // 从Intent中获取图片路径
            currentImagePath = intent.getStringExtra("image_path")

            if (currentImagePath.isNullOrEmpty()) {
                Toast.makeText(this, "没有找到图片路径", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
// 加载图片
            bitmap = BitmapFactory.decodeFile(currentImagePath)

            if (bitmap == null) {
                Toast.makeText(this, "加载图片失败", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

            // 读取图片的旋转信息
            try {
                val exif = ExifInterface(currentImagePath!!)
                val orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
                rotation = getRotationFromOrientation(orientation)
            } catch (e: IOException) {
                Log.e("ImageClipActivity", "读取Exif信息失败", e)
            }

            // 应用初始旋转
applyRotation()

            Log.d("ImageClipActivity", "图片加载成功")
        } catch (e: Exception) {
            Log.e("ImageClipActivity", "loadImage error", e)
            Toast.makeText(this, "加载图片失败: ${e.message}", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun getRotationFromOrientation(orientation: Int): Int {
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }

    // 修改saveImage方法，使用MediaStore API保存到相册
    private fun saveImage() {
        try {
            if (bitmap == null) {
                Toast.makeText(this, "没有可保存的图片", Toast.LENGTH_SHORT).show()
                return
            }
    
            // 创建最终的Bitmap（应用旋转和亮度对比度调整）
            val finalBitmap = createFinalBitmap()
            if (finalBitmap == null) {
                Toast.makeText(this, "创建最终图片失败", Toast.LENGTH_SHORT).show()
                return
            }
    
            // 生成文件名
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "JPEG_$timeStamp.jpg"
    
            // 保存到相册
            val savedUri = saveToGallery(finalBitmap, imageFileName)
            
            if (savedUri != null) {
                Toast.makeText(this, "图片已保存到相册", Toast.LENGTH_SHORT).show()
                Log.d("ImageClipActivity", "图片保存成功，路径: $savedUri")
                
                // 返回上一页
                finish()
            } else {
                Toast.makeText(this, "保存图片失败", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("ImageClipActivity", "saveImage error", e)
            Toast.makeText(this, "保存图片失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    // 添加保存到相册的辅助方法，使用MediaStore API
    private fun saveToGallery(bitmap: Bitmap, fileName: String): String? {
        try {
            // 创建MediaStore内容值
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
                put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
                
                // Android Q及以上需要添加相对路径
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_DCIM + File.separator + "Demo")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }
    
            // 获取内容解析器
            val resolver = contentResolver
            
            // 插入到MediaStore
            val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            
            if (imageUri == null) {
                Log.e("ImageClipActivity", "无法插入到MediaStore")
                return null
            }
    
            // 写入图片数据
            resolver.openOutputStream(imageUri).use { outputStream ->
                if (outputStream == null) {
                    Log.e("ImageClipActivity", "无法获取输出流")
                    resolver.delete(imageUri, null, null)
                    return null
                }
                
                // 压缩并写入图片
                val success = bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                if (!success) {
                    Log.e("ImageClipActivity", "压缩图片失败")
                    resolver.delete(imageUri, null, null)
                    return null
                }
            }
    
            // Android Q及以上，更新IS_PENDING标志
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(imageUri, contentValues, null, null)
            }
    
            // 通知媒体扫描器，确保图片立即可见
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
                sendBroadcast(Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    imageUri
                ))
            }
    
            return imageUri.toString()
        } catch (e: Exception) {
            Log.e("ImageClipActivity", "saveToGallery error", e)
            return null
        }
    }

    // 添加对比度调整方法
    // 修改applyContrastAdjustment方法，确保对比度调整正确生效
    private fun applyContrastAdjustment() {
        if (bitmap == null) return
        
        try {
            Log.d("ImageClipActivity", "开始应用对比度调整，当前对比度值: $contrast")
            
            val matrix = Matrix()
            matrix.postRotate(rotation.toFloat())
            
            // 使用ColorMatrix来调整亮度和对比度
            val colorMatrix = ColorMatrix()
            
            // 计算亮度和对比度因子 - 调整对比度范围使其效果更明显
            val brightnessFactor = 1.0f + (brightness / 200f)
            val contrastFactor = 1.0f + (contrast / 100f)
            
            Log.d("ImageClipActivity", "对比度因子: $contrastFactor, 亮度因子: $brightnessFactor")
            
            // 先应用亮度调整
            colorMatrix.setScale(brightnessFactor, brightnessFactor, brightnessFactor, 1f)
            
            // 再应用对比度调整 - 使用正确的公式
            // 对比度调整公式：factor*(value-0.5)+0.5
            val cmArray = floatArrayOf(
                contrastFactor, 0f, 0f, 0f, 128 * (1 - contrastFactor),
                0f, contrastFactor, 0f, 0f, 128 * (1 - contrastFactor),
                0f, 0f, contrastFactor, 0f, 128 * (1 - contrastFactor),
                0f, 0f, 0f, 1f, 0f
            )
            colorMatrix.set(cmArray)
            
            Log.d("ImageClipActivity", "ColorMatrix已设置完成")
            
            val paint = Paint()
            paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
            
            val adjustedBitmap = Bitmap.createBitmap(
                bitmap!!.width,
                bitmap!!.height,
                bitmap!!.config ?: Bitmap.Config.RGB_565
            )
            
            val canvas = Canvas(adjustedBitmap)
            canvas.drawBitmap(bitmap!!, 0f, 0f, paint)
            val finalBitmap = Bitmap.createBitmap(
                adjustedBitmap,
                0,
                0,
                adjustedBitmap.width,
                adjustedBitmap.height,
                matrix,
                true
            )
            
            Log.d("ImageClipActivity", "调整后的位图已创建，设置到预览")
            imagePreview.setImageBitmap(finalBitmap)
            
            // 强制刷新视图
            imagePreview.invalidate()
        } catch (e: Exception) {
            Log.e("ImageClipActivity", "applyContrastAdjustment error", e)
            Toast.makeText(this, "调整对比度失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // 修复applyRotation方法
    private fun applyRotation() {
        if (bitmap == null) return
    
        try {
            val matrix = Matrix()
            matrix.postRotate(rotation.toFloat())
    
            // 同时应用亮度和对比度调整
            val colorMatrix = ColorMatrix()
    
            // 计算亮度和对比度因子
            val brightnessFactor = 1.0f + (brightness / 100f)
            val contrastFactor = 1.0f + (contrast / 100f)
    
            // 先应用对比度调整：factor * (value - 0.5) + 0.5
            colorMatrix.set(
                floatArrayOf(
                    contrastFactor, 0f, 0f, 0f, (1 - contrastFactor) * 128f,
                    0f, contrastFactor, 0f, 0f, (1 - contrastFactor) * 128f,
                    0f, 0f, contrastFactor, 0f, (1 - contrastFactor) * 128f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
    
            // 再应用亮度调整（只保留一个setScale调用）
            colorMatrix.setScale(brightnessFactor, brightnessFactor, brightnessFactor, 1f)
    
            val paint = Paint()
            paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
    
            val adjustedBitmap = Bitmap.createBitmap(
                bitmap!!.width,
                bitmap!!.height,
                bitmap!!.config ?: Bitmap.Config.RGB_565
            )
    
            val canvas = Canvas(adjustedBitmap)
            canvas.drawBitmap(bitmap!!, 0f, 0f, paint)
    
            val finalBitmap = Bitmap.createBitmap(
                adjustedBitmap,
                0,
                0,
                adjustedBitmap.width,
                adjustedBitmap.height,
                matrix,
                true
            )
    
            imagePreview.setImageBitmap(finalBitmap)
        } catch (e: Exception) {
            Log.e("ImageClipActivity", "applyRotation error", e)
            Toast.makeText(this, "应用旋转失败", Toast.LENGTH_SHORT).show()
        }
    }


    // 修改applyBrightnessAdjustment方法，同时应用对比度调整
    private fun applyBrightnessAdjustment() {
        if (bitmap == null) return

        try {
            val matrix = Matrix()
            matrix.postRotate(rotation.toFloat())

            // 使用ColorMatrix来调整亮度和对比度
            val colorMatrix = ColorMatrix()

            // 计算亮度和对比度因子
            val brightnessFactor = 1.0f + (brightness / 100f)
            val contrastFactor = 1.0f + (contrast / 100f)

            // 先应用对比度调整：factor * (value - 0.5) + 0.5
            colorMatrix.set(
                floatArrayOf(
                    contrastFactor, 0f, 0f, 0f, (1 - contrastFactor) * 128f,
                    0f, contrastFactor, 0f, 0f, (1 - contrastFactor) * 128f,
                    0f, 0f, contrastFactor, 0f, (1 - contrastFactor) * 128f,
                    0f, 0f, 0f, 1f, 0f
                )
            )

            // 再应用亮度调整
            colorMatrix.setScale(brightnessFactor, brightnessFactor, brightnessFactor, 1f)

            val paint = Paint()
            paint.colorFilter = ColorMatrixColorFilter(colorMatrix)

            val adjustedBitmap = Bitmap.createBitmap(
                bitmap!!.width,
                bitmap!!.height,
                bitmap!!.config ?: Bitmap.Config.RGB_565
            )

            val canvas = Canvas(adjustedBitmap)
            canvas.drawBitmap(bitmap!!, 0f, 0f, paint)

            val finalBitmap = Bitmap.createBitmap(
                adjustedBitmap,
                0,
                0,
                adjustedBitmap.width,
                adjustedBitmap.height,
                matrix,
                true
            )

            imagePreview.setImageBitmap(finalBitmap)
        } catch (e: Exception) {
            Log.e("ImageClipActivity", "applyBrightnessAdjustment error", e)
            Toast.makeText(this, "调整亮度失败", Toast.LENGTH_SHORT).show()
        }
    }


    // 修改createFinalBitmap方法，同时应用亮度和对比度调整
    private fun createFinalBitmap(): Bitmap? {
        return try {
            if (bitmap == null) return null
    
            val matrix = Matrix()
            matrix.postRotate(rotation.toFloat())
    
            // 应用亮度和对比度调整
            val colorMatrix = ColorMatrix()
            val brightnessFactor = 1.0f + (brightness / 100f)
            val contrastFactor = 1.0f + (contrast / 100f)
            // 先应用对比度调整：factor * (value - 0.5) + 0.5
            colorMatrix.set(
                floatArrayOf(
                    contrastFactor, 0f, 0f, 0f, (1 - contrastFactor) * 128f,
                    0f, contrastFactor, 0f, 0f, (1 - contrastFactor) * 128f,
                    0f, 0f, contrastFactor, 0f, (1 - contrastFactor) * 128f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
    
            // 再应用亮度调整
            colorMatrix.setScale(brightnessFactor, brightnessFactor, brightnessFactor, 1f)
    
            val paint = Paint()
            paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
    
            val adjustedBitmap = Bitmap.createBitmap(
                bitmap!!.width,
                bitmap!!.height,
                bitmap!!.config ?: Bitmap.Config.RGB_565
            )
    
            val canvas = Canvas(adjustedBitmap)
            canvas.drawBitmap(bitmap!!, 0f, 0f, paint)
    
            val finalBitmap = Bitmap.createBitmap(
                adjustedBitmap,
                0,
                0,
                adjustedBitmap.width,
                adjustedBitmap.height,
                matrix,
                true
            )
            
            // 添加水印
            val watermarkedBitmap = addWatermark(finalBitmap)
            
            watermarkedBitmap
        } catch (e: Exception) {
            Log.e("ImageClipActivity", "createFinalBitmap error", e)
            null
        }
    }

    // 添加水印的辅助方法
    private fun addWatermark(sourceBitmap: Bitmap): Bitmap {
        // 创建一个可绘制的新位图
        val watermarkedBitmap = Bitmap.createBitmap(sourceBitmap.width, sourceBitmap.height, sourceBitmap.config)
        val canvas = Canvas(watermarkedBitmap)
        
        // 先绘制原图
        canvas.drawBitmap(sourceBitmap, 0f, 0f, null)
        
        // 设置水印文字属性
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 48f // 字体大小，可以根据需要调整
            isAntiAlias = true
            alpha = 180 // 半透明效果
            style = Paint.Style.FILL
            textAlign = Paint.Align.RIGHT
        }
        
        // 水印文字
        val watermarkText = "训练营"
        
        // 计算水印位置（右下角，留出一些边距）
        val margin = 50f
        val x = sourceBitmap.width - margin
        val y = sourceBitmap.height - margin
        
        // 绘制文字
        canvas.drawText(watermarkText, x, y, textPaint)
        
        return watermarkedBitmap
    }
}