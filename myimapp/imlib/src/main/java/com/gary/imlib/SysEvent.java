package com.gary.imlib;

public interface SysEvent {
    void onConnect();
    void onDisconnect();
    void onSetChange(String key,String value);
}
