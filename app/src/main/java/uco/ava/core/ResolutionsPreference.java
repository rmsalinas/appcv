package uco.ava.core;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;


import org.opencv.core.Size;

import java.util.List;

import uco.ava.calibration.CameraCalibrations;

public class ResolutionsPreference extends ListPreference {

    public ResolutionsPreference(Context context) {
        super(context);
    }

    public ResolutionsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);


        List<Size> resolutions=CameraResolutions.getKnownCameraResolutions(context);
          String[] evalues = new String[resolutions.size()];
           String[] eentries =  new String[resolutions.size()];
           for(int i=0;i<resolutions.size();i++){
               eentries[i]= String.valueOf((int)(resolutions.get(i).width))+"x"+String.valueOf((int)resolutions.get(i).height);
               if (CameraCalibrations.isCalibrated((int)resolutions.get(i).width,(int)resolutions.get(i).height))
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