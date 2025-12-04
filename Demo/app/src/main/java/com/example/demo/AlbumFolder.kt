package com.example.demo.model

import java.util.ArrayList

/**
 * 相册文件夹数据模型
 */
class AlbumFolder(val name: String, val path: String, val coverPath: String) {
    val photos: ArrayList<PhotoItem> = ArrayList()

    val photoCount: Int
        get() = photos.size
}