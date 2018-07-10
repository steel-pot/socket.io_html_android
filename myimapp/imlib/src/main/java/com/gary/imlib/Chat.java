package com.gary.imlib;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class Chat {
    private Socket mSocket;
    private ChatUser currUser;

    public ChatUser getCurrUser() {
        return currUser;
    }

    public void conn(String chat_server_url)
    {
        try {
            if(mSocket==null) {
                mSocket = IO.socket(chat_server_url);
                bindEvent();
                mSocket.connect();
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
    //最好不要使用token登陆,因为token有可能发生变化
    public void login(String account,String password,String token) throws JSONException {
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("account",account);
        jsonObject.put("password",password);
        jsonObject.put("token",token);
        mSocket.emit("login",jsonObject);
    }
    //获取当前用户数量
    public void getNums()
    {
        mSocket.emit("getNums");
    }

    //发送消息
    public void sendMsg(MessageType type,String content) throws JSONException {
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("type",type.name());
        jsonObject.put("content",content);
        mSocket.emit("sendMsg",jsonObject);
    }
    /*
    * connect：连接成功
connecting：正在连接
disconnect：断开连接
connect_failed：连接失败
error：错误发生，并且无法被其他事件类型所处理
message：同服务器端message事件
anything：同服务器端anything事件
reconnect_failed：重连失败
reconnect：成功重连
reconnecting：正在重连
当第一次连接时，事件触发顺序为：connecting->connect；当失去连接时，事件触发顺序为：disconnect->reconnecting（可能进行多次）->connecting->reconnect->connect。
     */

    private void bindEvent()
    {
        //链接断开事件
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        //连接事件成功事件
        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_ERROR, onError);


        //系统配置变化
        mSocket.on("setChange", onSetChange);


        //登陆事件
        mSocket.on("loginBack", onLoginBack);
        //获取用户数量返回
        mSocket.on("getNumsBack", onGetNumsBack);
        //有消息到达
        //普通消息   系统公告  通知信息  管理员消息
        //                    通知信息,比如你被禁言   比如禁止发言,只有管理员可以发言 notice advert
        mSocket.on("messageComing", onMessageComing);
       //用户发生变化,增加或减少
        mSocket.on("userChange", onUserChange);
    }


    private Emitter.Listener onError= new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i("gary", String.valueOf(args.length));
        }
    };

    private Emitter.Listener onConnect= new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            for(SysEvent event:mSysEventList)
            {
                if(event!=null)
                {
                    event.onConnect();
                }else{
                    mSysEventList.remove(event);
                }
            }
        }
    };
    private Emitter.Listener onDisconnect= new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            for(SysEvent event:mSysEventList)
            {
                if(event!=null)
                {
                    event.onDisconnect();
                }else{
                    mSysEventList.remove(event);
                }
            }
        }
    };
    private Emitter.Listener onSetChange= new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject jobj;
            String key=null;
            String value=null;
            try {
                jobj = (JSONObject) args[0];
                key=jobj.getString("key");
                value=jobj.getString("value");
            }catch (Exception ex)
            {
                ex.printStackTrace();
            }
            if(key!=null&&value!=null)
            for(SysEvent event:mSysEventList)
            {
                if(event!=null)
                {
                    event.onSetChange(key,value);
                }else{
                    mSysEventList.remove(event);
                }
            }
        }
    };

    private Emitter.Listener onLoginBack= new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject jobj=(JSONObject)args[0];
                boolean status=jobj.getInt("status")==1;
                String info=jobj.getString("info");
                if(status) {
                    currUser = ChatUser.FromJSON(jobj.getJSONObject("userinfo"));
                }

                for(MsgEvent event:mMsgEventList)
                {
                    if(event!=null)
                    {
                        event.onLoginBack(status,info);
                    }else{
                        mSysEventList.remove(event);
                    }
                }
            }catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    };
    private Emitter.Listener onGetNumsBack=new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject jobj=(JSONObject)args[0];
                int nums=jobj.getInt("nums");
                for(MsgEvent event:mMsgEventList)
                {
                    if(event!=null)
                    {
                        event.onGetNumsBack(nums);
                    }else{
                        mSysEventList.remove(event);
                    }
                }
            }catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    };
    private Emitter.Listener onMessageComing=new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject jsonObject=(JSONObject)args[0];
            try {
                ChatMessage msg=ChatMessage.fromJSON(jsonObject);
                for(MsgEvent event:mMsgEventList)
                {
                    if(event!=null)
                    {
                        event.onMessageComing(msg);
                    }else{
                        mSysEventList.remove(event);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    private Emitter.Listener onUserChange=new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject jobj=(JSONObject)args[0];
                boolean isIn=jobj.getInt("isIn")==1;
                int nums=jobj.getInt("nums");
                JSONObject userinfo=jobj.getJSONObject("userinfo");
                for(MsgEvent event:mMsgEventList)
                {
                    if(event!=null)
                    {
                        event.onUserChange(isIn,ChatUser.FromJSON(userinfo),nums);
                    }else{
                        mSysEventList.remove(event);
                    }
                }
            }catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    };

    private List<SysEvent>mSysEventList=new ArrayList<SysEvent>();
    public void regSysEvent(SysEvent event)
    {
        mSysEventList.add(event);
    }
    public void unRegSysEvent(SysEvent event)
    {
        mSysEventList.remove(event);
    }
    private List<MsgEvent>mMsgEventList=new ArrayList<MsgEvent>();
    public void regMsgEvent(MsgEvent event)
    {
        mMsgEventList.add(event);
    }
    public void unRegMsgEvent(MsgEvent event)
    {
        mMsgEventList.remove(event);
    }


}
