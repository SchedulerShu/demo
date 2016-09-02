package com.example.weixindemo.adapt;

import com.example.weixindemo.json.CommonJson;
import com.example.weixindemo.json.FqaJson;
import com.example.weixindemo.json.WeatherJson;
import com.google.gson.Gson;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

public class UnderstanderConttoller {
	private static final String TAG = UnderstanderConttoller.class.getSimpleName();
	private static UnderstanderConttoller mInstance = null;
	
	
	private UnderstanderConttoller() {

    }
	
	public static UnderstanderConttoller getInstance() {
		if (mInstance == null) {
			synchronized (UnderstanderConttoller.class) {
				if (mInstance == null) {
					mInstance = new UnderstanderConttoller();
				}
			}
		}
		return mInstance;
	}
	
	public String execute(Context context, String service, String command){
		Log.d(TAG, "command...:"+command);
		
		if (!TextUtils.isEmpty(command)){
			Gson gson  = new Gson();
			
			if(service.equals("baike")){
				CommonJson ss = gson.fromJson(command, CommonJson.class);
				String text = ss.getAnswer().getText();
				Log.d(TAG, "text  :"+text);
				
				return text;
			}else if(service.equals("music")){

				KuwoController.getInstance().execute(context, command);
				
				return null;
			}else if(service.equals("faq")){
				FqaJson fqa = gson.fromJson(command, FqaJson.class);
				String text = fqa.getAnswer().getText();
				Log.d(TAG, "text  :"+text);
				return text;
			}else if(service.equals("openQA")){
				CommonJson ss = gson.fromJson(command, CommonJson.class);
				String text = ss.getAnswer().getText();
				Log.d(TAG, "text  :"+text);
				return text;
			}else if("chat".equals(service)){
				CommonJson ss = gson.fromJson(command, CommonJson.class);
				String text = ss.getAnswer().getText();
				Log.d(TAG, "text  :"+text);
				return text;
			}else if("calc".equals(service))
			{
				CommonJson ss = gson.fromJson(command, CommonJson.class);
				String text = ss.getAnswer().getText();
				Log.d(TAG, "text  :"+text);
				return text;
			}
			else if("datetime".equals(service))
			{
				CommonJson ss = gson.fromJson(command, CommonJson.class);
				String text = ss.getAnswer().getText();
				Log.d(TAG, "text  :"+text);
				return text;
			}else if("weather".equals(service)){
				WeatherJson weather = gson.fromJson(command, WeatherJson.class);
				StringBuffer sbBuffer = new StringBuffer();
				sbBuffer.append(weather.getData().getResult().get(0).getCity());
				sbBuffer.append("，");
				sbBuffer.append(weather.getData().getResult().get(0).getWeather());
				sbBuffer.append("空气质量"+weather.getData().getResult().get(0).getAirQuality());
				sbBuffer.append("，温度为"+weather.getData().getResult().get(0).getTempRange());
				Log.d(TAG, "text  :"+sbBuffer);
				return sbBuffer.toString();
			}else if("websearch".equals(service)){
				CommonJson ss = gson.fromJson(command, CommonJson.class);
				String text = ss.getText();
				Log.d(TAG, "text  :"+text);
				return text;
			}else if("cookbook".equals(service)){
				return "暂时不能识别！";
			}else{
				return "不能识别！";
			}
		}
		return null;
	}

}
