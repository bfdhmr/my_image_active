# my_image_active
这是一个使用Kotlin开发的Android图片处理应用，提供相机拍摄、相册选择和图片编辑等功能，支持图片旋转、翻转、亮度/对比度调整以及水印添加。
功能特性
# 📷 相机拍摄
实时相机预览
照片拍摄和保存
自动旋转校正
# 📁 相册管理
相册图片浏览（网格布局）
快速图片选择
# 🎨 图片编辑
旋转功能：90°和270°顺时针旋转
翻转功能：水平镜像和垂直镜像翻转
亮度调整：0-100范围的亮度调节，实时预览
对比度调整：0-100范围的对比度调节，实时预览
水印添加：在图片上添加文字水印
# 💾 图片保存
保存到系统相册（DCIM/Demo目录）
自动通知媒体扫描器更新
# 技术栈
开发语言：Kotlin
Android SDK：
最低支持：Android 5.0 Lollipop (API 21)
目标版本：Android 14 (API 34)
核心依赖：
AndroidX组件
Material3设计库
CameraX API（相机功能）
ConstraintLayout（UI布局）
权限说明
应用需要以下权限：

相机权限：用于拍摄照片
存储权限：
Android 13以下：读取和写入外部存储
Android 13及以上：读取媒体图像权限
安装说明
前提条件
Android Studio最新版本
JDK 11或更高版本
Android设备或模拟器（API 21+）
构建步骤
克隆项目到本地
使用Android Studio打开项目
等待Gradle同步完成
