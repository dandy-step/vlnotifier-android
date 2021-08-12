package com.dandystep.vlnotifier;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


class CheckChannelAsync extends AsyncTask<String, Integer, Boolean> {
    //check if the channel exists in the background
    protected Boolean doInBackground(String... channel) {
        Boolean res = true;
        try {
            String address = "https://vaughnlive.tv/" + channel[0];
            URL url = new URL(address);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.connect();
            BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                if ((line.indexOf("Channel Not Found")) != -1) {
                    res = false;
                }
                if ((line.indexOf("Channel you are looking for doesn't exist")) != -1) {
                    res = false;
                }
                if ((line.indexOf("Channel closed for violating ")) != -1) {
                    res = false;
                }
                if ((line.indexOf("Stream you are looking for doesn't exist")) != -1) {
                    res = false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return res;
    }
}
