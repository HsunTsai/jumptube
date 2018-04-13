package com.turtle.hsun.jumptube.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.turtle.hsun.jumptube.API.API;
import com.turtle.hsun.jumptube.API.ResponseModel;
import com.turtle.hsun.jumptube.Config;
import com.turtle.hsun.jumptube.Custom.CustomSwipeRefresh;
import com.turtle.hsun.jumptube.PlayerService;
import com.turtle.hsun.jumptube.R;
import com.turtle.hsun.jumptube.Utils.HandleMessage;
import com.turtle.hsun.jumptube.Utils.Internet;
import com.turtle.hsun.jumptube.Utils.LogUtil;
import com.turtle.hsun.jumptube.Utils.Service;
import com.turtle.hsun.jumptube.Utils.UITransform;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.turtle.hsun.jumptube.Config.OVERLAY_PERMISSION_REQ_CODE;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SearchView.OnQueryTextListener, View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    //Component
    private Activity activity;
    private WebView webView_youtube_list;
    private Button bt_retry_connect, bt_settings, bt_exit_app;
    private SearchView searchView;
    private ViewStub viewStub;
    private CustomSwipeRefresh swipeRefreshLayout;

    //Parameter
    private String youtubeHome = "https://m.youtube.com/",
            currentUrl = "https://m.youtube.com/",
            videoID, playListID;
    private Boolean isExit = false;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        activity = this;
        Handler();
        initView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        if (searchView != null) {
            searchView.setSearchableInfo(
                    searchManager.getSearchableInfo(getComponentName()));
            searchView.setOnQueryTextListener(this);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, Settings.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        webView_youtube_list.loadUrl("http://m.youtube.com/results?q=" + query);
        searchView.clearFocus();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String keyword) {
        if (keyword.length() > 0) {
            GetYoutubeSuggestion getYoutubeSuggestion = new GetYoutubeSuggestion();
            getYoutubeSuggestion.setData(keyword);
            getYoutubeSuggestion.run();
        }
        return true;
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        viewStub = (ViewStub) findViewById(R.id.view_stub);
        if (Internet.isAvailable(activity)) {
            viewStub.setLayoutResource(R.layout.component_webview);
            viewStub.inflate();
            swipeRefreshLayout = (CustomSwipeRefresh) findViewById(R.id.swipeRefreshLayout);
            swipeRefreshLayout.setColorSchemeResources(R.color.holo_green_dark,
                    R.color.holo_blue_dark,
                    R.color.holo_orange_light,
                    R.color.holo_red_light);
            swipeRefreshLayout.setProgressViewOffset(true, UITransform.dp2px(activity, 30),
                    UITransform.dp2px(activity, 60));
            swipeRefreshLayout.setOnRefreshListener(this);
            swipeRefreshLayout.setCanChildScrollUpCallback(new CustomSwipeRefresh.CanChildScrollUpCallback() {
                @Override
                public boolean canSwipeRefreshChildScrollUp() {
                    return webView_youtube_list.getScrollY() > 0;
                }
            });
            webView_youtube_list = (WebView) findViewById(R.id.webView_youtube_list);
            webView_youtube_list.getSettings().setJavaScriptEnabled(true);
            webView_youtube_list.setWebViewClient(new webViewClient());
            webView_youtube_list.canGoBack();
            webView_youtube_list.loadUrl(youtubeHome);
        } else {
            viewStub.setLayoutResource(R.layout.component_no_internet);
            viewStub.inflate();
            isExit = true;
            bt_retry_connect = (Button) findViewById(R.id.bt_retry_connect);
            bt_settings = (Button) findViewById(R.id.bt_settings);
            bt_exit_app = (Button) findViewById(R.id.bt_exit_app);
            bt_retry_connect.setOnClickListener(this);
            bt_settings.setOnClickListener(this);
            bt_exit_app.setOnClickListener(this);
        }
    }

    private class webViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap bitmap) {
            super.onPageStarted(view, url, bitmap);
            LogUtil.show("Main Page Loading to ", url);
            HandleMessage.set(handler, "refresh_start");
            currentUrl = url;
        }

        @Override
        public void onPageFinished(WebView view, String str) {
            super.onPageFinished(view, str);
            HandleMessage.set(handler, "refresh_finish");
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.contains("?app=desktop") && !url.contains("signin?app=desktop")) {
                Toast.makeText(activity, R.string.desktop_not_support, Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (String.valueOf(request.getUrl()).contains("http://m.youtube.com/watch?") ||
                        String.valueOf(request.getUrl()).contains("https://m.youtube.com/watch?")) {
                    LogUtil.show("loading URL => ", String.valueOf(request.getUrl()));

                    String url = String.valueOf(request.getUrl());
                    videoID = url.substring(url.indexOf("&v=") + 3, url.length());
                    String listID = url.substring(url.indexOf("&list=") + 6, url.length());
                    Pattern pattern = Pattern.compile(
                            "([A-Za-z0-9_-]+)&[\\w]+=.*",
                            Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(listID);
                    playListID = "";
                    if (matcher.matches()) playListID = matcher.group(1);
                    if (listID.contains("m.youtube.com")) {
                        playListID = null;
                    } else {
                        Config.linkType = 1;
                    }

                    HandleMessage.set(handler, "startService", playListID);

                    LogUtil.show("loading Video ID => ", url);
                    LogUtil.show("loading List ID => ", String.valueOf(listID));
                    LogUtil.show("loading PlayList ID => ", playListID);

                }
            }
            return super.shouldInterceptRequest(view, request);
        }
    }

    private class GetYoutubeSuggestion extends Thread {
        public String keyword = "";

        private void setData(String keyword) {
            this.keyword = keyword;
        }

        @Override
        public void run() {
            ResponseModel responseModel = API.getYoutubeSuggest(this.keyword);
            if (responseModel.getResponseCode() == 200) {
                HandleMessage.set(handler, "showSuggestionList", responseModel.getMessgae());
            } else if (responseModel.getResponseCode() == 9999) {
                //沒有網路
                HandleMessage.set(handler, "check_internet");
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_settings:
                startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
                break;
            case R.id.bt_retry_connect:
                activity.recreate();
                break;
            case R.id.bt_exit_app:
                finish();
                break;
        }
    }

    @Override
    public void onRefresh() {
        HandleMessage.set(handler, "refresh_start");
        webView_youtube_list.loadUrl(webView_youtube_list.getUrl());
    }

    @SuppressLint("HandlerLeak")
    private void Handler() {
        handler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.getData().getString("title", "")) {
                    case "check_internet":
                        Toast.makeText(MainActivity.this, getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
                        break;
                    case "refresh_start":
                        swipeRefreshLayout.setRefreshing(true);
                        break;
                    case "refresh_finish":
                        swipeRefreshLayout.setRefreshing(false);
                        break;
                    case "startService":
                        String playListID = msg.getData().getString("message", null);
                        webView_youtube_list.stopLoading();
                        webView_youtube_list.goBack();
                        if (Service.isRunning(activity, PlayerService.class)) {
                            Log.d("Service : ", "Already Running!");
                            Bundle bundle = new Bundle();
                            bundle.putString("VIDEO_ID", videoID);
                            bundle.putString("PLAYLIST_ID", playListID);
                            HandleMessage.set(PlayerService.handler, "startVideo", bundle);
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !android.provider.Settings.canDrawOverlays(MainActivity.this)) {
                                Intent i = new Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:" + getPackageName()));
                                startActivityForResult(i, OVERLAY_PERMISSION_REQ_CODE);
//                                Intent intent = new Intent(MainActivity.this, GetPermission.class);
//                                startActivity(intent);
                            } else {
                                Intent i = new Intent(MainActivity.this, PlayerService.class);
                                i.putExtra("VIDEO_ID", videoID);
                                i.putExtra("PLAYLIST_ID", playListID);
                                i.setAction(Config.ACTION.STARTFOREGROUND_WEB_ACTION);
                                startService(i);
                            }
                        }
                        break;
                    case "showSuggestionList":
                        final String suggestList = msg.getData().getString("message", null);
                        try {
                            JSONArray suggestList_ = new JSONArray(suggestList);
                            JSONArray suggestListArray = (JSONArray) suggestList_.get(1);
                            ArrayList<String> suggestions = new ArrayList<>();
                            for (int i = 0; i < 10; i++) {
                                String suggestion = suggestListArray.get(i).toString();
                                if (null != suggestion) {
                                    suggestions.add(suggestion);
                                }
                            }
                            String[] columnNames = {"_id", "suggestion"};
                            MatrixCursor cursor = new MatrixCursor(columnNames);
                            String[] temp = new String[2];
                            int id = 0;
                            for (String item : suggestions) {
                                if (item != null) {
                                    temp[0] = Integer.toString(id++);
                                    temp[1] = item;
                                    cursor.addRow(temp);
                                }
                            }
                            CursorAdapter cursorAdapter = new CursorAdapter(getApplicationContext(), cursor, false) {
                                @Override
                                public View newView(Context context, Cursor cursor, ViewGroup parent) {
                                    return LayoutInflater.from(context).inflate(R.layout.component_search_suggestion_list_item, parent, false);
                                }

                                @Override
                                public void bindView(View view, Context context, Cursor cursor) {
                                    final Button suggest = (Button) view.findViewById(R.id.bt_suggest);
                                    String body = cursor.getString(cursor.getColumnIndexOrThrow("suggestion"));
                                    suggest.setText(body);
                                    suggest.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            searchView.setQuery(suggest.getText(), true);
                                            searchView.clearFocus();
                                        }
                                    });
                                }
                            };
                            searchView.setSuggestionsAdapter(cursorAdapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        };
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        }
        if (currentUrl.equals("https://m.youtube.com/")) {
            isExit = true;
            Toast.makeText(this, getString(R.string.press_again_to_exit), Toast.LENGTH_SHORT).show();
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false;
                }
            }, 2000);
        } else {
            webView_youtube_list.goBack();
        }
        if (isExit) {
            super.onBackPressed();
            return;
        }

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
