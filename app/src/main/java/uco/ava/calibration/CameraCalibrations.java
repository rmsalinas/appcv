package uco.ava.calibration;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class CameraCalibrations {


    private static Context AppContext;
    public static void setContext(Context c){
        AppContext=c;
    }
    public static void saveCalibration( String calibdata,int w,int h){
        String key=getKey(w,h);
        SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(AppContext).edit();
        editor.putString(key, calibdata);
        editor.commit();
    }

    public static  String readCalibration( int w,int h){
        return PreferenceManager.getDefaultSharedPreferences(AppContext).getString(getKey(w,h),"");
    }

    public static boolean isCalibrated(int w,int h){
        return PreferenceManager.getDefaultSharedPreferences(AppContext).getString(getKey(w,h),"").length()!=0;

    }
    private static String getKey(int w,int h){
        return "Calibration:"+String.valueOf(w)+"x"+String.valueOf(h);

    }
}
