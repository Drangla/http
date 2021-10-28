package com.example.http;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private static String TV_RESULT_KEY = "TVRESULTDATA";
    private Button btnResult;
    private TextView txtResult;
    private String result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnResult = findViewById(R.id.tv_btn);
        txtResult = findViewById(R.id.tv_result);

        btnResult.setOnClickListener(v -> {
            this.loadWebResult();
        });

        if(savedInstanceState.containsKey("TVRESULTDATA")){
            if(savedInstanceState.containsKey(TV_RESULT_KEY)) {
                txtResult.setText(savedInstanceState.getString(TV_RESULT_KEY));
            }
        }

    }
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(TV_RESULT_KEY, txtResult.getText().toString());
    }

    private void loadWebResult() {
        NetworkRunable runable = new NetworkRunable("http://fhtw-building-control.technikum-wien.at:8080/rest/items");
        // nicht so: runable.run();
        new Thread(runable).start();
    }

    private String getResponseFromUrl(URL url) throws IOException {
        String result;
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        try {
            httpURLConnection.setRequestMethod("GET");
            InputStream is = httpURLConnection.getInputStream();
            Scanner scanner = new Scanner(is);
            scanner.useDelimiter("\\A"); //EOF
            if (scanner.hasNext()) {
                result = scanner.next();
            } else {
                return null;
            }
        }  finally {
            httpURLConnection.disconnect();
        }
        return result;

    }

    class NetworkRunable implements Runnable {

        URL url;

        NetworkRunable(String urlString) {
            try {
                url = new URL(urlString);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                JSONObject root = new JSONObject(getResponseFromUrl(url));
                JSONArray results = root.getJSONArray("result");

                result = "";


                //JSONArray results = new JSONArray(getResponseFromUrl(url));
                // JSONObject root = new JSONObject(getResponseFromUrl(url));
                //JSONArray results = root.getJSONArray("result");
                //result = "";
/*
                for (int i = 0; i < results.length(); i++) {
                    System.out.println("test");
                    JSONObject entry = results.getJSONObject(i);
                    if (entry.getString("type").equals("Switch")) {
                        result += entry.getString("name") + "\n";
                    }*/

                for(int i = 0; i < results.length(); i++) {
                    JSONObject entry = results.getJSONObject(i);
                    JSONObject message;

                    if(entry.has("message")) {
                        message = entry.getJSONObject("message");
                    } else {
                        message = entry.getJSONObject("edited_message");
                    }

                    result += entry.getString("update_id") + "\n";
                    result += entry.getString("text") + "\n";
                }

                Handler mainHandler = new Handler(Looper.getMainLooper()); // ist berechtigt auf main thread zu schreiben
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        txtResult.setText(result);
                    }
                });

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }
    }
}