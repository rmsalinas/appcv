#include <jni.h>
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/calib3d.hpp>
#include <aruco/aruco.h>
#include <mutex>
namespace Java_uco_ava_calibration_CalibrationActivity {
    aruco::CameraParameters
    cameraCalibrate(std::vector<std::vector<aruco::Marker> > &allMarkers, int imageWidth,
                    int imageHeight, float markerSize, float *currRepjErr);

    static std::vector<aruco::Marker> lastDetectedMarkers;
    static std::vector<std::vector<aruco::Marker> > CalibrationMarkers;
    static std::mutex detectionMutex;
    static aruco::MarkerDetector MDetector;
    static aruco::CameraParameters _CameraParameters;
    static cv::Size imageSize;
    static std::stringstream LogStream,CameraParamsStream;


};


using namespace std;
using namespace Java_uco_ava_calibration_CalibrationActivity;




extern "C" {
 JNIEXPORT void JNICALL
Java_uco_ava_calibration_CalibrationActivity_initNativeCalib( JNIEnv *env,
                                                            jobject){
lastDetectedMarkers.clear();
CalibrationMarkers.clear();
MDetector.setDetectionMode(aruco::DM_FAST,0.02);
MDetector.setDictionary(aruco::Dictionary::ARUCO_MIP_36h12);
}

JNIEXPORT void JNICALL
Java_uco_ava_calibration_CalibrationActivity_processCameraFrame(
        JNIEnv *env,
        jobject , jlong ImageGray,jlong ImageColor) {
    cv::Mat &matgray = *(cv::Mat *) ImageGray;
    cv::Mat &matcolor = *(cv::Mat *) ImageColor;

    if (matgray.type()!=CV_8UC1) return;

    std::unique_lock<std::mutex> lock(detectionMutex);

    lastDetectedMarkers=MDetector.detect(matgray);
    for(auto m:lastDetectedMarkers)
        m.draw(matcolor,cv::Scalar(0,0,255),2);
    imageSize=matgray.size();
}

JNIEXPORT jstring JNICALL
        Java_uco_ava_calibration_CalibrationActivity_addLastImage( JNIEnv *env,
                                                                 jobject){

    std::unique_lock<std::mutex> lock(detectionMutex);

    std::string msg;
    if (lastDetectedMarkers.size()==0){
        msg="Error, no markers found.";
    }
    else CalibrationMarkers.push_back(lastDetectedMarkers);
    return env->NewStringUTF(msg.c_str());

}

JNIEXPORT jint JNICALL
Java_uco_ava_calibration_CalibrationActivity_getNCalibrationImages( JNIEnv *env,
                                                         jobject) {
  return CalibrationMarkers.size();
}

JNIEXPORT void JNICALL
Java_uco_ava_calibration_CalibrationActivity_clearCalibrationVec( JNIEnv *env,jobject) {
     CalibrationMarkers.clear();
    lastDetectedMarkers.clear();
}

JNIEXPORT jobjectArray JNICALL
Java_uco_ava_calibration_CalibrationActivity_calibrate( JNIEnv *env,jobject,jfloat markersize) {

    float repjerr=0;
    _CameraParameters=cameraCalibrate(CalibrationMarkers,imageSize.width,imageSize.height,markersize,&repjerr);


    std::stringstream cp_str;
    cp_str<<_CameraParameters;
    LogStream<<_CameraParameters;

    //Se convierte el error (float) en texto
    char charcurrRepjErr[50];
    sprintf (charcurrRepjErr, "%f", repjerr);

//Se declra una estructura de 2 elementos para devolver el error y los parametros
    jobjectArray ParamYErr= env->NewObjectArray(2, env->FindClass("java/lang/String"), nullptr);


//Se introducen los valores para devolver
    const char * parameters = cp_str.str().c_str();
    env->SetObjectArrayElement(ParamYErr, 0, env->NewStringUTF(parameters));
    env->SetObjectArrayElement(ParamYErr, 1, env->NewStringUTF(charcurrRepjErr));

    return ParamYErr;

}
JNIEXPORT jstring JNICALL
Java_uco_ava_calibration_CalibrationActivity_jniGetLog(JNIEnv *env, jobject) {


    string str = LogStream.str();
    LogStream.str(std::string());
    return env->NewStringUTF(str.c_str());
}

JNIEXPORT jstring JNICALL
Java_uco_ava_calibration_CalibrationActivity_jniGetCalibratedCameraParams(JNIEnv *env, jobject) {
    CameraParamsStream.str(std::string());
    CameraParamsStream<<_CameraParameters;
    string str = CameraParamsStream.str();
    return env->NewStringUTF(str.c_str());

}


}


