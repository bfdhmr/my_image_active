package com.example.demo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 获取Switch组件
        val featureSwitch = findViewById<Switch>(R.id.featureSwitch)
        
        // 获取按钮组件
        val startCreationBtn = findViewById<Button>(R.id.startCreationBtn)
        
        // 设置开关状态变化监听器
        featureSwitch.setOnCheckedChangeListener { _, isChecked ->
            // 当开关状态变化时执行的逻辑
            if (isChecked) {
                // 开关打开时的操作
                println("功能已开启")
            } else {
                // 开关关闭时的操作
                println("功能已关闭")
            }
        }
        
        // 设置按钮点击事件监听器
        startCreationBtn.setOnClickListener {
            // 根据开关状态跳转到不同页面
            val intent = Intent()
            if (featureSwitch.isChecked) {
                // 开关为开，跳转到相机拍摄页面
                intent.setClass(this, CameraCaptureActivity::class.java)
            } else {
                // 开关为关，跳转到相册选择页面
                intent.setClass(this, AlbumImagePickerActivity::class.java)
            }
            startActivity(intent)
        }
    }
}