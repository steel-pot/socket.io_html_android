package com.gary.myimapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.gary.imlib.ChatMessage;
import com.gary.imlib.ChatUser;
import com.gary.imlib.MsgEvent;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
