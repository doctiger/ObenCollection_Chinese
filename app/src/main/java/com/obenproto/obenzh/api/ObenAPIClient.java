package com.obenproto.obenzh.api;

import android.util.Base64;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.concurrent.TimeUnit;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

public class ObenAPIClient {

    public static final String BASE_URL = "https://oben.us/";

    private final static OkHttpClient httpClient = new OkHttpClient();

    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create());

    public static void init() {
        httpClient.interceptors().add(new Interceptor() {
            @Override
            public Response intercept(Interceptor.Chain chain) throws IOException {
                Request original = chain.request();

                String userCredentials = "ObenUp:ObenSesame!";
                String basicAuth = "Basic " + new String(
                        Base64.encode(userCredentials.getBytes(), Base64.NO_WRAP));
                Request request = original.newBuilder()
                        .header("Authorization", basicAuth)
                        .method(original.method(), original.body())
                        .build();

                return chain.proceed(request);
            }
        });

        //create a cookieManager so your client can be cookie persistant
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        httpClient.setCookieHandler(cookieManager); //finally set the cookie handler on client
    }

    public static <S> S newInstance(Class<S> serviceClass) {

        httpClient.setReadTimeout(120, TimeUnit.SECONDS);
        httpClient.setConnectTimeout(120, TimeUnit.SECONDS);

        Retrofit retrofit = builder.client(httpClient).build();
        return retrofit.create(serviceClass);
    }
}
