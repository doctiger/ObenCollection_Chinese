package com.obenproto.obenzh.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.obenproto.obenzh.R;
import com.obenproto.obenzh.api.domain.AvatarInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper utility class.
 * <p/>
 * Created by Petro Rington on 2/3/2016.
 */
public class CommonUtils {

    public boolean avatarLoaded;
    public AvatarInfo chineseRegular;

    private Context context;
    private static CommonUtils mInstance = null;

    public static synchronized CommonUtils init(Context context) {
        if (mInstance == null) {
            mInstance = new CommonUtils(context);
        }
        return mInstance;
    }

    public static CommonUtils getInstance() {
        return mInstance;
    }

    private CommonUtils(Context context) {
        this.context = context;
    }

    public void showMessage(int resId) {
        try {
            showMessage(context.getString(resId));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showMessage(String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        toast.show();
    }

    /**
     * Validate email address
     *
     * @return true if @param email is valid email address,
     * otherwise false
     */
    public boolean validateEmail(String email) {
        Pattern p = Pattern.compile("[A-Z0-9._%+-]+@[A-Z0-9.-]+.[A-Z]{2,4}", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(email);
        return m.matches();
    }

    /**
     * Shake edit text to represent input error.
     *
     * @param editText EditText object.
     */
    public void shakeForError(EditText editText) {
        Animation shake = AnimationUtils.loadAnimation(editText.getContext(), R.anim.shake_horizontal);
        editText.startAnimation(shake);
    }

    /**
     * Get real path from Uri.
     *
     * @param contentUri Uri object.
     * @param context    Context object.
     * @return Full path from Uri.
     */
    public String getRealPathFromURI(Uri contentUri, Context context) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
            cursor.close();
        }
        return res;
    }
}
