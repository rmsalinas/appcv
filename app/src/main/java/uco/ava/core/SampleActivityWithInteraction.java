package uco.ava.core;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.uco.ava.appcv.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.util.concurrent.atomic.AtomicBoolean;

import uco.ava.calibration.CameraCalibrations;

// OpenCV Classes

public class SampleActivityWithInteraction extends Activity implements CvCameraViewListener2, View.OnTouchListener {
    static {
        System.loadLibrary("sampleactivity_wi_jni");
    }

    // Used for logging success or failure messages
    private static final String TAG = "SampleActivityWithInteraction";

    // Loads camera view of OpenCV for us to use. This lets us see using OpenCV
    private JavaCameraView mOpenCvCameraView;

    private Toast toastMain;
    private Boolean backButtonPressedOnce = false;
    private Boolean isCalibrating = false;
    public int cameraWidth, cameraHeight;
    public int imageWidth = -1, imageHeight = -1; // or other values

    private Mat image;
    Bitmap bitmapCanvas;
    ImageView imageCanvas;
    AtomicBoolean mustDrawImage = new AtomicBoolean(false);
    // These variables are used (at the moment) to fix camera orientation from 270degree to 0degree

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);


        setContentView(R.layout.sampleactivity_wi);

        //getSupportActionBar().hide();

        toastMain = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        Bundle b = getIntent().getExtras();

        if (b != null) {
            imageWidth = b.getInt("width");
            imageHeight = b.getInt("height");
        }

        mOpenCvCameraView = findViewById(R.id.show_camera_activity_java_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        if (imageWidth * imageHeight > 0)
            mOpenCvCameraView.setMaxFrameSize(imageWidth, imageHeight);
        else {
            Log.d(TAG, "SampleActivity Need input parameters");
            finish();
        }

        bitmapCanvas = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmapCanvas);
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(60);

        imageCanvas = (ImageView) findViewById(R.id.canvasSpace);
        imageCanvas.setImageBitmap(bitmapCanvas);
        imageCanvas.setOnTouchListener(this);

        initNativeCalib();
        if (CameraCalibrations.isCalibrated(imageWidth, imageHeight)) {
            String calibration = CameraCalibrations.readCalibration(imageWidth, imageHeight);
            boolean res = setCalibrationParams(calibration);
            Log.d("SAAMPLE", "calib =" + res);
        }
        image = new Mat(imageWidth, imageHeight, CvType.CV_8UC(4));

    }

    @Override
    public void onPause() {
        super.onPause();
        mapdler.removeCallbacks(null);
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
        mapdler.postDelayed(runmaple, 5);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }


    public void onCameraViewStarted(int width, int height) {

    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        boolean mustDraw = onNewCameraFrame(inputFrame.rgba().getNativeObjAddr());
        mustDrawImage.set(mustDraw);

        return null;
    }

    final Handler mapdler = new Handler();
    final Runnable runmaple = new Runnable() {
        public void run() {

            if (mustDrawImage.get()) {
                drawDisplayableImage(image.getNativeObjAddr());
                Utils.matToBitmap(image, bitmapCanvas);
                imageCanvas.setImageBitmap(bitmapCanvas);
                mustDrawImage.set(false);
            }
            mapdler.postDelayed(this, 2);
        }
    };


    private void messenger(String message) {
        Log.i(TAG, "called messenger");

        toastMain.cancel();
        toastMain = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toastMain.show();
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {


        int action = event.getAction() & event.ACTION_MASK;
        float x1 = -1, x2 = -1, y1 = -1, y2 = -1;
        if (event.getPointerCount() >= 2) {
            x1 = event.getX(1);
            y1 = event.getY(1);
        }
        if (event.getPointerCount() >= 3) {
            x2 = event.getX(2);
            y2 = event.getY(2);
        }
        Log.d("POINTER", "" + event.getPointerCount() + " " + event.getX() + " " + event.getY() + " " + x1 + " " + y1 + " " + x2 + " " + y2);

        boolean res = onTouchEvent(action, event.getX(), event.getY(), x1, y1, x2, y2);
        mustDrawImage.set(res);

        return true;
    }

    public native void initNativeCalib();

    public native boolean setCalibrationParams(String str);

    public native boolean onTouchEvent(int action, float x0, float y0, float x1, float y1, float x2, float y3);

    public native boolean onNewCameraFrame(long image);

    public native void drawDisplayableImage(long image);

}
