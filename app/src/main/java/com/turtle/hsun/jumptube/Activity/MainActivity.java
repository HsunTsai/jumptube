package com.turtle.hsun.jumptube.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewStub;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.javiersantos.appupdater.AppUpdater;
import com.github.javiersantos.appupdater.enums.Display;
import com.github.javiersantos.appupdater.enums.UpdateFrom;
import com.turtle.hsun.jumptube.Config.Config;
import com.turtle.hsun.jumptube.Custom.Components.CustomSwipeRefresh;
import com.turtle.hsun.jumptube.Custom.Utils.NetworkErrorHandler;
import com.turtle.hsun.jumptube.Custom.Utils.SuggestAdapter;
import com.turtle.hsun.jumptube.PlayerService;
import com.turtle.hsun.jumptube.R;
import com.turtle.hsun.jumptube.Utils.Decode;
import com.turtle.hsun.jumptube.Utils.HandleMessage;
import com.turtle.hsun.jumptube.Utils.Internet;
import com.turtle.hsun.jumptube.Utils.LogUtil;
import com.turtle.hsun.jumptube.Utils.Service;
import com.turtle.hsun.jumptube.Utils.UITransform;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Timer;
import java.util.TimerTask;

import static com.turtle.hsun.jumptube.Config.Config.OVERLAY_PERMISSION_REQ_CODE;
import static com.turtle.hsun.jumptube.Config.Config.webAccountPage;
import static com.turtle.hsun.jumptube.Config.Config.webHomePage;
import static com.turtle.hsun.jumptube.Config.Config.webSubscriptionPage;
import static com.turtle.hsun.jumptube.Config.Config.webTrendingPage;
import static com.turtle.hsun.jumptube.Config.Config.sharedPreferences;
import static com.turtle.hsun.jumptube.Config.Config.youtubeSuggestURL;
import static com.turtle.hsun.jumptube.Config.ServerDomain.appVersionUrl;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SearchView.OnQueryTextListener, View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    //Component
    private Activity activity;
    private LinearLayout layout;
    private WebView webView_youtube_list;
    private Button bt_retry_connect, bt_settings, bt_exit_app;
    private SearchView searchView;
    private SearchView.SearchAutoComplete searchAutoComplete;
    private ViewStub viewStub;
    private CustomSwipeRefresh swipeRefreshLayout;

    //Parameter
    private RequestQueue queue;
    private String currentUrl, videoID, playListID, searchHistory = "[]";
    private Boolean isExit = false, canSearch = true;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        activity = this;
        //init playing quality
        Config.playbackQuality = sharedPreferences.getInt(getString(R.string.videoQuality), 0);
        queue = Volley.newRequestQueue(this);
        layout = (LinearLayout) findViewById(R.id.layout);
        Handler();
        initView();

        //check app version
        checkAppVersion();
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
            webView_youtube_list.loadUrl(webHomePage);
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

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setOnQueryTextListener(this);

            //searchAutoComplete default hold query with 2 words,
            //if we want to query when keyword length equal one word, we should setThreshold
            searchAutoComplete = (SearchView.SearchAutoComplete) searchView.findViewById(R.id.search_src_text);
            searchAutoComplete.setThreshold(0);
            searchHistory = sharedPreferences.getString("searchHistory", "[]");
            HandleMessage.set(handler, "showSuggestionList", "[\"歷史資料\"," + searchHistory + "]");
        }
        // get AutoCompleteTextView from SearchView
        final AutoCompleteTextView searchEditText = (AutoCompleteTextView) searchView.findViewById(R.id.search_src_text);
        final View dropDownAnchor = searchView.findViewById(searchEditText.getDropDownAnchor());
        if (dropDownAnchor != null) {
            dropDownAnchor.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                           int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    int point[] = new int[2];
                    dropDownAnchor.getLocationOnScreen(point);
                    Rect screenSize = new Rect();
                    getWindowManager().getDefaultDisplay().getRectSize(screenSize);
                    int screenWidth = screenSize.width();
                    searchEditText.setDropDownWidth(screenWidth);
                }
            });
        }
        return true;
    }

    @Override
    public boolean onQueryTextChange(final String keyword) {
        if (keyword.length() > 0) {
            StringRequest getSuggestArray = new StringRequest(Request.Method.GET, youtubeSuggestURL + keyword,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            HandleMessage.set(handler, "showSuggestionList", Decode.unicodeToUtf8(response));
                        }
                    }, NetworkErrorHandler.Listener(layout));
            queue.add(getSuggestArray);
        } else {
            if (searchHistory.length() > 4) {
                HandleMessage.set(handler, "showSuggestionList", "[\"歷史資料\"," + searchHistory + "]");
            } else {
                searchAutoComplete.dismissDropDown();
            }
        }
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if (Internet.isAvailable(activity)) {
            webView_youtube_list.loadUrl("http://m.youtube.com/results?q=" + query);
            searchView.clearFocus();
            //紀錄搜尋內容
            try {
                JSONArray searchHistoryArray = new JSONArray(searchHistory);
                for (int i = searchHistoryArray.length() - 1; i > -1; --i) {
                    if (searchHistoryArray.getString(i).equals(query)) searchHistoryArray.remove(i);
                }
                if (searchHistoryArray.length() > 5) searchHistoryArray.remove(5);
                for (int i = searchHistoryArray.length() - 1; i > -1; --i) {
                    searchHistoryArray.put((i + 1), searchHistoryArray.getString(i));
                }
                searchHistoryArray.put(0, query);
                searchHistory = searchHistoryArray.toString();
                sharedPreferences.edit().putString("searchHistory", searchHistory).apply();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            activity.recreate();
        }
        return true;
    }

    private class webViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap bitmap) {
            super.onPageStarted(view, url, bitmap);
            LogUtil.show("Main Page Loading to ", url);
            HandleMessage.set(handler, "refresh_start");
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            currentUrl = url;
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
        public void onReceivedError(WebView view, WebResourceRequest request,
                                    WebResourceError error) {
            super.onReceivedError(view, request, error);
            Snackbar.make(layout, getString(R.string.load_error), Snackbar.LENGTH_SHORT).show();
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (String.valueOf(request.getUrl()).contains("http://m.youtube.com/watch?") ||
                        String.valueOf(request.getUrl()).contains("https://m.youtube.com/watch?")) {
                    if (Internet.isAvailable(activity)) {
                        LogUtil.show("loading URL => ", String.valueOf(request.getUrl()));
                        String url = String.valueOf(request.getUrl());
                        Uri uri = Uri.parse(url);
                        videoID = uri.getQueryParameter("v");
                        playListID = uri.getQueryParameter("list");
                        if (null == playListID) {
                            //this url is single video
                            //do nothing
                            LogUtil.show("Playing Single Video ID => ", videoID);
                        } else {
                            //this url is play list
                            Config.linkType = 1;
                            LogUtil.show("Playing Video List ID => ", playListID);
                        }
                        HandleMessage.set(handler, "startService", playListID);
                    }
                }
            }
            return super.shouldInterceptRequest(view, request);
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
                        webView_youtube_list.loadUrl(currentUrl);
                        if (Service.isRunning(activity, PlayerService.class)) {
                            LogUtil.show("Service => ", "Already Running!");
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
                        String suggestList = msg.getData().getString("message", null);
                        SuggestAdapter.set(suggestList, getApplicationContext(), searchView);
                        searchAutoComplete.showDropDown();
                        break;
                }
            }
        };
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        searchAutoComplete.dismissDropDown();
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        }
        if (isExit) {
            super.onBackPressed();
            return;
        }
        if (currentUrl.contains(webAccountPage) || currentUrl.contains(webTrendingPage) || currentUrl.contains(webSubscriptionPage)) {
            webView_youtube_list.loadUrl(webHomePage);
        } else if (currentUrl.equals(webHomePage)) {
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
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_version:
                return true;
            case R.id.nav_learn_more:
                break;
            case R.id.nav_home_page:
                webView_youtube_list.loadUrl(webHomePage);
                break;
            case R.id.nav_trending_page:
                webView_youtube_list.loadUrl(webTrendingPage);
                break;
            case R.id.nav_account_page:
                webView_youtube_list.loadUrl(webAccountPage);
                break;
            case R.id.nav_video_quality:
                final Integer[] checkedIndex = {0};
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(getString(R.string.video_quality));
                builder.setPositiveButton(getString(R.string.done), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //set Playing quality
                        Config.playbackQuality = checkedIndex[0];
                        sharedPreferences.edit()
                                .putInt(getString(R.string.videoQuality), Config.playbackQuality).apply();
                        HandleMessage.set(PlayerService.handler, "setPlaybackQuality", String.valueOf(Config.playbackQuality));
                        LogUtil.show("New Video Quality => ", Config.getPlaybackQuality());
                    }
                });
                String[] items = {"Auto", "1080p", "720p", "480p", "360p", "240p", "144p"};
                builder.setSingleChoiceItems(items, Config.playbackQuality, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int ith) {
                        checkedIndex[0] = ith;
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
            default:
                Toast.makeText(this, getString(R.string.unStart), Toast.LENGTH_SHORT).show();

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void checkAppVersion() {
        new AppUpdater(this)
                .setUpdateFrom(UpdateFrom.JSON)
                .setUpdateJSON(appVersionUrl)
                .setTitleOnUpdateAvailable(getString(R.string.version_available))
//                .setContentOnUpdateAvailable("Check out the latest version available of my app!")
//                .setTitleOnUpdateNotAvailable("Update not available")
//                .setContentOnUpdateNotAvailable("No update available. Check for updates again later!")
                .setButtonUpdate(getString(R.string.update_now))
//                .setButtonUpdateClickListener(...)
                .setButtonDismiss(getString(R.string.later))
//                .setButtonDismissClickListener(...)
                .setButtonDoNotShowAgain(null)
//                .setButtonDoNotShowAgainClickListener(...)
//                .setIcon(R.drawable.ic_update) // Notification icon
                .setCancelable(false)
                .setDisplay(Display.DIALOG)
                .showAppUpdated(false)
                .start();
    }
}
