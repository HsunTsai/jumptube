package com.turtle.hsun.jumptube.Custom.Utils;

import android.support.design.widget.Snackbar;
import android.view.View;

import com.android.volley.Response;
import com.android.volley.VolleyError;

public class NetworkErrorHandler {
    public static Response.ErrorListener Listener(final View view) {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (null == volleyError.networkResponse) {
                    Snackbar.make(view, "please check your Internet", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                switch (volleyError.networkResponse.statusCode) {
                    case 404:
                        Snackbar.make(view, "找不到", Snackbar.LENGTH_SHORT).show();
                        break;
                }
            }
        };
    }
}


//left and right icon
//    TSnackbar snackbar = TSnackbar
//            .make(relative_layout_main, "Snacking Left & Right", TSnackbar.LENGTH_LONG);
//         snackbar.setActionTextColor(Color.WHITE);
//                 snackbar.setIconLeft(R.mipmap.ic_core,24); //Size in dp - 24 is great!
//                 snackbar.setIconRight(R.drawable.ic_android_green_24dp,48); //Resize to bigger dp
//                 snackbar.setIconPadding(8);
//                 snackbar.setMaxWidth(3000); //if you want fullsize on tablets
//                 View snackbarView=snackbar.getView();
//                 snackbarView.setBackgroundColor(Color.parseColor("#CC00CC"));
//                 TextView textView=(TextView)snackbarView.findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text);
//                 textView.setTextColor(Color.YELLOW);
//                 snackbar.show();