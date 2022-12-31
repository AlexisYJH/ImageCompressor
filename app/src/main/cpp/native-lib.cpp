#include <jni.h>
#include <string>
#include <android/bitmap.h>
#include "log.h"
#include <jpeglib.h>
#include <setjmp.h>

typedef u_int8_t BYTE;

int jpegCompress(BYTE *data, uint32_t width, uint32_t height, jint quality, const char *file,
                 jboolean optimize);

struct my_error_mgr {
    struct jpeg_error_mgr pub;
    jmp_buf set_jmp_buffer;
};

typedef struct my_error_mgr *my_error_ptr;

//使用libjpeg解压文件时难免产生错误，原因可能是图片文件损坏、io错误、内存不足等。
//默认的错误处理函数会调用exit()函数，导致整个进程结束。
//这里注册自定义错误处理函数，改变此行为。
METHODDEF(void)
my_error_exit(j_common_ptr cinfo) {
    my_error_ptr myerr = (my_error_ptr) cinfo->err;
    (*cinfo->err->output_message)(cinfo);
    LOGW("jpeg_message_table[%d]:%s",
         myerr->pub.msg_code, myerr->pub.jpeg_message_table[myerr->pub.msg_code]);
    longjmp(myerr->set_jmp_buffer, 1);
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_imagecompressor_CompressUtils_nativeCompress(JNIEnv *env, jobject thiz,
                                                              jobject bitmap, jint quality,
                                                              jstring dest_file,
                                                              jboolean optimize) {
    const char *destFile = env->GetStringUTFChars(dest_file, NULL);

    //解码Android Bitmap信息
    AndroidBitmapInfo bitmapInfo;
    BYTE *pixelsColor;
    int result;
    if ((result = AndroidBitmap_getInfo(env, bitmap, &bitmapInfo)) < 0) {
        LOGW("AndroidBitmap_getInfo failed error=%d", result);
        return result;
    }
    //通过Android Native层的API获取Bitmap对象对应的像素数据
    if ((result = AndroidBitmap_lockPixels(env, bitmap, (void **) &pixelsColor)) < 0) {
        LOGW("AndroidBitmap_lockPixels failed error=%d", result);
        return result;
    }

    BYTE r, g, b;
    //width*height个像素，每个像素3个字节
    BYTE *data = (BYTE *) malloc(bitmapInfo.width * bitmapInfo.height * 3);
    BYTE *tempData = data;
    int color;
    LOGD("bitmapInfo width=%d, height=%d", bitmapInfo.width, bitmapInfo.height);
    //从pixelsColor数组中取出像素数据，获取RGB的值
    for (int i = 0; i < bitmapInfo.height; ++i) {
        for (int j = 0; j < bitmapInfo.width; ++j) {
            if (bitmapInfo.format == ANDROID_BITMAP_FORMAT_RGBA_8888) {
                color = *(int *) (pixelsColor);
                //从color值中读取RGBA的值
                //ABGR
                b = (color >> 16) & 0xFF;
                g = (color >> 8) & 0xFF;
                r = (color >> 0) & 0xFF;
                *data = r;
                *(data + 1) = g;
                *(data + 2) = b;
                data += 3;
                // 移动步长4个字节
                pixelsColor += 4;
            } else {
                LOGW("bitmapInfo error format=%d", bitmapInfo.format);
                return -2;
            }
        }
    }
    AndroidBitmap_unlockPixels(env, bitmap);
    //把像素数据交给libjpeg去进行压缩，压缩完成后，libjpeg写入磁盘文件
    result = jpegCompress(tempData, bitmapInfo.width, bitmapInfo.height, quality, destFile,
                          optimize);
    free((void *) tempData);
    env->ReleaseStringUTFChars(dest_file, destFile);
    return result;
}

//传入像素数组，通过libjpeg压缩生成一个本地文件
int jpegCompress(BYTE *data, uint32_t width, uint32_t height, jint quality, const char *file,
                 jboolean optimize) {
    jpeg_compress_struct jcs;
    //自定义error
    my_error_mgr jem;
    jcs.err = jpeg_std_error(&jem.pub);
    jem.pub.error_exit = my_error_exit;

    //建立setjmp返回上下文，以供my_error_exit使用
    if (setjmp(jem.set_jmp_buffer)) {
        return 0;
    }

    //为jcs分配空间并初始化
    jpeg_create_compress(&jcs);
    //打开文件
    FILE *f = fopen(file, "wb");
    if (f == NULL) {
        LOGW("file error");
        return 0;
    }
    //指定压缩数据源
    jpeg_stdio_dest(&jcs, f);
    jcs.image_width = width;
    jcs.image_height = height;

    //false代表使用Huffman算法
    jcs.arith_code = false;
    //色彩通道数（每像素采样），1代表灰度图，3代表彩色位图图像
    int nComponent = 3;
    jcs.input_components = nComponent;
    //JCS_GRAYSCALE表示灰度图，JCS_RGB代表彩色位图图像
    jcs.in_color_space = JCS_RGB;

    jpeg_set_defaults(&jcs);
    //如果设置optimize_coding为TRUE，将会使压缩图像过程中基于图像数据计算哈夫曼表
    jcs.optimize_coding = optimize;

    //为压缩设置参数，包括图像大小，颜色空间
    jpeg_set_quality(&jcs, quality, true);
    //开始压缩
    jpeg_start_compress(&jcs, true);
    int row_stride = jcs.image_width * nComponent;
    JSAMPROW row_point[1];
    //从data数组中读取rgb数据，逐行读取，一行的数据总量为image_width * 3（一个像素，有RGB 3个字节）
    while (jcs.next_scanline < jcs.image_height) {
        row_point[0] = &data[jcs.next_scanline * row_stride];
        jpeg_write_scanlines(&jcs, row_point, 1);
    }

    //压缩完成
    jpeg_finish_compress(&jcs);
    //释放资源
    jpeg_destroy_compress(&jcs);
    fclose(f);
    return 1;
}

