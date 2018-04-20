
#include <jni.h>
#include "jniactions.h"
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/calib3d.hpp>
#include <aruco/aruco.h>
#include <set>
#include <mutex>
namespace Java_uco_ava_core_SampleActivityWithInteraction{
   cv::Mat cameraimage;
    static aruco::MarkerDetector MDetector;
    cv::Mat grayImage;
    static std::vector<aruco::Marker> lastDetectedMarkers;
    static std::mutex detectionMutex;
    std::set<int> selectedMarkers;
    std::vector<cv::Point2f> touchedPositions;
    aruco::CameraParameters cameraParameters;

};
using namespace std;
using namespace Java_uco_ava_core_SampleActivityWithInteraction;
using namespace JniActions;
extern "C" {
JNIEXPORT void JNICALL
Java_uco_ava_core_SampleActivityWithInteraction_initNativeCalib( JNIEnv *env,
                                                            jobject){
    MDetector.setDetectionMode(aruco::DM_FAST,0.02);
    MDetector.setDictionary(aruco::Dictionary::ARUCO_MIP_36h12);
    cameraimage=cv::Mat();
    grayImage=cv::Mat();
    lastDetectedMarkers.clear();
    selectedMarkers.clear();
    touchedPositions.clear();
    cameraParameters.clear();
}



JNIEXPORT jboolean JNICALL
Java_uco_ava_core_SampleActivityWithInteraction_setCalibrationParams( JNIEnv *env,
                                                   jobject,jstring calibparams) {

    auto ConvertJString=[](JNIEnv* env, jstring str)
    {
        const jsize len = env->GetStringUTFLength(str);
        const char* strChars = env->GetStringUTFChars(str, (jboolean *)0);

        std::string Result(strChars, len);
        env->ReleaseStringUTFChars(str, strChars);

        return Result;
    };

    stringstream sstr;sstr<<ConvertJString(env, calibparams);
    sstr>>cameraParameters ;
    return cameraParameters.isValid();


}

JNIEXPORT jboolean JNICALL
Java_uco_ava_core_SampleActivityWithInteraction_onNewCameraFrame( JNIEnv *env,
jobject,jlong image){
    cv::Mat &matcolor = *(cv::Mat *) image;

    matcolor.copyTo(cameraimage);
    cv::cvtColor(matcolor,grayImage,CV_BGRA2GRAY);

    std::unique_lock<std::mutex> lock(detectionMutex);
    lastDetectedMarkers=MDetector.detect(grayImage,cameraParameters,1);

    return true;
}



JNIEXPORT void JNICALL
Java_uco_ava_core_SampleActivityWithInteraction_drawDisplayableImage( JNIEnv *env,
                                                   jobject,jlong image) {
    std::unique_lock<std::mutex> lock(detectionMutex);

    cv::Mat &matcolor = *(cv::Mat *) image;
    cameraimage.copyTo(matcolor);

    for (auto m:lastDetectedMarkers) {
        if (selectedMarkers.count(m.id))
            m.draw(matcolor, cv::Scalar(255,0, 0 ), 6);
        else
            m.draw(matcolor, cv::Scalar(0, 0, 255), 2);
    }
    if (cameraParameters.isValid()){
        for (auto m:lastDetectedMarkers)
            aruco::CvDrawingUtils::draw3dCube(matcolor,m,cameraParameters);
    }

    for(auto p:touchedPositions){
        cv::rectangle(matcolor,p-cv::Point2f(4,4),p+cv::Point2f(4,4),cv::Scalar(0,255,0),1);
    }

}
JNIEXPORT jboolean JNICALL
Java_uco_ava_core_SampleActivityWithInteraction_onTouchEvent( JNIEnv *env,
                                                       jobject,jint action,jfloat x0,jfloat y0,jfloat x1,jfloat y1,jfloat x2,jfloat y2){



    auto isIntoMarker=[](int x,int y,const aruco::Marker &m){

        return cv::pointPolygonTest(m,cv::Point2f(x,y),false);
     };

    if( x1>0) return false;//only one finger allowed

    std::unique_lock<std::mutex> lock(detectionMutex);

    switch(action){

        case  MotionEvent::ACTION_DOWN:{

            touchedPositions.push_back(cv::Point2f(x0,y0));
/*            for(auto m:lastDetectedMarkers){
                if ( isIntoMarker(x0,y0,m)){
                    if (selectedMarkers.count(m.id)!=0) selectedMarkers.erase(m.id);
                    else selectedMarkers.insert(m.id);
                }
            }
            */
        }break;

    };


    return false;
}


}
