cmake_minimum_required(VERSION 3.4.1)



find_package(OpenCV REQUIRED)
include_directories(${OpenCV_INCLUDE_DIRS})
include_directories( ${CMAKE_SOURCE_DIR}/3rdparty/)
add_subdirectory( ${CMAKE_SOURCE_DIR}/3rdparty/aruco)
find_library( log-lib log )

add_library( calibration_jni SHARED src/main/cpp/calibration_jni.cpp )
target_link_libraries( calibration_jni ${log-lib} ${OpenCV_LIBS}  aruco)

add_library( sampleactivity_wi_jni SHARED src/main/cpp/sampleactivity_wi_jni.cpp src/main/cpp/jniactions.h)
target_link_libraries( sampleactivity_wi_jni ${log-lib} ${OpenCV_LIBS}  aruco)

add_library( arucotestactivity_jni SHARED src/main/cpp/arucotestactivity_jni.cpp)
target_link_libraries( arucotestactivity_jni ${log-lib} ${OpenCV_LIBS}  aruco)

