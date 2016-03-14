package com.obenproto.obenzh.api.domain;

import com.google.gson.Gson;
import com.obenproto.obenzh.utils.LocalStorage;

public class ObenUser {
    public boolean canChangeVoiceMode;
    public boolean canCreateOwnAvatar;
    public String email;
    public String login;
    public String message;
    public String userDisplayName;
    public Integer userId;

    private static final String AUTH_USER = "AUTH_USER";

    public void saveToStorage() {
        String json = null;
        Gson gson = new Gson();
        try {
            json = gson.toJson(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (json != null) {
            LocalStorage.getInstance().put(AUTH_USER, json);
        }
    }

    public static ObenUser getSavedUser() {
        Gson gson = new Gson();
        try {
            String json = LocalStorage.getInstance().getStringPreference(AUTH_USER);
            return gson.fromJson(json, ObenUser.class);
        } catch (Exception e) {
            return null;
        }
    }

    public static void removeSavedUser() {
        LocalStorage.getInstance().put(AUTH_USER, null);
    }
}

