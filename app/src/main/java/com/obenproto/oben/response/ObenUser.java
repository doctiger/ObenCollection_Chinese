package com.obenproto.oben.response;

/**
 * Created by Petro Rington on 12/5/2015.
 */
public class ObenUser {

    private boolean canChangeVoiceMode;
    private boolean canCreateOwnAvatar;
    private String email;
    private String login;
    private String message;
    private String userDisplayName;
    private int userId;

    public boolean isCanChangeVoiceMode() {
        return canChangeVoiceMode;
    }

    public boolean isCanCreateOwnAvatar() {
        return canCreateOwnAvatar;
    }

    public String getEmail() {
        return email;
    }

    public String getLogin() {
        return login;
    }

    public String getMessage() {
        return message;
    }

    public String getUserDisplayName() {
        return userDisplayName;
    }

    public int getUserId() {
        return userId;
    }

    @Override
    public String toString() {
        return "ObenUser{" +
                "canChangeVoiceMode=" + canChangeVoiceMode +
                ", canCreateOwnAvatar=" + canCreateOwnAvatar +
                ", email='" + email + '\'' +
                ", login='" + login + '\'' +
                ", message='" + message + '\'' +
                ", userDisplayName='" + userDisplayName + '\'' +
                ", userId=" + userId +
                '}';
    }
}

