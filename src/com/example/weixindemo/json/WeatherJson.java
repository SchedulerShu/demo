package com.example.weixindemo.json;

import java.util.List;

public class WeatherJson {

	/**
	 * rc : 0 operation : QUERY service : weather data :
	 * {"result":[{"airQuality":"��","sourceName":"�й�������","date":"2016-08-15",
	 * "lastUpdateTime":"2016-08-15 18:40:40"
	 * ,"dateLong":1471190400,"pm25":"14","city":"��ݸ","humidity":"99%",
	 * "windLevel":0,"weather":"�е�����","tempRange":"25��","wind":"�޳�������΢��"},{
	 * "airQuality":"","sourceName":"�й�������","date":"2016-08-16",
	 * "lastUpdateTime":"2016-08-15 18:40:40"
	 * ,"city":"��ݸ","dateLong":1471276800,"windLevel":0,"weather":"�󵽱���",
	 * "tempRange":"25��~30��","wind":"�޳�������΢��"},{"airQuality":"","sourceName":
	 * "�й�������","date":"2016-08-17","lastUpdateTime":"2016-08-15 18:40:40"
	 * ,"city":"��ݸ","dateLong":1471363200,"windLevel":0,"weather":"�󵽱���",
	 * "tempRange":"25��~29��","wind":"�޳�������΢��"},{"airQuality":"","sourceName":
	 * "�й�������","date":"2016-08-18","lastUpdateTime":"2016-08-15 18:40:40"
	 * ,"city":"��ݸ","dateLong":1471449600,"windLevel":0,"weather":"�е�����ת������",
	 * "tempRange":"26��~30��","wind":"�޳�������΢��"},{"airQuality":"","sourceName":
	 * "�й�������","date":"2016-08-19","lastUpdateTime":"2016-08-15 18:40:40"
	 * ,"city":"��ݸ","dateLong":1471536000,"windLevel":0,"weather":"������",
	 * "tempRange":"26��~31��","wind":"�޳�������΢��"},{"airQuality":"","sourceName":
	 * "�й�������","date":"2016-08-20","lastUpdateTime":"2016-08-15 18:40:40"
	 * ,"city":"��ݸ","dateLong":1471622400,"windLevel":0,"weather":"������ת����",
	 * "tempRange":"26��~32��","wind":"�޳�������΢��"},{"airQuality":"","sourceName":
	 * "�й�������","date":"2016-08-21","lastUpdateTime":"2016-08-15 18:40:40"
	 * ,"city":"��ݸ","dateLong":1471708800,"windLevel":0,"weather":"��",
	 * "tempRange":"26��~34��","wind":"�޳�������΢��"}]}
	 */

	private int rc;
	private String operation;
	private String service;
	private DataBean data;

	public int getRc() {
		return rc;
	}

	public void setRc(int rc) {
		this.rc = rc;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public DataBean getData() {
		return data;
	}

	public void setData(DataBean data) {
		this.data = data;
	}

	public static class DataBean {
		/**
		 * airQuality : �� sourceName : �й������� date : 2016-08-15 lastUpdateTime :
		 * 2016-08-15 18:40:40 dateLong : 1471190400 pm25 : 14 city : ��ݸ
		 * humidity : 99% windLevel : 0 weather : �е����� tempRange : 25�� wind :
		 * �޳�������΢��
		 */

		private List<ResultBean> result;

		public List<ResultBean> getResult() {
			return result;
		}

		public void setResult(List<ResultBean> result) {
			this.result = result;
		}

		public static class ResultBean {
			private String airQuality;
			private String sourceName;
			private String date;
			private String lastUpdateTime;
			private int dateLong;
			private String pm25;
			private String city;
			private String humidity;
			private int windLevel;
			private String weather;
			private String tempRange;
			private String wind;

			public String getAirQuality() {
				return airQuality;
			}

			public void setAirQuality(String airQuality) {
				this.airQuality = airQuality;
			}

			public String getSourceName() {
				return sourceName;
			}

			public void setSourceName(String sourceName) {
				this.sourceName = sourceName;
			}

			public String getDate() {
				return date;
			}

			public void setDate(String date) {
				this.date = date;
			}

			public String getLastUpdateTime() {
				return lastUpdateTime;
			}

			public void setLastUpdateTime(String lastUpdateTime) {
				this.lastUpdateTime = lastUpdateTime;
			}

			public int getDateLong() {
				return dateLong;
			}

			public void setDateLong(int dateLong) {
				this.dateLong = dateLong;
			}

			public String getPm25() {
				return pm25;
			}

			public void setPm25(String pm25) {
				this.pm25 = pm25;
			}

			public String getCity() {
				return city;
			}

			public void setCity(String city) {
				this.city = city;
			}

			public String getHumidity() {
				return humidity;
			}

			public void setHumidity(String humidity) {
				this.humidity = humidity;
			}

			public int getWindLevel() {
				return windLevel;
			}

			public void setWindLevel(int windLevel) {
				this.windLevel = windLevel;
			}

			public String getWeather() {
				return weather;
			}

			public void setWeather(String weather) {
				this.weather = weather;
			}

			public String getTempRange() {
				return tempRange;
			}

			public void setTempRange(String tempRange) {
				this.tempRange = tempRange;
			}

			public String getWind() {
				return wind;
			}

			public void setWind(String wind) {
				this.wind = wind;
			}
		}
	}
}
