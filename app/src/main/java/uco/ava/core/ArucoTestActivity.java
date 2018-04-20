package uco.ava.core;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.uco.ava.appcv.MainActivityPreferencesActivity;
import com.uco.ava.appcv.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Size;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import uco.ava.calibration.CalibrationActivity;
import uco.ava.calibration.CameraCalibrations;

// OpenCV Classes

public class ArucoTestActivity extends Activity implements CvCameraViewListener2 {
    static {

        System.loadLibrary("arucotestactivity_jni");
    }
     // Used for logging success or failure messages
     private static final String TAG = "ArucoTestActivity";
    private static final String JNITAG = "JNIArucoTestActivity";

    // Loads camera view of OpenCV for us to use. This lets us see using OpenCV
    private JavaCameraView mOpenCvCameraView;

    private Toast toastMain;
    public int cameraWidth=0,cameraHeight=0;
    boolean isGoggleModeActive=false;
    ImageButton calibButton;


    // These variables are used (at the moment) to fix camera orientation from 270degree to 0degree

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public ArucoTestActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);



        setContentView(R.layout.arucotestactivity);

        //avoid screen to activate screen saver
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //getSupportActionBar().hide();


         Bundle b = getIntent().getExtras();
        if(b != null) {
            cameraWidth = b.getInt("width");
            cameraHeight = b.getInt("height");
        }
        else finishAffinity();



        jniInitNativeCalib();
        setMarkerDetectorParameters();


        mOpenCvCameraView = findViewById(R.id.show_camera_activity_java_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        if ( cameraWidth*cameraHeight<=0) finishAffinity();

        mOpenCvCameraView.setMaxFrameSize(cameraWidth,cameraHeight);
    }

    private void showMessage(String message) {
        Log.i(TAG, "called messenger");

        if (toastMain ==null)        toastMain = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        toastMain.cancel();
        toastMain = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toastMain.show();
    }

    private void setCalibration(){
        calibButton=findViewById(R.id.calibration);
        if (CameraCalibrations.isCalibrated(cameraWidth, cameraHeight)) {
            String calibration = CameraCalibrations.readCalibration(cameraWidth, cameraHeight);
            boolean res = jniSetCalibrationParams(calibration);
            Log.d("SAAMPLE", "calib =" + res);
            calibButton.setVisibility(SurfaceView.GONE);
        }
        else {
            showMessage("Uncalibrated: Calibrate for 3D features");
            calibButton.setVisibility(SurfaceView.VISIBLE);
        }
    }
    @Override
    public void onPause()
    {
        FloatingActionMenu fmenu = findViewById(R.id.floating_menu);
        fmenu.close(true);
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        setCalibration();

    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "called onBackPressed");


        super.onBackPressed();

    }


    public void onCameraViewStarted(int width, int height) {

       // mRgba = new Mat(height, width, CvType.CV_8UC4);
    }

    public void onCameraViewStopped() {
     }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        Log.d(TAG,"Image onCameraFrame");
        // TODO Auto-generated method stub
        Mat rgba=inputFrame.rgba();
        cameraWidth=rgba.width();
        cameraHeight=rgba.height();
        jniProcessCameraFrame(inputFrame.gray().getNativeObjAddr(),rgba.getNativeObjAddr(),isGoggleModeActive);
        Log.d(JNITAG,jniGetLog());
        return rgba; // This function must return
    }

    private void messenger(String message) {
        Log.i(TAG, "called messenger");

        toastMain.cancel();
        toastMain = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toastMain.show();
    }

    void onSettingsBtnClicked(View v){
        startActivityForResult(new Intent(this,uco.ava.core.ArucoPreferencesActivity.class),666);

    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==666  )
            setMarkerDetectorParameters();


    }

    void onGogglesBtnClicked(View v){
        ImageButton button= findViewById(R.id.goggles);
        isGoggleModeActive=!isGoggleModeActive;
        if (isGoggleModeActive)
            button.setImageResource(R.drawable.ic_goggles_deactived);
        else
            button.setImageResource(R.drawable.ic_goggles_active);
    }
    void onCalibrationBtnClicked(View v){
        // setCameraResolution();
        Intent intent=new Intent(this,CalibrationActivity.class);
         intent.putExtra("width",(int) cameraWidth);
        intent.putExtra("height",(int) cameraHeight);
        startActivity(intent);    }

    void setMarkerDetectorParameters(){
        ArucoPreferences.loadFromPreferences(this);
        jniSetMarkerDetectorParameters( ArucoPreferences.MarkerType,ArucoPreferences.DetectionMode,ArucoPreferences.CornerRefinement,ArucoPreferences.minmarkersize,ArucoPreferences.markersize,ArucoPreferences.detectEnclosed);

    }
    //----  JNI

    public native void jniInitNativeCalib();

    public native void jniSetMarkerDetectorParameters(String MarkerType,String DetectionMode,String CornerRefinement,float minmarkersize,float markerSize,boolean detectEnclosed );
    public native void jniProcessCameraFrame(long imgGray, long imgColor,boolean splitView4Goggles);
    public native boolean jniSetCalibrationParams(String str);
    public native String jniGetLog();






}
