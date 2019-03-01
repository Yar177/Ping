package com.example.pc13.pingrequest;

import android.os.AsyncTask;

class PingHTTP extends AsyncTask<Void, String, Integer> {

    private String urlString;
    private boolean ping_success;
    private int item;
    private int status;


    public PingHTTP(String ip, int item) {
        this.ping_success = false;
        this.item = item;
        this.urlString = ip;
        this.status = null;
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        return null;
    }
}
