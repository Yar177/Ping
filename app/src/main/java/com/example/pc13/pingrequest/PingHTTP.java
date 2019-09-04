package com.example.pc13.pingrequest;
import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.TextView;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

class PingHTTP extends AsyncTask<Void, String, Integer> {
    private static TextView[][] pingTextView = new TextView[20][3];
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
            pingTextView[item][2].setTextColor(Color.parseColor("#ff000"));
        }
    }
}
