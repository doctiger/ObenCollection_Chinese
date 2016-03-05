package com.obenproto.obenzh.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.obenproto.obenzh.R;
import com.obenproto.obenzh.api.ObenAPIClient;
import com.obenproto.obenzh.api.ObenAPIService;
import com.obenproto.obenzh.response.ObenApiResponse;

import java.net.HttpURLConnection;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class ObenUserLogin extends Activity {

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    public static EditText emailText, passwordText;
    String userLogin;
    int userID, avatarID;
    String email, email_pattern, password;
    public static ProgressBar progressBar;
    String errorMsg = "ERROR";
    String successMsg = "SUCCESS";

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#80DAEB"),
                android.graphics.PorterDuff.Mode.MULTIPLY);
        progressBar.setVisibility(View.GONE);

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        editor = pref.edit();

        // Get the shareedpreference.
        if (!pref.getString("userEmail", "").equals("")) {
            Log.d("Already registered", "*****");

            editor.putString("InitialLogin", "no");
            editor.apply();

            Intent intent = new Intent(ObenUserLogin.this, ProfileActivity.class);
            startActivity(intent);
            finish();
        }

        emailText = (EditText) findViewById(R.id.emailText);
        passwordText = (EditText) findViewById(R.id.passwordText);
        TextView textView = (TextView) findViewById(R.id.loginLbl);

        email_pattern = "[a-zA-Z0-9._-]+@[a-z0-9]+\\.+[a-z]+";

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = emailText.getText().toString().trim();
                password = passwordText.getText().toString().trim();
                userLogin = null;

                // Compare user login info.
                if (TextUtils.isEmpty(email)) {
                    emailText.setError("請填寫此領域");
                    emailText.focusSearch(View.FOCUS_DOWN);

                } else if (TextUtils.isEmpty(password)) {
                    passwordText.setError("請填寫此領域");
                    passwordText.focusSearch(View.FOCUS_DOWN);

                } else if (email.matches(email_pattern)) {
                    // Send the request with email, password, name.
                    onUserLogin(email, password);

                    progressBar.setVisibility(View.VISIBLE);

                } else {
                    emailText.setError("電子郵件格式必須是：abc@abc.com");
                    emailText.focusSearch(View.FOCUS_DOWN);

                }
            }
        });
    }

    public void onUserLogin(final String email, String password) {
        // Email login.
        Log.d("Connected", "OK");
        ObenAPIService client = ObenAPIClient.newInstance(ObenAPIService.class);
        Call<ObenApiResponse> call = client.userLogin(email, password, "Oben User");

        call.enqueue(new Callback<ObenApiResponse>() {
            @Override
            public void onResponse(Response<ObenApiResponse> response, Retrofit retrofit) {
                if (response.code() == HttpURLConnection.HTTP_OK) { // success
                    ObenApiResponse response_result = response.body();
                    userLogin = response_result.User.getLogin();
                    userID = response_result.User.getUserId();

                    Log.d("User Login Status :", String.valueOf(userLogin));

                    if (userLogin.equals(successMsg)) {
                        // Save the login infomation to sharedpreference.
                        editor.putString("InitialLogin", "yes");
                        editor.putString("userEmail", email);
                        editor.putInt("userID", userID);
                        editor.apply();

//                        passwordText.setError("Login Success");
                        Intent intent = new Intent(ObenUserLogin.this, ProfileActivity.class);
                        startActivity(intent);
                        finish();

                    } else if (userLogin.equals(errorMsg)) {
                        progressBar.setVisibility(View.GONE);
                        passwordText.setError("无效的密码");
                        passwordText.focusSearch(View.FOCUS_DOWN);

                    }

                } else if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    Log.d("User Login Status :", "Http Unauthorized");
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), R.string.unauthorized_toast_msg, Toast.LENGTH_LONG).show();

                } else {
                    Log.d("User Login Status", "Server Connection Failure");
                    userLogin = "Connection Failure";
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("failure", t.getMessage());
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}