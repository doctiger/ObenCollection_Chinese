package com.obenproto.obenzh;

import android.app.Application;

import com.obenproto.obenzh.api.APIClient;
import com.obenproto.obenzh.utils.CommonUtils;
import com.obenproto.obenzh.utils.LocalStorage;

public class ObenApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        initSingletons();
    }

    private void initSingletons() {
        APIClient.init();
        LocalStorage.init(this);
        CommonUtils.init(this);
    }
}
