package com.example.weixindemo;

import com.example.weixindemo.wakeup.WakeUpServeiver;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class ChatActivity extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	   setContentView(R.layout.activity_main);
	   
		Intent inten = new Intent();
		inten.setClass(this, WakeUpServeiver.class);
		startService(inten);
	   
	   new Thread(new Runnable() {
			@Override
			public void run() {
				FaceConversionUtil.getInstace().getFileText(getApplication());
			}
		}).start();
	}
	public void btnClick(View v){
		Intent intent=new Intent(ChatActivity.this,MainActivity.class);
		ChatActivity.this.startActivity(intent);
	}
}
