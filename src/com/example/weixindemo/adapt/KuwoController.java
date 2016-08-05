package com.example.weixindemo.adapt;

import com.example.weixindemo.json.MusicJson;
import com.google.gson.Gson;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import cn.kuwo.autosdk.api.KWAPI;
import cn.kuwo.autosdk.api.OnPlayerStatusListener;
import cn.kuwo.autosdk.api.PlayState;
import cn.kuwo.autosdk.api.PlayerStatus;

/**
 * @author
 * 
 */
public class KuwoController implements OnPlayerStatusListener {
	private static final String TAG = "KuwoController";

	private static final int MSG_PAUSE = 1001;
	private static final int MSG_PLAY = 1002;
	private static final int MSG_PREVOIUS = 1003;
	private static final int MSG_NEXT = 1004;
	private static final int MSG_SEARCH = 1005;
	private static final int MSG_STOP = 1006;
	private static final int MSG_START = 1007;
	private static final int MSG_EXIT = 1008;
	public static final int TIME_SET_STATE_DELAY = 100;

	private PlayerStatus mPlayerStatus = PlayerStatus.STOP;
	
	private SpeechSynthesizer mTts;
	public static String voicerCloud = "xiaoyan";

	// protected MusicControlProxy mMusicControl = null;
	private String mCmdString;

	private static KuwoController mInstance;
	private Context mContext;
	private KWAPI mKwapi;

	private String name;
	private String singer;
	
	private KuwoController() {

	}

	public static KuwoController getInstance() {
		if (mInstance == null) {
			mInstance = new KuwoController();
		}
		return mInstance;
	}

	@Override
	public void onPlayerStatus(PlayerStatus playerStatus) {
		if (playerStatus == null) {
			return;
		}
		Log.d(TAG, "onPlayerStatus: status=" + playerStatus.name());
		mPlayerStatus = playerStatus;
	}

	private Handler mMusicHandler = new Handler(Looper.getMainLooper()) {

		public void handleMessage(Message msg) {
			if (mKwapi == null) {
				mKwapi = KWAPI.createKWAPI(mContext, "auto");
				Log.d(TAG, "thread: " + Thread.currentThread().getName());
				mKwapi.registerPlayerStatusListener(mContext, KuwoController.this);
			}
			switch (msg.what) {
			case MSG_PAUSE:
				pause(mContext);
				break;
			case MSG_PLAY:
				play(mContext);
				break;
			case MSG_PREVOIUS:
				previous(mContext);
				break;
			case MSG_NEXT:
				next(mContext);
				break;
			case MSG_SEARCH:
				Log.d(TAG, "name  :"+name+"  singer:"+singer);
				search(mContext, name, singer,null);
				break;
			case MSG_STOP:
				stop(mContext);
				break;
			case MSG_START:
				openKw(mContext);
				break;
			case MSG_EXIT:
				closKw(mContext);
			default:
				break;
			}

		}
	};

	

	public String execute(Context context, String str) {
		mContext = context;
		mTts = SpeechSynthesizer.createSynthesizer(context, mInitListener);
		setTtsParam();
		Log.d(TAG, "mCmdString  :" + str);
		if (TextUtils.isEmpty(str)) {
			Log.e(TAG, "mCmdString empty error!!!");
			return null;
		}

		MusicJson music = new Gson().fromJson(str, MusicJson.class);
		String operation = music.getOperation();
		singer = music.getData().getResult().get(0).getSinger();
		name = music.getData().getResult().get(0).getName();
		String answerText = music.getText();
		
		if (operation.equals("PLAY")) {
			Log.d(TAG, "KuwoController  PLAY ....");
			mTts.startSpeaking(answerText, mTtsListener);
		}
		return answerText;
	}

	/**
	 * ���Žӿ�
	 * @param context
	 */
	public void play(Context context) {
		Log.d(TAG, "--------KW play()");
		if (mPlayerStatus == PlayerStatus.PAUSE || mPlayerStatus == PlayerStatus.PLAYING
				|| mPlayerStatus == PlayerStatus.BUFFERING) {
			mKwapi.setPlayState(context, PlayState.STATE_PLAY);
		} else {
			mKwapi.playClientMusics(context, null, null, null);
		}
	}

