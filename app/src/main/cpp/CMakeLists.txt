cmake_minimum_required(VERSION 3.22.1)

project("orbocameraassignment")

add_library(
        native_image_processor
        SHARED
        native_image_processor.cpp
)