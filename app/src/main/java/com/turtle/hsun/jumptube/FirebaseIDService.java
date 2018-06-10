package com.turtle.hsun.jumptube;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.turtle.hsun.jumptube.Config.Config;
import com.turtle.hsun.jumptube.Utils.LogUtil;

public class FirebaseIDService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.e("TAG", "Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
//        sendRegistrationToServer(refreshedToken);
    }

//    private void sendRegistrationToServer(String token) {
//        // Add custom implementation, as needed.
//        LogUtil.show("FireBase token: ", token);
//        Config.sharedPreferences.edit().putString("push_token", token).apply();
//    }
}
