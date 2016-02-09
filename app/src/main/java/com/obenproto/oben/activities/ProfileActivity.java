package com.obenproto.oben.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.obenproto.oben.R;
import com.obenproto.oben.api.ObenAPIClient;
import com.obenproto.oben.api.ObenAPIService;
import com.obenproto.oben.response.ObenApiResponse;

import java.net.HttpURLConnection;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class ProfileActivity extends Activity {

    TextView userIDTxt, avatarIDTxt, userEmailTxt;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    ProgressBar progressBar;
    TextView setupAvatar, logoutTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.profile_activity);

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        editor = pref.edit();

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        userIDTxt = (TextView) findViewById(R.id.userIDLbl);
        avatarIDTxt = (TextView) findViewById(R.id.avatarIDLbl);
        userEmailTxt = (TextView) findViewById(R.id.userEmailLbl);

        setupAvatar = (TextView) findViewById(R.id.setUpAvatarLbl);
        setupAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, OptionActivity.class);
                startActivity(intent);
                finish();
            }
        });

        logoutTxt = (TextView) findViewById(R.id.logoutLbl);
        logoutTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onUserLogout();
                logoutTxt.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);
            }
        });

        // User login
        onUserLogin(pref.getString("userEmail", ""), "ObenSesame");
    }

    // Recall of user avatar.
    public void onGetUserAvatar(int userId) {
        // Email login.
        ObenAPIService client = ObenAPIClient.newInstance(ObenAPIService.class);
        Call<ObenApiResponse> call = client.getUserAvatar(userId);

        call.enqueue(new Callback<ObenApiResponse>() {
            @Override
            public void onResponse(Response<ObenApiResponse> response, Retrofit retrofit) {
                if (response.code() == HttpURLConnection.HTTP_OK) { // success
                    ObenApiResponse response_result = response.body();
                    int avatarId = response_result.UserAvatar.getAvatarId();
                    Log.d("avatar ID ", String.valueOf(avatarId));

                    // Save the avatar ID to shared preference.
                    editor.putInt("avatarID", avatarId);
                    editor.commit();

                    progressBar.setVisibility(View.GONE);

                    userIDTxt.setText(String.valueOf(pref.getInt("userID", 0)));
                    avatarIDTxt.setText(String.valueOf(avatarId));
                    userEmailTxt.setText(pref.getString("userEmail", ""));

                    setupAvatar.setEnabled(true);
                    logoutTxt.setEnabled(true);

                } else if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    Log.d("Status", "Http Unauthorized");

                } else {
                    Log.d("Status", "Server Connection Failure");
                }
            }

            @Override
            public void onFailure(Throwable t) {
                userIDTxt.setText(String.valueOf(pref.getInt("userID", 0)));
                avatarIDTxt.setText(String.valueOf(0));
                userEmailTxt.setText(pref.getString("userEmail", ""));
                progressBar.setVisibility(View.GONE);

                setupAvatar.setEnabled(true);
                logoutTxt.setEnabled(true);

                Log.d("Failure", t.getMessage());
            }
        });
    }

    // Recall of user logout
    public void onUserLogout() {
        // Email login.
        ObenAPIService client = ObenAPIClient.newInstance(ObenAPIService.class);
        Call<ObenApiResponse> call = client.userLogout();

        call.enqueue(new Callback<ObenApiResponse>() {
            @Override
            public void onResponse(Response<ObenApiResponse> response, Retrofit retrofit) {
                progressBar.setVisibility(View.GONE);

                if (response.code() == HttpURLConnection.HTTP_OK) { // success
                    ObenApiResponse response_result = response.body();
                    String message = response_result.User.getMessage();
                    Log.d("Logout Sucess:", message);

                    // Save the avatar ID to shared preference.
                    editor.putString("userEmail", "");
                    editor.putInt("userID", 0);
                    editor.putInt("avatarID", 0);
                    editor.putInt("RegularAvatarID", 0);
                    editor.putInt("CommercialAvatarID", 0);
                    editor.putInt("FreestyleAvatarID", 0);
                    editor.commit();

                    // Go to the Login page.
                    startActivity(new Intent(ProfileActivity.this, ObenUserLogin.class));
                    finish();

                } else if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    Log.d("Status", "Http Unauthorized");
                    logoutTxt.setEnabled(true);

                } else {
                    Log.d("Status", "Server Connection Failure");
                    logoutTxt.setEnabled(true);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("Failure", t.getMessage());
                logoutTxt.setEnabled(true);
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    public void onUserLogin(String email, String password) {
        progressBar.setVisibility(View.VISIBLE);
        setupAvatar.setEnabled(false);
        logoutTxt.setEnabled(false);

        ObenAPIService client = ObenAPIClient.newInstance(ObenAPIService.class);
        Call<ObenApiResponse> call = client.userLogin(email, password, "Oben User");

        call.enqueue(new Callback<ObenApiResponse>() {
            @Override
            public void onResponse(Response<ObenApiResponse> response, Retrofit retrofit) {
                if (response.code() == HttpURLConnection.HTTP_OK) { // success
                    ObenApiResponse response_result = response.body();

                    int userId = response_result.User.getUserId();
                    editor.putInt("userID", userId);
                    editor.commit();

                    // Get the user avatar ID.
                    onGetUserAvatar(response_result.User.getUserId());

                } else if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    Log.d("User login Status", "Http Unauthorized");

                } else {
                    Log.d("User login Status", "Server Connection Failure");
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("Upload", t.getMessage());
            }
        });
    }
}