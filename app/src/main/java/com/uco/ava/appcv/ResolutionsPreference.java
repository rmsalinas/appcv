package com.uco.ava.appcv;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;

import uco.ava.calibration.CameraCalibrations;

public class ResolutionsPreference extends ListPreference {

    public ResolutionsPreference(Context context) {
        super(context);
    }

    public ResolutionsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

          String[] evalues = new String[MainActivity.CameraResolutions.size()];
           String[] eentries =  new String[MainActivity.CameraResolutions.size()];
           for(int i=0;i<MainActivity.CameraResolutions.size();i++){
               eentries[i]= String.valueOf(MainActivity.CameraResolutions.get(i).width)+"x"+String.valueOf(MainActivity.CameraResolutions.get(i).height);
               if (CameraCalibrations.isCalibrated(MainActivity.CameraResolutions.get(i).width,MainActivity.CameraResolutions.get(i).height))
                   eentries[i]+=" (Calibrated)"  ;
               evalues[i]=String.valueOf(i);
           }



        setEntries(eentries);
        setEntryValues(evalues);
        setPositiveButtonText("Accept");
        setNegativeButtonText("Cancel");

    }
    private int initializeIndex() {
        return 0;
    }

}