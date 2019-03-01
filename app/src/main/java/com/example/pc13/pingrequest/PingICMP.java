package com.example.pc13.pingrequest;

import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.TextView;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

class PingICMP extends AsyncTask<Void, String, Integer> {

    private static Integer	timeoutReachable =	5000;

    private static TextView[][] pingTextView = new TextView[20][3];


    private String ip;
    private boolean code;
    private int item;
    private InetAddress inetAddress;


    public PingICMP (String ip, int item) {
        this.ip = ip;
        this.inetAddress = null;
        this.item = item;
        this.code = false;
    }


    @Override
    protected Integer doInBackground(Void... voids) {
        try {
            inetAddress = InetAddress.getByName(ip);

        } catch (UnknownHostException e) {
            e.printStackTrace();
            code = false;
        }

        try {
            if (inetAddress.isReachable(timeoutReachable)){
                code =true;
            }else {
                code = false;
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
        if (code){
            pingTextView[item][2].setText("Reachable");
            pingTextView[item][2].setTextColor(Color.parseColor("#5d9356"));
        }else {
            pingTextView[item][2].setText("Not Reachable");
            pingTextView[item][2].setTextColor(Color.parseColor("#ff0000"));
        }
    }
}
