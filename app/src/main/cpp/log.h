#define TAG "ImageCompressor"
#include <android/log.h>

#define LOGI(...) \
__android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)

#define LOGD(...) \
__android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)

#define LOGW(...) \
__android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)