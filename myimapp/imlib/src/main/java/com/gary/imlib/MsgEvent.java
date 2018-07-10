package com.gary.imlib;

public interface MsgEvent {
    void onLoginBack(boolean status,String info);
    void onGetNumsBack(int nums);
    void onMessageComing(ChatMessage message);
    void onUserChange(boolean isIn,ChatUser user,int nums);
}
