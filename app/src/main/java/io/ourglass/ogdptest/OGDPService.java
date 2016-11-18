package io.ourglass.ogdptest;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONObject;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by mitch on 11/10/16.
 * Does ogdp discovery.
 *
 */

public class OGDPService extends Service implements OGDPListenHandlerThread.OGDPListenerListener {

    public static final String TAG = "OGDPService";
    public static final int OGDP_PORT = 9091;

    private static OGDPService sService;

    // Provide static/singleton instance. This can be null if Service has not been created!
    public static OGDPService getInstance(){
        return sService;
    }

    // handlerThread that only sends out the inquiry packet
    private OGDPPingHandlerThread mOGDPDiscoveryThread;
    private OGDPListenHandlerThread mListenerThread;

    public HashMap<String, JSONObject> allOGs = new HashMap<>();
    public HashSet<String> allOGAddresses = new HashSet<>();
    public Exception lastException;

    private DatagramSocket mSocket;

    // Stock stuff that needs to be here for all services

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        sService = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // optional flag to not do a discovery immediately
        processIntent(intent);
        return Service.START_STICKY;

    }

    private void processIntent(Intent intent){

        // This function is here in case there are some optional params passed in the intent
        // Like they are in the AmstelBright SSDPDisco service this is based on.

        discover();

    }

    private void prepSocket() throws SocketException {

        if (mSocket==null){
            mSocket = new DatagramSocket(null);
            mSocket.setReuseAddress(true);
            mSocket.setBroadcast(true);
            mSocket.bind(new InetSocketAddress(OGDP_PORT));
        }

    }

    private void prepPingThread(){

        if (mOGDPDiscoveryThread==null){
            mOGDPDiscoveryThread = new OGDPPingHandlerThread("ssdpdicsoping");
            mOGDPDiscoveryThread.start(this, mSocket);
        }

    }

    private void prepListenerThread(){

        if (mListenerThread==null){
            mListenerThread = new OGDPListenHandlerThread("ssdpdiscolisten");
            mListenerThread.start(this, mSocket, this);
        }
    }


    // Triggers a discovery pass
    public void discover(){

        try {
            prepSocket();
            prepListenerThread();
            prepPingThread();

        } catch (SocketException e) {
            e.printStackTrace();
        }

        mListenerThread.listen(5000);
        mOGDPDiscoveryThread.discover(); // send ping and sit back and chill

    }


    public void onDestroy() {

        Log.d(TAG, "onDestroy");

        if (mOGDPDiscoveryThread!=null)
            mOGDPDiscoveryThread.quit();

        if (mListenerThread!=null){
            mSocket.close();
            mSocket = null;
        }

    }

    private void notifyNewDevices(){

        Intent intent = new Intent();
        intent.setAction("ogdp");
        intent.putExtra("devices", allOGs);
        intent.putExtra("addresses", allOGAddresses);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

    }

    private void notifyError(String message){

        Intent intent = new Intent();
        intent.setAction("ogdp");
        intent.putExtra("error", message);
        sendBroadcast(intent);

    }


    @Override
    public void devicesFound(HashMap<String, JSONObject> devices) {
        Log.d( TAG, "Found "+ devices.keySet().size()+" devices");
        allOGs = devices;
        allOGAddresses = new HashSet<>(devices.keySet());
        notifyNewDevices();
    }

    @Override
    public void error(Exception e) {
        Log.e( TAG, "Error discovering OGs");
        lastException = e;
        notifyError(e.getMessage());
    }
}