# 图片压缩
- 质量压缩
- 尺寸压缩
- Native压缩

## libjpeg
libjpeg是一个完全用C语言编写的库，包含了被广泛使用的JPEG解码、JPEG编码和其他的JPEG功能的实现。

## libjpeg-turbo(速度是libjpeg的2-6倍)
libjpeg-turbo图像编解码器，使用SIMD指令（MMX, SSE2, NEON, AltiVec）来加速x86, x86-64, ARM和PowerPC系统上的JPEG压缩和解压缩。在这样的系统上， libjpeg-turbo的速度通常是libjpeg的2-6倍，其他条件相同。在其他类型的系统上，凭借其高度优化的哈夫曼编码， libjpeg-turbo仍然可以大大超过libjpeg。在许多情况下，libjpeg-turbo的性能可与专有的高速JPEG编解码器相媲美。

> 1995年JPEG图片处理引擎最初用于PC。2005年，为了方便浏览器的使用，基于JPEG引擎开发了skia引擎。2007年安卓用的skia引擎，但去掉了哈夫曼编码算法，采用定长编码算法，但解码还是保留了哈夫曼算法，但会导致图片处理后文件变大了。
> 早期由于CPU和内存在手机上都非常有限，而哈夫曼算法非常耗CPU资源，所以谷歌被迫选择了其他算法。我们可以绕过安卓Bitmap API层，来自己编码实现——修复使用哈夫曼算法。

### 微信为什么使用libjpeg压缩？
- 兼容低版本
- 跨平台算法复用

## 图片压缩流程
1. Bitmap对象 ——AndroidBitmap_lockPixels——>
2. RGBA_8888像素数组（每个像素4个字节） ——二进制运算——>
3. RGB像素数组（每个像素3个字节） ——写入——>
4. libjpeg引擎压缩 ——持久化——>
5. 磁盘文件