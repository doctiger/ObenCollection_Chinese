package com.obenproto.obenzh.api.response;

import java.util.Map;

public class GetAvatarResponse {
    public Map<String, String> Avatar;

    public Integer getActivation() {
        return findIntegerByKey("activation");
    }

    public Integer getAvatarID() {
        return findIntegerByKey("avatarId");
    }

    public Integer getRecordCount() {
        return findIntegerByKey("recordCount");
    }

    public Integer getStageId() {
        return findIntegerByKey("stageId");
    }

    public String getSentence(Integer recordId) {
        if (recordId > getRecordCount()) {
            return null;
        }
        String key = "record" + recordId.toString();
        return findStringByKey(key);
    }

    private String findStringByKey(String key) {
        if (Avatar == null)
            return null;

        for (Map.Entry<String, String> entry : Avatar.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private Integer findIntegerByKey(String key) {
        String value = findStringByKey(key);
        if (value != null) {
            return Integer.parseInt(value);
        }
        return 0;
    }
}