package uco.ava.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.opencv.android.JavaCameraView;
import org.opencv.core.Size;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CameraResolutions {

    public static void createListOfCameraResolutions(Context ctx, JavaCameraView camera,int width,int height){
        if (PreferenceManager.getDefaultSharedPreferences(ctx).getString("CameraResolutions", "").length() != 0) return;
        List<android.hardware.Camera.Size>  CameraResolutions = camera.getResolutions();

        if (CameraResolutions.size() == 0) return;

        String resolutionSizes = "";
        for (int i = 0; i < CameraResolutions.size(); i++) {
            resolutionSizes = resolutionSizes + String.valueOf(CameraResolutions.get(i).width) + " " + String.valueOf(CameraResolutions.get(i).height);
            if (i != CameraResolutions.size() - 1)
                resolutionSizes = resolutionSizes + ",";
        }
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(ctx).edit();
        editor.putString("CameraResolutions", resolutionSizes);
        //now, select the correct index according the the with and height indicated

        int bestRes=0,dist=999999999;
        for (int i = 0; i < CameraResolutions.size(); i++) {
                int diff=   (CameraResolutions.get(i).width-width)*(CameraResolutions.get(i).width-width) +
                        (CameraResolutions.get(i).height-height)*(CameraResolutions.get(i).height-height);
                if (diff<dist){
                    dist=diff;
                    bestRes=i;
                }
        }

        editor.putString("resolution",String.valueOf(bestRes));


        editor.commit();
    }
    public static void updateCurrentResolution(Context ctx,int width,int height){

        List<Size> CameraResolutions=getKnownCameraResolutions(ctx);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(ctx).edit();
        int bestRes=0;double dist=999999999;
        for (int i = 0; i < CameraResolutions.size(); i++) {
            double diff=   (CameraResolutions.get(i).width-width)*(CameraResolutions.get(i).width-width) +
                    (CameraResolutions.get(i).height-height)*(CameraResolutions.get(i).height-height);
            if (diff<dist){
                dist=diff;
                bestRes=i;
            }
        }

        editor.putString("resolution",String.valueOf(bestRes));


        editor.commit();

    }

public static    List<Size> getKnownCameraResolutions( Context ctx){
        String Resolutions= PreferenceManager.getDefaultSharedPreferences(ctx).getString("CameraResolutions","");
        List <Size> Result=new ArrayList<Size>();
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
    public static Size getSelectedCameraSize(Context ctx){
        List<Size> resolutions=getKnownCameraResolutions(ctx);
        if (resolutions.size()==0) return new Size(0,0);

        int cameraResolutionIndex=Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(ctx).getString("resolution","0"));
        Size s=new Size();
        s.width=(int)resolutions.get(cameraResolutionIndex).width;
        s.height=(int)resolutions.get(cameraResolutionIndex).height;
        return s;
    }


}
