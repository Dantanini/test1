package tw.com.gear.marcorder;

import tw.com.gear.marcorder.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.os.Build;

public class ActForgetPwd extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_PROGRESS);// 让进度条显示在标题栏上，前提是飆頭一定要存在，所以和上面那個只能存在一個
		setContentView(R.layout.activity_act_forget_pwd);
		InitialComponent();
	}

	// 就回首頁阿
	private void GoToHome() {
		finish();
		// int version = Integer.valueOf(android.os.Build.VERSION.SDK_INT);
		// if (version >= 5) {
		// overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
		// }
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

	private void fun_thread() {
		new Thread() {
			public void run() {
				try {
					// 若15後仍未讀到網頁, 則顯示網路雍塞中
					for (int i = 0; i < 15; i++) {
						if (progressBar.isShowing()) {
							sleep(1000);
						} else {
							return;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				// 為了能與handler建立關聯進而控制Progress Dialog / Progress Bar的關閉
				Message m = new Message();
				Bundle b = m.getData();
				b.putInt("WHICH", 1);
				m.setData(b);
				handler.sendMessage(m);
			}
		}.start();
	}

	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (progressBar.isShowing()) {
				int which = msg.getData().getInt("WHICH");
				progressbar.setVisibility(View.GONE);
				progressBar.dismiss();
				if (which == 1) {
					MyToast.Show(ActForgetPwd.this, "連線逾時\n可能是訊號不太好呦^.<");
					finish();
				}
			}
		}
	};

	// 設定網頁開啟的瀏覽器為目前的webView，不然手機會用其他瀏覽器開啟，例如Chrome.
	WebViewClient mWebViewClient = new WebViewClient() {
		// 設定開啟的webView和連結的網址
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}

		// 設定如果有網頁讀取要做甚麼，通常都是跑出progressBar，記得每次開啟新頁要跑一下fun_thread因為要避免連線時間過長的問題.
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			// 下面這個是每次換頁讀取中讓progressBar跑出來的事件，但因為還沒自製蘭條，所以是輪圈，每次點擊就跑出來很煩人
			// 所以mark掉，如果哪天要改掉蘭條和標頭，想打開這個，再打開吧！
			if (!progressBar.isShowing()) {
				// progressBar.show();
				// fun_thread();// 同時偵測是否連線超過設定的秒數，免的因為讀取不到網頁而讓使用者一直卡在這頁
			}
			super.onPageStarted(view, url, favicon);
		}

		// 網頁讀取結束要跑出甚麼，通常都是關掉剛剛的progressBar
		@Override
		public void onPageFinished(WebView view, String url) {
			// 如果進度條已經存在就消失
			if (progressBar.isShowing()) {
				progressBar.dismiss();
			}
			super.onPageFinished(view, url);
		}

		// 資源下載過程要執行甚麼，我想暫時別動這個，還不是很清楚他的所有設定
		@Override
		public void onLoadResource(WebView view, String url) {
			super.onLoadResource(view, url);
		}
	};

	// 設置標頭顯示用的，如果標頭被關掉了...就會導致進度條甚麼的都沒有了XD
	WebChromeClient mWebChromeClient = new WebChromeClient() {
		@Override
		public void onReceivedTitle(WebView view, String title) {
			// if ((title != null) && (title.trim().length() != 0)) {
			setTitle("MarcOrder");// 設定title的文字
			// }
		}

		// 當進度條改變的時候執行的事件
		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			// Activity和Webview根据加载程度决定进度条的进度大小
			// 当加载到100%的时候 进度条自动消失
			ActForgetPwd.this.setProgress(newProgress * 100);
			// super.onProgressChanged(view, newProgress);
		}

	};

	// 更改回車鍵事件，讓使用者按下上一頁不是整個頁面關掉回到首頁，而是網頁回上一頁
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// 先判斷網頁還有沒有上一頁，如果有就執行下面是劍
		if ((keyCode == KeyEvent.KEYCODE_BACK) && wvForgetPwd.canGoBack()) {
			wvForgetPwd.goBack();// 網頁回上一頁
			return true;
		}
		// 如果不能回上一頁那就整個頁面觀掉
		GoToHome();
		return true;
	}

	// 初始化
	private void InitialComponent() {
		btnHome = (Button) findViewById(R.id.btnForgetPwdGoHome);
		btnHome.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				GoToHome();// 回首頁
			}

		});

		wvForgetPwd = (WebView) findViewById(R.id.wvForgetPwd);
		wvForgetPwd.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		WebSettings websettings = wvForgetPwd.getSettings();// 取得webview的設定
		websettings.setJavaScriptEnabled(true);// 預設會限制JavaScript，所以要打開他

		final AlertDialog alertDialog = new AlertDialog.Builder(this).create();

		progressbar = (ProgressBar) findViewById(R.id.myProgressBarForgetPwd);
		progressbar.setVisibility(View.VISIBLE);

		// 檢查有網路連線再讓她顯示讀取以及跑出網頁
		if (checkInternet(ActForgetPwd.this) == true) {
			progressBar = ProgressDialog.show(ActForgetPwd.this, "MarcOrder",
					"讀取中...");

			// 啟用內置的縮放功能.
			websettings.setSupportZoom(true);
			websettings.setBuiltInZoomControls(true);

			// 設定開啟的瀏覽器為目前的webView，避免打開或點其他網址的時候會用Chrome等其他瀏覽器開啟，使用者就無法停在此頁
			wvForgetPwd.setWebViewClient(mWebViewClient);

			wvForgetPwd.setWebChromeClient(mWebChromeClient);// 如果有標頭才有用
			wvForgetPwd
					.loadUrl("http://mymarcorder.azurewebsites.net/ForgetPassword.aspx");
			fun_thread();// 同時偵測是否連線超過設定的秒數，免的因為讀取不到網頁而讓使用者一直卡在這頁
			// wvFans.loadUrl("https://m.facebook.com/mosburger.tw?_rdr");
			// wvFans.loadUrl("http://case.518.com.tw/workroom-index-1825771.html");
		}
		// 若沒有則不讓使用者連線
		else {
			MyToast.Show(ActForgetPwd.this, "偵測到網路連線不太穩定噢。\n檢查一下你的網路有沒有問題呦~^.<");
			fileList();
		}
	}

	private ProgressDialog progressBar;
	Button btnHome;
	WebView wvForgetPwd;
	// ProgressDialog pd = null;
	ProgressBar progressbar;
}