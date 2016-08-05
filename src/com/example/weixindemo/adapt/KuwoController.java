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
	 * 播放接口
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
	 * 暂停接口
	 * 
	 * @param context
	 */
	private void pause(Context context) {
		Log.d(TAG, "--------KW pause()");
		mKwapi.setPlayState(context, PlayState.STATE_PAUSE);
	}

	/**
	 * 上一首接口
	 * 
	 * @param context
	 */
	private void previous(Context context) {
		Log.d(TAG, "--------KW previous()");
		mKwapi.setPlayState(context, PlayState.STATE_PRE);
	}

	/**
	 * 下一曲
	 * 
	 * @param context
	 */
	private void next(Context context) {
		Log.d(TAG, "--------KW next()");
		mKwapi.setPlayState(context, PlayState.STATE_NEXT);
	}

	/**
	 * 退出接口
	 * 
	 * @param context
	 */
	private void stop(Context context) {
		Log.d(TAG, "--------KW stop()");
		mKwapi.exitAPP(context);
	}

	/**
	 * 通过歌名或者歌手搜索音乐接口
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

	// 打开音乐
	public void openKw(Context context) {
		Log.d(TAG, "open kuwo  mKwapi:" + mKwapi);

		mKwapi.startAPP(context, true);
	}

	// 关闭音乐
	public void closKw(Context context) {
		Log.d(TAG, "open kuwo  mKwapi:" + mKwapi);

		mKwapi.exitAPP(mContext);
	}
	
	
	/**
	 * 初始化监听器（语音到语义）。
	 */
	private InitListener mInitListener = new InitListener() {
		@Override
		public void onInit(int code) {
			Log.d(TAG, "speechUnderstanderListener init() code = " + code);
			if (code != ErrorCode.SUCCESS) {
				Log.d(TAG, "初始化失败,错误码：" + code);
			}
		}
	};
	
	private void setTtsParam() {
		// 清空参数
		mTts.setParameter(SpeechConstant.PARAMS, null);
		mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
		// 设置发音人
		mTts.setParameter(SpeechConstant.VOICE_NAME, voicerCloud);
		mTts.setParameter(SpeechConstant.SPEED, "50");// 设置合成语速
		mTts.setParameter(SpeechConstant.PITCH, "50");// 设置合成音调
		mTts.setParameter(SpeechConstant.VOLUME, "50");// 设置合成音量
		mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");// 设置播放器音频流类型
		mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");// 设置播放合成音频打断音乐播放，默认为true
		mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
		mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/tts.wav");
	}
	
	/**
	 * 合成回调监听。
	 */
	private SynthesizerListener mTtsListener = new SynthesizerListener() {

		@Override
		public void onSpeakBegin() {
			Log.d(TAG, "开始播放");
		}

		@Override
		public void onSpeakPaused() {
			Log.d(TAG, "暂停播放");
		}

		@Override
		public void onSpeakResumed() {
			Log.d(TAG, "继续播放");
		}

		@Override
		public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
			// 合成进度
		}

		@Override
		public void onSpeakProgress(int percent, int beginPos, int endPos) {
			// 播放进度
		}

		@Override
		public void onCompleted(SpeechError error) {
			if (error == null) {
				Log.d(TAG, "播放完成");
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
