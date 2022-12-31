package com.example.imagecompressor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object CompressUtils {
    /**
     * 质量压缩
     */
    fun qualityCompress(applicationContext: Context) : Boolean {
        val imageFile = File(applicationContext.filesDir, "photo.png")
        if (!imageFile.exists()) {
            return false
        }
        val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath, BitmapFactory.Options())
        val fos = FileOutputStream(
            File(
                applicationContext.filesDir,
                "${System.currentTimeMillis()}_quality.jpg"
            )
        )
        val quality = 50
        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos)
        } catch (e: Throwable) {
            e.printStackTrace()
            return false
        } finally {
            fos.apply {
                flush()
                close()
            }
        }
        return bitmap != null
    }

    /**
     * 图片大小限定在100kb
     */
    fun qualityCompress2(applicationContext: Context): Boolean {
        val imageFile = File(applicationContext.filesDir, "photo.png")
        val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath, BitmapFactory.Options())
        val fos = FileOutputStream(
            File(
                applicationContext.filesDir,
                "${System.currentTimeMillis()}_quality.jpg"
            )
        )

        val baos = ByteArrayOutputStream()
        var quality = 50
        try {
            do {
                quality -= 10
                baos.reset()
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
            } while (baos.toByteArray().size / 1024 > 100)
            fos.write(baos.toByteArray())
        } catch (e: Throwable) {
            e.printStackTrace()
            return false
        } finally {
            fos.apply {
                flush()
                close()
            }
        }
        return bitmap != null
    }

    /**
     * 尺寸压缩
     */
    fun scaleCompress(applicationContext: Context) : Boolean{
        val imageFile = File(applicationContext.filesDir, "photo.png")
        if (!imageFile.exists()) {
            return false
        }
        val reqWidth = 200
        val reqHeight = 200
        val bitmap = decodeSampledBitmapFromFile(imageFile, reqWidth, reqHeight)
        val fos = FileOutputStream(
            File(
                applicationContext.filesDir,
                "${System.currentTimeMillis()}_scale.jpg"
            )
        )
        val quality = 50
        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos)
        } catch (e: Throwable) {
            e.printStackTrace()
        } finally {
            fos.apply {
                flush()
                close()
            }
        }
        return bitmap != null
    }

    private fun decodeSampledBitmapFromFile(
        imageFile: File,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap {
        return BitmapFactory.Options().run {
            inJustDecodeBounds = true
            //先获取原始图片的宽高，不会将Bitmap加载到内存中，返回null
            BitmapFactory.decodeFile(imageFile.absolutePath, this)
            inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)
            inJustDecodeBounds = false
            //对图片进行缩放，返回Bitmap对象
            BitmapFactory.decodeFile(imageFile.absolutePath, this)
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        //解构语法，获取原始图片的宽高
        val (width: Int, height: Int) = options.run { outWidth to outHeight }
        //计算最大的inSampleSize，该值为2的幂次方
        var inSampleSize = 1
        //原始图片的宽高要大于要求的宽高
        if (width > reqWidth || height > reqHeight) {
            while (width / inSampleSize >= reqWidth && height / inSampleSize >= reqHeight) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    /**
     * Native压缩
     * Native压缩和原生压缩生成文件大小一致，Android高版本上已经实现了libjpeg。
     */
    fun nativeCompress(applicationContext: Context) : Boolean{
        val imageFile = File(applicationContext.filesDir, "photo.png")
        if (!imageFile.exists()) {
            return false
        }
        val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath, BitmapFactory.Options())
        val destFile = File(
            applicationContext.filesDir,
            "${System.currentTimeMillis()}_native.jpg"
        ).absolutePath
        return nativeCompress(bitmap, 50, destFile, true) == 1
    }

    private external fun nativeCompress(
        bitmap: Bitmap,
        quality: Int,
        destFile: String,
        optimize: Boolean
    ): Int

    init {
        System.loadLibrary("imagecompressor")
    }
}
