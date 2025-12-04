package com.example.demo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class AlbumImagePickerActivity : AppCompatActivity() {
    private val REQUEST_PERMISSION = 1001
    private val REQUEST_PREVIEW = 1002 // 添加预览请求码常量
    private lateinit var photoRecyclerView: RecyclerView
    private lateinit var photoAdapter: PhotoAdapter
    private lateinit var confirmButton: Button // 添加确认按钮引用
    private val photoList = mutableListOf<String>() // 显示的列表
    private val allPhotosList = mutableListOf<String>() // 所有图片的完整列表
    private val selectedPhotos = mutableSetOf<String>() // 选中的图片集合
    private val INITIAL_LOAD_COUNT = 20 // 初始加载数量
    private val LOAD_MORE_COUNT = 6 // 每次加载更多的数量
    private var isLoading = false // 防止重复加载
    private var currentLoadedCount = 0 // 当前已加载的数量

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album_image_picker)

        // 找到返回按钮并设置点击事件
        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        // 找到确认按钮并设置点击事件
        confirmButton = findViewById(R.id.confirmButton)
        confirmButton.setOnClickListener {
            // 处理确认选择的逻辑
            handleConfirmSelection()
        }

        // 初始化RecyclerView
        photoRecyclerView = findViewById(R.id.photoRecyclerView)
        val layoutManager = GridLayoutManager(this, 3) // 每行3张图片（从4改为3）
        photoRecyclerView.layoutManager = layoutManager
        photoAdapter = PhotoAdapter(photoList)
        photoRecyclerView.adapter = photoAdapter

        // 添加滚动监听
        photoRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                
                // 当滚动到底部附近且没有在加载中时，加载更多
                if (!isLoading && lastVisibleItemPosition >= totalItemCount - 4 && totalItemCount < allPhotosList.size) {
                    loadMorePhotos()
                }
            }
        })

        // 检查并请求权限
        checkPermissionAndLoadPhotos()
    }

    // 更新确认按钮的状态和文字
    private fun updateConfirmButton() {
        val count = selectedPhotos.size
        if (count > 0) {
            confirmButton.text = "确认"
            confirmButton.isEnabled = true
            confirmButton.setBackgroundResource(R.drawable.confirm_button_enabled) // 设置为红色
        } else {
            confirmButton.text = "确认"
            confirmButton.isEnabled = false
            confirmButton.setBackgroundResource(R.drawable.confirm_button_disabled) // 设置为灰色
        }
    }

    // 处理确认选择的逻辑
    private fun handleConfirmSelection() {
        if (selectedPhotos.isNotEmpty()) {
            try {
                // 获取选中的图片路径（由于是单选模式，只取第一个元素）
                val imagePath = selectedPhotos.first()
                
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

    private fun checkPermissionAndLoadPhotos() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            // 请求权限
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_PERMISSION
            )
        } else {
            // 已有权限，加载照片
            loadPhotosFromGallery()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限被授予，加载照片
                loadPhotosFromGallery()
            } else {
                // 权限被拒绝
                Toast.makeText(this, "需要存储权限才能访问相册", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadPhotosFromGallery() {
        allPhotosList.clear()
        photoList.clear()
        selectedPhotos.clear()
        currentLoadedCount = 0

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA
        )

        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC" // 按添加日期降序排列

        val cursor: Cursor? = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )

        cursor?.use {
            val dataColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            while (it.moveToNext()) {
                val photoPath = it.getString(dataColumn)
                allPhotosList.add(photoPath)
            }
        }

        // 初始加载前20张
        loadInitialPhotos()
    }

    private fun loadInitialPhotos() {
        isLoading = true
        
        // 计算要加载的数量
        val endIndex = Math.min(INITIAL_LOAD_COUNT, allPhotosList.size)
        if (endIndex > 0) {
            photoList.addAll(allPhotosList.subList(0, endIndex))
            currentLoadedCount = endIndex
            photoAdapter.notifyDataSetChanged()
        }
        
        isLoading = false
    }

    private fun loadMorePhotos() {
        if (isLoading) return
        
        isLoading = true
        
        // 计算要加载的数量
        val startIndex = currentLoadedCount
        val endIndex = Math.min(startIndex + LOAD_MORE_COUNT, allPhotosList.size)
        
        if (startIndex < endIndex) {
            // 使用postDelayed模拟异步加载
            photoRecyclerView.postDelayed({
                photoList.addAll(allPhotosList.subList(startIndex, endIndex))
                currentLoadedCount = endIndex
                photoAdapter.notifyItemRangeInserted(startIndex, endIndex - startIndex)
                isLoading = false
            }, 100)
        } else {
            isLoading = false
        }
    }

    // 添加打开图片预览的方法
    private fun openImagePreview(position: Int) {
        val intent = Intent(this, ImagePreviewActivity::class.java)
        intent.putExtra(ImagePreviewActivity.EXTRA_IMAGE_PATH, photoList[position])
        intent.putExtra(ImagePreviewActivity.EXTRA_ALL_IMAGES, ArrayList(photoList))
        intent.putExtra(ImagePreviewActivity.EXTRA_CURRENT_POSITION, position)
        intent.putExtra(ImagePreviewActivity.EXTRA_SELECTED_PHOTOS, ArrayList(selectedPhotos))
        startActivityForResult(intent, REQUEST_PREVIEW)
    }

    // 添加处理预览返回结果的方法
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PREVIEW && resultCode == RESULT_OK && data != null) {
            // 更新选中的图片集合
            val updatedSelectedPhotos = data.getStringArrayListExtra(ImagePreviewActivity.RESULT_SELECTED_PHOTOS)
            if (updatedSelectedPhotos != null) {
                // 清空并重新设置，确保单选逻辑正确
                selectedPhotos.clear()
                // 即使返回了多个，我们也只取第一张，确保严格单选
                if (updatedSelectedPhotos.isNotEmpty()) {
                    selectedPhotos.add(updatedSelectedPhotos[0])
                }
                // 更新适配器中的UI
                photoAdapter.notifyDataSetChanged()
                // 更新确认按钮状态
                updateConfirmButton()
            }
        }
    }

    // 照片适配器
    inner class PhotoAdapter(private val photos: List<String>) :
        RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {
    
        inner class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val photoImageView: ImageView = itemView.findViewById(R.id.photoImageView)
            val selectionCircle: ImageView = itemView.findViewById(R.id.selectionCircle)
            val checkMark: ImageView = itemView.findViewById(R.id.checkMark)
            val selectedOverlay: View = itemView.findViewById(R.id.selectedOverlay)
        }
    
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_photo_grid, parent, false)
            return PhotoViewHolder(view)
        }
    
        override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
            val photoPath = photos[position]
            // 使用文件路径创建URI并加载图片
            try {
                val imageUri = Uri.fromFile(File(photoPath))
                // 修复：使用holder.itemView.context代替未定义的context变量
                val bitmap = MediaStore.Images.Media.getBitmap(holder.itemView.context.contentResolver, imageUri)
                holder.photoImageView.setImageBitmap(bitmap)
            } catch (e: Exception) {
                Log.e("PhotoAdapter", "Error loading image: $e")
            }
    
            // 更新选中状态UI
            updateSelectionUI(holder, photoPath)
    
            // 修改点击事件，点击整个item进入预览
            holder.itemView.setOnClickListener {
                // 单击进入预览
                this@AlbumImagePickerActivity.openImagePreview(position)
            }
    
            // 添加点击右下角选择图标的事件用于选择图片
            holder.selectionCircle.setOnClickListener { view ->
                // 阻止事件冒泡到itemView
                view.isClickable = true
                view.isFocusable = true
                toggleSelection(holder, photoPath)
            }
        }
    
        private fun updateSelectionUI(holder: PhotoViewHolder, photoPath: String) {
            val isSelected = this@AlbumImagePickerActivity.selectedPhotos.contains(photoPath)
            
            if (isSelected) {
                // 选中状态
                holder.selectionCircle.setBackgroundResource(R.drawable.selection_circle_selected)
                holder.checkMark.visibility = View.VISIBLE
                holder.selectedOverlay.visibility = View.VISIBLE
            } else {
                // 未选中状态
                holder.selectionCircle.setBackgroundResource(R.drawable.selection_circle_unselected)
                holder.checkMark.visibility = View.GONE
                holder.selectedOverlay.visibility = View.GONE
            }
        }
    
        private fun toggleSelection(holder: PhotoViewHolder, photoPath: String) {
            val isSelected = this@AlbumImagePickerActivity.selectedPhotos.contains(photoPath)
            
            if (isSelected) {
                // 取消选中
                this@AlbumImagePickerActivity.selectedPhotos.remove(photoPath)

                // 更新UI
                updateSelectionUI(holder, photoPath)
            } else {
                // 单选逻辑：先清空之前的选择
                val previousSelection = ArrayList(this@AlbumImagePickerActivity.selectedPhotos)
                this@AlbumImagePickerActivity.selectedPhotos.clear()
                // 添加新选择的图片
                this@AlbumImagePickerActivity.selectedPhotos.add(photoPath)
                
                // 更新UI：更新之前选中的图片和当前选中的图片
                if (previousSelection.isNotEmpty()) {
                    // 找到之前选中的图片的位置并更新UI
                    for (previousPath in previousSelection) {
                        val previousPosition = photos.indexOf(previousPath)
                        if (previousPosition != -1) {
                            val previousHolder = photoRecyclerView.findViewHolderForAdapterPosition(previousPosition) as? PhotoViewHolder
                            if (previousHolder != null) {
                                updateSelectionUI(previousHolder, previousPath)
                            }
                        }
                    }
                }
                // 更新当前选中的图片UI
                updateSelectionUI(holder, photoPath)
            }
            
            // 更新确认按钮状态
            this@AlbumImagePickerActivity.updateConfirmButton()
        }
    
        override fun getItemCount(): Int = photos.size
    }
}