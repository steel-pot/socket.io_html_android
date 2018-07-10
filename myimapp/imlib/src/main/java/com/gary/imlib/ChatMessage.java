package com.gary.imlib;

import org.json.JSONException;
import org.json.JSONObject;

public class ChatMessage {
    private MessageFromType fromType;

    public MessageFromType getFromType() {
        return fromType;
    }

    public void setFromType(MessageFromType fromType) {
        this.fromType = fromType;
    }

    private MessageType type;
    //正常情况下不应该让用户信息重复发送,只是为了客户端不需要处理用户信息问题这样做
    //如果以后用户量增加,应该由客户端来维护用户信息
    private ChatUser user;
    private String content;

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public ChatUser getUser() {
        return user;
    }

    public void setUser(ChatUser user) {
        this.user = user;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    public static ChatMessage fromJSON(JSONObject jsonObject) throws JSONException {
        ChatMessage cm=new ChatMessage();
        cm.setFromType(MessageFromType.valueOf(jsonObject.getString("fromType")));
        cm.setType(MessageType.valueOf(jsonObject.getString("type")));
        if(cm.getFromType()==MessageFromType.user) {
            cm.setUser(ChatUser.FromJSON(jsonObject.getJSONObject("userinfo")));
        }
        cm.setContent( jsonObject.getString("content"));
        return cm;
    }
}
