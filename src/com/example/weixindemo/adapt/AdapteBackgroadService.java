package com.example.weixindemo.adapt;

import com.example.weixindemo.json.FqaJson;
import com.google.gson.Gson;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

public class AdapteBackgroadService extends Service {
	private static final String TAG = AdapteBackgroadService.class.getSimpleName();
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate....");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String command = intent.getStringExtra("service");
		String text = intent.getStringExtra("text");
		openService(command,text);
		
		return START_STICKY;
	}

	private void openService(String command,String text) {
		if (!TextUtils.isEmpty(command)){
			Gson gson  = new Gson();
			if(command.equals("baike")){
				FqaJson fqa = gson.fromJson(text, FqaJson.class);
				
			}else if(command.equals("music")){
				
			}else if(command.equals("faq")){
				
			}
		}	
	}
	
	
}
