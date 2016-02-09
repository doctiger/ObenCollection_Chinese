package com.obenproto.oben.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.obenproto.oben.R;
import com.obenproto.oben.adapters.CommercialListViewAdapter;
import com.obenproto.oben.api.ObenAPIClient;
import com.obenproto.oben.api.ObenAPIService;
import com.obenproto.oben.response.ObenApiResponse;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class CommercialActivity extends Activity {

    public static int LIMIT_NUM = 286;
    public static int COMMERCIAL_PHRASES_COUNT = 0;
    public static Context context;
    public static ArrayList<HashMap<String, String>> list;
    public static CommercialListViewAdapter adapter;
    public static ListView listView;
    public static ProgressBar progressBar;
    public static int recordcount = 0;
    public static List<ObenApiResponse> phraseList;
    public static Activity activity = null;
    public static Map recordMap;
    public static Map avatarMap;
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.commercial_activity);
        activity = this;

        context = this.getBaseContext();
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        editor = pref.edit();

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        listView = (ListView)findViewById(R.id.listView);
        list = new ArrayList<>();

        // Get the list contents.
        onGetPhrases();

        TextView cancelTxt = (TextView) findViewById(R.id.cancelBtn);
        cancelTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlert();
            }
        });
    }

    @Override
    public void onBackPressed() {
        showAlert();
    }

    public void showAlert() {
        if (CommercialListViewAdapter.isAudioPlaying) {
            CommercialListViewAdapter.mediaPlayerListen.stop();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(CommercialActivity.this);
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

    public static void populateList(int index) {
        HashMap<String, String> temp = new HashMap<>();

        Log.d("Index", String.valueOf(index));
        if (index >= LIMIT_NUM) index = LIMIT_NUM - 1;

        if (index < 9) {
            temp.put(String.valueOf(0), phraseList.get(index % COMMERCIAL_PHRASES_COUNT).Phrase.getSentence());
            list.add(temp);
        }

        for (int i = 1; i <= index; i++) {
            temp.put(String.valueOf(i), phraseList.get((index-i)%COMMERCIAL_PHRASES_COUNT).Phrase.getSentence());
            list.add(temp);
        }

        adapter = new CommercialListViewAdapter(context, list);
        listView.setAdapter(adapter);

    }

    public static void refreshListView() {
        activity.finish();
        activity.startActivity(activity.getIntent());
    }

    // Get the avatarID for commercial
    public void onCommercialAvatarID(int userId) {

        ObenAPIService client = ObenAPIClient.newInstance(ObenAPIService.class);
        Call<List<ObenApiResponse>> call = client.getCommercialAvatars(userId);

        call.enqueue(new Callback<List<ObenApiResponse>>() {
            @Override
            public void onResponse(Response<List<ObenApiResponse>> response, Retrofit retrofit) {
                if (response.code() == HttpURLConnection.HTTP_OK) {

                    if (response.body().size() == 0) {
                        editor.putInt("CommercialAvatarID", 0);

                    } else {
                        ObenApiResponse response_result = response.body().get(0);
                        avatarMap = (Map) response_result.Avatar;

                        if (avatarMap != null) {
                            editor.putInt("CommercialAvatarID", Float.valueOf(avatarMap.get("avatarId").toString()).intValue());

                        } else {
                            editor.putInt("CommercialAvatarID", 0);
                        }
                    }

                    editor.commit();
                    Log.d("Commercial avatarID", String.valueOf(pref.getInt("CommercialAvatarID", 0)));

                    // Get the avatar data.
                    onAvatarData(pref.getInt("CommercialAvatarID", 0));

                } else {
                    Log.d("Status", "Http Unauthorized");
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("Commercial avtar ID", t.getMessage());
            }
        });
    }

    //// Get the all avatar data for Commercial.
    public void onAvatarData(int avatarId) {
        ObenAPIService client = ObenAPIClient.newInstance(ObenAPIService.class);
        Call<ObenApiResponse> call = client.getAvatarData(avatarId);

        call.enqueue(new Callback<ObenApiResponse>() {
            @Override
            public void onResponse(Response<ObenApiResponse> response, Retrofit retrofit) {
                if (response.code() == HttpURLConnection.HTTP_OK) { // success
                    ObenApiResponse response_result = response.body();
                    recordMap = (Map) response_result.Avatar;
                    Log.d("debug avatar List", String.valueOf(recordMap.get("record" + 5)));

                    progressBar.setVisibility(View.GONE);
                    if (recordMap.get("status") == null) {
                        String str = recordMap.get("recordCount").toString();
                        recordcount = Float.valueOf(str).intValue();
                        Log.d("debug record count", String.valueOf(recordcount));

                        editor.putInt("CommercialRecordedCount", recordcount);
                        editor.apply();

                        listView = (ListView) findViewById(R.id.listView);
                        list = new ArrayList<>();

                        Log.d("Record count : ", String.valueOf(recordcount));
                        if (recordcount > LIMIT_NUM)  recordcount = LIMIT_NUM;

                        populateList(recordcount);

                    } else {

                        populateList(0);
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

            }
        });
    }

    public void onGetPhrases() {
        ObenAPIService client = ObenAPIClient.newInstance(ObenAPIService.class);
        Call<List<ObenApiResponse>> call = client.getPhraseData(2);

        call.enqueue(new Callback<List<ObenApiResponse>>() {
            @Override
            public void onResponse(Response<List<ObenApiResponse>> response, Retrofit retrofit) {
                if (response.code() == HttpURLConnection.HTTP_OK) { // success
                    phraseList = response.body();

                    COMMERCIAL_PHRASES_COUNT = phraseList.size();
                    Log.d("phrases count", String.valueOf(COMMERCIAL_PHRASES_COUNT));

                    // get the avatar data for show listview.
                    if (pref.getInt("CommercialAvatarID", 0) == 0) {
                        onCommercialAvatarID(pref.getInt("userID", 0));
                    } else {
                        onAvatarData(pref.getInt("CommercialAvatarID", 0));
                    }

                } else if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    Log.d("Status", "Http Unauthorized");

                } else {
                    Log.d("Status", "Server Connection Failure");
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("Failure", t.getMessage());
            }
        });
    }
}