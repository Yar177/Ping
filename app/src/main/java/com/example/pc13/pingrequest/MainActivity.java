package com.example.pc13.pingrequest;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    TextView currentConectivity;
    TextView pingStatus;
    Button pingICMP;
    Button pingHTTP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentConectivity = (TextView) findViewById(R.id.current_network);
        pingStatus = (TextView) findViewById(R.id.ping_status);

        pingHTTP = (Button) findViewById(R.id.ping_http);
        pingICMP = (Button) findViewById(R.id.ping_icmp);



    }



    void  curenrNetworkStatus(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        StringBuilder stringBuilder = new StringBuilder();

        if (networkInfo.getTypeName().equalsIgnoreCase("WIFI")){
            stringBuilder.append("You are connected through WIFI");
        }else if(networkInfo.getTypeName().equalsIgnoreCase("Mobile")){
            stringBuilder.append("You are connected through Mobile data");
        }

        currentConectivity.setText(stringBuilder);

    }
}
