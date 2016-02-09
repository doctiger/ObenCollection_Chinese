package com.obenproto.oben.response;

/**
 * Created by Petro Rington on 12/5/2015.
 */
public class ObenAvatar {

    private String message;
    private String status;
    private int avatarId;
    private String avatarName;
    private int recordCount;
    private int stageId;

    public String getMessage() {
        return message;
    }

    public String getStatus() {
        return status;
    }

    public int getAvatarId() {
        return avatarId;
    }

    public String getAvatarName() {
        return avatarName;
    }

    public int getRecordCount() {
        return recordCount;
    }

    public int getStageId() {
        return stageId;
    }

    @Override
    public String toString() {
        return "ObenAvatar{" +
                "message='" + message + '\'' +
                ", status='" + status + '\'' +
                ", avatarId=" + avatarId +
                ", avatarName='" + avatarName + '\'' +
                ", recordCount=" + recordCount +
                ", stageId=" + stageId +
                '}';
    }
}