namespace Java_uco_ava_calibration_CalibrationActivity {

//portable calibration function
    aruco::CameraParameters
    cameraCalibrate(std::vector<std::vector<aruco::Marker> > &allMarkers, int imageWidth,
                    int imageHeight, float markerSize, float *currRepjErr) {


        unsigned char default_a4_board[] = {
                0x30, 0x20, 0x32, 0x34, 0x20, 0x31, 0x36, 0x31, 0x20, 0x34, 0x20, 0x2d,
                0x31, 0x30, 0x30, 0x30, 0x20, 0x2d, 0x31, 0x30, 0x30, 0x30, 0x20, 0x30,
                0x20, 0x2d, 0x35, 0x30, 0x30, 0x20, 0x2d, 0x31, 0x30, 0x30, 0x30, 0x20,
                0x30, 0x20, 0x2d, 0x35, 0x30, 0x30, 0x20, 0x2d, 0x31, 0x35, 0x30, 0x30,
                0x20, 0x30, 0x20, 0x2d, 0x31, 0x30, 0x30, 0x30, 0x20, 0x2d, 0x31, 0x35,
                0x30, 0x30, 0x20, 0x30, 0x20, 0x32, 0x32, 0x37, 0x20, 0x34, 0x20, 0x2d,
                0x34, 0x30, 0x30, 0x20, 0x2d, 0x31, 0x30, 0x30, 0x30, 0x20, 0x30, 0x20,
                0x31, 0x30, 0x30, 0x20, 0x2d, 0x31, 0x30, 0x30, 0x30, 0x20, 0x30, 0x20,
                0x31, 0x30, 0x30, 0x20, 0x2d, 0x31, 0x35, 0x30, 0x30, 0x20, 0x30, 0x20,
                0x2d, 0x34, 0x30, 0x30, 0x20, 0x2d, 0x31, 0x35, 0x30, 0x30, 0x20, 0x30,
                0x20, 0x38, 0x35, 0x20, 0x34, 0x20, 0x32, 0x30, 0x30, 0x20, 0x2d, 0x31,
                0x30, 0x30, 0x30, 0x20, 0x30, 0x20, 0x37, 0x30, 0x30, 0x20, 0x2d, 0x31,
                0x30, 0x30, 0x30, 0x20, 0x30, 0x20, 0x37, 0x30, 0x30, 0x20, 0x2d, 0x31,
                0x35, 0x30, 0x30, 0x20, 0x30, 0x20, 0x32, 0x30, 0x30, 0x20, 0x2d, 0x31,
                0x35, 0x30, 0x30, 0x20, 0x30, 0x20, 0x31, 0x36, 0x36, 0x20, 0x34, 0x20,
                0x38, 0x30, 0x30, 0x20, 0x2d, 0x31, 0x30, 0x30, 0x30, 0x20, 0x30, 0x20,
                0x31, 0x33, 0x30, 0x30, 0x20, 0x2d, 0x31, 0x30, 0x30, 0x30, 0x20, 0x30,
                0x20, 0x31, 0x33, 0x30, 0x30, 0x20, 0x2d, 0x31, 0x35, 0x30, 0x30, 0x20,
                0x30, 0x20, 0x38, 0x30, 0x30, 0x20, 0x2d, 0x31, 0x35, 0x30, 0x30, 0x20,
                0x30, 0x20, 0x32, 0x34, 0x34, 0x20, 0x34, 0x20, 0x2d, 0x31, 0x30, 0x30,
                0x30, 0x20, 0x2d, 0x34, 0x30, 0x30, 0x20, 0x30, 0x20, 0x2d, 0x35, 0x30,
                0x30, 0x20, 0x2d, 0x34, 0x30, 0x30, 0x20, 0x30, 0x20, 0x2d, 0x35, 0x30,
                0x30, 0x20, 0x2d, 0x39, 0x30, 0x30, 0x20, 0x30, 0x20, 0x2d, 0x31, 0x30,
                0x30, 0x30, 0x20, 0x2d, 0x39, 0x30, 0x30, 0x20, 0x30, 0x20, 0x31, 0x34,
                0x34, 0x20, 0x34, 0x20, 0x2d, 0x34, 0x30, 0x30, 0x20, 0x2d, 0x34, 0x30,
                0x30, 0x20, 0x30, 0x20, 0x31, 0x30, 0x30, 0x20, 0x2d, 0x34, 0x30, 0x30,
                0x20, 0x30, 0x20, 0x31, 0x30, 0x30, 0x20, 0x2d, 0x39, 0x30, 0x30, 0x20,
                0x30, 0x20, 0x2d, 0x34, 0x30, 0x30, 0x20, 0x2d, 0x39, 0x30, 0x30, 0x20,
                0x30, 0x20, 0x39, 0x30, 0x20, 0x34, 0x20, 0x32, 0x30, 0x30, 0x20, 0x2d,
                0x34, 0x30, 0x30, 0x20, 0x30, 0x20, 0x37, 0x30, 0x30, 0x20, 0x2d, 0x34,
                0x30, 0x30, 0x20, 0x30, 0x20, 0x37, 0x30, 0x30, 0x20, 0x2d, 0x39, 0x30,
                0x30, 0x20, 0x30, 0x20, 0x32, 0x30, 0x30, 0x20, 0x2d, 0x39, 0x30, 0x30,
                0x20, 0x30, 0x20, 0x32, 0x31, 0x34, 0x20, 0x34, 0x20, 0x38, 0x30, 0x30,
                0x20, 0x2d, 0x34, 0x30, 0x30, 0x20, 0x30, 0x20, 0x31, 0x33, 0x30, 0x30,
                0x20, 0x2d, 0x34, 0x30, 0x30, 0x20, 0x30, 0x20, 0x31, 0x33, 0x30, 0x30,
                0x20, 0x2d, 0x39, 0x30, 0x30, 0x20, 0x30, 0x20, 0x38, 0x30, 0x30, 0x20,
                0x2d, 0x39, 0x30, 0x30, 0x20, 0x30, 0x20, 0x31, 0x35, 0x33, 0x20, 0x34,
                0x20, 0x2d, 0x31, 0x30, 0x30, 0x30, 0x20, 0x32, 0x30, 0x30, 0x20, 0x30,
                0x20, 0x2d, 0x35, 0x30, 0x30, 0x20, 0x32, 0x30, 0x30, 0x20, 0x30, 0x20,
                0x2d, 0x35, 0x30, 0x30, 0x20, 0x2d, 0x33, 0x30, 0x30, 0x20, 0x30, 0x20,
                0x2d, 0x31, 0x30, 0x30, 0x30, 0x20, 0x2d, 0x33, 0x30, 0x30, 0x20, 0x30,
                0x20, 0x37, 0x20, 0x34, 0x20, 0x2d, 0x34, 0x30, 0x30, 0x20, 0x32, 0x30,
                0x30, 0x20, 0x30, 0x20, 0x31, 0x30, 0x30, 0x20, 0x32, 0x30, 0x30, 0x20,
                0x30, 0x20, 0x31, 0x30, 0x30, 0x20, 0x2d, 0x33, 0x30, 0x30, 0x20, 0x30,
                0x20, 0x2d, 0x34, 0x30, 0x30, 0x20, 0x2d, 0x33, 0x30, 0x30, 0x20, 0x30,
                0x20, 0x31, 0x34, 0x33, 0x20, 0x34, 0x20, 0x32, 0x30, 0x30, 0x20, 0x32,
                0x30, 0x30, 0x20, 0x30, 0x20, 0x37, 0x30, 0x30, 0x20, 0x32, 0x30, 0x30,
                0x20, 0x30, 0x20, 0x37, 0x30, 0x30, 0x20, 0x2d, 0x33, 0x30, 0x30, 0x20,
                0x30, 0x20, 0x32, 0x30, 0x30, 0x20, 0x2d, 0x33, 0x30, 0x30, 0x20, 0x30,
                0x20, 0x32, 0x31, 0x39, 0x20, 0x34, 0x20, 0x38, 0x30, 0x30, 0x20, 0x32,
                0x30, 0x30, 0x20, 0x30, 0x20, 0x31, 0x33, 0x30, 0x30, 0x20, 0x32, 0x30,
                0x30, 0x20, 0x30, 0x20, 0x31, 0x33, 0x30, 0x30, 0x20, 0x2d, 0x33, 0x30,
                0x30, 0x20, 0x30, 0x20, 0x38, 0x30, 0x30, 0x20, 0x2d, 0x33, 0x30, 0x30,
                0x20, 0x30, 0x20, 0x37, 0x38, 0x20, 0x34, 0x20, 0x2d, 0x31, 0x30, 0x30,
                0x30, 0x20, 0x38, 0x30, 0x30, 0x20, 0x30, 0x20, 0x2d, 0x35, 0x30, 0x30,
                0x20, 0x38, 0x30, 0x30, 0x20, 0x30, 0x20, 0x2d, 0x35, 0x30, 0x30, 0x20,
                0x33, 0x30, 0x30, 0x20, 0x30, 0x20, 0x2d, 0x31, 0x30, 0x30, 0x30, 0x20,
                0x33, 0x30, 0x30, 0x20, 0x30, 0x20, 0x31, 0x35, 0x39, 0x20, 0x34, 0x20,
                0x2d, 0x34, 0x30, 0x30, 0x20, 0x38, 0x30, 0x30, 0x20, 0x30, 0x20, 0x31,
                0x30, 0x30, 0x20, 0x38, 0x30, 0x30, 0x20, 0x30, 0x20, 0x31, 0x30, 0x30,
                0x20, 0x33, 0x30, 0x30, 0x20, 0x30, 0x20, 0x2d, 0x34, 0x30, 0x30, 0x20,
                0x33, 0x30, 0x30, 0x20, 0x30, 0x20, 0x32, 0x30, 0x39, 0x20, 0x34, 0x20,
                0x32, 0x30, 0x30, 0x20, 0x38, 0x30, 0x30, 0x20, 0x30, 0x20, 0x37, 0x30,
                0x30, 0x20, 0x38, 0x30, 0x30, 0x20, 0x30, 0x20, 0x37, 0x30, 0x30, 0x20,
                0x33, 0x30, 0x30, 0x20, 0x30, 0x20, 0x32, 0x30, 0x30, 0x20, 0x33, 0x30,
                0x30, 0x20, 0x30, 0x20, 0x31, 0x33, 0x20, 0x34, 0x20, 0x38, 0x30, 0x30,
                0x20, 0x38, 0x30, 0x30, 0x20, 0x30, 0x20, 0x31, 0x33, 0x30, 0x30, 0x20,
                0x38, 0x30, 0x30, 0x20, 0x30, 0x20, 0x31, 0x33, 0x30, 0x30, 0x20, 0x33,
                0x30, 0x30, 0x20, 0x30, 0x20, 0x38, 0x30, 0x30, 0x20, 0x33, 0x30, 0x30,
                0x20, 0x30, 0x20, 0x32, 0x34, 0x37, 0x20, 0x34, 0x20, 0x2d, 0x31, 0x30,
                0x30, 0x30, 0x20, 0x31, 0x34, 0x30, 0x30, 0x20, 0x30, 0x20, 0x2d, 0x35,
                0x30, 0x30, 0x20, 0x31, 0x34, 0x30, 0x30, 0x20, 0x30, 0x20, 0x2d, 0x35,
                0x30, 0x30, 0x20, 0x39, 0x30, 0x30, 0x20, 0x30, 0x20, 0x2d, 0x31, 0x30,
                0x30, 0x30, 0x20, 0x39, 0x30, 0x30, 0x20, 0x30, 0x20, 0x32, 0x33, 0x37,
                0x20, 0x34, 0x20, 0x2d, 0x34, 0x30, 0x30, 0x20, 0x31, 0x34, 0x30, 0x30,
                0x20, 0x30, 0x20, 0x31, 0x30, 0x30, 0x20, 0x31, 0x34, 0x30, 0x30, 0x20,
                0x30, 0x20, 0x31, 0x30, 0x30, 0x20, 0x39, 0x30, 0x30, 0x20, 0x30, 0x20,
                0x2d, 0x34, 0x30, 0x30, 0x20, 0x39, 0x30, 0x30, 0x20, 0x30, 0x20, 0x31,
                0x30, 0x30, 0x20, 0x34, 0x20, 0x32, 0x30, 0x30, 0x20, 0x31, 0x34, 0x30,
                0x30, 0x20, 0x30, 0x20, 0x37, 0x30, 0x30, 0x20, 0x31, 0x34, 0x30, 0x30,
                0x20, 0x30, 0x20, 0x37, 0x30, 0x30, 0x20, 0x39, 0x30, 0x30, 0x20, 0x30,
                0x20, 0x32, 0x30, 0x30, 0x20, 0x39, 0x30, 0x30, 0x20, 0x30, 0x20, 0x36,
                0x20, 0x34, 0x20, 0x38, 0x30, 0x30, 0x20, 0x31, 0x34, 0x30, 0x30, 0x20,
                0x30, 0x20, 0x31, 0x33, 0x30, 0x30, 0x20, 0x31, 0x34, 0x30, 0x30, 0x20,
                0x30, 0x20, 0x31, 0x33, 0x30, 0x30, 0x20, 0x39, 0x30, 0x30, 0x20, 0x30,
                0x20, 0x38, 0x30, 0x30, 0x20, 0x39, 0x30, 0x30, 0x20, 0x30, 0x20, 0x31,
                0x37, 0x37, 0x20, 0x34, 0x20, 0x2d, 0x31, 0x30, 0x30, 0x30, 0x20, 0x32,
                0x30, 0x30, 0x30, 0x20, 0x30, 0x20, 0x2d, 0x35, 0x30, 0x30, 0x20, 0x32,
                0x30, 0x30, 0x30, 0x20, 0x30, 0x20, 0x2d, 0x35, 0x30, 0x30, 0x20, 0x31,
                0x35, 0x30, 0x30, 0x20, 0x30, 0x20, 0x2d, 0x31, 0x30, 0x30, 0x30, 0x20,
                0x31, 0x35, 0x30, 0x30, 0x20, 0x30, 0x20, 0x39, 0x33, 0x20, 0x34, 0x20,
                0x2d, 0x34, 0x30, 0x30, 0x20, 0x32, 0x30, 0x30, 0x30, 0x20, 0x30, 0x20,
                0x31, 0x30, 0x30, 0x20, 0x32, 0x30, 0x30, 0x30, 0x20, 0x30, 0x20, 0x31,
                0x30, 0x30, 0x20, 0x31, 0x35, 0x30, 0x30, 0x20, 0x30, 0x20, 0x2d, 0x34,
                0x30, 0x30, 0x20, 0x31, 0x35, 0x30, 0x30, 0x20, 0x30, 0x20, 0x38, 0x36,
                0x20, 0x34, 0x20, 0x32, 0x30, 0x30, 0x20, 0x32, 0x30, 0x30, 0x30, 0x20,
                0x30, 0x20, 0x37, 0x30, 0x30, 0x20, 0x32, 0x30, 0x30, 0x30, 0x20, 0x30,
                0x20, 0x37, 0x30, 0x30, 0x20, 0x31, 0x35, 0x30, 0x30, 0x20, 0x30, 0x20,
                0x32, 0x30, 0x30, 0x20, 0x31, 0x35, 0x30, 0x30, 0x20, 0x30, 0x20, 0x32,
                0x32, 0x39, 0x20, 0x34, 0x20, 0x38, 0x30, 0x30, 0x20, 0x32, 0x30, 0x30,
                0x30, 0x20, 0x30, 0x20, 0x31, 0x33, 0x30, 0x30, 0x20, 0x32, 0x30, 0x30,
                0x30, 0x20, 0x30, 0x20, 0x31, 0x33, 0x30, 0x30, 0x20, 0x31, 0x35, 0x30,
                0x30, 0x20, 0x30, 0x20, 0x38, 0x30, 0x30, 0x20, 0x31, 0x35, 0x30, 0x30,
                0x20, 0x30, 0x20, 0x41, 0x52, 0x55, 0x43, 0x4f, 0x5f, 0x4d, 0x49, 0x50,
                0x5f, 0x33, 0x36, 0x68, 0x31, 0x32
        };
        unsigned int default_a4_board_size = 1254;
// given the set of markers detected, the function determines the get the 2d-3d correspondes
        auto getMarker2d_3d_ = [](vector<cv::Point2f> &p2d, vector<cv::Point3f> &p3d,
                                  const vector<aruco::Marker> &markers_detected,
                                  const aruco::MarkerMap &bc) {
            p2d.clear();
            p3d.clear();
// for each detected marker
            for (size_t i = 0; i < markers_detected.size(); i++) {
// find it in the bc
                auto fidx = std::string::npos;
                for (size_t j = 0; j < bc.size() && fidx == std::string::npos; j++)
                    if (bc[j].id == markers_detected[i].id)
                        fidx = j;
                if (fidx != std::string::npos) {
                    for (int j = 0; j < 4; j++) {
                        p2d.push_back(markers_detected[i][j]);
                        p3d.push_back(bc[fidx][j]);
                    }
                }
            }
        };

        aruco::MarkerMap mmap;
        stringstream sstr;
        sstr.write((char *) default_a4_board, default_a4_board_size);
        mmap.fromStream(sstr);
        if (!mmap.isExpressedInMeters())
            mmap = mmap.convertToMeters(static_cast<float>( markerSize));


        vector<vector<cv::Point2f> > calib_p2d;
        vector<vector<cv::Point3f> > calib_p3d;

        for (auto &detected_markers:allMarkers) {
            vector<cv::Point2f> p2d;
            vector<cv::Point3f> p3d;

            getMarker2d_3d_(p2d, p3d, detected_markers, mmap);
            if (p3d.size() > 0) {
                calib_p2d.push_back(p2d);
                calib_p3d.push_back(p3d);
            }
        }

        vector<cv::Mat> vr, vt;
        aruco::CameraParameters cameraParams;
        cameraParams.CamSize = cv::Size(imageWidth, imageHeight);
        float err = cv::calibrateCamera(calib_p3d, calib_p2d, cameraParams.CamSize,
                                        cameraParams.CameraMatrix, cameraParams.Distorsion, vr, vt);
        cameraParams.CameraMatrix.convertTo(cameraParams.CameraMatrix, CV_32F);
        cameraParams.Distorsion.convertTo(cameraParams.Distorsion, CV_32F);
        if (currRepjErr != 0) *currRepjErr = err;

        stringstream str;
        str << cameraParams;
        str << " cm=" << cameraParams.CameraMatrix;
        //  __android_log_write(ANDROID_LOG_INFO, "processDebug calibFunction", str.str().c_str());
        return cameraParams;
    }
};