package com.gary.imlib;

import org.json.JSONException;
import org.json.JSONObject;

public class ChatUser {
    private String userID;
    private String name;
    private String avatar;

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    public static ChatUser FromJSON(JSONObject jsonObject) throws JSONException {
        ChatUser cu=new ChatUser();
        cu.setName(jsonObject.getString("name"));
        cu.setAvatar(jsonObject.getString("avatar"));
        cu.setUserID(jsonObject.getString("userID"));
        return cu;
    }
}
