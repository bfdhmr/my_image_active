package com.example.demo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button // 新增：导入Button类
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.util.*
import android.graphics.Color

// 添加缺失的类定义
class ImagePreviewActivity : AppCompatActivity() {
    private lateinit var previewImageView: ImageView
    private lateinit var backButton: ImageButton
    private lateinit var selectButton: ImageButton
    private lateinit var counterTextView: TextView
    private lateinit var bottomSelectButton: Button // 新增：底部确认按钮引用
    private lateinit var currentImagePath: String
    private var currentPosition: Int = 0
    private lateinit var allImages: ArrayList<String>
    private lateinit var selectedPhotos: HashSet<String>

    companion object {
        const val EXTRA_IMAGE_PATH = "extra_image_path"
        const val EXTRA_ALL_IMAGES = "extra_all_images"
        const val EXTRA_SELECTED_PHOTOS = "extra_selected_photos"
        const val EXTRA_CURRENT_POSITION = "extra_current_position"
        const val RESULT_SELECTED_PHOTOS = "result_selected_photos"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_preview)

        // 初始化视图 - 修正ID引用
        previewImageView = findViewById(R.id.previewImageView)
        backButton = findViewById(R.id.backButton)
        selectButton = findViewById(R.id.topSelectButton)
        counterTextView = findViewById(R.id.counterTextView)
        bottomSelectButton = findViewById(R.id.bottomSelectButton) // 新增：初始化底部确认按钮
        
        // 获取传递的数据
        currentImagePath = intent.getStringExtra(EXTRA_IMAGE_PATH) ?: ""
        allImages = intent.getStringArrayListExtra(EXTRA_ALL_IMAGES) ?: ArrayList() // 明确指定类型
        currentPosition = intent.getIntExtra(EXTRA_CURRENT_POSITION, 0)
        
        // 恢复选中的图片集合
        val selectedSet = intent.getStringArrayListExtra(EXTRA_SELECTED_PHOTOS)
        selectedPhotos = if (selectedSet != null) HashSet(selectedSet) else HashSet()

        // 加载并显示当前图片
        loadAndDisplayImage(currentImagePath)
        updateCounter()
        updateSelectButtonState()

        // 设置返回按钮点击事件
        backButton.setOnClickListener {
            finishWithResult()
        }

        // 设置选择按钮点击事件
        selectButton.setOnClickListener {
            toggleSelection()
        }

        // 设置底部确认按钮点击事件
        bottomSelectButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                handleConfirmSelection()
            }
        })

        // 设置图片点击事件（可以实现双击放大等功能）
        previewImageView.setOnClickListener {
            // 可以在这里添加双击放大等功能
        }

        // 设置左右滑动切换图片的手势
        setupGestureDetection()
    }

    private fun loadAndDisplayImage(imagePath: String) {
        try {
            val imageUri = Uri.fromFile(File(imagePath))
            previewImageView.setImageURI(imageUri)
            updateCounter()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "加载图片失败", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateCounter() {
        counterTextView.text = "${currentPosition + 1}/${allImages.size}"
    }

    private fun updateSelectButtonState() {
        if (selectedPhotos.contains(currentImagePath)) {
            selectButton.setBackgroundResource(R.drawable.selection_circle_selected)
            // 添加对勾图标
            selectButton.setImageResource(R.drawable.custom_check_mark)
            selectButton.setColorFilter(Color.WHITE) // 设置对勾为白色
        } else {
            selectButton.setBackgroundResource(R.drawable.selection_circle_unselected)
            // 清除对勾图标
            selectButton.setImageResource(0)
        }
    }

    private fun toggleSelection() {
        val isSelected = selectedPhotos.contains(currentImagePath)
        
        if (isSelected) {
            // 如果当前图片已被选中，则取消选择
            selectedPhotos.remove(currentImagePath)
        } else {
            // 单选逻辑：先清空之前的所有选择
            selectedPhotos.clear()
            // 然后只选择当前图片
            selectedPhotos.add(currentImagePath)
        }
        updateSelectButtonState()
    }

    private fun setupGestureDetection() {
        // 修复3：在onTouch中添加performClick调用
        findViewById<View>(android.R.id.content).setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View, event: android.view.MotionEvent): Boolean {
                if (event.action == android.view.MotionEvent.ACTION_DOWN) {

                    v.performClick()
                    return true
                }
                return false
            }
        })
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        finishWithResult()
        super.onBackPressed()
    }

    private fun showPreviousImage() {
        if (currentPosition > 0) {
            currentPosition--
            currentImagePath = allImages[currentPosition]
            loadAndDisplayImage(currentImagePath)
            updateSelectButtonState()
        }
    }

    private fun showNextImage() {
        if (currentPosition < allImages.size - 1) {
            currentPosition++
            currentImagePath = allImages[currentPosition]
            loadAndDisplayImage(currentImagePath)
            updateSelectButtonState()
        }
    }

    private fun finishWithResult() {
        val resultIntent = Intent()
        resultIntent.putExtra(RESULT_SELECTED_PHOTOS, ArrayList(selectedPhotos))
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    // 新增：处理确认选择的逻辑
    private fun handleConfirmSelection() {
        try {
            // 使用当前预览的图片路径
            val imagePath = currentImagePath
            
            // 创建Intent跳转到ImageClipActivity
            val intent = Intent(this, ImageClipActivity::class.java)
            
            // 将图片路径作为extra参数传递
            intent.putExtra("image_path", imagePath)
            
            // 启动ImageClipActivity
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "跳转失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}