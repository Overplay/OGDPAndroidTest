package io.ourglass.ogdptest;

/**
 * Created by mkahn on 11/14/16.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.json.JSONObject;

import java.util.HashMap;


public class OGDPBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "OGDPBroadcastReceiver";

    private OGDPBroadcastReceiverListener mListener;

    public interface OGDPBroadcastReceiverListener {

        void foundOGs(HashMap<String, JSONObject> devices);
        void ogSearchFail(String errString, Exception e);
    }

    public OGDPBroadcastReceiver(OGDPBroadcastReceiverListener listener){
        mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "Received notification of new OGDP search complete");

        // The getInstance can return a null but only if the service is not started and this
        // should never be the case when this is called. If it is the case, it is a bug.
        if (mListener!=null){
            if (intent.getStringExtra("error")==null){
                mListener.foundOGs(OGDPService.getInstance().allOGs);
            } else {
                mListener.ogSearchFail(intent.getStringExtra("error"), OGDPService.getInstance().lastException);
            }
        }

    }

}
