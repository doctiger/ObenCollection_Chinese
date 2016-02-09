package com.obenproto.oben.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.obenproto.oben.R;
import com.obenproto.oben.activities.CommercialActivity;
import com.obenproto.oben.api.ObenAPIClient;
import com.obenproto.oben.api.ObenAPIService;
import com.obenproto.oben.recorder.ExtAudioRecorder;
import com.obenproto.oben.response.ObenApiResponse;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by Petro RingTon on 12/9/2015.
 */
public class CommercialListViewAdapter extends BaseAdapter {

    public ArrayList<HashMap<String, String>> list;
    public Context cont_;
    public LayoutInflater mInflater;
    public static boolean isAudioPlaying = false;
    boolean isRecording = false;
    boolean isUploading = false;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    MediaRecorder mediaRecorder;
    String filePath, recordedFilePath, sampleFilePath;
    String listenAudioUrl, sampleAdudioUrl;
    int record_index = 0;
    ExtAudioRecorder extAudioRecorder;
    public static MediaPlayer mediaPlayerListen;

    public CommercialListViewAdapter(Context context, ArrayList<HashMap<String, String>> list) {
        super();
        this.cont_ = context;
        this.list = list;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        pref = PreferenceManager.getDefaultSharedPreferences(this.cont_);
        editor = pref.edit();

        filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ObenRegularRecordVoice.wav";
        recordedFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ObenRegularListenAudio.wav";
        sampleFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ObenRegularSampleAudio.wav";
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @SuppressLint("LongLogTag")
    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        convertView = mInflater.inflate(R.layout.record_item, null);

        TextView descriptionTxt = (TextView) convertView.findViewById(R.id.descriptionTxt);
        final Button hearSampleBtn = (Button) convertView.findViewById(R.id.hearSampleBtn);
        final Button listenBtn = (Button) convertView.findViewById(R.id.listenBtn);
        final Button recBtn = (Button) convertView.findViewById(R.id.recBtn);

        if (position == 0 && list.size() < CommercialActivity.LIMIT_NUM) {
            listenBtn.setEnabled(false);
            listenBtn.setAlpha(0.1f);
        }

        final HashMap<String, String> map = list.get(position);
        descriptionTxt.setText(map.get(String.valueOf(position)));
        Log.d("d-debug description text", map.get(String.valueOf(position)));

        hearSampleBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onClick(View v) {
                if (isUploading) return;
                if (isRecording) return;
                if (isAudioPlaying) return;

                sampleAdudioUrl = String.valueOf(CommercialActivity.phraseList.get(list.size() - position - 1).Phrase.getExample());

                // Play the sample audio file from the remote url.
                final MediaPlayer mediaPlayerHear = new MediaPlayer();
                mediaPlayerHear.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayerHear.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        return false;
                    }
                });
                mediaPlayerHear.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        CommercialActivity.progressBar.setVisibility(View.GONE);
                        mediaPlayerHear.start();
                    }
                });
                mediaPlayerHear.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        isAudioPlaying = false;
                    }
                });

                try {
                    mediaPlayerHear.setDataSource(sampleAdudioUrl);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mediaPlayerHear.setVolume(1.0f, 1.0f);
                mediaPlayerHear.prepareAsync();
                CommercialActivity.progressBar.setVisibility(View.VISIBLE);
                isAudioPlaying = true;

                Log.d("d-debug sample record url", sampleAdudioUrl);
            }
        });

        listenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isUploading) return;
                if (isRecording) return;
                if (isAudioPlaying) return;

                listenAudioUrl = CommercialActivity.recordMap.get("record" + (list.size() - position)).toString();

                // Play the recorded audio file from the remote url.
                mediaPlayerListen = new MediaPlayer();
                mediaPlayerListen.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayerListen.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @SuppressLint("LongLogTag")
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        CommercialActivity.progressBar.setVisibility(View.GONE);
                        mediaPlayerListen.start();
                        Log.d("d-starting recorded audio paying", listenAudioUrl);
                    }
                });
                mediaPlayerListen.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        Toast.makeText(cont_, "MEDIA_ERROR_SYSTEM", Toast.LENGTH_SHORT).show();
                        CommercialActivity.progressBar.setVisibility(View.GONE);
                        return false;
                    }
                });
                mediaPlayerListen.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        Log.d("audio playing", "Completion");
                        isAudioPlaying = false;
                    }
                });

                try {
                    mediaPlayerListen.setDataSource(listenAudioUrl);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mediaPlayerListen.setVolume(1.0f, 1.0f);
                mediaPlayerListen.prepareAsync();
                CommercialActivity.progressBar.setVisibility(View.VISIBLE);
                isAudioPlaying = true;

                Log.d("d-debug record url ", listenAudioUrl);
            }
        });

        recBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isUploading) return;
                if (isRecording && position != record_index) return;
                if (isAudioPlaying) return;

                Log.d("d-Debug Recording Flag", String.valueOf(isRecording));
                Log.d("d-Debug Recording position", String.valueOf(position) + "-" + String.valueOf(record_index));

                recBtn.setText(isRecording ? "REC" : "STOP");
                isRecording = !isRecording;

                if (isRecording) {
                    record_index = position;

                    try {
                        startRecording();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    int btnIndex = position;

                    stopRecording(list.size() - btnIndex);
                }

            }
        });

        return convertView;
    }

    // Upload the recorded audio file.
    public void onSaveRegularAvatar(int userId, final int recordId, RequestBody audioFile, final int avatarId) {
        ObenAPIService client = ObenAPIClient.newInstance(ObenAPIService.class);

        Call<ObenApiResponse> call;
        if (avatarId == 0) {
            call = client.saveOriginalCommercialUserAvatar(userId, recordId, audioFile);
        } else {
            call = client.saveCommercialUserAvatar(userId, recordId, audioFile, avatarId);
        }

        call.enqueue(new Callback<ObenApiResponse>() {
            @Override
            public void onResponse(Response<ObenApiResponse> response, Retrofit retrofit) {
                CommercialActivity.progressBar.setVisibility(View.GONE);
                isUploading = false;
                if (response.code() == HttpURLConnection.HTTP_OK) { // success
                    Log.v("Upload", "Success");
                    ObenApiResponse response_result = response.body();

                    int commercialAvatarID = response_result.UserAvatar.getAvatarId();
                    editor.putInt("CommercialAvatarID", commercialAvatarID);
                    editor.commit();

                    if (record_index == 0) {
                        // Refresh the listview.
                        CommercialActivity.refreshListView();
                        Log.d("d-d- upload success", response_result.UserAvatar.getRecordURL());
                        Toast.makeText(cont_, "Upload Success", Toast.LENGTH_LONG).show();

                    } else {
                        Log.d("d- change success", response_result.UserAvatar.getRecordURL());
                        Toast.makeText(cont_, "Change Success", Toast.LENGTH_LONG).show();
                    }
                    Log.d("d-debug record ID ", String.valueOf(recordId));


                } else if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    Log.d("d-Status", "Http Unauthorized");

                } else {
                    Log.d("d-Status", "Server Connection Failure");
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("Upload", t.getMessage());
                Toast.makeText(cont_, t.getMessage(), Toast.LENGTH_LONG).show();
                CommercialActivity.progressBar.setVisibility(View.GONE);
                isUploading = false;
            }
        });
    }

    // Start the audio record
    public void startRecording() throws IOException {
        Log.d("d-Recorder", "Start recording");

        extAudioRecorder = ExtAudioRecorder.getInstanse(false); // Uncompressed recording (WAV) : IF true - AMR

        extAudioRecorder.setOutputFile(filePath);
        extAudioRecorder.prepare();
        extAudioRecorder.start();
    }

    public void stopRecording(int btnIndex) {
        CommercialActivity.progressBar.setVisibility(View.VISIBLE);
        isUploading = true;
        Log.d("d-Recorder", "Stop recording");

        extAudioRecorder.stop();
        extAudioRecorder.release();

        String str = "/storage/emulated/0/iPhoneRecVoice1.wav";
        Log.d("d-audio file path : ", filePath);
        File audioFileName = new File(filePath);
        RequestBody requestBody = RequestBody.create(MediaType.parse("audio/wav"), audioFileName);
        onSaveRegularAvatar(pref.getInt("userID", 0),
                btnIndex,
                requestBody,
                pref.getInt("CommercialAvatarID", 0));

    }
}
