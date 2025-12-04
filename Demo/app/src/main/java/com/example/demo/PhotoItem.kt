package com.example.demo.model

import java.io.File

/**
 * 图片项数据模型
 */
class PhotoItem(val id: String, val path: String, val displayName: String, val folderName: String, val dateAdded: Long) {
    val file: File = File(path)
    val isVideo: Boolean = path.toLowerCase().endsWith(".mp4") || path.toLowerCase().endsWith(".mov")
}