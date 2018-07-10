package com.gary.myimapp;

import android.app.Application;

import com.gary.imlib.Chat;
import com.gary.imlib.SysEvent;

import org.json.JSONException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MyApplication extends Application implements SysEvent {
    private Chat mChat;
    {
        mChat = new Chat();
        mChat.regSysEvent(this);
        //把连接事件放到你自己的APP的登陆成功事件中,因为需要用到正确的帐号密码
        mChat.conn("http://121.42.42.112:1013/room");
    }
    public Chat getChat()
    {
        return mChat;
    }
    @Override
    public void onConnect() {
         //如果连接断开会自动重连,也会调用这个方法,
        //登陆的帐号密码最好不要放在这个类里面,也不要使用静态变量
        //因为application可能会被销毁,而activity可能会被恢复,会导至这里获取到的变量是空的
        try {
            mChat.login("15361032007",parseStrToMd5L32("123456"),"");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisconnect() {

    }

    @Override
    public void onSetChange(String key, String value) {
        //当全局配置发生变化时,最好是同时修改APP里面的配置,比如 直播被关闭, 或某个图片地址变了
    }

    public static String parseStrToMd5L32(String str){
        String reStr = null;
        try{
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(str.getBytes());
            StringBuffer stringBuffer = new StringBuffer();
            for(byte b : bytes){
                int bt = b & 0xff;
                if(bt < 16){
                    stringBuffer.append(0);
                }
                stringBuffer.append(Integer.toHexString(bt));
            }
            reStr = stringBuffer.toString();
        }catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return reStr;
    }
}
