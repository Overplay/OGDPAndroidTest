package io.ourglass.ogdptest;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;


/**
 * Created by mkahn on 11/13/16.
 */


// This thread serves ONLY to send out the discovery enumeration packets, listening is done in the
// main service.

public class OGDPPingHandlerThread extends HandlerThread {

    public static final String TAG = "OGDPPingHandlerThread";
    public Context mContext;

    private Handler mWorkerHandler;
    private DatagramSocket ogdpSocket;



    public OGDPPingHandlerThread(String name) {
        super(name);
    }

    public void start(Context context, DatagramSocket socket){

        super.start();
        ogdpSocket = socket;
        mContext = context;
        mWorkerHandler = new Handler(getLooper());

    }

    public void discover(){

        // Delay ping a quarter second to allow listener thread to spin up.
        Log.d(TAG, "Issuing delayed discovery packet.");
        mWorkerHandler.postDelayed(discoveryRunnable, 250);

    }

    Runnable discoveryRunnable = new Runnable() {
        @Override
        public void run() {

            WifiManager wifi = (WifiManager) mContext.getSystemService(mContext.getApplicationContext().WIFI_SERVICE);

            if (wifi != null) {

                WifiManager.MulticastLock lock = wifi.createMulticastLock("The Lock");
                lock.acquire();

                String query = "{ \"delay\":350 }";

                try {
                    InetAddress group = InetAddress.getByName("255.255.255.255");


                    DatagramPacket dgram = new DatagramPacket(query.getBytes(), query.length(),
                            group, OGDPService.OGDP_PORT);

                    ogdpSocket.send(dgram);
                    Log.d(TAG, "Sent discovery packet successfully.");


                } catch (SocketException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Sent discovery packet, got SocketException");

                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Sent discovery packet, got UnknownHostException");

                } catch (IOException e) {
                    Log.e(TAG, "Sent discovery packet, got IOException");
                    e.printStackTrace();
                }

                lock.release();
            }
        }
    };


}
