package com.obenproto.obenzh.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.obenproto.obenzh.R;
import com.obenproto.obenzh.activities.base.BaseActivity;
import com.obenproto.obenzh.api.APIClient;
import com.obenproto.obenzh.api.domain.ObenPhrase;
import com.obenproto.obenzh.api.domain.ObenUser;
import com.obenproto.obenzh.api.domain.ObenUserAvatar;
import com.obenproto.obenzh.api.response.GetAllUserAvatarsResponse;
import com.obenproto.obenzh.api.response.GetAvatarResponse;
import com.obenproto.obenzh.api.response.GetPhrasesResponse;
import com.obenproto.obenzh.api.response.SaveUserAvatarResponse;
import com.obenproto.obenzh.recorder.ExtAudioRecorder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;

import java.io.File;
import java.net.HttpURLConnection;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class RegularActivity extends BaseActivity implements View.OnClickListener {

    private static final int LIMIT_COUNT = 35;
    private final int CURRENT_MODE = CHINESE_REGULAR_MODE;

    RelativeLayout progressView;
    ListView listView;
    LayoutInflater inflater;

    Integer avatarID = null;
    GetPhrasesResponse phrasesData;
    GetAvatarResponse avatarData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_regular);

        // Map view elements to class members.
        progressView = (RelativeLayout) findViewById(R.id.layout_progress_view);
        listView = (ListView) findViewById(R.id.listView);
        inflater = LayoutInflater.from(this);

        // Map event handlers.
        findViewById(R.id.cancelBtn).setOnClickListener(this);

        // Recall get all phrases endpoint to fetch all phrases for regular mode.
        getAllPhrases();
    }

    @Override
    protected void showProgress() {
        progressView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void dismissProgress() {
        progressView.setVisibility(View.GONE);
    }

    private void getAllPhrases() {
        showProgress();
        Call<GetPhrasesResponse> call = APIClient.getAPIService().getPhrases(CURRENT_MODE);
        call.enqueue(new Callback<GetPhrasesResponse>() {
            @Override
            public void onResponse(Response<GetPhrasesResponse> response, Retrofit retrofit) {
                dismissProgress();
                if (response.code() == HttpURLConnection.HTTP_OK) {
                    phrasesData = response.body();
                    getAvatar();
                } else if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    helperUtils.showMessage(R.string.unauthorized_toast);
                    requestLogout();
                } else {
                    helperUtils.showMessage(R.string.Network_Error);
                    finish();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                dismissProgress();
                helperUtils.showMessage(t.getLocalizedMessage());
                finish();
            }
        });
    }

    private void getAvatar() {
        if (helperUtils.avatarLoaded) {
            if (helperUtils.chineseRegular != null) {
                avatarID = helperUtils.chineseRegular.Avatar.avatarId;
                getRecordedSentences();
            } else {
                populateListView();
            }
        } else {
            getAllUserAvatars();
        }
    }

    private void getAllUserAvatars() {
        ObenUser user = ObenUser.getSavedUser();
        if (user != null) {
            Call<GetAllUserAvatarsResponse> call = APIClient.getAPIService().getAllUserAvatars(user.userId);
            call.enqueue(new Callback<GetAllUserAvatarsResponse>() {
                @Override
                public void onResponse(Response<GetAllUserAvatarsResponse> response, Retrofit retrofit) {
                    dismissProgress();
                    if (response.code() == HttpURLConnection.HTTP_OK) {
                        GetAllUserAvatarsResponse result = response.body();
                        if (result != null) {
                            helperUtils.avatarLoaded = true;
                            helperUtils.chineseRegular = result.getAvatar(CHINESE_REGULAR_MODE);

                            getAvatar();
                        }
                    } else if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                        helperUtils.showMessage(R.string.unauthorized_toast);
                        requestLogout();
                    } else {
                        helperUtils.showMessage(R.string.Network_Error);
                        finish();
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    dismissProgress();
                    helperUtils.showMessage(t.getLocalizedMessage());
                    finish();
                }
            });
        }
    }

    private void getRecordedSentences() {
        showProgress();
        Call<GetAvatarResponse> call = APIClient.getAPIService().getAvatar(avatarID);
        call.enqueue(new Callback<GetAvatarResponse>() {
            @Override
            public void onResponse(Response<GetAvatarResponse> response, Retrofit retrofit) {
                dismissProgress();
                if (response.code() == HttpURLConnection.HTTP_OK) {
                    avatarData = response.body();
                    populateListView();
                } else if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    helperUtils.showMessage(R.string.unauthorized_toast);
                    requestLogout();
                } else {
                    helperUtils.showMessage(R.string.Network_Error);
                    finish();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                dismissProgress();
                helperUtils.showMessage(t.getLocalizedMessage());
                finish();
            }
        });
    }

    private void requestLogout() {
        showLoginPage();
    }

    private void showLoginPage() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void populateListView() {
        listView.setAdapter(new RegularAdapter());
    }

    @Override
    public void onBackPressed() {
        showAlert();
    }

    private void showAlert() {
        stopPlaying();
        stopRecording(null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.cancelButtonStr);
        builder.setMessage(R.string.exit_message_str);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.cancelButtonStr,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        stopPlaying();
                        finish();
                        dialog.cancel();
                    }
                });
        builder.setNegativeButton(R.string.Keep_Recording,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.cancelBtn) {
            showAlert();
        }
    }

    private class RegularAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            int recordCount = 0;
            if (avatarData != null) {
                recordCount = avatarData.getRecordCount();
            }
            return recordCount < LIMIT_COUNT ? recordCount + 1 : LIMIT_COUNT;
        }

        @Override
        public Integer getItem(int position) {
            return getCount() - position; // Return record ID.
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.layout_record_item, parent, false);
            }

            TextView tvSentence = (TextView) convertView.findViewById(R.id.descriptionTxt);
            Button btnHearSample = (Button) convertView.findViewById(R.id.hearSampleBtn);
            final Button btnListen = (Button) convertView.findViewById(R.id.listenBtn);
            final Button btnRec = (Button) convertView.findViewById(R.id.recBtn);

            final Integer recordId = getItem(position);
            final ObenPhrase.PhraseObj phrase = phrasesData.getPhraseByRecordID(recordId);
            tvSentence.setText(phrase.sentence);

            // Setup function for Hear Sample button.
            btnHearSample.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isRecording) {
                        listenFrom(phrase.example);
                    }
                }
            });

            // Setup function for LISTEN button.
            if (avatarData == null || avatarData.getSentence(recordId) == null) {
                btnListen.setAlpha(0.1f);
                btnListen.setEnabled(false);
            } else {
                btnListen.setAlpha(1.0f);
                btnListen.setEnabled(true);
            }
            btnListen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isRecording) {
                        listenFrom(avatarData.getSentence(recordId));
                    }
                }
            });

            // Setup function for REC button.
            btnRec.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (hasGrantedAppPermissions()) {
                        stopPlaying();
                        String stop = getString(R.string.STOP);
                        if (isRecording) {
                            if (btnRec.getText().toString().equalsIgnoreCase(stop)) {
                                isRecording = false;
                                stopRecording(recordId);
                                btnRec.setText(R.string.REC);
                            }
                        } else {
                            isRecording = true;
                            startRecording();
                            btnRec.setText(R.string.STOP);
                        }
                    } else {
                        requestPermissions();
                    }
                }
            });

            return convertView;
        }

        private void listenFrom(String sentence) {
            new PlayTask().execute(sentence);
        }
    }

    private MediaPlayer mediaPlayer;

    private void stopPlaying() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private class PlayTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress();
            stopPlaying();
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setVolume(1.0f, 1.0f);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    dismissProgress();
                }
            });
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                mediaPlayer.setDataSource(params[0]);
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    boolean isRecording = false;
    final String PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/oben_audio.wav";
    ExtAudioRecorder extAudioRecorder;

    private void startRecording() {
        // Uncompressed recording (WAV) : IF true - AMR
        extAudioRecorder = ExtAudioRecorder.getInstanse(false);
        extAudioRecorder.setOutputFile(PATH);
        extAudioRecorder.prepare();
        extAudioRecorder.start();
    }

    private void stopRecording(Integer recordID) {
        if (extAudioRecorder != null) {
            extAudioRecorder.stop();
            extAudioRecorder.release();
        }

        if (recordID == null) return;

        // Upload user recording.
        File audioFileName = new File(PATH);
        RequestBody requestBody = RequestBody.create(MediaType.parse("audio/wav"), audioFileName);
        saveAvatar(recordID, requestBody);
    }

    private void saveAvatar(Integer recordID, RequestBody requestBody) {
        ObenUser user = ObenUser.getSavedUser();
        if (user != null) {
            showProgress();
            Call<SaveUserAvatarResponse> call = APIClient.getAPIService().saveUserAvatar(
                    CURRENT_MODE, user.userId, recordID, requestBody, avatarID);
            call.enqueue(new Callback<SaveUserAvatarResponse>() {
                @Override
                public void onResponse(Response<SaveUserAvatarResponse> response, Retrofit retrofit) {
                    dismissProgress();
                    if (response.code() == HttpURLConnection.HTTP_OK) {
                        ObenUserAvatar savedAvatar = response.body().UserAvatar;
                        if (savedAvatar.status.equalsIgnoreCase("SUCCESS")) {
                            if (avatarID == null) {
                                helperUtils.avatarLoaded = false;
                            }
                            avatarID = savedAvatar.avatarId;
                            getRecordedSentences();
                        } else {
                            helperUtils.showMessage(savedAvatar.message);
                        }
                    } else if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                        helperUtils.showMessage(R.string.unauthorized_toast);
                        requestLogout();
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
}