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
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUnderstander;
import com.iflytek.cloud.SpeechUnderstanderListener;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.UnderstanderResult;
import com.iflytek.cloud.SpeechEvent;

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

	private Button mBtnBack;
	private EditText mEditTextContent;
	private ListView mListView;
	private ChatMsgViewAdapter mAdapter;
	private List<ChatMsgEntity> mDateArrays = new ArrayList<ChatMsgEntity>();

	// ����
	private ImageView volume;
	private ImageView btn_photo;
	private SoundMeter mSensor;
	private CircleWaveView mCircleWaveView;

	public static final int SHOW_ALL_PICTURE = 0x14;// �鿴ͼƬ
	public static final int SHOW_PICTURE_RESULT = 0x15;// �鿴ͼƬ����
	public static final int CLOSE_INPUT = 0x01;// �ر������
	public static Handler handlerInput;// ���������+
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
	
	boolean     isshow = false;
	int ret = 0;// �������÷���ֵ
	private Handler mHandler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);//���ر���
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
	    WindowManager.LayoutParams.FLAG_FULLSCREEN);//����ȫ��
		   
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
		//mediaPlayer01.setLooping(true);
		mediaPlayer01.start();
	}

	MediaPlayer mediaPlayer;

	private void startPlay() {
		if (mediaPlayer == null) {
			try {
				mediaPlayer = new MediaPlayer();
				mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
					@Override
					public void onPrepared(MediaPlayer mp) {
						mediaPlayer.start();
					}
				});
				mediaPlayer.reset();
				mediaPlayer.setAudioSessionId(R.raw.bdspeech_recognition_start);
				mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
				mediaPlayer.prepareAsync();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void initView() {
		// btn_face = (ImageButton)findViewById(R.id.btn_face);
		//btn_photo = (ImageView) findViewById(R.id.btn_photo);
		mListView = (ListView) findViewById(R.id.listview);
		//mBtnBack = (Button) findViewById(R.id.btn_back);
		volume = (ImageView) this.findViewById(R.id.volume);
		mCircleWaveView = (CircleWaveView) findViewById(R.id.crircle);
		mSensor = new SoundMeter();
		//mBtnBack.setOnClickListener(this);
		//btn_photo.setOnClickListener(this);
		findViewById(R.id.btn_speech).setOnClickListener(this);
		mCircleWaveView.setVisibility(View.VISIBLE);
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
		  * ����Ϊ����
		  */
		 if(getRequestedOrientation()!=ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
		  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		 }
		
		super.onResume();
		Log.d(TAG, "onResume....");
		Intent intentTowakeup = new Intent();
		intentTowakeup.setClass(this, WakeUpServeiver.class);
		stopService(intentTowakeup);
		mCircleWaveView.setVisibility(View.GONE);
		
	
		String text ="��ӭ���٣���Ը�";
		
		//mTts.startSpeaking(text, mTtsListener);
		send(text);
		
		isshow = true;
		
		startSpeech();
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		/*
		case R.id.btn_back:
			finish();
			break;
		case R.id.btn_photo:
			new PopupWindows(MainActivity.this, btn_photo);
			// ���ر���ѡ���
			((FaceRelativeLayout) findViewById(R.id.FaceRelativeLayout)).hideFaceView();
			break;
			*/
		case R.id.btn_speech:
			Log.d(TAG, "btn_speech.....");
			startSpeech();
			break;
		}
	}

	private void startSpeech() {
		startAnimation();
		
		if(mTts.isSpeaking()){
			mTts.stopSpeaking();
		}
		
		setUnderParam();

		if (mSpeechUnderstander.isUnderstanding()) {// ��ʼǰ���״̬
			mSpeechUnderstander.cancel();
			Log.d(TAG, "ֹͣ¼��");
			stat = STATUS_None;
			mCircleWaveView.setVisibility(View.GONE);
		} else {
			
			mCircleWaveView.setVisibility(View.VISIBLE);
			ret = mSpeechUnderstander.startUnderstanding(mSpeechUnderstanderListener);
			if (ret != 0) {
				Log.d(TAG, "�������ʧ��,������:" + ret);
			} else {
				Log.d(TAG, " �뿪ʼ˵����");
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
			entity.setDate(getDate());
			entity.setName("���¸�Ƿ");
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
			entity.setDate(getDate());
			entity.setName("Ǿޱ��ĭ");
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
					 * EXTERNAL_CONTENT_URI);//����android��ͼ��
					 * startActivity(intent); dismiss();
					 */
					Intent intent = new Intent(MainActivity.this, ScaleImageFromSdcardActivity.class);
					MainActivity.this.startActivityForResult(intent, SHOW_ALL_PICTURE);
					dismiss();
					overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);// �����л����������ұ߽��룬����˳�
				}
			});
			bt3.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					dismiss();
				}
			});

		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == TAKE_PICTURE && resultCode == Activity.RESULT_OK && null != data) {
			if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				Toast.makeText(MainActivity.this, "δ�ҵ�SDK", 1).show();
				return;
			}
			new android.text.format.DateFormat();
			String name = android.text.format.DateFormat.format("yyyyMMdd_hhmmss", Calendar.getInstance(Locale.CHINA))
					+ ".jpg";
			Bundle bundle = data.getExtras();
			// ��ȡ������ص����ݣ���ת��ΪͼƬ��ʽ
			Bitmap bitmap;
			String filename = null;
			bitmap = (Bitmap) bundle.get("data");
			FileOutputStream fout = null;
			// �����ļ��洢·��
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
			Log.d("TAG", "��Ҫ���������ͼƬ��������" + camera.toString());
			// ��ͼƬ���͵��������
			if (camera.toString().length() > 0) {
				ChatMsgEntity entity = new ChatMsgEntity();
				entity.setDate(getDate());
				entity.setName("���¸�Ƿ");
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
				entity.setName("���¸�Ƿ");
				entity.setMsgType(false);
				entity.setText("[" + bmpUrl + "]");
				mDateArrays.add(entity);
				mAdapter.notifyDataSetChanged();
				mEditTextContent.setText("");
				mListView.setSelection(mListView.getCount() - 1);
			}
			Toast.makeText(MainActivity.this, "ѡ��ͼƬ��" + selectPictures.length, Toast.LENGTH_LONG).show();
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
			// entity.setName("���¸�Ƿ");
			// entity.setMsgType(false);
			// entity.setText(path);
			// mDateArrays.add(entity);
			// mAdapter.notifyDataSetChanged();
			// mListView.setSelection(mListView.getCount() - 1);

		} else {
			Toast.makeText(MainActivity.this, "û�д��濨", Toast.LENGTH_LONG).show();
		}
	}

	public void head_xiaohei(View v) { // ������ ���ذ�ť

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
		if (mSpeechUnderstander.isUnderstanding()) // ��ʼǰ���״̬
			 mSpeechUnderstander.cancel();
		
		Intent inten = new Intent();
		inten.setClass(this, WakeUpServeiver.class);
		startService(inten);
		
		
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

	/**
	 * �������ص���
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
					send(answerText);
				}
				if (rc == 0) {
					String sh = UnderstanderConttoller.getInstance().execute(MainActivity.this, service, text);
					Log.d(TAG, "sh  :" + sh);
					if (!TextUtils.isEmpty(sh)) {
						mTts.startSpeaking(sh, mTtsListener);
						sendGril(sh);
					}
				}
			} else {
				Log.d(TAG, "ʶ��������ȷ��");
			}
			
			
		}

		@Override
		public void onVolumeChanged(int volume, byte[] data) {
			// Log.d(TAG, "��ǰ����˵����������С��" + volume);
			// Log.d(TAG, data.length + "");
		}

		@Override
		public void onEndOfSpeech() {
			// �˻ص���ʾ����⵽��������β�˵㣬�Ѿ�����ʶ����̣����ٽ�����������
			Log.d(TAG, "����˵��");
			mCircleWaveView.setVisibility(View.GONE);
			stat = STATUS_None;
		}

		@Override
		public void onBeginOfSpeech() {
			// �˻ص���ʾ��sdk�ڲ�¼�����Ѿ�׼�����ˣ��û����Կ�ʼ��������
			mCircleWaveView.setVisibility(View.VISIBLE);
			Log.d(TAG, "��ʼ˵��");
		}

		@Override
		public void onError(SpeechError error) {
			Log.d(TAG, error.getPlainDescription(true));
			mTts.startSpeaking("������û˵��Ŷ��", mTtsListener);
			send("������û˵��Ŷ��");
			mCircleWaveView.setVisibility(View.GONE);
			stat = STATUS_None;
		}

		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
			// ���´������ڻ�ȡ���ƶ˵ĻỰid����ҵ�����ʱ���Ựid�ṩ������֧����Ա�������ڲ�ѯ�Ự��־����λ����ԭ��
			 if (SpeechEvent.EVENT_SESSION_ID == eventType) {
			 String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
			 Log.d(TAG, "session id =" + sid);
			 }
		}
	};

	/**
	 * ��������
	 * 
	 * @param param
	 * @return
	 */
	private void setUnderParam() {
		mSpeechUnderstander.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
		mSpeechUnderstander.setParameter(SpeechConstant.ACCENT, "zh_cn");
		mSpeechUnderstander.setParameter(SpeechConstant.VAD_BOS, "5000");// ��������ǰ�˵�
		mSpeechUnderstander.setParameter(SpeechConstant.VAD_EOS, "1500");// ����������˵�
		mSpeechUnderstander.setParameter(SpeechConstant.ASR_PTT, "1");
		mSpeechUnderstander.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
		mSpeechUnderstander.setParameter(SpeechConstant.ASR_AUDIO_PATH,
				Environment.getExternalStorageDirectory() + "/msc/sud.wav");
	}

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

			} else if (error != null) {
				Log.d(TAG, error.getPlainDescription(true));
			}
			
			if(isshow == true)
			   startSpeech();
			
		}

		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
		}
	};
}
