package com.obenproto.obenzh.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.obenproto.obenzh.R;
import com.obenproto.obenzh.activities.base.BaseActivity;
import com.obenproto.obenzh.api.APIClient;
import com.obenproto.obenzh.api.domain.AvatarInfo;
import com.obenproto.obenzh.api.domain.ObenUser;
import com.obenproto.obenzh.api.response.GetAllUserAvatarsResponse;

import java.net.HttpURLConnection;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class ProfileActivity extends BaseActivity implements View.OnClickListener {

    TextView tvUserID, tvEmail, tvRegular;
    RelativeLayout progressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_profile);

        progressView = (RelativeLayout) findViewById(R.id.layout_progress_view);
        tvUserID = (TextView) findViewById(R.id.tv_user_id);
        tvEmail = (TextView) findViewById(R.id.tv_user_email);
        tvRegular = (TextView) findViewById(R.id.tv_regular_avatar);

        // Map event handlers.
        findViewById(R.id.setUpAvatarLbl).setOnClickListener(this);
        findViewById(R.id.logoutLbl).setOnClickListener(this);

        // Setup user info.
        ObenUser user = ObenUser.getSavedUser();
        if (user != null) {
            tvUserID.setText(String.valueOf(user.userId));
            tvEmail.setText(user.email);
        }

        helperUtils.avatarLoaded = false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Recall getUserAvatar endpoint.
        getAllUserAvatars();
    }

    private void getAllUserAvatars() {
        ObenUser user = ObenUser.getSavedUser();
        if (user != null && !helperUtils.avatarLoaded) {
            progressView.setVisibility(View.VISIBLE);
            Call<GetAllUserAvatarsResponse> call = APIClient.getAPIService().getAllUserAvatars(user.userId);
            call.enqueue(new Callback<GetAllUserAvatarsResponse>() {
                @Override
                public void onResponse(Response<GetAllUserAvatarsResponse> response, Retrofit retrofit) {
                    progressView.setVisibility(View.GONE);
                    if (response.code() == HttpURLConnection.HTTP_OK) {
                        if (response.body() != null) {
                            helperUtils.avatarLoaded = true;
                            showAvatarInfo(response.body());
                        }
                    } else if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                        helperUtils.showMessage(R.string.unauthorized_toast);
                        showLoginPage();
                    } else {
                        helperUtils.showMessage(R.string.Network_Error);
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    progressView.setVisibility(View.GONE);
                    helperUtils.showMessage(t.getLocalizedMessage());
                }
            });
        }
    }

    private void showAvatarInfo(GetAllUserAvatarsResponse response) {
        String notExist = "n/a";
        AvatarInfo chineseRegular = response.getAvatar(CHINESE_REGULAR_MODE);
        if (chineseRegular != null) {
            tvRegular.setText(String.valueOf(chineseRegular.Avatar.avatarId));
        } else {
            tvRegular.setText(notExist);
        }

        // Save loaded avatar info.
        helperUtils.chineseRegular = chineseRegular;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.setUpAvatarLbl) {
            Intent intent = new Intent(ProfileActivity.this, RegularActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.logoutLbl) {
            requestLogout();
        }
    }

    private void requestLogout() {
        ObenUser.removeSavedUser();
        showLoginPage();
    }

    private void showLoginPage() {
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}