cmake_minimum_required(VERSION 3.18.1)

# Declares and names the project.

project("imagecompressor")

set(libs_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../../libs)
#添加lib，SHARED类型，是IMPORTED 引入的库
add_library(jpeg
        SHARED
        IMPORTED)

#设置 库的属性 里面是名称 ，属性：引入地址把我们的真实地址填写进去
set_target_properties(jpeg
        PROPERTIES IMPORTED_LOCATION
        ${libs_DIR}/${ANDROID_ABI}/libjpeg.so)

#添加lib，SHARED类型，是IMPORTED 引入的库
add_library(jpeg-turbo
        SHARED
        IMPORTED)

#设置 库的属性 里面是名称 ，属性：引入地址把我们的真实地址填写进去
set_target_properties(jpeg-turbo
        PROPERTIES IMPORTED_LOCATION
        ${libs_DIR}/${ANDROID_ABI}/libturbojpeg.so)

# 官方标准配置
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wall")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 -fno-rtti -fno-exceptions -Wall")

add_library(
        imagecompressor
        SHARED
        native-lib.cpp)

# 头文件
target_include_directories(imagecompressor PRIVATE ${libs_DIR}/${ANDROID_ABI}/include)

target_link_libraries(
        imagecompressor
        jpeg
        jpeg-turbo
        android
        jnigraphics
        log)