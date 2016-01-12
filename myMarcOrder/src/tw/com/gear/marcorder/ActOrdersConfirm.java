package tw.com.gear.marcorder;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import tw.com.gear.marcorder.R;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

public class ActOrdersConfirm extends Activity{
//	public static Activity actOrdersConfirm;
	private static final int GUI_OK = 0x101;
	public static String ResultMsg = "";
	private String google = "";
	private boolean isMamber = false;
	private boolean isGoogleMamber = false;
	private boolean useGoogleSignUp = false;
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.act_orders_confirm);
//		actOrdersConfirm = this;
//這是Theard的新api，如果有使用網路通訊功能就必須開啟此功能，除非自己建立Thread包住網路功能的程式
//		StrictMode.ThreadPolicy l_policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//		StrictMode.setThreadPolicy(l_policy);
		
		initialComponent();
		SharedPreferences sharedPre = getSharedPreferences("T1", 0);
		Boolean googleLogIn = sharedPre.getBoolean(ActMain.googleLogInStr, false);
		String id = sharedPre.getString("id", "");
		String pwd = sharedPre.getString("pwd", "");
		String name = sharedPre.getString("name", "");
		String phone = sharedPre.getString("phone", "");
		if(googleLogIn || (!id.equals("") && !pwd.equals(""))){
			//2.如果有資料就將txtName和txtPhone設為使用者的姓名電話
			txtCName.setText(name);
			txtCPhone.setText(phone);
			layoutckb.setVisibility(View.GONE);
			isMamber = true;	
			if(googleLogIn)
				isGoogleMamber = true;
		}
	}


	OnClickListener btnBack_Click = new OnClickListener() {
		@Override
		public void onClick(View v) {
			finish();
			
			// 動畫由左至右
			int version = Integer.valueOf(android.os.Build.VERSION.SDK_INT);
			if (version >= 5) {
				overridePendingTransition(R.anim.zoomin2, R.anim.zoomout2);
			}
		}
	};
	//取消現在訂單的資訊
	OnClickListener btnConfirm_Click = new OnClickListener() {
		@Override
		public void onClick(View v) {

			
			new AlertDialog.Builder(ActOrdersConfirm.this).setIcon(R.drawable.marcordericon2)
				.setMessage("確定要取消嗎?\r\n所有資料會清除").setTitle("取消確認")
				.setNeutralButton("確定", OrderClear_Click)
				.setNegativeButton("取消", OrderNothing_Click).show();
		}
	};
	DialogInterface.OnClickListener OrderClear_Click = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {

			//結束自己頁面
			finish();
			//結束點餐頁面
			ActReadyToOrder.actReadyTorder.finish();
			ActOrder.actOrderActivity.finish();
		}
	};
	DialogInterface.OnClickListener OrderNothing_Click = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
		
		}
	};
	OnCheckedChangeListener ckb1_CheckedChange = new OnCheckedChangeListener() {
		@SuppressLint("NewApi")
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			if(isChecked == true){
				//判斷是否有google帳號
				AccountManager accountManger = AccountManager.get(ActOrdersConfirm.this);
				Account[ ] accounts = accountManger.getAccountsByType("com.google");
				google = accounts[0].name.toString();
				if(!google.equals("")){
					Builder build = new AlertDialog.Builder(ActOrdersConfirm.this).setTitle("請問是否使用gmail帳號註冊").setMessage("使用gmail帳號註冊，不需填寫任何資料，即可購買，並且下次購買還可使用夢幻餐車功能喔!").setNeutralButton("否", gmailNo_click).setNegativeButton("是", gmailYes_clcik);
					Dialog d = build.create();
					d.show();
				}
			}
			
			
			
			if (isChecked == true) {
				layout1.setVisibility(View.VISIBLE);
				layout2.setVisibility(View.VISIBLE);
				layout5.setVisibility(View.VISIBLE);
				layout7.setVisibility(View.VISIBLE);
			} else {
				layout1.setVisibility(View.GONE);
				layout2.setVisibility(View.GONE);
				layout5.setVisibility(View.GONE);
				layout7.setVisibility(View.GONE);
			}

		}
	};
	
	android.content.DialogInterface.OnClickListener gmailYes_clcik = new android.content.DialogInterface.OnClickListener(){
		@Override
		public void onClick(DialogInterface dialog, int which) {
			layout1.setVisibility(View.GONE);
			layout2.setVisibility(View.GONE);
			layout5.setVisibility(View.GONE);
			layout7.setVisibility(View.GONE);
			useGoogleSignUp = true;
			
		}
	};
	
	android.content.DialogInterface.OnClickListener gmailNo_click = new android.content.DialogInterface.OnClickListener(){
		@Override
		public void onClick(DialogInterface dialog, int which) {
			layout1.setVisibility(View.VISIBLE);
			layout2.setVisibility(View.VISIBLE);
			layout5.setVisibility(View.VISIBLE);
			layout7.setVisibility(View.VISIBLE);
			useGoogleSignUp = false;
		}
	};
	
	
