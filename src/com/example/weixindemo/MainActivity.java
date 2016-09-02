package com.example.weixindemo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import com.example.weixindemo.adapt.UnderstanderConttoller;
import com.example.weixindemo.json.CommonJson;
import com.example.weixindemo.util.CircleWaveView;
import com.example.weixindemo.wakeup.WakeUpServeiver;
import com.google.gson.Gson;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUnderstander;
import com.iflytek.cloud.SpeechUnderstanderListener;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.UnderstanderResult;
import com.iflytek.cloud.SpeechEvent;
import android.annotation.SuppressLint;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

	private static final String TAG = MainActivity.class.getSimpleName();

	private EditText mEditTextContent;
	private ListView mListView;
	private ChatMsgViewAdapter mAdapter;
	private List<ChatMsgEntity> mDateArrays = new ArrayList<ChatMsgEntity>();

	// 语音
	private ImageView volume;
	private ImageView btn_photo;
	private SoundMeter mSensor;
	private CircleWaveView mCircleWaveView;

	public static final int SHOW_ALL_PICTURE = 0x14;// 查看图片
	public static final int SHOW_PICTURE_RESULT = 0x15;// 查看图片返回
	public static final int CLOSE_INPUT = 0x01;// 关闭软键盘
	public static Handler handlerInput;// 用于软键盘+
	// private String photoName;

	private SpeechUnderstander mSpeechUnderstander;
	private SpeechSynthesizer mTts;
	public static String voicerCloud = "xiaoyan";

	public static final int STATUS_None = 0;
	public static final int STATUS_WaitingReady = 2;
	public static final int STATUS_Ready = 3;
	public static final int STATUS_Speaking = 4;
	public static final int STATUS_Recognition = 5;
	private int stat = STATUS_None;

	boolean isshow = false;
	int ret = 0;// 函数调用返回值
	private Handler mHandler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏

		setContentView(R.layout.chat);

		initView();
		initData();

		mSpeechUnderstander = SpeechUnderstander.createUnderstander(MainActivity.this, mInitListener);
		mTts = SpeechSynthesizer.createSynthesizer(this, mInitListener);
	}

	public void playerWarning() {
		MediaPlayer mediaPlayer01;
		mediaPlayer01 = MediaPlayer.create(getBaseContext(), R.raw.bdspeech_recognition_start);
		mediaPlayer01.setAudioStreamType(AudioManager.STREAM_MUSIC);
		// mediaPlayer01.setLooping(true);
		mediaPlayer01.start();
	}

	public void initView() {
		// btn_face = (ImageButton)findViewById(R.id.btn_face);
		// btn_photo = (ImageView) findViewById(R.id.btn_photo);
		mListView = (ListView) findViewById(R.id.listview);
		volume = (ImageView) this.findViewById(R.id.volume);
		mCircleWaveView = (CircleWaveView) findViewById(R.id.crircle);
		mSensor = new SoundMeter();
		// btn_photo.setOnClickListener(this);
		findViewById(R.id.btn_speech).setOnClickListener(this);

	}

	private void start(String name) {
		mSensor.start(name);
		mHandler.postDelayed(mPollTask, POLL_INTERVAL);
	}

	private void stop() {
		mHandler.removeCallbacks(mSleepTask);
		mHandler.removeCallbacks(mPollTask);
		mSensor.stop();
		volume.setImageResource(R.drawable.amp1);
	}

	private static final int POLL_INTERVAL = 300;

	private Runnable mSleepTask = new Runnable() {
		public void run() {
			stop();
		}
	};
	private Runnable mPollTask = new Runnable() {
		public void run() {
			double amp = mSensor.getAmplitude();
			updateDisplay(amp);
			mHandler.postDelayed(mPollTask, POLL_INTERVAL);
		}
	};

	public void initData() {
		mAdapter = new ChatMsgViewAdapter(this, mDateArrays);
		mListView.setAdapter(mAdapter);
	}

	@Override
	protected void onResume() {

		/**
		 * 设置为横屏
		 */
		if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}

		super.onResume();
		Log.d(TAG, "onResume....");
		Intent intentTowakeup = new Intent();
		intentTowakeup.setClass(this, WakeUpServeiver.class);
		stopService(intentTowakeup);
		mCircleWaveView.setVisibility(View.VISIBLE);

		String text = "欢迎光临，请吩咐";

		// mTts.startSpeaking(text, mTtsListener);
		sendGril(text);

		isshow = true;

		startSpeech();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		/*
		 * case R.id.btn_back: finish(); break; case R.id.btn_photo: new
		 * PopupWindows(MainActivity.this, btn_photo); // 隐藏表情选择框
		 * ((FaceRelativeLayout)
		 * findViewById(R.id.FaceRelativeLayout)).hideFaceView(); break;
		 */
		case R.id.btn_speech:
			Log.d(TAG, "btn_speech.....");
			startSpeech();
			break;
		}
	}

	private void startSpeech() {
		startAnimation();
		// mListView.setAdapter(null);
		if (mTts.isSpeaking()) {
			mTts.stopSpeaking();
		}

		setUnderParam();

		if (mSpeechUnderstander.isUnderstanding()) {// 开始前检查状态
			mSpeechUnderstander.cancel();
			Log.d(TAG, "停止录音");
			stat = STATUS_None;
			mCircleWaveView.setVisibility(View.GONE);
		} else {
			mCircleWaveView.setVisibility(View.VISIBLE);
			ret = mSpeechUnderstander.startUnderstanding(mSpeechUnderstanderListener);
			if (ret != 0) {
				Log.d(TAG, "语义理解失败,错误码:" + ret);
			} else {
				Log.d(TAG, " 请开始说话…");
				mCircleWaveView.setVisibility(View.VISIBLE);
			}
		}
	}

	private void startAnimation() {
		Log.d(TAG, "stat :" + stat);
		switch (stat) {
		case STATUS_None:
			Log.d(TAG, "STATUS_None  ");
			playerWarning();
			mCircleWaveView.setVisibility(View.VISIBLE);
			stat = STATUS_WaitingReady;
			break;
		case STATUS_WaitingReady:
			Log.d(TAG, "STATUS_WaitingReady  ");
			mCircleWaveView.setVisibility(View.GONE);
			stat = STATUS_None;
			break;
		case STATUS_Speaking:
			break;
		}
	}

	public void send(String conString) {
		if (conString.length() > 0) {
			ChatMsgEntity entity = new ChatMsgEntity();
			// entity.setDate(getDate());
			entity.setName("小白");
			entity.setMsgType(false);
			entity.setText(conString);
			mDateArrays.add(entity);

			mAdapter.notifyDataSetChanged();

			mListView.setSelection(mListView.getCount() - 1);
		}
	}

	public void sendGril(String conString) {
		if (conString.length() > 0) {
			ChatMsgEntity entity = new ChatMsgEntity();
			// entity.setDate(getDate());
			entity.setName("小新");
			entity.setMsgType(true);
			entity.setText(conString);
			mDateArrays.add(entity);

			mAdapter.notifyDataSetChanged();
			mListView.setSelection(mListView.getCount() - 1);
		}
	}

	public String getDate() {
		Calendar c = Calendar.getInstance();
		String year = String.valueOf(c.get(Calendar.YEAR));
		String month = String.valueOf(c.get(Calendar.MONTH));
		String day = String.valueOf(c.get(Calendar.DAY_OF_MONTH) + 1);
		String hour = String.valueOf(c.get(Calendar.HOUR_OF_DAY));
		String mins = String.valueOf(c.get(Calendar.MINUTE));

		StringBuffer sbBuffer = new StringBuffer();
		sbBuffer.append(year + "-" + month + "-" + day + "-" + hour + ":" + mins);
		return sbBuffer.toString();
	}

	private void updateDisplay(double signalEMA) {

		switch ((int) signalEMA) {
		case 0:
		case 1:
			volume.setImageResource(R.drawable.amp1);
			break;
		case 2:
		case 3:
			volume.setImageResource(R.drawable.amp2);

			break;
		case 4:
		case 5:
			volume.setImageResource(R.drawable.amp3);
			break;
		case 6:
		case 7:
			volume.setImageResource(R.drawable.amp4);
			break;
		case 8:
		case 9:
			volume.setImageResource(R.drawable.amp5);
			break;
		case 10:
		case 11:
			volume.setImageResource(R.drawable.amp6);
			break;
		default:
			volume.setImageResource(R.drawable.amp7);
			break;
		}
	}

	public class PopupWindows extends PopupWindow {

		public PopupWindows(Context mContext, View parent) {

			super(mContext);

			View view = View.inflate(mContext, R.layout.item_popubwindows, null);
			view.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_ins));
			LinearLayout ll_popup = (LinearLayout) view.findViewById(R.id.ll_popup);
			ll_popup.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.push_bottom_in_2));

			setWidth(LayoutParams.FILL_PARENT);
			setHeight(LayoutParams.FILL_PARENT);
			setBackgroundDrawable(new BitmapDrawable());
			setFocusable(true);
			setOutsideTouchable(true);
			setContentView(view);
			showAtLocation(parent, Gravity.BOTTOM, 0, 0);
			update();

			Button bt1 = (Button) view.findViewById(R.id.item_popupwindows_camera);
			Button bt2 = (Button) view.findViewById(R.id.item_popupwindows_Photo);
			Button bt3 = (Button) view.findViewById(R.id.item_popupwindows_cancel);
			bt1.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					// photo();
					// dismiss();
					Intent intent = new Intent();
					Intent intent_camera = getPackageManager().getLaunchIntentForPackage("com.android.camera");
					if (intent_camera != null) {
						intent.setPackage("com.android.camera");
					}
					intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
					MainActivity.this.startActivityForResult(intent, TAKE_PICTURE);
					dismiss();
				}
			});
			bt2.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					// Intent intent = new Intent(MainActivity.this,
					// TestPicActivity.class);
					// startActivity(intent);
					// dismiss();
					/*
					 * Intent intent = new Intent(Intent.ACTION_PICK,
					 * android.provider.MediaStore.Images.Media.
					 * EXTERNAL_CONTENT_URI);//调用android的图库
					 * startActivity(intent); dismiss();
					 */
					Intent intent = new Intent(MainActivity.this, ScaleImageFromSdcardActivity.class);
					MainActivity.this.startActivityForResult(intent, SHOW_ALL_PICTURE);
					dismiss();
					overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);// 设置切换动画，从右边进入，左边退出
				}
			});
			bt3.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					dismiss();
				}
			});

		}
	}

	@SuppressLint("SdCardPath")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == TAKE_PICTURE && resultCode == Activity.RESULT_OK && null != data) {
			if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				Toast.makeText(MainActivity.this, "未找到SDK", 1).show();
				return;
			}
			new android.text.format.DateFormat();
			String name = android.text.format.DateFormat.format("yyyyMMdd_hhmmss", Calendar.getInstance(Locale.CHINA))
					+ ".jpg";
			Bundle bundle = data.getExtras();
			// 获取相机返回的数据，并转换为图片格式
			Bitmap bitmap;
			String filename = null;
			bitmap = (Bitmap) bundle.get("data");
			FileOutputStream fout = null;
			// 定义文件存储路径
			File file = new File("/sdcard/cloudteam/");
			if (!file.exists()) {
				file.mkdirs();
			}
			filename = file.getPath() + "/" + name;
			try {
				fout = new FileOutputStream(filename);
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fout);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} finally {
				try {
					fout.flush();
					fout.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			Intent intent = new Intent(MainActivity.this, CameraActivity.class);
			intent.putExtra("camera", filename);
			MainActivity.this.startActivityForResult(intent, SHOW_CAMERA);
		} else if (requestCode == SHOW_CAMERA && resultCode == SHOW_CAMERA_RESULT) {
			Bundle bundle = data.getExtras();
			Object camera = bundle.get("imgUrl");
			Log.d("TAG", "需要发送照相的图片到服务器" + camera.toString());
			// 将图片发送到聊天界面
			if (camera.toString().length() > 0) {
				ChatMsgEntity entity = new ChatMsgEntity();
				entity.setDate(getDate());
				entity.setName("小白");
				entity.setMsgType(false);
				entity.setText("[" + camera.toString() + "]");
				mDateArrays.add(entity);
				mAdapter.notifyDataSetChanged();
				mEditTextContent.setText("");
				mListView.setSelection(mListView.getCount() - 1);
			}
		} else if (requestCode == SHOW_ALL_PICTURE && resultCode == SHOW_PICTURE_RESULT) {
			List<String> bmpUrls = new ArrayList<String>();

			Bundle bundle = data.getExtras();
			Object[] selectPictures = (Object[]) bundle.get("selectPicture");
			for (int i = 0; i < selectPictures.length; i++) {
				Log.d("TAG", "selectPictures[i]" + selectPictures[i]);
				String bmpUrl = ScaleImageFromSdcardActivity.map.get(Integer.parseInt(selectPictures[i].toString()));
				bmpUrls.add(bmpUrl);
				ChatMsgEntity entity = new ChatMsgEntity();
				entity.setDate(getDate());
				entity.setName("小白");
				entity.setMsgType(false);
				entity.setText("[" + bmpUrl + "]");
				mDateArrays.add(entity);
				mAdapter.notifyDataSetChanged();
				mEditTextContent.setText("");
				mListView.setSelection(mListView.getCount() - 1);
			}
			Toast.makeText(MainActivity.this, "选择图片数" + selectPictures.length, Toast.LENGTH_LONG).show();
		}
	}

	private static final int TAKE_PICTURE = 0x000000;
	private static final int SHOW_CAMERA = 0x000001;
	private static final int SHOW_CAMERA_RESULT = 0x000002;
	private String path = "";

	public void photo() {
		String status = Environment.getExternalStorageState();
		if (status.equals(Environment.MEDIA_MOUNTED)) {
			File dir = new File(Environment.getExternalStorageDirectory() + "/myimage/");
			if (!dir.exists())
				dir.mkdirs();

			Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			File file = new File(dir, String.valueOf(System.currentTimeMillis()) + ".jpg");
			path = file.getPath();
			Uri imageUri = Uri.fromFile(file);
			openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
			openCameraIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
			startActivityForResult(openCameraIntent, TAKE_PICTURE);
			// ChatMsgEntity entity = new ChatMsgEntity();
			// entity.setDate(getDate());
			// entity.setName("古月哥欠");
			// entity.setMsgType(false);
			// entity.setText(path);
			// mDateArrays.add(entity);
			// mAdapter.notifyDataSetChanged();
			// mListView.setSelection(mListView.getCount() - 1);

		} else {
			Toast.makeText(MainActivity.this, "没有储存卡", Toast.LENGTH_LONG).show();
		}
	}

	public void head_xiaohei(View v) { // 标题栏 返回按钮

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& ((FaceRelativeLayout) findViewById(R.id.FaceRelativeLayout)).hideFaceView()) {
			return true;
		}
		Intent inten = new Intent();
		inten.setClass(this, WakeUpServeiver.class);
		startService(inten);
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG, "onStop...");

		isshow = false;
		if (mSpeechUnderstander.isUnderstanding()) // 开始前检查状态
			mSpeechUnderstander.cancel();

		Intent inten = new Intent();
		inten.setClass(this, WakeUpServeiver.class);
		startService(inten);

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

	/**
	 * 语义理解回调。
	 */
	private SpeechUnderstanderListener mSpeechUnderstanderListener = new SpeechUnderstanderListener() {

		@Override
		public void onResult(final UnderstanderResult result) {
			if (null != result) {
				String text = result.getResultString();

				Gson gson = new Gson();
				CommonJson ss = gson.fromJson(text, CommonJson.class);
				int rc = ss.getRc();
				String answer = ss.getOperation();
				String service = ss.getService();
				String answerText = ss.getText();

				Log.d(TAG, "answer   :" + answer);
				Log.d(TAG, "service   :" + service);
				Log.d(TAG, "answerText   :" + answerText);
				setTtsParam();
				if (!TextUtils.isEmpty(text)) {
					sendGril(answerText);
				}
				if (rc == 0) {
					String sh = UnderstanderConttoller.getInstance().execute(MainActivity.this, service, text);
					Log.d(TAG, "sh  :" + sh);
					if (!TextUtils.isEmpty(sh)) {
						mTts.startSpeaking(sh, mTtsListener);
						send(sh);
					}
				}
			} else {
				Log.d(TAG, "识别结果不正确。");
			}

		}

		@Override
		public void onVolumeChanged(int volume, byte[] data) {
			// Log.d(TAG, "当前正在说话，音量大小：" + volume);
			// Log.d(TAG, data.length + "");
		}

		@Override
		public void onEndOfSpeech() {
			// 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
			Log.d(TAG, "结束说话");
			mCircleWaveView.setVisibility(View.GONE);
			stat = STATUS_None;
		}

		@Override
		public void onBeginOfSpeech() {
			// 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
			mCircleWaveView.setVisibility(View.VISIBLE);
			Log.d(TAG, "开始说话");
		}

		@Override
		public void onError(SpeechError error) {
			Log.d(TAG, error.getPlainDescription(true));
			mTts.startSpeaking("您好像没说话哦！", mTtsListener);
			sendGril("您好像没说话哦！");
			mCircleWaveView.setVisibility(View.GONE);
			stat = STATUS_None;
		}

		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
			// 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
			if (SpeechEvent.EVENT_SESSION_ID == eventType) {
				String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
				Log.d(TAG, "session id =" + sid);
			}
		}
	};

	/**
	 * 参数设置
	 * 
	 * @param param
	 * @return
	 */
	private void setUnderParam() {
		mSpeechUnderstander.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
		mSpeechUnderstander.setParameter(SpeechConstant.ACCENT, "zh_cn");
		mSpeechUnderstander.setParameter(SpeechConstant.VAD_BOS, "5000");// 设置语音前端点
		mSpeechUnderstander.setParameter(SpeechConstant.VAD_EOS, "1500");// 设置语音后端点
		mSpeechUnderstander.setParameter(SpeechConstant.ASR_PTT, "1");
		mSpeechUnderstander.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
		mSpeechUnderstander.setParameter(SpeechConstant.ASR_AUDIO_PATH,
				Environment.getExternalStorageDirectory() + "/msc/sud.wav");
	}

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

			} else if (error != null) {
				Log.d(TAG, error.getPlainDescription(true));
			}

			if (isshow == true)
				startSpeech();

		}

		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
		}
	};
}
