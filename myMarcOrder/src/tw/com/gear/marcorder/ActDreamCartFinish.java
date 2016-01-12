package tw.com.gear.marcorder;


import java.net.URL;
import java.util.ArrayList;

import tw.com.gear.marcorder.R;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.os.Build;

public class ActDreamCartFinish extends Activity {
	ProgressDialog myDialog;
	private static final int GUI_OK = 0x101;//handler的驗證碼

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.act_dream_cart_finish);
		ActMain.whoIsActivity = "dreamCart";
		InitialComponent();
		ActMain.orderedIconList = new ArrayList<Bitmap>();
		
		ActMain.orderedIconList = findIconList();
	findViews();
	}
	
	private void findViews() {
		listItem = (ListView) findViewById(R.id.listADCFItem);
		listItem.setAdapter(new ImageTextAdapter(this, ActMain.myCart.getOrderList(), ActMain.orderedIconList));
//		Toast.makeText(ActPageCart.this, "有 "+ActOrder.myCart.getOrderList().size()+" 項商品", Toast.LENGTH_LONG).show();
		listItem.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//				TextView textView = (TextView) ((LinearLayout) view).getChildAt(1);
				MyToast.Show(getApplicationContext(), "" + ActMain.myCart.getOrderList().get(position).getAttrs());
				
//				Toast.makeText(getApplicationContext(), textView.getText(),	Toast.LENGTH_SHORT).show();
			}
		});
	}
	//找出相對應的Icon加入iconList來顯示
	private ArrayList<Bitmap> findIconList(){
		ArrayList<Bitmap> iconList = new ArrayList<Bitmap>();
		for(OrderItem i : ActMain.myCart.getOrderList()){
			if(i.getType().equals(ActMain.foodSetString)){//加入套餐的圖片
				iconList.add(((FoodSet)ActMain.foodSetFactoryHttpList.findBySerial(i.getSerial())).getIcon());
			}else if(!i.getType().equals(ActMain.foodSetString)){//加入單點的圖片
				iconList.add(((Food)ActMain.foodFactoryHttpList.findBySerial(i.getSerial())).getIcon());
			}
		}
		return iconList;
	}
	@Override
	protected void onResume() {
		int total = 0;
		for(OrderItem o : ActMain.myCart.getOrderList()){
			if(o.getAttrsState()==true){
				total += o.getPrice()*o.getQuantity()+o.getAttrsPrice()*o.getQuantity();				
			}else{				
				total += o.getPrice()*o.getQuantity();				
			}
		}
		txtMsg.setText("以下是您的餐點\r\n"+"總計："+total+"元");
		listItem.invalidateViews();
		super.onResume();
	}

	//返回點餐畫面
	OnClickListener btnCancel_Click = new OnClickListener() {
		public void onClick(View arg0) {
			ActMain.orderedIconList.clear();
			finish();
		}
	};
	String convertStr = "";
	//完成，把餐車資料存回資料庫
	OnClickListener btnOk_Click = new OnClickListener() {
		public void onClick(View arg0) {
			if(!(ActMain.myCart.getOrderList().size() > 0)){
				MyToast.Show(ActDreamCartFinish.this, "您尚未選取餐點");
				return;
			}
			if (!checkInternet(ActDreamCartFinish.this) == true) {
				MyToast.Show(ActDreamCartFinish.this,
						"偵測到網路連線狀況不穩！\n請檢查您的網路是否有連線或開啟了飛航模式。");
				return;
			}

			//存回雲端
			String index = getIntent().getStringExtra("jsonStr");
			convertStr = JsonFactory.GetJsonFromDreamCart(ActMain.myCart.getOrderList());
			if(index.equals("0"))
				convertStr = convertStr + "@" + getIntent().getStringExtra("str2") + "@" + getIntent().getStringExtra("str3");
			if(index.equals("1"))
				convertStr = getIntent().getStringExtra("str1") + "@" + convertStr +  "@" + getIntent().getStringExtra("str3");
			if(index.equals("2"))
				convertStr =  getIntent().getStringExtra("str1") + "@" + getIntent().getStringExtra("str2") + "@" + convertStr;
			if(convertStr.trim().equals("")){
				MyToast.Show(ActDreamCartFinish.this, "資料錯誤");
				return;				
			}
			//把購物車內容轉成jsonCode存上雲端資料庫
			myDialog = ProgressDialog.show(ActDreamCartFinish.this, "連線中", "請稍後...", true, true, new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					myDialog.dismiss();
				}
			});
			myDialog.setCanceledOnTouchOutside(false);
			new Thread(){
				public void run(){
					try{
						//上傳所有夢幻餐車資料
						URL url = new URL(ActMain.webpage);
						SharedPreferences sharePre =getSharedPreferences("T1", 0);
						String account = sharePre.getString("id", "");
						String responseStr = new DataBaseManagement()
							.upLoadDreamCart(url, account,convertStr).toString().trim();
						if(responseStr.equals("true")){
							Message msg = new Message();
							msg.what = GUI_OK;
							ActDreamCartFinish.this.myMessageHandler.sendMessage(msg);							
						}
					}catch(Exception e){
						
					}finally{
						myDialog.dismiss();
					}
				}
			}.start();		

		}
	};
	Handler myMessageHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			//super.handleMessage(msg);
			switch(msg.what){
			case GUI_OK:
					MyToast.Show(ActDreamCartFinish.this, "成功儲存");
					ActMain.myCart = new Cart();
					ActMain.orderedIconList.clear();
					ActDreamCartEdit.actDreamCartEdit.finish();
					finish();

			}
		}
	};
	
	// 檢查現在網路有沒有連線的方法
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
	
	private void InitialComponent() {
		btnBack = (Button)findViewById(R.id.btnADCFCancel);
		btnBack.setOnClickListener(btnCancel_Click);
		btnOk = (Button)findViewById(R.id.btnADCFOk);
		btnOk.setOnClickListener(btnOk_Click);		
		txtMsg = (TextView)findViewById(R.id.txtADCFMsg1);//點餐資訊(訂購時間日期，取餐方式.....)

	}
	Button btnBack;
	Button btnOk;
	TextView txtMsg;
	ListView listItem;

}
