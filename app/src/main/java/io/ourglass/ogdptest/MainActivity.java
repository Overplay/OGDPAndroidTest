package io.ourglass.ogdptest;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    Button searchButton;
    Button intentButton;
    TextView foundOGsTV;

    OGDPBroadcastReceiver mBroadcastRcvr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchButton = (Button)findViewById(R.id.button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchForOGs();
            }
        });

        foundOGsTV = (TextView)findViewById(R.id.foundOGTV);

    }

    @Override
    protected void onResume(){
        super.onResume();
        registerOGDPResponse();
    }

    @Override
    protected void onDestroy(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastRcvr);
        super.onDestroy();
    }

    private void searchForOGs(){

        searchButton.setEnabled(false);
        searchButton.setText("...searching...");
        Intent di = new Intent(this, OGDPService.class);
        startService(di);

    }


    public void registerOGDPResponse(){

        mBroadcastRcvr = new OGDPBroadcastReceiver(new OGDPBroadcastReceiver.OGDPBroadcastReceiverListener() {
            @Override
            public void foundOGs(final HashMap<String, JSONObject> devices) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Set<String> ipSet = devices.keySet();
                        String ipString = "...";
                        for (String ipAddr: ipSet){
                            ipString = ipString + ipAddr+ "...";
                        }
                        foundOGsTV.setText(ipString);

                        searchButton.setEnabled(true);
                        searchButton.setText("SEARCH FOR OGS");
                    }
                });
            }

            @Override
            public void ogSearchFail(String errString, Exception e) {

            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastRcvr,
                new IntentFilter("ogdp"));


    }



}
