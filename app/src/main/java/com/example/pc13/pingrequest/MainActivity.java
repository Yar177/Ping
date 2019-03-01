package com.example.pc13.pingrequest;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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

    private static TextView[][] pingTextView = new TextView[20][3];


    ImageView img;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




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
        } catch (IOException | JSONException e) {
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




    private void updateConnectionStatus(){

        img = (ImageView) findViewById(R.id.image1);
        img.setBackgroundResource(R.drawable.presence_invisible);

        if (checkInternetConnection()){
         img.setBackgroundResource(R.drawable.presence_online);
        }else {
            img.setBackgroundResource(R.drawable.presence_busy);
        }

        LinearLayout pingLinerLayout = (LinearLayout) findViewById(R.id.insertPings);
        pingLinerLayout.removeAllViews();

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        layoutParams.setMargins(5,5,5,5);
        layoutParams.gravity = Gravity.CENTER;


        final float WIDE = this.getResources().getDisplayMetrics().widthPixels;
        int valueWide = (int) (WIDE * 0.30f);
        LinearLayout.LayoutParams layoutParamsTV = new LinearLayout.LayoutParams(valueWide, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParamsTV.setMargins(5,5,5,5);
        layoutParamsTV.gravity = Gravity.CENTER;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int fontSize = (int)(WIDE/36.0f/(displayMetrics.scaledDensity));


        for (int i = 0; i < pingType.size(); i++){
            LinearLayout newLL;
            newLL = new LinearLayout((MainActivity.this));
            newLL.setLayoutParams(layoutParams);
            newLL.setOrientation(LinearLayout.HORIZONTAL);
            newLL.setHorizontalGravity(Gravity.CENTER);
            pingLinerLayout.addView(newLL, i);

            pingTextView[i][0] = new TextView(MainActivity.this);
            pingTextView[i][0].setText(connName.get(i));
            pingTextView[i][0].setTextSize(fontSize);
            newLL.addView(pingTextView[i][0], 0, layoutParams);

            pingTextView[i][1] = new TextView(MainActivity.this);
            pingTextView[i][1].setText(connName.get(i));
            pingTextView[i][1].setTextSize(fontSize);
            newLL.addView(pingTextView[i][1], 1, layoutParams);

            pingTextView[i][2] = new TextView(MainActivity.this);
            pingTextView[i][2].setText(connName.get(i));
            pingTextView[i][2].setTextSize(fontSize);
            newLL.addView(pingTextView[i][3], 3, layoutParams);

            if (pingType.get(i) == 0){
                new pingICMP(connURL.get(i), i).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else if (pingType.get(i) == 1){
                new pingHTTP(connURL.get(i), i).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            }

            TextView textRefr = (TextView) findViewById(R.id.textUpdate);
            textRefr.setText(GetTime());
        }









    }

    private String GetTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm:ss");
        String date = simpleDateFormat.format(new Date());

        return date;
    }

    private boolean checkInternetConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        TextView connType = (TextView) findViewById(R.id.textType);
        TextView connAval = (TextView) findViewById(R.id.textAvail);
        TextView connConn = (TextView) findViewById(R.id.textConn);


        connType.setText(getString(R.string.unknown));

        if (connectivityManager != null && networkInfo != null){
            if (networkInfo.getTypeName().equalsIgnoreCase("WIFI")){
                connType.setText(getString(R.string.wifi));
            }

            if (networkInfo.getTypeName().equalsIgnoreCase("MOBILE")){
                connType.setText(getString(R.string.mobile));
            }
            if (networkInfo.isAvailable()){
                connAval.setText(getString(R.string.available));
                connAval.setTextColor(Color.parseColor("#5d9356"));
                if (networkInfo.isConnected()){
                    connConn.setText(getString(R.string.connected));
                    connConn.setTextColor(Color.parseColor("#5d9356"));
                    return true;

                }else {
                    connConn.setText(getString(R.string.notconnected));
                    connConn.setTextColor(Color.parseColor("#ff0000"));
                    return false;
                }
            }else {
                connAval.setTextColor(Color.parseColor("#ff0000"));
                connAval.setText(getString(R.string.notavailable));
                return false;
            }
        }else {
            connType.setText(getString(R.string.unknown));

            connConn.setText(getString(R.string.notconnected));
            connConn.setTextColor(Color.parseColor("#ff0000"));

            connAval.setTextColor(Color.parseColor("#ff0000"));
            connAval.setText(getString(R.string.notavailable));

            return false;
        }

    }


} //mainActivity end
