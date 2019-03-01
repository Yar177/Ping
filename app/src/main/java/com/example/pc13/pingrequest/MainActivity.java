package com.example.pc13.pingrequest;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 12111;
    private static final int timeoutConnection = 5000;
    private static final int timeoutSocket = 5000;
    private static final int timeoutReachable = 5000;
    private static final int timeoutInterval = 10000;



    ArrayList<Integer> pingType = new ArrayList<Integer>();
    ArrayList<String > connName = new ArrayList<String >();
    ArrayList<String > connURL = new ArrayList<String >();
    ArrayList<String > response = new ArrayList<String >();

    public static JSONArray jsonArray = null;
    public static String configFile = "";




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


        checkPermission();


        Resources resources = getResources();
        InputStream inputStream = resources.openRawResource(R.raw.connectionfile);

        try {
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);

            configFile = (new String(bytes));

            jsonArray = new JSONArray(configFile);

            for (int i =0; i < jsonArray.length(); i++){
                int type = (Integer) jsonGetter2(jsonArray.getJSONArray(i), "type");
                pingType.add(type);
                String cname = jsonGetter2(jsonArray.getJSONArray(i),"name").toString();
                connName.add(cname);
                String url = jsonGetter2(jsonArray.getJSONArray(i),"url").toString();
                connURL.add(url);
                String resp = jsonGetter2(jsonArray.getJSONArray(i),"res").toString();
                response.add(resp);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    void checkPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);

        }else {
            curenrNetworkStatus();


        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS:{
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    curenrNetworkStatus();
                } else {
                    Toast.makeText(this, "You need permission", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
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


    private Object jsonGetter2(JSONArray jsonArray,String key){
            Object object = null;
            for (int i = 0; i<jsonArray.length() ;i++){
                try {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.has(key)){
                        object = jsonObject.get(key);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }            }

        return object;
    }
}
