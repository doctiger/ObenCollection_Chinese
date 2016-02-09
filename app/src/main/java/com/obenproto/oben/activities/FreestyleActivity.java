package com.obenproto.oben.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.obenproto.oben.R;
import com.obenproto.oben.api.ObenAPIClient;
import com.obenproto.oben.api.ObenAPIService;
import com.obenproto.oben.recorder.ExtAudioRecorder;
import com.obenproto.oben.response.ObenApiResponse;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class FreestyleActivity extends Activity {

    RelativeLayout start;
    RelativeLayout stop;
    ImageButton start_rec;
    ProgressBar progressBar;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Map recordMap;
    Map avatarMap;
    int userId = 0;
    private static String filePath;
    ExtAudioRecorder extAudioRecorder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.freestyle_activity);

        start = (RelativeLayout) findViewById(R.id.start_recording_layout);
        stop = (RelativeLayout) findViewById(R.id.stop_recording_layout);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        stop.setVisibility(View.GONE);

        filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ObenFreestyleRecord.wav";

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        editor = pref.edit();

        userId = pref.getInt("userID", 0);
        Log.d("userID", String.valueOf(pref.getInt("userID", 0)));

        start_rec = (ImageButton) findViewById(R.id.btnStart);
        start_rec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startRecording(v);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

