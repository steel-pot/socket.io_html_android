package com.gary.myimapp;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.gary.imlib.Chat;
import com.gary.imlib.ChatMessage;
import com.gary.imlib.ChatUser;
import com.gary.imlib.MessageFromType;
import com.gary.imlib.MessageType;
import com.gary.imlib.MsgEvent;

import org.json.JSONException;

import static com.gary.imlib.MessageFromType.*;

public class ChatFragment extends Fragment implements MsgEvent {

    /*
     * 需要使用自定义application 或者使用服务来执行连接操作,
     * 恢复activity已经注册过的事件可能会丢失,因为application会被重建
     * 所以需要在activity中注册事件
     * 在activity中取消事件
     * */
    private Chat mChat;
    public ChatFragment() {
        super();
    }
    private EditText mEditText;
    private EditText mInput;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);




        MyApplication app = (MyApplication) getActivity().getApplication();
        mChat=app.getChat();
        mChat.regMsgEvent(this);
    }
    private void addMessage(final String name, final String msg)
    {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mEditText.setText(name+":"+msg+"\r\n"+mEditText.getText());
            }
        });


    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.chat_fragment, container, false);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mEditText=(EditText) this.getView().findViewById(R.id.editText);
        mInput=(EditText)this.getView().findViewById(R.id.message_input);
        this.getView().findViewById(R.id.send_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addMessage("你",mInput.getText().toString());
                try {
                    mChat.sendMsg(MessageType.txt,mInput.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mInput.setText("");
            }
        });
    }
    @Override
    public void onDestroy() {
        mChat.unRegMsgEvent(this);
        super.onDestroy();
    }








    @Override
    public void onLoginBack(boolean status, String info) {

    }

    @Override
    public void onGetNumsBack(int nums) {
        addMessage("","当前聊天室有"+nums+"人");
    }

    @Override
    public void onMessageComing(ChatMessage message) {

        switch (message.getFromType())
        {
            case admin:
                    addMessage("管理员",message.getContent());
                break;
            case advert:
                    addMessage("公告",message.getContent());
                break;
            case notice:
                    addMessage("提示",message.getContent());
                break;
            case user:
                addMessage(message.getUser().getName(),message.getContent());
                break;

        }
    }

    @Override
    public void onUserChange(boolean isIn, ChatUser user,int nums) {
        String str=isIn?"进入聊天室":"离开聊天室";
        addMessage("",user.getName()+str+",当前在线"+nums+"人 ");
    }
}
