package com.obenproto.oben.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.app.Activity;
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

public class OptionActivity extends Activity {

    TextView regularLbl, commercialLbl, freestyleLbl;
    TextView cancelLbl, logoutLbl;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    ProgressBar progressBar;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.option_activity);

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        editor = pref.edit();

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        regularLbl = (TextView)findViewById(R.id.regularLbl);
        regularLbl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(OptionActivity.this, RegularActivity.class));
            }
        });

        commercialLbl = (TextView)findViewById(R.id.commercialLbl);
        commercialLbl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(OptionActivity.this, CommercialActivity.class));
            }
        });

        freestyleLbl = (TextView)findViewById(R.id.freestyleLbl);
        freestyleLbl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(OptionActivity.this, FreestyleActivity.class));
            }
        });

        cancelLbl = (TextView) findViewById(R.id.cancelBtn);
        cancelLbl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(OptionActivity.this, ProfileActivity.class));
                finish();
            }
        });

        logoutLbl = (TextView) findViewById(R.id.logoutBtn);
        logoutLbl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onUserLogout();
                logoutLbl.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);
            }
        });
    }


    @Override
    public void onBackPressed() {
        startActivity(new Intent(OptionActivity.this, ProfileActivity.class));
        finish();
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
                    startActivity(new Intent(OptionActivity.this, ObenUserLogin.class));
                    finish();

                } else if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    Log.d("Status", "Http Unauthorized");
                    logoutLbl.setEnabled(true);

                } else {
                    Log.d("Status", "Server Connection Failure");
                    logoutLbl.setEnabled(true);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("Failure", t.getMessage());
                progressBar.setVisibility(View.GONE);
                logoutLbl.setEnabled(true);
            }
        });
    }
}