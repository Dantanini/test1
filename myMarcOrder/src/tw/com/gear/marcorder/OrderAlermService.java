package tw.com.gear.marcorder;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import tw.com.gear.marcorder.R;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.StrictMode;

public class OrderAlermService extends Service {
	NotificationManager notifimanger;
	Notification notification;
	HashMap<String, String> stateCompare;
	int downLoadState = 0;
	@Override
	public void onCreate() {

		super.onCreate();
	}
	
	@SuppressLint("NewApi")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		notifimanger = (NotificationManager)getApplication().getSystemService(NOTIFICATION_SERVICE);
		Intent notifiIntent = new Intent(getApplication(), ActSerchOrders.class);
		notifiIntent.setFlags(PendingIntent.FLAG_UPDATE_CURRENT);
		notifiIntent.putExtra("test", "intentSuccess");
		notifiIntent.setAction(Intent.ACTION_MAIN);
		notifiIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		PendingIntent appIntent = PendingIntent.getActivity(getApplication(), 0, notifiIntent, 0);
		
		Notification.Builder builder = new Notification.Builder(getApplication());
		long[] vibratepattern = {100, 800};
		Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		
		builder
		.setSmallIcon(R.drawable.ic_launcher)
		.setContentTitle("MarcOrder")
		.setContentText("您的餐點已經完成，請盡快來取餐!")
		.setTicker("您的餐點已經完成，請盡快來取餐!")
		.setLights(0xFFFFFFF, 1000, 1000)
		.setSound(uri)
		.setVibrate(vibratepattern)
		.setContentIntent(appIntent)
		.setAutoCancel(true);
		notification = builder.getNotification();
//		notification.defaults |= Notification.DEFAULT_SOUND;
		myHandler.postDelayed(myRunnable, 5000);
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
//		soundPool.stop(alertId);
		myHandler.removeCallbacks(myRunnable);
		super.onDestroy();
	}
	
	Handler myHandler = new Handler(){
		public void handleMessage(Message msg){
			notifimanger.notify(R.drawable.ic_launcher, notification);

		}
	};
	Runnable myRunnable = new Runnable() {
		@SuppressLint("NewApi")
		public void run() {
			try {			
				//如果網路連線正常，就連線伺服器
				if (checkInternet(getApplication()) == true) {
					
					// 1.先收尋出SQLite所有的訂單編號存入hashMap
					SQLiteMenagement manger = new SQLiteMenagement(
							getApplication());
					Cursor table = manger.Select("SELECT * FROM Ordered WHERE oState='新訂單' OR oState='處理中'");
					stateCompare = new HashMap<String, String>();
					table.moveToFirst();
					for (int tableInt = 0; tableInt < table.getCount(); tableInt++) {
						stateCompare.put(table.getString(1), table.getString(6));
						table.moveToNext();
					}
					String tmpStr = "";
					if(stateCompare.size() > 0){
						//連線伺服器
						//這是Theard的新api，如果有使用網路通訊功能就必須開啟此功能，除非自己建立Thread包住網路功能的程式
						StrictMode.ThreadPolicy l_policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
						StrictMode.setThreadPolicy(l_policy);
						/////////////////////////////////////////////////
						URL url = new URL(ActMain.webpage);
						for (Object oSerial : stateCompare.keySet()) {
							
							tmpStr = new DataBaseManagement().findOrderState(url, oSerial.toString().trim()).toString().trim();

						//如果訂單狀態有變化，而且是待取，就顯示提示Notification，並存入SQLite
							if(!tmpStr.equals(stateCompare.get(oSerial))){
								//寫入資料庫
								ContentValues data = new ContentValues();
								data.put("oState", tmpStr);
								manger.Update("Ordered", data, oSerial.toString());
								//判斷是否資料是待取，就顯示Notificaton
								if(tmpStr.equals("待取")){
									downLoadState++;
								}
							}
						}
					}
					if(downLoadState > 0){
						downLoadState = 0;
						Message msg = new Message();
						myHandler.sendMessage(msg);
					}
				}
			} catch (Exception e) {
//				icount = "error";
//				Message msg = new Message();
//				myHandler.sendMessage(msg);
			}
			myHandler.postDelayed(myRunnable, 10000);
		}
	};
	//判斷網路狀態
	public boolean checkInternet(android.content.Context context) {
		boolean result = false;
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connManager.getActiveNetworkInfo();
		if (info == null || !info.isConnected()) {
			result = false;
		} else {
			if (!info.isAvailable()) {
				result = false;
			} else {
				result = true;
			}
		}
		return result;
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}
