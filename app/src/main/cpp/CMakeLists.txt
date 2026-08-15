cmake_minimum_required(VERSION 3.22.1)

project("orbocameraassignment")

find_package(OpenCV REQUIRED CONFIG)

add_library(
        native_image_processor
        SHARED
        native_image_processor.cpp
)

target_link_libraries(
        native_image_processor
        OpenCV::opencv_java4
        jnigraphics
)