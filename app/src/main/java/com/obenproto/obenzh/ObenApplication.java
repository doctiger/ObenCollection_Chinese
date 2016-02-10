package com.obenproto.obenzh;

import android.app.Application;

import com.obenproto.obenzh.api.ObenAPIClient;

/**
 * Created by Petro Rington on 12/10/2015.
 */
public class ObenApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ObenAPIClient.init();
    }
}
