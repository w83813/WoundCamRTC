# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)
#set(CMAKE_C_FLAGS_DEBUG "${CMAKE_C_FLAGS_DEBUG} -std=c++11 -frtti -fexceptions -lstdc++ -O0 -ggdb -g" )
#set(CMAKE_CXX_FLAGS_DEBUG "${CMAKE_CXX_FLAGS_DEBUG} -std=c++11 -frtti -fexceptions -lstdc++ -O0 -ggdb -g" )
#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 -frtti -fexceptions -lstdc++ -O0 -ggdb -g")

set(CMAKE_C_FLAGS_DEBUG "${CMAKE_C_FLAGS_DEBUG} -std=c++11 -frtti -fexceptions -lstdc++" )
set(CMAKE_CXX_FLAGS_DEBUG "${CMAKE_CXX_FLAGS_DEBUG} -std=c++11 -frtti -fexceptions -lstdc++" )
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 -frtti -fexceptions -lstdc++")
set(OPENCV_DIR "${CMAKE_SOURCE_DIR}/openCV3470")

message(STATUS "    ANDROID_ABI: ${ANDROID_ABI}")
message(STATUS "    CMAKE_SOURCE_DIR: ${CMAKE_SOURCE_DIR}")
message(STATUS "    OPENCV_DIR: ${OpenCVDir}")

add_library(opencv_java3 SHARED IMPORTED)
set_target_properties(opencv_java3 PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/openCV3470/native/libs/${ANDROID_ABI}/libopencv_java3.so)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
        native_utils

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        app/src/main/cpp/org_jni_nativeutils.cpp
        app/src/main/cpp/Mesh3DAPI.cpp
        app/src/main/cpp/ITRITriMesh.cpp
        )


include_directories(
        app/src/main/cpp/include/
        ${OPENCV_DIR}/native/jni/include/
)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib
        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        native_utils
        opencv_java3
        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})