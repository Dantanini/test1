package tw.com.gear.marcorder;

import java.net.URL;

import tw.com.gear.marcorder.R;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.os.Build;

public class ActLogin extends Activity {
	String id = "";
	String pwd = "";
	String logInType = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.actlogin);
		InitialComponent();
	}

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

	// 關閉小鍵盤的方法
	OnClickListener closeEtxtFocus = new OnClickListener() {
		public void onClick(View v) {
			InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(etxtAccount.getWindowToken(), 0);
		}
	};

	// 註冊按鈕事件
	OnClickListener btnregist_Click = new OnClickListener() {
		public void onClick(View v) {
			if (!checkInternet(ActLogin.this) == true) {
				MyToast.Show(ActLogin.this, "偵測到網路連線不太穩定噢。\n檢查一下你的網路有沒有問題^.<");
				finish();
				return;
			}
			Intent intent = new Intent(ActLogin.this, ActSignUp.class);
			startActivity(intent);
			finish();
		}
	};

	// google登入事件
	OnClickListener googleLogin_click = new OnClickListener() {
		public void onClick(View v) {
			if (!checkInternet(ActLogin.this) == true) {
				MyToast.Show(ActLogin.this, "偵測到網路連線不太穩定噢。\n檢查一下你的網路有沒有問題^.<");
				finish();
				return;
			}
			final ProgressDialog myDialog = ProgressDialog.show(ActLogin.this,
					"連線中", "請等待...");

			new Thread() {
				public void run() {
					try {
						// /////////////////////////////////////////////////
						AccountManager accountManger = AccountManager
								.get(ActLogin.this);
						Account[] accounts = accountManger
								.getAccountsByType("com.google");
						String google = accounts[0].name.toString();

						URL url = new URL(ActMain.webpage);
						String s = new DataBaseManagement()
								.logIn(url, google, "").toString().trim();// 用google帳號，密碼放空自串
						ResultMsg = s;
						logInType = "google";
						Message msg = new Message();
						msg.what = GUI_OK;
						ActLogin.this.myMessageHandler.sendMessage(msg);

						// /////////////////////////////////////////////////////
					} catch (Exception e) {
						e.printStackTrace();
						MyToast.Show(ActLogin.this, "error");
					} finally {
						myDialog.dismiss();
					}
				}
			}.start();
		}
	};

	// 登入按鈕事件
	OnClickListener login_Click = new OnClickListener() {
		public void onClick(View v) {
			if (!checkInternet(ActLogin.this) == true) {
				MyToast.Show(ActLogin.this, "偵測到網路連線不太穩定噢。\n檢查一下你的網路有沒有問題^.<");
				finish();
				return;
			}

			if(etxtAccount.getText().toString().trim().equals("")){
				MyToast.Show(ActLogin.this, "請輸入帳號");
				return;
			}
			if(etxtPassword.getText().toString().trim().equals("")){
				MyToast.Show(ActLogin.this, "請輸入密碼");
				return;				
			}
			final ProgressDialog myDialog = ProgressDialog.show(ActLogin.this,
					"連線中", "請等待...");

			new Thread() {
				public void run() {
					try {
						// /////////////////////////////////////////////////
						id = etxtAccount.getText().toString().trim();
						pwd = etxtPassword.getText().toString().trim();

						URL url = new URL(ActMain.webpage);
						String s = new DataBaseManagement().logIn(url, id, pwd)
								.toString().trim();
						ResultMsg = s;
						Message msg = new Message();
						msg.what = GUI_OK;
						ActLogin.this.myMessageHandler.sendMessage(msg);

						// /////////////////////////////////////////////////////
					} catch (Exception e) {
						e.printStackTrace();
						MyToast.Show(ActLogin.this, "error");
					} finally {
						myDialog.dismiss();
					}
				}
			}.start();

		}
	};

	// ***Magic!***//
	Handler myMessageHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// super.handleMessage(msg);
			switch (msg.what) {
			case GUI_OK:
				String[] result = ResultMsg.split("@");
				if (result[0].equals("true")) {
					// 寫入資料到實體記憶體裡，以後可以檢查登入或是登出的狀態
					SharedPreferences sharedPre = getSharedPreferences("T1", 0);
					Editor editor = sharedPre.edit();
					AccountManager accountManger = AccountManager
							.get(ActLogin.this);
					Account[] accounts = accountManger
							.getAccountsByType("com.google");
					String googleAccount = accounts[0].name.toString();
					if (logInType.equals("google")) {
						editor.putBoolean(ActMain.googleLogInStr, true)
								.commit();
						editor.putString("id", googleAccount).commit();
						editor.putString("pwd", "").commit();
					} else {
						editor.putBoolean(ActMain.googleLogInStr, false)
								.commit();
						editor.putString("id", id).commit();
						editor.putString("pwd", pwd).commit();
					}
					editor.putString("name", result[1]).commit();
					editor.putString("phone", result[2]).commit();
					MyToast.Show(ActLogin.this, "登入成功");
					finish();
				} else if (result[0].equals("false")) {
					MyToast.Show(ActLogin.this, "登入失敗");
				}

				break;
			}
		}
	};

	// 圖片緊貼按鈕文字方法
	/**
	 * 讓圖片的Image緊鄰文字的方法，第一個參數放你要改的按鈕，第二個放圖片，第三個放按鈕上的字　　
	 * ※注意！這個方法目前會固定把圖片固定在左邊！而且會吃掉第一個文字！所以請在第一個文字多放隨便一個字母以便圖片蓋過而不影響原文字！
	 */
	private void ChangeBtnImage(Button btn, Drawable d, String s) {
		SpannableString spanText = new SpannableString(s);
		int a = (int) btn.getTextSize();
		// d.setBounds(0, 0, a, a);//設定圖片為文字的大小
		d.setBounds(0, 0, d.getIntrinsicWidth() * 5 / 4,
				d.getIntrinsicHeight() * 5 / 4);// 原始大小
		ImageSpan imageSpan = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);
		spanText.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		btn.setText(spanText);
	}

	// 回上一頁事件
	private void goBack() {
		finish();
		int version = Integer.valueOf(android.os.Build.VERSION.SDK_INT);
		if (version >= 5) {
			overridePendingTransition(R.anim.zoomlittlemove,
					R.anim.zoomloginout);
		}
	}

	// 更改回車鍵事件，讓使用者按下上一頁不是整個頁面關掉回到首頁，而是網頁回上一頁
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		goBack();
		return true;
	}

	// 初始化
	private void InitialComponent() {
		btnBack = (Button) findViewById(R.id.btnBack);
		btnBack.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				goBack();
			}
		});

		etxtAccount = (EditText) findViewById(R.id.etxtAccount);
		etxtPassword = (EditText) findViewById(R.id.etxtPassword);

		touch1 = (TextView) findViewById(R.id.touch1);
		touch1.setOnClickListener(closeEtxtFocus);
		touch2 = (TextView) findViewById(R.id.touch2);
		touch2.setOnClickListener(closeEtxtFocus);
		touch3 = (TextView) findViewById(R.id.touch3);
		touch3.setOnClickListener(closeEtxtFocus);
		lDown = (LinearLayout) findViewById(R.id.linearLayoutLogin);
		lDown.setOnClickListener(closeEtxtFocus);
		btnRegist = (Button) findViewById(R.id.btnRegist);
		btnRegist.setOnClickListener(btnregist_Click);
		btnLogin = (Button) findViewById(R.id.btnLogin);
		btnLogin.setOnClickListener(login_Click);
		btnForgetPwd = (Button) findViewById(R.id.btnForgetPwd);
		btnForgetPwd.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				Intent intent = new Intent(ActLogin.this, ActForgetPwd.class);
				startActivity(intent);
			}
		});
		btnGoogleLogin = (Button) findViewById(R.id.btnGoogleLogin);
		btnGoogleLogin.setOnClickListener(googleLogin_click);
		// 配置圖片給按鈕
		Drawable dGoogleLogin = getResources().getDrawable(
				R.drawable.googlemark2);
		ChangeBtnImage(btnGoogleLogin, dGoogleLogin, "1G登入");
		Drawable dMarcOrderLogin = getResources().getDrawable(R.drawable.mark4);
		ChangeBtnImage(btnLogin, dMarcOrderLogin, "1一般登入");
	}

	// 全域變數宣告區
	Button btnBack, btnLogin, btnRegist, btnForgetPwd, btnGoogleLogin;
	EditText etxtAccount, etxtPassword;
	LinearLayout lDown, lTop;// 關掉小鍵盤用的，這些都是被景元件，方便設定他們的click事件，點擊後關閉小鍵盤
	TextView touch1, touch2, touch3;// 關掉小鍵盤用的

	private static String ResultMsg = "";
	private static final int GUI_OK = 0x101;
}
