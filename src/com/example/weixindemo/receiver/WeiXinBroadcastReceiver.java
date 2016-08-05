package com.example.weixindemo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class WeiXinBroadcastReceiver extends BroadcastReceiver {
	private static final String TAG = WeiXinBroadcastReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "onReceive .....");

		// Intent service = new Intent();
		// service.setClass(context, WakeUpServeiver.class);
		// context.startService(service);

	}

}