	/**
	 * ��ͣ�ӿ�
	 * 
	 * @param context
	 */
	private void pause(Context context) {
		Log.d(TAG, "--------KW pause()");
		mKwapi.setPlayState(context, PlayState.STATE_PAUSE);
	}

	/**
	 * ��һ�׽ӿ�
	 * 
	 * @param context
	 */
	private void previous(Context context) {
		Log.d(TAG, "--------KW previous()");
		mKwapi.setPlayState(context, PlayState.STATE_PRE);
	}

	/**
	 * ��һ��
	 * 
	 * @param context
	 */
	private void next(Context context) {
		Log.d(TAG, "--------KW next()");
		mKwapi.setPlayState(context, PlayState.STATE_NEXT);
	}

	/**
	 * �˳��ӿ�
	 * 
	 * @param context
	 */
	private void stop(Context context) {
		Log.d(TAG, "--------KW stop()");
		mKwapi.exitAPP(context);
	}

	/**
	 * ͨ���������߸����������ֽӿ�
	 * 
	 * @param context
	 * @param title
	 * @param artist
	 * @param album
	 */
	private void search(Context context, String title, String artist, String album) {
		Log.d(TAG, "--------KW search() title=" + title + " artist=" + artist + " album=" + album);
		mKwapi.playClientMusics(context, title, artist, album);
		mKwapi.setPlayState(context, PlayState.STATE_PLAY);
	}

	// ������
	public void openKw(Context context) {
		Log.d(TAG, "open kuwo  mKwapi:" + mKwapi);

		mKwapi.startAPP(context, true);
	}

	// �ر�����
	public void closKw(Context context) {
		Log.d(TAG, "open kuwo  mKwapi:" + mKwapi);

		mKwapi.exitAPP(mContext);
	}
	
	
	/**
	 * ��ʼ�������������������壩��
	 */
	private InitListener mInitListener = new InitListener() {
		@Override
		public void onInit(int code) {
			Log.d(TAG, "speechUnderstanderListener init() code = " + code);
			if (code != ErrorCode.SUCCESS) {
				Log.d(TAG, "��ʼ��ʧ��,�����룺" + code);
			}
		}
	};
	
	private void setTtsParam() {
		// ��ղ���
		mTts.setParameter(SpeechConstant.PARAMS, null);
		mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
		// ���÷�����
		mTts.setParameter(SpeechConstant.VOICE_NAME, voicerCloud);
		mTts.setParameter(SpeechConstant.SPEED, "50");// ���úϳ�����
		mTts.setParameter(SpeechConstant.PITCH, "50");// ���úϳ�����
		mTts.setParameter(SpeechConstant.VOLUME, "50");// ���úϳ�����
		mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");// ���ò�������Ƶ������
		mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");// ���ò��źϳ���Ƶ������ֲ��ţ�Ĭ��Ϊtrue
		mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
		mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/tts.wav");
	}
	
	/**
	 * �ϳɻص�������
	 */
	private SynthesizerListener mTtsListener = new SynthesizerListener() {

		@Override
		public void onSpeakBegin() {
			Log.d(TAG, "��ʼ����");
		}

		@Override
		public void onSpeakPaused() {
			Log.d(TAG, "��ͣ����");
		}

		@Override
		public void onSpeakResumed() {
			Log.d(TAG, "��������");
		}

		@Override
		public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
			// �ϳɽ���
		}

		@Override
		public void onSpeakProgress(int percent, int beginPos, int endPos) {
			// ���Ž���
		}

		@Override
		public void onCompleted(SpeechError error) {
			if (error == null) {
				Log.d(TAG, "�������");
				mMusicHandler.sendEmptyMessageDelayed(MSG_SEARCH, TIME_SET_STATE_DELAY);
			} else if (error != null) {
				Log.d(TAG, error.getPlainDescription(true));
			}
		}
		
		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
		}
	};
}
