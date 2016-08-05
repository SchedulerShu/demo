package com.example.weixindemo.wakeup;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.weixindemo.MainActivity;
import com.example.weixindemo.R;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.VoiceWakeuper;
import com.iflytek.cloud.WakeuperListener;
import com.iflytek.cloud.WakeuperResult;
import com.iflytek.cloud.util.ResourceUtil;
import com.iflytek.cloud.util.ResourceUtil.RESOURCE_TYPE;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class WakeUpServeiver extends Service {
	private static final String TAG = WakeUpServeiver.class.getSimpleName();
	private Context mContext;

	// �������Ѷ���
	private VoiceWakeuper mIvw;
	// ���ѽ������
	private String resultString;

	private String keep_alive = "0";
	private String ivwNetMode = "0";

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate....");
		super.onCreate();
		mContext = this;
		mIvw = VoiceWakeuper.createWakeuper(mContext, null);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// �ǿ��жϣ���ֹ���ָ��ʹ�������
		mIvw = VoiceWakeuper.getWakeuper();
		if (mIvw != null) {

			resultString = "";

			// ��ղ���
			mIvw.setParameter(SpeechConstant.PARAMS, null);
			// ��������ֵ��������ԴЯ���Ļ��Ѵʸ������ա�id:����;id:���ޡ��ĸ�ʽ����
			mIvw.setParameter(SpeechConstant.IVW_THRESHOLD, "0:" + 60);
			// ���û���ģʽ
			mIvw.setParameter(SpeechConstant.IVW_SST, "wakeup");
			// ���ó������л���
			mIvw.setParameter(SpeechConstant.KEEP_ALIVE, keep_alive);
			// ���ñջ��Ż�����ģʽ
			mIvw.setParameter(SpeechConstant.IVW_NET_MODE, ivwNetMode);
			// ���û�����Դ·��
			mIvw.setParameter(SpeechConstant.IVW_RES_PATH, getResource());
			// ��������
			mIvw.startListening(mWakeuperListener);
		} else {
			Log.d(TAG, "����δ��ʼ��");
		}

		return START_STICKY;

	}

	private WakeuperListener mWakeuperListener = new WakeuperListener() {

		@Override
		public void onResult(WakeuperResult result) {
			Log.d(TAG, "onResult");
			if (!"1".equalsIgnoreCase(keep_alive)) {

			}
			try {
				String text = result.getResultString();
				JSONObject object;
				object = new JSONObject(text);
				StringBuffer buffer = new StringBuffer();
				buffer.append("��RAW�� " + text);
				buffer.append("\n");
				buffer.append("���������͡�" + object.optString("sst"));
				buffer.append("\n");
				buffer.append("�����Ѵ�id��" + object.optString("id"));
				buffer.append("\n");
				buffer.append("���÷֡�" + object.optString("score"));
				buffer.append("\n");
				buffer.append("��ǰ�˵㡿" + object.optString("bos"));
				buffer.append("\n");
				buffer.append("��β�˵㡿" + object.optString("eos"));
				resultString = buffer.toString();
				int scors = Integer.parseInt(object.optString("score"));

				Intent intent = new Intent(mContext, MainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mContext.startActivity(intent);

			} catch (JSONException e) {
				resultString = "�����������";
				e.printStackTrace();
			}
			Log.d(TAG, "resultString :" + resultString);
		}

		@Override
		public void onError(SpeechError error) {

		}

		@Override
		public void onBeginOfSpeech() {
		}

		@Override
		public void onEvent(int eventType, int isLast, int arg2, Bundle obj) {

		}

		@Override
		public void onVolumeChanged(int volume) {

		}
	};

	private String getResource() {
		return ResourceUtil.generateResourcePath(this, RESOURCE_TYPE.assets,
				"ivw/" + getString(R.string.app_id) + ".jet");

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy WakeDemo");
		mIvw = VoiceWakeuper.getWakeuper();
		if (mIvw != null) {
			mIvw.destroy();
		}
	}

}
