package uco.ava.calibration;


import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.uco.ava.appcv.BuildConfig;
import com.uco.ava.appcv.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

// OpenCV Classes

public class CalibrationActivity extends AppCompatActivity implements CvCameraViewListener2 {
    static {

        System.loadLibrary("calibration_jni");
    }
    // Used for logging success or failure messages
    private static final String TAG = "OCVSample::Activity";

    // Loads camera view of OpenCV for us to use. This lets us see using OpenCV
    private JavaCameraView mOpenCvCameraView;

    private Toast toastMain;
    private Boolean backButtonPressedOnce =false;
    private Boolean isCalibrating=false;
    public int cameraWidth,cameraHeight;
    private FloatingActionButton ShareCalibBtn;


    final int STORAGEPERMREQUEST_SHARECALIBRESULT =100;
    final int STORAGEPERMREQUEST_SHARECALIBPATTERN =200;
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

    public CalibrationActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);



        setContentView(R.layout.calibration);

        //getSupportActionBar().hide();

        toastMain = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        int width= -1,height=-1; // or other values
        Bundle b = getIntent().getExtras();
        if(b != null) {
            width = b.getInt("width");
            height = b.getInt("height");
        }

        initNativeCalib();
        mOpenCvCameraView = findViewById(R.id.show_camera_activity_java_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        if ( width*height>0)
            mOpenCvCameraView.setMaxFrameSize(width,height);
        ShareCalibBtn=findViewById(R.id.share_cal_fbtn);
        if(CameraCalibrations.isCalibrated(width,height))
            ShareCalibBtn.setEnabled(true);
        else ShareCalibBtn.setEnabled(false);
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
        //makes the window in inmersive mode
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "called onBackPressed");


        if (getNCalibrationImages()==0) super.onBackPressed();
        else         messenger("Press again to stop the calibration");

        if (backButtonPressedOnce) {
            toastMain.cancel();
            super.onBackPressed();
        }


        this.backButtonPressedOnce = true;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                toastMain.cancel();
                backButtonPressedOnce = false;
            }
        }, 2000);
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
        if(!isCalibrating) processCameraFrame(inputFrame.gray().getNativeObjAddr(),rgba.getNativeObjAddr());
        return rgba; // This function must return
    }

    private void messenger(String message) {
        Log.i(TAG, "called messenger");

        toastMain.cancel();
        toastMain = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toastMain.show();
    }


    public void onClick_addImageBtn(View v){
        String str = addLastImage();
        if (!str.isEmpty())
            messenger(str);

        String number=String.valueOf(getNCalibrationImages());
        if(number.length()==1) number="0"+number;
        ((Button) findViewById(R.id.count_bubble)).setText(number);

    }

    public void askUserMarkerSize(float defValue){

        final EditText markerSizeDialog = new EditText(this);
        markerSizeDialog.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        markerSizeDialog.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        markerSizeDialog.setText(String.valueOf(defValue));


        final float[] res=new float[1];
        res[0]=defValue;
        android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(this).create();
        alertDialog.setTitle("Marker size");
        alertDialog.setMessage("Enter the marker size");
        alertDialog.setView(markerSizeDialog);
        alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Value of EditText

                float userMarkerSize= Float.valueOf(markerSizeDialog.getText().toString());
                if (userMarkerSize<=0)askUserMarkerSize(res[0]);
                else {
                    PreferenceManager.getDefaultSharedPreferences(CalibrationActivity.this).edit().putFloat("Calibration:MarkerSize", res[0]);
                    calibrateFunction();
                }
            }
        });
        alertDialog.setButton(android.app.AlertDialog.BUTTON_NEGATIVE, "Cancel",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Value of EditText
                        res[0]= -1;
                    }}
        );
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        alertDialog.show();
        //Set the dialog to immersive
        alertDialog.getWindow().getDecorView().setSystemUiVisibility(this.getWindow().getDecorView().getSystemUiVisibility());
        //Clear the not focusable flag from the window
        alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }

    public void onClick_CalibrationBtn(View v){


        if(getNCalibrationImages() < 5) {
            messenger("At least 5 images needed");
            return;
        }


        Float markerSize = PreferenceManager.getDefaultSharedPreferences(this).getFloat("Calibration:MarkerSize",1);

        askUserMarkerSize(markerSize);

    }


    public void calibrateFunction(){
        Float markerSize = PreferenceManager.getDefaultSharedPreferences(this).getFloat("Calibration:MarkerSize",1);
        if (markerSize.floatValue()<=0) return;


        isCalibrating=true;
        String[] result = calibrate(markerSize.floatValue());
        isCalibrating=false;
        messenger(result[1]);
        String log=jniGetLog();
        Log.d("CAL",log);
        String cameraParams=jniGetCalibratedCameraParams();
        CameraCalibrations.saveCalibration(cameraParams,cameraWidth,cameraHeight);
        clearCalibrationVec();
        String number=String.valueOf(getNCalibrationImages());
        if(number.length()==1) number="0"+number;
        ((Button) findViewById(R.id.count_bubble)).setText(number);

        if(CameraCalibrations.isCalibrated(cameraWidth,cameraHeight))
            ShareCalibBtn.setEnabled(true);
    }

    public void sendCalibrationFile(){
        String fileContents = CameraCalibrations.readCalibration(cameraWidth,cameraHeight);
        if (fileContents.length()==0) return;


        String filename = "calibration_"+cameraWidth+"x"+cameraHeight+".yml";
        String calibDirPath = Environment.getExternalStorageDirectory() + File.separator + "AVA"+ File.separator +"CameraParameters";
        File calibDirPathFile = new File(calibDirPath);
        if (!calibDirPathFile.exists())
            calibDirPathFile.mkdirs();
        String fullpath=calibDirPath+File.separator+filename;




        try {
            FileOutputStream outputStream = new FileOutputStream(fullpath);
            outputStream.write(fileContents.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //   InputStream inputStream = openFileInput(urifile);


        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent .setType("vnd.android.cursor.dir/email");

        // the attachment
        Uri urifilePath = Uri.parse("file://"+fullpath);

        emailIntent.putExtra(Intent.EXTRA_STREAM,urifilePath);
        // the mail subject
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Aruco camera parameters");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Latest Aruco parameters file attached");

        startActivity(Intent.createChooser(emailIntent , "Send email..."));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        // getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        switch (requestCode) {
            case STORAGEPERMREQUEST_SHARECALIBRESULT: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    sendCalibrationFile();
                }
                return;
            }

            case STORAGEPERMREQUEST_SHARECALIBPATTERN: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    sendCalibrationPatternFile();
                }
                return;
            }
        }

    }

    public void onShareBtnClicked(View v){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    STORAGEPERMREQUEST_SHARECALIBRESULT);
        }
        else
            sendCalibrationFile();
    }

    public void onShareCalibPatternBtnClicked(View v){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    STORAGEPERMREQUEST_SHARECALIBPATTERN);
        }
        else
            sendCalibrationPatternFile();
    }

    void sendCalibrationPatternFile(){

        //Create dir to path
        String filesPath = Environment.getExternalStorageDirectory() + File.separator + "AVA/Files";
        File filesDir = new File(filesPath);
        if (!filesDir.exists())
            filesDir.mkdirs();
        //save the pattern to the path

        String fullFilePath=filesPath + File.separator + "aruco_calibration_pattern.pdf";
        try {
            InputStream in = getResources().openRawResource(R.raw.calibration_grid);
            FileOutputStream out = null;
            out = new FileOutputStream(fullFilePath);
            byte[] buff = new byte[1024];
            int read = 0;
            try {
                while ((read = in.read(buff)) > 0) {
                    out.write(buff, 0, read);
                }
            } finally {
                in.close();
                out.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent .setType("vnd.android.cursor.dir/email");

        // the attachment
        Uri urifilePath = Uri.parse("file://"+fullFilePath);

        emailIntent.putExtra(Intent.EXTRA_STREAM,urifilePath);
        // the mail subject
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Aruco Calibration Pattern");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Print the attached pdf on a piece of paper and use it for calibrating the camera.");

        startActivity(Intent.createChooser(emailIntent , "Send email..."));

        Log.d("CEE","Could not open folder");

    }


    public native void initNativeCalib();
    public native void processCameraFrame(long imgGray, long imgColor);
    public native String addLastImage();
    public native int getNCalibrationImages();
    public native void clearCalibrationVec();
    public native String[] calibrate(float markerSize);
    public native String jniGetCalibratedCameraParams();
    public native String jniGetLog();
}