//        start_rec.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                switch (event.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                        // pressed
//                        try {
//                            startRecording(v);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                        return true;
//                    case MotionEvent.ACTION_UP:
//                        // released
//                        stopRecording(v);
//                        return true;
//                }
//                return false;
//            }
//        });

        ImageButton stop_rec = (ImageButton) findViewById(R.id.btnStop);
        stop_rec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording(v);
            }
        });

        TextView cancelTxt = (TextView) findViewById(R.id.cancelBtn);
        cancelTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlert();
            }
        });

        // Get the freestyle avatar ID.
        Log.d("FreestyleAvatarID", String.valueOf(pref.getInt("FreestyleAvatarID", 0)));
        Log.d("RecordCount", String.valueOf(pref.getInt("RecordCount", 0)));
        if (pref.getInt("FreestyleAvatarID", 0) == 0) {
            onFreestyleAvatarID(userId);
            progressBar.setVisibility(View.VISIBLE);
            start_rec.setEnabled(false);

        } else {
            if (pref.getInt("RecordCount", 0) == 0) {
                progressBar.setVisibility(View.VISIBLE);
                start_rec.setEnabled(false);
                onAvatarData(pref.getInt("FreestyleAvatarID", 0));
            }
        }
    }

    @Override
    public void onBackPressed() {
        showAlert();
    }

    public void showAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(FreestyleActivity.this);
        builder.setTitle("Save & Exit");
        builder.setMessage("All of your recordings have been saved. You may return and continue recording where you left off at any time.");
        builder.setCancelable(true);
        builder.setPositiveButton("Save & Exit",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        dialog.cancel();
                    }
                });
        builder.setNegativeButton("Keep Recording",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void startRecording(View view) throws IOException {
        start.setVisibility(View.GONE);
        stop.setVisibility(View.VISIBLE);
        Log.d("Recorder", "Start recording");

        extAudioRecorder = ExtAudioRecorder.getInstanse(false);

        extAudioRecorder.setOutputFile(filePath);
        extAudioRecorder.prepare();
        extAudioRecorder.start();
    }

    public void stopRecording(View view) {
        start.setVisibility(View.VISIBLE);
        stop.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        start_rec.setEnabled(false);
        Log.d("Recorder", "Stop recording");

        extAudioRecorder.stop();
        extAudioRecorder.release();

        String str = "/storage/emulated/0/iPhoneRecVoice1.wav";
        Log.d("audio file path : ", filePath);
        File audioFileName = new File(filePath);
        RequestBody requestBody = RequestBody.create(MediaType.parse("audio/wav"), audioFileName);

        // Upload the recorded audio file.
        userId = pref.getInt("userID", 0);
        int avatarId = pref.getInt("FreestyleAvatarID", 0);
        int recordId = pref.getInt("RecordCount", 0) + 1;

        // Upload the voice.
        onSaveUserAvatarRequest(userId, recordId, requestBody, avatarId);

        editor.putInt("RecordCount", recordId);
        editor.commit();
    }

    // Recall of save user avatar
    public void onSaveUserAvatarRequest(int userId, final int recordId, RequestBody audioFile, final int avatarId) {
        // save user avatar
        ObenAPIService client = ObenAPIClient.newInstance(ObenAPIService.class);

        Call<ObenApiResponse> call;
        if (avatarId == 0) {
            call = client.saveOriginalFreestyleUserAvatar(userId, recordId, audioFile);
        } else {
            call = client.saveFreestyleUserAvatar(userId, recordId, audioFile, avatarId);
        }

        call.enqueue(new Callback<ObenApiResponse>() {
            @Override
            public void onResponse(Response<ObenApiResponse> response, Retrofit retrofit) {
                progressBar.setVisibility(View.GONE);
                start_rec.setEnabled(true);
                if (response.code() == HttpURLConnection.HTTP_OK) { // success
                    Log.v("Upload", "Success");
                    Log.d("record ID - " + String.valueOf(recordId), "avatar ID - " + String.valueOf(avatarId));

                    int regularAvatarID = response.body().UserAvatar.getAvatarId();
                    editor.putInt("FreestyleAvatarID", regularAvatarID);
                    editor.commit();

                    Toast.makeText(getApplicationContext(), "Upload Success", Toast.LENGTH_LONG).show();

                } else if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    Log.d("Status", "Http Unauthorized");

                } else {
                    Log.d("Status", "Connection Failure");
                }
            }

            @Override
            public void onFailure(Throwable t) {
                progressBar.setVisibility(View.GONE);
                start_rec.setEnabled(true);
                Log.d("Upload", t.getMessage());
            }
        });
    }

    // Get avatarID for regular
    public void onFreestyleAvatarID(int userId) {
        ObenAPIService client = ObenAPIClient.newInstance(ObenAPIService.class);
        Call<List<ObenApiResponse>> call = client.getFreestyleAvatars(userId);

        call.enqueue(new Callback<List<ObenApiResponse>>() {
            @Override
            public void onResponse(Response<List<ObenApiResponse>> response, Retrofit retrofit) {
                if (response.code() == HttpURLConnection.HTTP_OK) {
                    progressBar.setVisibility(View.GONE);
                    start_rec.setEnabled(true);

                    if (response.body().size() == 0) {
                        editor.putInt("FreestyleAvatarID", 0);

                    } else {
                        ObenApiResponse response_result = response.body().get(0);
                        avatarMap = (Map) response_result.Avatar;

                        if (avatarMap != null) {
                            editor.putInt("FreestyleAvatarID", Float.valueOf(avatarMap.get("avatarId").toString()).intValue());

                        } else {
                            editor.putInt("FreestyleAvatarID", 0);
                        }
                    }

                    editor.commit();
                    Log.d("freestyle avatarID", String.valueOf(pref.getInt("FreestyleAvatarID", 0)));

                } else {
                    Log.d("Status", "Http Unauthorized");
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("Failure", t.getMessage());
            }
        });
    }

    // Get all avatar data for freestyle
    public void onAvatarData(int avatarID) {
        ObenAPIService client = ObenAPIClient.newInstance(ObenAPIService.class);
        Call<ObenApiResponse> call = client.getAvatarData(avatarID);

        call.enqueue(new Callback<ObenApiResponse>() {

            @Override
            public void onResponse(Response<ObenApiResponse> response, Retrofit retrofit) {
                if (response.code() == HttpURLConnection.HTTP_OK) { // success
                    progressBar.setVisibility(View.GONE);
                    start_rec.setEnabled(true);

                    ObenApiResponse response_result = response.body();
                    recordMap = (Map) response_result.Avatar;

                    if (recordMap.get("status") == null) {
                        String str = recordMap.get("recordCount").toString();
                        int recordcount = Float.valueOf(str).intValue();

                        Log.d("record count" , String.valueOf(recordcount));
                        editor.putInt("RecordCount", recordcount);
                        editor.commit();

                    } else {
                        editor.putInt("RecordCount", 0);
                        editor.commit();
                        Log.d("Status", "Avatar with id 4 not found");
                    }

                } else if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    Log.d("Status", "Http Unauthorized");

                } else {
                    Log.d("Status", "Server Connection Failure");
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("failure", t.getMessage());
            }
        });
    }
}