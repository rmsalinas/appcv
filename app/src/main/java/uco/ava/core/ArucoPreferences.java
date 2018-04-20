package uco.ava.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class ArucoPreferences {

    public static String MarkerType,DetectionMode,CornerRefinement;
    public static float minmarkersize=-1,markersize=-1;
    public static boolean detectEnclosed=false;

    public static void loadFromPreferences(Context AppContext){
         SharedPreferences pref= PreferenceManager.getDefaultSharedPreferences(AppContext);
        MarkerType=pref.getString( "ARUCO::markertype","ARUCO_MIP_36h12");
        DetectionMode=pref.getString( "ARUCO::detectionmode","FAST");
        CornerRefinement=pref.getString( "ARUCO::cornerrefinement","SUBPIX");
        minmarkersize=Float.valueOf( pref.getString( "ARUCO::minmarkersize","0"));
        markersize=Float.valueOf( pref.getString( "ARUCO::markersize","1"));
        detectEnclosed=pref.getBoolean( "ARUCO::detect_enclosed",false);
    }
}
