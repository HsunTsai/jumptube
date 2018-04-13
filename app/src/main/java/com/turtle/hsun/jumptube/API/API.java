package com.turtle.hsun.jumptube.API;

import android.text.Html;
import android.util.Log;

import com.turtle.hsun.jumptube.Config;
import com.turtle.hsun.jumptube.Utils.Decode;
import com.turtle.hsun.jumptube.Utils.LogUtil;
import com.turtle.hsun.jumptube.Utils.SSL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


public class API {
    //get Youtube suggest list
    public static ResponseModel getYoutubeSuggest(String keyword) {
        ResponseModel responseModel = new ResponseModel();
        String youtubeSuggestList = "";
        try {
            HttpClient client = new DefaultHttpClient();
            client = SSL.createMyHttpClient();
            String get_url = "http://suggestqueries.google.com/complete/search?client=youtube&ds=yt&client=firefox&q=" + URLEncoder.encode(keyword, HTTP.UTF_8);
            HttpGet get = new HttpGet(get_url);
            LogUtil.show("get_url", get_url);
            HttpResponse response = client.execute(get);
            HttpEntity resEntity = response.getEntity();
            if (resEntity != null) youtubeSuggestList = EntityUtils.toString(resEntity);
            youtubeSuggestList = Decode.unicodeToUtf8(youtubeSuggestList);
            LogUtil.show("YoutubeSuggestList", youtubeSuggestList);

            Integer statusCode = response.getStatusLine().getStatusCode();
            responseModel.setResponseCode(statusCode);
            responseModel.setMessgae(youtubeSuggestList);
        } catch (Exception e) {
            responseModel.setResponseCode(9999);
            e.printStackTrace();
        }
        return responseModel;
    }

    //Post Sample
    public static ResponseModel PostSample(String parameter_1, String parameter_2) {
        ResponseModel responseModel = new ResponseModel();
        String SampleResp = "";
        try {
            HttpClient client = new DefaultHttpClient();
            client = SSL.createMyHttpClient();
            String post_url = "https://your_url";
            HttpPost post = new HttpPost(post_url);
            LogUtil.show("post_url", post_url);
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("parameter_1", parameter_1));
            params.add(new BasicNameValuePair("parameter_2", parameter_2));
            UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params, HTTP.UTF_8);
            post.setEntity(ent);
            HttpResponse response = client.execute(post);
            HttpEntity resEntity = response.getEntity();
            if (resEntity != null) SampleResp = EntityUtils.toString(resEntity);
            LogUtil.show("SampleResp", SampleResp);

            Integer statusCode = response.getStatusLine().getStatusCode();
            responseModel.setResponseCode(statusCode);
            responseModel.setMessgae(SampleResp);
        } catch (Exception e) {
            responseModel.setResponseCode(9999);
            e.printStackTrace();
        }
        return responseModel;
    }

    //Delete Sample
    public static ResponseModel DeleteSample(String parameter_1, String parameter_2) {
        ResponseModel responseModel = new ResponseModel();
        String SampleResp = "";
        try {
            HttpClient client = new DefaultHttpClient();
            client = SSL.createMyHttpClient();
            String delete_url = "https://your_url";
            LogUtil.show("delete_url", delete_url);
            HttpDelete delete = new HttpDelete(delete_url);
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("parameter_1", parameter_1));
            params.add(new BasicNameValuePair("parameter_2", parameter_2));
            UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params, HTTP.UTF_8);
            delete.setEntity(ent);
            HttpResponse response = client.execute(delete);
            HttpEntity resEntity = response.getEntity();
            if (resEntity != null) SampleResp = EntityUtils.toString(resEntity);
            LogUtil.show("SampleResp", SampleResp);

            Integer statusCode = response.getStatusLine().getStatusCode();
            responseModel.setResponseCode(statusCode);
            responseModel.setMessgae(SampleResp);
        } catch (Exception e) {
            responseModel.setResponseCode(9999);
            e.printStackTrace();
        }
        return responseModel;
    }

    public static class HttpDelete extends HttpEntityEnclosingRequestBase {
        public static final String METHOD_NAME = "DELETE";

        public String getMethod() {
            return METHOD_NAME;
        }

        public HttpDelete(final String uri) {
            super();
            setURI(URI.create(uri));
        }

        public HttpDelete(final URI uri) {
            super();
            setURI(uri);
        }

        public HttpDelete() {
            super();
        }
    }
}
