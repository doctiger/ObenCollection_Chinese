package com.obenproto.obenzh.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import com.obenproto.obenzh.R;
import com.obenproto.obenzh.activities.base.BaseActivity;
import com.obenproto.obenzh.api.APIClient;
import com.obenproto.obenzh.api.domain.ObenUser;
import com.obenproto.obenzh.api.response.LoginResponse;

import java.net.HttpURLConnection;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class LoginActivity extends BaseActivity implements View.OnClickListener {

    EditText emailText, passwordText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        emailText = (EditText) findViewById(R.id.emailText);
        passwordText = (EditText) findViewById(R.id.passwordText);

        findViewById(R.id.btn_login).setOnClickListener(this);

        ObenUser user = ObenUser.getSavedUser();
        if (user != null) {
            String defaultPassword = "ObenSesame";
            emailText.setText(user.email);
            passwordText.setText(defaultPassword);
            requestLogin(user.email, defaultPassword);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_login) {
            checkAndRequest();
        }
    }

    private void checkAndRequest() {
        String email = emailText.getText().toString().trim();
        String password = passwordText.getText().toString().trim();

        // Compare user login info.
        if (TextUtils.isEmpty(email)) {
            helperUtils.showMessage(R.string.Empty_Email);
            helperUtils.shakeForError(emailText);
        } else if (TextUtils.isEmpty(password)) {
            helperUtils.showMessage(R.string.Empty_Password);
            helperUtils.shakeForError(passwordText);
        } else if (!helperUtils.validateEmail(email)) {
            helperUtils.showMessage(R.string.Invalid_Email);
            helperUtils.shakeForError(emailText);
        } else {
            requestLogin(email, password);
        }
    }

    private void requestLogin(final String email, String password) {
        showProgress();
        Call<LoginResponse> call = APIClient.getAPIService().userLogin(email, password, "Oben User");
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Response<LoginResponse> response, Retrofit retrofit) {
                dismissProgress();
                if (response.code() == HttpURLConnection.HTTP_OK) {
                    ObenUser user = response.body().User;
                    if (user.login.equalsIgnoreCase("SUCCESS")) {
                        user.saveToStorage();
                        Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        helperUtils.showMessage(user.message);
                    }
                } else {
                    helperUtils.showMessage(R.string.Network_Error);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                dismissProgress();
                helperUtils.showMessage(t.getLocalizedMessage());
            }
        });
    }
}