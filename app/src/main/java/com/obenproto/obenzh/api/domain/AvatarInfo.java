package com.obenproto.obenzh.api.domain;

public class AvatarInfo {

    public Avatar Avatar;

    public class Avatar {
        public Integer avatarId;
        public String avatarName;
        public boolean canDelete;
        public String image;
        public Integer modeId;
        public Integer stageId;
    }
}