OnClickListener mainLayout_click = new OnClickListener() {
	@Override
	public void onClick(View v) {
		InputMethodManager imm = (InputMethodManager)getSystemService(ActOrdersConfirm.this.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(layout4.getWindowToken(), 0);
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
	String EmailText = "";
	String nameText = "";
	String phoneText = "";
	String txtIdStr = "";
	String txtPasswordStr = "";
	String txtPasswordCompareStr = "";
	String sPid = "";
	String sPpwd = "";
	boolean googleLoginState = false;
	int orderUser = 1;//有三種狀態，1."非會員"2."會員"3."立即註冊成會員並購買"
	private void initialComponent() {
		txtCEmail = (EditText)findViewById(R.id.txtAOCCEmail);
		txtCName = (EditText)findViewById(R.id.txtAOCCName);
		txtCPhone = (EditText)findViewById(R.id.txtAOCCPhone);
		txtCOther = (EditText)findViewById(R.id.txtAOCCOther);
		btnBack = (Button) findViewById(R.id.btnAOCCencel);
		btnBack.setOnClickListener(btnBack_Click);
		btnConfirm = (Button) findViewById(R.id.btnAOCOk);
		btnConfirm.setOnClickListener(btnConfirm_Click);
		ckb1 = (CheckBox) findViewById(R.id.ckbAOC1);
		ckb1.setOnCheckedChangeListener(ckb1_CheckedChange);
		txtID = (EditText)findViewById(R.id.txtAOCCId);
		txtPassword = (EditText)findViewById(R.id.txtAOCCPassword);
		txtPasswordCompar = (EditText)findViewById(R.id.txtAOCCPasswordCompare);
		layout1 = (LinearLayout) findViewById(R.id.LinearLayoutAOC1);
		layout2 = (LinearLayout) findViewById(R.id.LinearLayoutAOC2);
		layout3 = (LinearLayout) findViewById(R.id.LinearLayoutAOC3);
		layout5 = (LinearLayout) findViewById(R.id.LinearLayoutAOC6);
		layout7 = (LinearLayout) findViewById(R.id.LinearLayoutAOC7);
		layout4 = (LinearLayout) findViewById(R.id.LinearLayoutAOC4);
		layout4.setOnClickListener(mainLayout_click);
		layoutckb = (LinearLayout) findViewById(R.id.LinearLayoutAOC5);
		btnSend = (Button) findViewById(R.id.btnSend);
		//送出訂單按鈕事件
		btnSend.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {//判斷連線狀態
				if(checkInternet(ActOrdersConfirm.this) == false){
					MyToast.Show(ActOrdersConfirm.this, "偵測到網路連線不太穩定噢。\n檢查一下你的網路有沒有問題呦~^.<");
					return;
				}
				EmailText = txtCEmail.getText().toString().trim();
				nameText = txtCName.getText().toString().trim();
				phoneText = txtCPhone.getText().toString().trim();
				txtIdStr = txtID.getText().toString().trim();
				txtPasswordStr = txtPassword.getText().toString().trim();
				txtPasswordCompareStr = txtPasswordCompar.getText().toString().trim();
				//1.先判斷是哪種會員或非會員
				if(isMamber==true){
					if(nameText.equals("") || phoneText.equals("")){
						MyToast.Show(ActOrdersConfirm.this, "請填寫姓名電話");
						return;						
					}	
					SharedPreferences sharedPre = getSharedPreferences("T1", 0);
					sPid = sharedPre.getString("id", "");
					sPpwd = sharedPre.getString("pwd", "");
					orderUser = 2;//"會員"
				}else if(ckb1.isChecked() == false){
					if(nameText.equals("") || phoneText.equals("")){
						MyToast.Show(ActOrdersConfirm.this, "請填寫姓名電話");
						return;						
					}					
					orderUser = 1;//"非會員"
				}else if(ckb1.isChecked() == true){
					//先註冊在購買
					if(!useGoogleSignUp){						
						if(nameText.equals("") && phoneText.equals("")){
							MyToast.Show(ActOrdersConfirm.this, "請填寫姓名電話");
							return;						
						}
						if(txtIdStr.equals("")){
							MyToast.Show(ActOrdersConfirm.this, "帳號未填");
							return;						
						}
						if(txtPasswordStr.equals("")){
							MyToast.Show(ActOrdersConfirm.this, "密碼未填");
							return;						
						}
						if(!txtPasswordStr.equals(txtPasswordCompareStr)){
							MyToast.Show(ActOrdersConfirm.this, "密碼不符!");
							return;						
						}
						if(EmailText.equals("")){
							MyToast.Show(ActOrdersConfirm.this, "Email未填");
							return;												
						}
					}else{
						if(nameText.equals("") && phoneText.equals("")){
							MyToast.Show(ActOrdersConfirm.this, "請填寫姓名電話");
							return;						
						}
					}
					orderUser = 3;
				}
				
				//判斷orderUser的狀態是哪一種去執行
				switch(orderUser){
					case 1://非會員
						nomalOrder();
						break;
					case 2://會員
						accountOrder();
						break;
					case 3://立即註冊會員，並且點餐
						signUpOrder();
						break;
				}

			}
		});
	}
	//1非會員訂購
	private void nomalOrder(){
		ActMain.myCart.setCustomerName(txtCName.getText().toString());
		ActMain.myCart.setCustomerPhone(txtCPhone.getText().toString());
		ActMain.myCart.setOther(txtCOther.getText().toString());
		final ProgressDialog myDialog = ProgressDialog.show(ActOrdersConfirm.this, "連線中", "請等待...");
		
		new Thread(){
			public void run(){
				try{
			///////////////////////////////////////////////////

						URL url = new URL(ActMain.webpage);
						String s = new DataBaseManagement().sendOrdersToDataBase(url, ActMain.myCart).toString().trim();
						ResultMsg = s;
						Message msg = new Message();
						msg.what = GUI_OK;
						ActOrdersConfirm.this.finishMessageHandler.sendMessage(msg);

				///////////////////////////////////////////////////////
				}catch(Exception e){
					e.printStackTrace();
					MyToast.Show(ActOrdersConfirm.this, "送出失敗");
				}finally{
					myDialog.dismiss();
				}
			}
		}.start();
	}
	//2會員訂購
	private void accountOrder(){
		ActMain.myCart.setCustomerName(txtCName.getText().toString());
		ActMain.myCart.setCustomerPhone(txtCPhone.getText().toString());
		ActMain.myCart.setOther(txtCOther.getText().toString());
		final ProgressDialog myDialog = ProgressDialog.show(ActOrdersConfirm.this, "連線中", "請等待...");
		
		new Thread(){
			public void run(){
				try{
			///////////////////////////////////////////////////

						URL url = new URL(ActMain.webpage);
						String s = new DataBaseManagement().accountSendOrdersToDataBase(url, ActMain.myCart, sPid, sPpwd).toString().trim();
						ResultMsg = s;
						Message msg = new Message();
						msg.what = GUI_OK;
						ActOrdersConfirm.this.finishMessageHandler.sendMessage(msg);

				///////////////////////////////////////////////////////
				}catch(Exception e){
					e.printStackTrace();
					MyToast.Show(ActOrdersConfirm.this, "送出失敗");
				}finally{
					myDialog.dismiss();
					

				}
			}
		}.start();
	}
	//3註冊會員並訂購
	private void signUpOrder(){
		ActMain.myCart.setCustomerName(txtCName.getText().toString());
		ActMain.myCart.setCustomerPhone(txtCPhone.getText().toString());
		ActMain.myCart.setOther(txtCOther.getText().toString());
		final ProgressDialog myDialog = ProgressDialog.show(ActOrdersConfirm.this, "連線中", "請等待...");
		
		new Thread(){
			public void run(){
				try{
					ResultMsg = "";
			///////////////////////////////////////////////////
					URL url = new URL(ActMain.webpage);
						//先註冊
					ArrayList<String> signUpData= new ArrayList<String>();
					if(useGoogleSignUp){
						signUpData.add(nameText);
						signUpData.add(phoneText);
						signUpData.add("");
						signUpData.add(google);//帳號是google帳號
						signUpData.add("");//使用google帳號，密碼就放空字串
						signUpData.add("");
						signUpData.add(google);//信箱也是google帳號
					}else{
						signUpData.add(nameText);
						signUpData.add(phoneText);
						signUpData.add("");
						signUpData.add(txtIdStr);
						signUpData.add(txtPasswordStr);
						signUpData.add(txtPasswordCompareStr);						
						signUpData.add(EmailText);						
					}
					String s = new DataBaseManagement().signUp(url, signUpData).toString().trim();
					
					if(s.equals("id has been used")){
						ResultMsg = "帳號已被使用，請填其他帳號";
						Message msg = new Message();
						msg.what = GUI_OK;
						ActOrdersConfirm.this.errorMessageHandler.sendMessage(msg);
						myDialog.dismiss();
						this.interrupt();
					}else if(s.equals("true")){
						//註冊完成，登入帳號
						if(useGoogleSignUp){
							s = new DataBaseManagement().logIn(url, google, "").toString().trim();							
						}else{							
							s = new DataBaseManagement().logIn(url, signUpData.get(3), signUpData.get(4)).toString().trim();							
						}
						
						String[] result = s.split("@");
						if(result[0].equals("true")){
							SharedPreferences sharedPre = getSharedPreferences("T1", 0);
							Editor editor = sharedPre.edit();
							if(useGoogleSignUp){
								editor.putBoolean(ActMain.googleLogInStr, true);
								editor.putString("id", google).commit();
								editor.putString("pwd", "").commit();
							}else{
								editor.putBoolean(ActMain.googleLogInStr, false);
								editor.putString("id", signUpData.get(3)).commit();
								editor.putString("pwd", signUpData.get(4)).commit();
							}
							editor.putString("name", result[1]).commit();
							editor.putString("phone", result[2]).commit();	
						}
						

						sPid = signUpData.get(3);
						sPpwd = signUpData.get(4);
						//下訂單
						s = new DataBaseManagement().accountSendOrdersToDataBase(url, ActMain.myCart, sPid, sPpwd).toString().trim();
						ResultMsg = s;
						Message msg = new Message();
						msg.what = GUI_OK;
						ActOrdersConfirm.this.finishMessageHandler.sendMessage(msg);
					}

				///////////////////////////////////////////////////////
				}catch(Exception e){
					e.printStackTrace();
					MyToast.Show(ActOrdersConfirm.this, "送出失敗");
				}finally{
					myDialog.dismiss();

				}
			}
		}.start();
	}
	
	
	//這是thread的handler目的是用來將訊息傳回mainThread
	//正常完成訂單時，會執行這個handler去開啟下一頁面
	Handler finishMessageHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			//super.handleMessage(msg);
			switch(msg.what){
			case GUI_OK:
				if(ResultMsg.equals("error")){
					MyToast.Show(ActOrdersConfirm.this, "資料輸入錯誤");
					return;
				}
				if(ResultMsg.length() == 19){//判斷回傳的訂單序號有沒有19碼，正確才轉到結束頁
					Intent intent = new Intent(ActOrdersConfirm.this, ActOrderFinish.class);
					intent.putExtra("result", ResultMsg);
					if(ckb1.isChecked() == true){
						intent.putExtra("ans", "恭喜你完成該筆訂單\r\n系統已經自動登入\r\n下次購買即可使用此帳號快速購買!");
					}else{
						intent.putExtra("ans", "恭喜你完成該筆訂單");
					}
					startActivity(intent);
					finish();									
				}else if(ResultMsg.equals("") && ResultMsg.length() == 19){
					ResultMsg = "回傳資料錯誤，請洽工程師";
					MyToast.Show(ActOrdersConfirm.this, ResultMsg);
				}
				break;
			}
			
		}
		
	};
	//發生錯誤時會啟動這個handler跳出提醒
	Handler errorMessageHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			//super.handleMessage(msg);
			switch(msg.what){
			case GUI_OK:
				MyToast.Show(ActOrdersConfirm.this, ResultMsg);

				break;
			}

		}
		
	};
	Button btnBack;
	Button btnConfirm, btnSend;
	EditText txtCName;
	EditText txtCPhone;
	EditText txtCAddress;
	EditText txtCOther;
	
	EditText txtCEmail;
	EditText txtID;
	EditText txtPassword;
	EditText txtPasswordCompar;	
	CheckBox ckb1;
	LinearLayout layout1;
	LinearLayout layout2;
	LinearLayout layout3;
	LinearLayout layout4;
	LinearLayout layout5;
	LinearLayout layout7;
	LinearLayout layoutckb;
}
