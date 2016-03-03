package com.obenproto.obenzh.adapters;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.obenproto.obenzh.R;
import com.obenproto.obenzh.activities.RegularActivity;
import com.obenproto.obenzh.api.ObenAPIClient;
import com.obenproto.obenzh.api.ObenAPIService;
import com.obenproto.obenzh.recorder.ExtAudioRecorder;
import com.obenproto.obenzh.response.ObenApiResponse;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class RegularListViewAdapter extends BaseAdapter implements ActivityCompat.OnRequestPermissionsResultCallback {

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
    public static MediaPlayer mediaPlayer;

    public static final String TAG = "RegularListViewAdapter";

    /**
     * Id to identify a microphone permission request.
     */
    private static final int REQUEST_MICROPHONE = 0;

    /**
     * Id to identify a storage permission request.
     */
    private static final int REQUEST_STORAGE = 1;

    public RegularListViewAdapter(Context context, ArrayList<HashMap<String, String>> list) {
        super();
        this.cont_ = context;
        this.list = list;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        pref = PreferenceManager.getDefaultSharedPreferences(this.cont_);
        editor = pref.edit();

        filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ObenRegularRecordVoice.wav";
        recordedFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ObenRegularListenAudio.wav";
        sampleFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ObenRegularSampleAudio.wav";

        ////////////////////////////////////
        new Thread(new SimpleServer()).start();
        new Thread(new SimpleClient()).start();
    }

    static class SimpleServer implements Runnable {

        @Override
        public void run() {

            ServerSocket serverSocket = null;

            try {
                serverSocket = new ServerSocket(3333);
                serverSocket.setSoTimeout(20000);

                while (true) {
                    try {
                        Socket clientSocket = serverSocket.accept();

                        BufferedReader inputReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                        System.out.println("Client said :"+ inputReader.readLine());

                    } catch (SocketTimeoutException e) {
                        e.printStackTrace();
                    }
                }

            } catch (IOException e1) {
                e1.printStackTrace();
            } finally {
                try {
                    if (serverSocket != null) {
                        serverSocket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

    }

    static class SimpleClient implements Runnable {

        @Override
        public void run() {

            Socket socket = null;
            try {

                Thread.sleep(5000);

                socket = new Socket("localhost", 3333);

                PrintWriter outWriter = new PrintWriter(
                        socket.getOutputStream(), true);

                outWriter.println("Hello Mr. Server!");

            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            } finally {

                try {
                    if (socket != null)
                        socket.close();
                } catch (IOException e) {

                    e.printStackTrace();
                }
            }
        }

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

        if (position == 0 && list.size() < RegularActivity.LIMIT_NUM) {
            listenBtn.setEnabled(false);
            listenBtn.setAlpha(0.1f);
        }

        final HashMap<String, String> map = list.get(position);
        descriptionTxt.setText(map.get(String.valueOf(position)));
        Log.d("d-debug description text", map.get(String.valueOf(position)));

        hearSampleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isUploading) return;
                if (isRecording) return;
                if (isAudioPlaying) return;

                Log.d("d- phrase", String.valueOf(RegularActivity.phraseList.get(0)) + String.valueOf(list.size()) + " - " + String.valueOf(position));
                sampleAdudioUrl = String.valueOf(RegularActivity.phraseList.get(list.size() - position - 1).Phrase.getExample());

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
                        RegularActivity.progressBar.setVisibility(View.GONE);
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
                RegularActivity.progressBar.setVisibility(View.VISIBLE);
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

                listenAudioUrl = RegularActivity.recordMap.get("record" + (list.size() - position)).toString();

                // Play the recorded audio file from the remote url.
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        Toast.makeText(cont_, "MEDIA_ERROR_SYSTEM", Toast.LENGTH_SHORT).show();
                        RegularActivity.progressBar.setVisibility(View.GONE);
                        return false;
                    }
                });
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @SuppressLint("LongLogTag")
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        RegularActivity.progressBar.setVisibility(View.GONE);
                        mediaPlayer.start();
//                        isAudioPlaying = false;
                        Log.d("d-starting recorded audio paying", listenAudioUrl);
                    }
                });
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        Log.d("audio playing", "Completion");
                        isAudioPlaying = false;
                    }
                });

                try {
                    mediaPlayer.setDataSource(listenAudioUrl);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mediaPlayer.setVolume(1.0f, 1.0f);
                mediaPlayer.prepareAsync();
                RegularActivity.progressBar.setVisibility(View.VISIBLE);
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

                if (ActivityCompat.checkSelfPermission(cont_, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {

                    // MICROPHONE permission has not been granted.
                    requestMicrophonePermission();

                } else if ((ActivityCompat.checkSelfPermission(cont_, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED)) {

                    // STORAGE permission has not been granted.
                    requestStoragePermission();

                } else {

                    Log.i(TAG,
                            "MICROPHONE and STORAGE permission has already been granted. Starting the record.");

                    // Microphone and Storage permissions is already available
                    recBtn.setText(isRecording ? "录音" : "停止");
                    isRecording = !isRecording;

                    if (isRecording) {
                        record_index = position;

                        try {
                            startRecording();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    } else {
                        stopRecording(list.size() - position);
                    }
                }
            }
        });

        return convertView;
    }

    private void requestMicrophonePermission() {
        Log.i(TAG, "MICROPHONE permission has NOT been granted. Requesting permission.");

        // BEGIN_INCLUDE(microphone_permission_request)
        if (ActivityCompat.shouldShowRequestPermissionRationale(RegularActivity.activity,
                Manifest.permission.RECORD_AUDIO)) {

            Log.i(TAG,
                    "Displaying microphone permission rationale to provide additional context.");

            Snackbar.make(RegularActivity.mLayout, R.string.permission_microphone_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(RegularActivity.activity,
                                    new String[]{Manifest.permission.RECORD_AUDIO},
                                    REQUEST_MICROPHONE);
                        }
                    })
                    .show();
        } else {
            // Microphone permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(RegularActivity.activity, new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_MICROPHONE);
        }
    }

    private void requestStoragePermission() {
        Log.i(TAG, "Storage permission has NOT been granted. Requesting permission.");

        // BEGIN_INCLUDE(storage_permission_request)
        if (ActivityCompat.shouldShowRequestPermissionRationale(RegularActivity.activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            Log.i(TAG,
                    "Displaying storage permission rationale to provide additional context.");

            Snackbar.make(RegularActivity.mLayout, R.string.permission_storage_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(RegularActivity.activity,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    REQUEST_STORAGE);
                        }
                    })
                    .show();
        } else {
            // Storage permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(RegularActivity.activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == REQUEST_MICROPHONE) {
            // BEGIN_INCLUDE(permission_result)
            // Received permission result for microphone permission.
            Log.i(TAG, "Received response for microphone permission request.");

            // Check if the only required permission has been granted
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Microphone permission has been granted, preview can be displayed
                Log.i(TAG, "MICROPHONE permission has now been granted.");
                Snackbar.make(RegularActivity.mLayout, R.string.permission_available_microphone,
                        Snackbar.LENGTH_SHORT).show();
            } else {
                Log.i(TAG, "MICROPHONE permission was NOT granted.");
                Snackbar.make(RegularActivity.mLayout, R.string.permissions_not_granted,
                        Snackbar.LENGTH_SHORT).show();

            }
            // END_INCLUDE(permission_result)

        } else if (requestCode == REQUEST_STORAGE) {
            Log.i(TAG, "Received response for storage permissions request.");

            // We have requested storage permission, so all of them need to be
            // checked.
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // All required permissions have been granteds.
                Log.i(TAG, "SOTRAGE permission has now been granted.");
                Snackbar.make(RegularActivity.mLayout, R.string.permission_available_storage,
                        Snackbar.LENGTH_SHORT).show();
            } else {
                Log.i(TAG, "Storage permissions were NOT granted.");
                Snackbar.make(RegularActivity.mLayout, R.string.permissions_not_granted,
                        Snackbar.LENGTH_SHORT).show();
            }

        }
    }

    // Upload the recorded audio file.
    public void onSaveRegularAvatar(int userId, final int recordId, RequestBody audioFile, final int avatarId) {
        ObenAPIService client = ObenAPIClient.newInstance(ObenAPIService.class);

        Call<ObenApiResponse> call;
        if (avatarId == 0) {
            call = client.saveOriginalRegularZhUserAvatar(userId, recordId, audioFile);
        } else {
            call = client.saveRegularZhUserAvatar(userId, recordId, audioFile, avatarId);
        }
        call.enqueue(new Callback<ObenApiResponse>() {
            @Override
            public void onResponse(Response<ObenApiResponse> response, Retrofit retrofit) {
                RegularActivity.progressBar.setVisibility(View.GONE);
                isUploading = false;
                if (response.code() == HttpURLConnection.HTTP_OK) { // success
                    Log.v("Upload", "Success");
                    ObenApiResponse response_result = response.body();

                    int regularAvatarID = response_result.UserAvatar.getAvatarId();
                    editor.putInt("RegularAvatarID", regularAvatarID);
                    editor.commit();

                    if (record_index == 0) {
                        // Refresh the listview.
                        RegularActivity.refreshListView();
                        Log.d("d-d- upload success", response_result.UserAvatar.getRecordURL());
                        Toast.makeText(cont_, "上传成功", Toast.LENGTH_LONG).show();

                    } else {
                        Log.d("d- change success", response_result.UserAvatar.getRecordURL());
                        Toast.makeText(cont_, "更改成功", Toast.LENGTH_LONG).show();
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
                Log.e("Upload", t.getMessage());
                Toast.makeText(cont_, "失败", Toast.LENGTH_SHORT).show();
                RegularActivity.progressBar.setVisibility(View.GONE);
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
        RegularActivity.progressBar.setVisibility(View.VISIBLE);
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
                pref.getInt("RegularAvatarID", 0));
    }
}