package com.turtle.hsun.jumptube.AsyncTask;

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Hsun on 18/4/12.
 */
public class LoadDetailsTask extends AsyncTask<String, String, String> {

    private HttpURLConnection urlConnection;
    private URL url;

    public LoadDetailsTask(String url) throws MalformedURLException {
        this.url = new URL(url);
    }

    @Override
    protected String doInBackground(String... args) {

        StringBuilder result = new StringBuilder();

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

        }catch( Exception e) {
            e.printStackTrace();
        }
        finally {
            urlConnection.disconnect();
        }


        return result.toString();
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

    }
}
