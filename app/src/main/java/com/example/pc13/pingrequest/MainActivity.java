package com.example.pc13.pingrequest;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 12111;
    private static final int timeoutConnection = 5000;
    private static final int timeoutSocket = 5000;
    private static final int timeoutReachable = 5000;
    private static final int timeoutInterval = 10000;
    private static final int updateInterval = 10000;
    public static JSONArray jsonArray = null;
    public static String configFile = "";
    private static TextView[][] pingTextView = new TextView[20][3];
    ArrayList<Integer> pingType = new ArrayList<Integer>();
    ArrayList<String> connName = new ArrayList<String>();
    ArrayList<String> connURL = new ArrayList<String>();
    ArrayList<String> response = new ArrayList<String>();
    boolean m_bStatusThreadStop;
    boolean m_bClockThreadStop;
    Thread m_statusThread;
    Thread m_clockThread;
    ImageView img;
    BroadcastReceiver wifiBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            TextView textBR = findViewById(R.id.textBroadcastReceiver);
            SupplicantState supplicantState = null;
            WifiManager wifiManager =
                    (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            supplicantState = wifiInfo.getSupplicantState();
            if (supplicantState.equals(SupplicantState.COMPLETED)) {
                //wifi is connected
                textBR.setTextColor(Color.parseColor("#ffff00"));
                textBR.setText(getString(R.string.connected));
            } else if (supplicantState.equals(SupplicantState.SCANNING)) {
                textBR.setTextColor(Color.parseColor("#ff0000"));
                textBR.setText(getString(R.string.scanning));
            } else if (supplicantState.equals(SupplicantState.DISCONNECTED)) {
                textBR.setTextColor(Color.parseColor("#ff0000"));
                textBR.setText(getString(R.string.notconnected));
            }
            checkInternetConnection();
        }
    };

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
            for (int i = 0; i < jsonArray.length(); i++) {
                int type = (Integer) jsonGetter2(jsonArray.getJSONArray(i), "type");
                pingType.add(type);
                String cname = jsonGetter2(jsonArray.getJSONArray(i), "name").toString();
                connName.add(cname);
                String url = jsonGetter2(jsonArray.getJSONArray(i), "url").toString();
                connURL.add(url);
                String resp = jsonGetter2(jsonArray.getJSONArray(i), "res").toString();
                response.add(resp);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        createAndRunStatusThread(this);
        createAndRunClockThread(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter =
                new IntentFilter(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        this.registerReceiver(wifiBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(wifiBroadcastReceiver);
    }

    private boolean checkInternetConnection() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        TextView connType = findViewById(R.id.textType);
        TextView connAval = findViewById(R.id.textAvail);
        TextView connConn = findViewById(R.id.textConn);
        connType.setText(getString(R.string.unknown));
        if (connectivityManager != null && networkInfo != null) {
            if (networkInfo.getTypeName().equalsIgnoreCase("WIFI")) {
                connType.setText(getString(R.string.wifi));
            }
            if (networkInfo.getTypeName().equalsIgnoreCase("MOBILE")) {
                connType.setText(getString(R.string.mobile));
            }
            if (networkInfo.isAvailable()) {
                connAval.setText(getString(R.string.available));
                connAval.setTextColor(Color.parseColor("#5d9356"));
                if (networkInfo.isConnected()) {
                    connConn.setText(getString(R.string.connected));
                    connConn.setTextColor(Color.parseColor("#5d9356"));
                    return true;
                } else {
                    connConn.setText(getString(R.string.notconnected));
                    connConn.setTextColor(Color.parseColor("#ff0000"));
                    return false;
                }
            } else {
                connAval.setTextColor(Color.parseColor("#ff0000"));
                connAval.setText(getString(R.string.notavailable));
                return false;
            }
        } else {
            connType.setText(getString(R.string.unknown));
            connConn.setText(getString(R.string.notconnected));
            connConn.setTextColor(Color.parseColor("#ff0000"));
            connAval.setTextColor(Color.parseColor("#ff0000"));
            connAval.setText(getString(R.string.notavailable));
            return false;
        }
    }

    private void createAndRunClockThread(final Activity activity) {
        m_bClockThreadStop = false;
        m_clockThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!m_bClockThreadStop) {
                    try {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateClock();
                            }
                        });
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        m_bStatusThreadStop = true;
                        messageBox(activity,
                                "Exception in clock thread: " + e.toString() + " - " + e.getMessage(), "createAndRunClockThread Error");
                    }
                }
            }
        });
        m_clockThread.start();
    }

    private String getTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm:ss");
        String date = simpleDateFormat.format(new Date());
        return date;
    }

    private Object jsonGetter2(JSONArray jsonArray, String key) {
        Object object = null;
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.has(key)) {
                    object = jsonObject.get(key);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return object;
    }

    private void messageBox(final Activity activity, final String message, final String title) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
                alertDialog.setTitle(title);
                alertDialog.setIcon(android.R.drawable.stat_sys_warning);
                alertDialog.setMessage(message);
                alertDialog.setCancelable(false);
                alertDialog.setButton("Back", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alertDialog.cancel();
                    }
                });
                alertDialog.show();
            }
        });
    }

    private void updateClock() {
        TextView textTime = findViewById(R.id.textTime);
        textTime.setText(getTime());
    }

    void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {
            curenrNetworkStatus();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    curenrNetworkStatus();
                } else {
                    Toast.makeText(this, "You need permission", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    void curenrNetworkStatus() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        StringBuilder stringBuilder = new StringBuilder();
        if (networkInfo == null) return;
        if (networkInfo.getTypeName().equalsIgnoreCase("WIFI")) {
            stringBuilder.append("You are connected through WIFI");
        } else if (networkInfo.getTypeName().equalsIgnoreCase("Mobile")) {
            stringBuilder.append("You are connected through Mobile data");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.actions, menu);
        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.exit) {
            finish();
            return (true);
        }
        return (super.onOptionsItemSelected(item));
    }

    public void createAndRunStatusThread(final Activity activity) {
        m_bStatusThreadStop = false;
        m_statusThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!m_bStatusThreadStop) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateConnectionStatus();
                        }
                    });
                    try {
                        Thread.sleep(updateInterval);
                    } catch (InterruptedException e) {
                        m_bStatusThreadStop = true;
                        messageBox(activity, "Exception in status thread: " + e.toString() + " - "
                                + e.getMessage(), "createAndRunStatusThread Error");
                        e.printStackTrace();
                    }
                }
            }
        });
        m_statusThread.start();
    }

    public void updateConnectionStatus() {
        img = findViewById(R.id.image1);
        img.setBackgroundResource(R.drawable.presence_invisible);
        if (checkInternetConnection()) {
            img.setBackgroundResource(R.drawable.presence_online);
        } else {
            img.setBackgroundResource(R.drawable.presence_busy);
        }
        LinearLayout pingLinerLayout = findViewById(R.id.insertPings);
        pingLinerLayout.removeAllViews();
        LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(5, 5, 5, 5);
        layoutParams.gravity = Gravity.CENTER;
        final float WIDE = this.getResources().getDisplayMetrics().widthPixels;
        int valueWide = (int) (WIDE * 0.30f);
        LinearLayout.LayoutParams layoutParamsTV = new LinearLayout.LayoutParams(valueWide,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParamsTV.setMargins(5, 5, 5, 5);
        layoutParamsTV.gravity = Gravity.CENTER;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int fontSize = (int) (WIDE / 36.0f / (displayMetrics.scaledDensity));
        for (int i = 0; i < pingType.size(); i++) {
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
            newLL.addView(pingTextView[i][2], 2, layoutParams);
            if (pingType.get(i) == 0) {
                new PingICMP(connURL.get(i), i).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else if (pingType.get(i) == 1) {
                new PingHTTP(connURL.get(i), i).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
        TextView textRefr = findViewById(R.id.textUpdate);
        textRefr.setText(getTime());
    }

    class PingICMP extends AsyncTask<Void, String, Integer> {
        private String ip;
        private boolean code;
        private int item;
        private InetAddress inetAddress;

        public PingICMP(String ip, int item) {
            this.ip = ip;
            this.inetAddress = null;
            this.item = item;
            this.code = false;
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            try {
                if (ip != null) {
                    inetAddress = InetAddress.getByName(ip);
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
                code = false;
            }
            try {
                if (inetAddress != null) {
                    code = inetAddress.isReachable(timeoutReachable);
                }
            } catch (IOException e) {
                e.printStackTrace();
                code = false;
            }
            return 1;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Integer integer) {
            if (code) {
                pingTextView[item][2].setText("Reachable");
                pingTextView[item][2].setTextColor(Color.parseColor("#5d9356"));
            } else {
                pingTextView[item][2].setText("Not Reachable");
                pingTextView[item][2].setTextColor(Color.parseColor("#ff0000"));
            }
        }
    }

    class PingHTTP extends AsyncTask<Void, String, Integer> {
        private String urlString;
        private boolean ping_success;
        private int item;
        private int status;

        public PingHTTP(String ip, int item) {
            this.ping_success = false;
            this.item = item;
            this.urlString = ip;
            this.status = 400;
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            try {
                URL url = new URL(urlString);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setAllowUserInteraction(false);
                httpURLConnection.setInstanceFollowRedirects(true);
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();
                status = httpURLConnection.getResponseCode();
                if ((status == HttpURLConnection.HTTP_NO_CONTENT) || (status == HttpURLConnection.HTTP_OK)) {
                    ping_success = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
                ping_success = false;
            }
            return 1;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            if (ping_success) {
                pingTextView[item][2].setText("Status Code= " + status);
                pingTextView[item][2].setTextColor(Color.parseColor("#5d9356"));
            } else {
                pingTextView[item][2].setText("Status Code= " + status);
                pingTextView[item][2].setTextColor(Color.parseColor("#ff0000"));
            }
        }
    }
} //mainActivity end
