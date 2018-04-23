package com.uco.ava.appcv;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import com.github.clans.fab.FloatingActionMenu;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Size;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uco.ava.calibration.CalibrationActivity;
import uco.ava.calibration.CameraCalibrations;

// OpenCV Classes

public class MainActivity extends AppCompatActivity implements CvCameraViewListener2 {

    // Used for logging success or failure messages
    private static final String TAG = "OCVSample::Activity";


    public static List<android.hardware.Camera.Size> CameraResolutions;

    private final int CAMERAPERMISSIONREQUESTCODE = 1;

    // Loads camera view of OpenCV for us to use. This lets us see using OpenCV
    private JavaCameraView mOpenCvCameraView;
    boolean isCameraOpened = false;


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };


    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }


    private void askForPermissions() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERAPERMISSIONREQUESTCODE);

        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        CameraCalibrations.setContext(this);
        askForPermissions();
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        //avoid screen to activate screen saver
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mOpenCvCameraView = findViewById(R.id.show_camera_activity_java_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        List<Size> resolutions = getKnownCameraResolution();
        Log.d("RESOLUTIONS", "SIZE=" + resolutions.size());
        int cameraResolutionIndex = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(this).getString("resolution", "0"));
        Log.d("RESOLUTIONS", "cameraResolutionIndex=" + cameraResolutionIndex);
        for (int i = 0; i < resolutions.size(); i++)
            Log.d("RESOLUTIONS", "XX=" + resolutions.get(i).width + "x" + resolutions.get(i).height);
        if (resolutions.size() != 0) {
            mOpenCvCameraView.setMaxFrameSize((int) resolutions.get(cameraResolutionIndex).width, (int) resolutions.get(cameraResolutionIndex).height);
        }
    }

    public void updateCameraResolution() {
        Size size = getSelectedCameraSize();
        mOpenCvCameraView.setMaxFrameSize((int) size.width, (int) size.height);
        mOpenCvCameraView.disableView();
        mOpenCvCameraView.enableView();

    }

    @Override
    public void onPause() {
        FloatingActionMenu fmenu = findViewById(R.id.floating_menu);
        fmenu.close(true);
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {

        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        if (isCameraOpened) updateCameraResolution();
        //makes the window in inmersive mode
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {

        CameraResolutions = mOpenCvCameraView.getResolutions();
        if (CameraResolutions.size() != 0) {
            String resolutionSizes = "";
            for (int i = 0; i < CameraResolutions.size(); i++) {
                resolutionSizes = resolutionSizes + String.valueOf(CameraResolutions.get(i).width) + " " + String.valueOf(CameraResolutions.get(i).height);
                if (i != CameraResolutions.size() - 1)
                    resolutionSizes = resolutionSizes + ",";
            }
            if (PreferenceManager.getDefaultSharedPreferences(this).getString("CameraResolutions", "").length() == 0) {
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
                editor.putString("CameraResolutions", resolutionSizes);
                editor.commit();
            }
        }
//        updateCameraResolution();
        isCameraOpened = true;

    }



    public void onCameraViewStopped() {
         isCameraOpened=false;
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        return inputFrame.rgba();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==666 ){
            updateCameraResolution();
        }
    }

    public void onPreferencesBtnClicked(View v){
          startActivityForResult(new Intent(this,MainActivityPreferencesActivity.class),666);
    }



    public void onArucoTestBtnClicked(View v){
        Intent intent=new Intent(this,uco.ava.core.ArucoTestActivity.class);
        Size camS=getSelectedCameraSize();
        intent.putExtra("width",(int) camS.width);
        intent.putExtra("height",(int) camS.height);
        startActivity(intent);
        }
    //-----------------------------------------
    public void onCalibBtnClicked(View v){
       // setCameraResolution();
        Intent intent=new Intent(this,CalibrationActivity.class);
        Size camS=getSelectedCameraSize();
        intent.putExtra("width",(int) camS.width);
        intent.putExtra("height",(int) camS.height);
        startActivity(intent);
    }
    public void onSampleActivityWithInteractionBtnClicked(View v){
        Intent intent=new Intent(this,uco.ava.core.SampleActivityWithInteraction.class);
        Size camS=getSelectedCameraSize();
        intent.putExtra("width",(int) camS.width);
        intent.putExtra("height",(int) camS.height);
        startActivity(intent);
    }
    List <Size> getKnownCameraResolution( ){
        String Resolutions=PreferenceManager.getDefaultSharedPreferences(this).getString("CameraResolutions","");
        List <Size> Result=new  ArrayList <Size>();
        if( Resolutions.length()==0) return Result;

        List<String> list = new ArrayList<String>(Arrays.asList( Resolutions.split(",")));
        for(int i=0;i<list.size();i++){
            List<String> wh= new ArrayList<String>(Arrays.asList( list.get(i).split(" ")));
            Size cs= new Size() ;
            cs.width=Integer.valueOf(wh.get(0));
            cs.height=Integer.valueOf(wh.get(1));
            Result.add(cs);
        }
        return Result;
    }

    private Size getSelectedCameraSize(){
        List<Size> resolutions=getKnownCameraResolution();
        int cameraResolutionIndex=Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(this).getString("resolution","0"));
        Size s=new Size();
        s.width=(int)resolutions.get(cameraResolutionIndex).width;
        s.height=(int)resolutions.get(cameraResolutionIndex).height;
        return s;
    }


}
