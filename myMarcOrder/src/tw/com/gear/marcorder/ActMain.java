package tw.com.gear.marcorder;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import tw.com.gear.marcorder.R;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;
import android.os.Build;

public class ActMain extends Activity {
	// 點餐頁面要使用的靜態變數在第一頁先做宣告
	public final static String webUrl = "http://mymarcorder.azurewebsites.net";// 抓取網路空間的圖片
	public final static String webpage = "http://mymarcorder.azurewebsites.net/Handlerx.aspx";// 經由網頁連結資料庫
	public static FoodFactory foodFactoryHttpList;// 單點的食物物件集合(分類存入各個List集合)
	public static FoodFactory foodFactoryHttpListSetUse;// 套餐的飲料物件集合
	public static FoodFactory foodSetFactoryHttpList;// 套餐的食物物件集合
	public static ArrayList<String> foodTypeArrayList = new ArrayList<String>();// 單點的食物種類的分類清單，用在分類處理時
	public static String foodSetString = "套餐類";
	public static Cart myCart = new Cart();;// 購物車
	public static OrderItem previewFood = new OrderItem();// 預覽畫面用的訂單集合
	public static ArrayList<Bitmap> orderedIconList;// 購物車畫面和夢幻餐車畫面要使用的圖片集合
	public static String whoIsActivity;// 判斷現在是誰在啟動

	public static String googleLogInStr = "googleLogIn";
	Thread myDreamCartThread;
	ProgressDialog myDreamCartDialog;
	String dreamCartStr = "";
	boolean run_Thread;
	private static final int GUI_OK = 0x101;// handler的驗證碼

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.actmain);
		InitialComponent();

		// 啟動service(測試)
		Intent serviceIntent = new Intent(ActMain.this, OrderAlermService.class);
		serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startService(serviceIntent);
		NotificationManager notifimanger = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notifimanger.cancelAll();

	}

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

	// 登出確定的DialogClick事件
	android.content.DialogInterface.OnClickListener logOutConfirmDialog_Click = new android.content.DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			SharedPreferences sharedPre = getSharedPreferences("T1", 0);
			Editor editor = sharedPre.edit();
			// Boolean googleLogIn = sharedPre.getBoolean(googleLogInStr,
			// false);

			editor.putBoolean(googleLogInStr, false).commit();
			editor.putString("id", "").commit();
			editor.putString("pwd", "").commit();
			MyToast.Show(ActMain.this, "登出成功");
			btnSign.setText("登入");
			btnSign.setOnClickListener(signIn_Click);

		}

	};
	// 登出取消的DialogClick事件
	android.content.DialogInterface.OnClickListener logOutCancelDialog_Click = new android.content.DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub

		}

	};
	// 登出的click事件
	OnClickListener signOut_Click = new OnClickListener() {
		public void onClick(View v) {
			Builder build = new AlertDialog.Builder(ActMain.this)
					.setIcon(R.drawable.marcordericon2).setTitle("確定登出嗎?")
					.setNeutralButton("確定", logOutConfirmDialog_Click)
					.setNegativeButton("取消", logOutCancelDialog_Click);
			Dialog d = build.create();
			d.show();

		}

	};
	// 登入的click事件
	OnClickListener signIn_Click = new OnClickListener() {
		public void onClick(View v) {
			if (!checkInternet(ActMain.this) == true) {
				MyToast.Show(ActMain.this,
						"偵測到網路連線狀況不穩！\n請檢查您的網路是否有連線或開啟了飛航模式。");
				return;
			}
			Intent intent = new Intent(ActMain.this, ActLogin.class);
			startActivity(intent);
			int version = Integer.valueOf(android.os.Build.VERSION.SDK_INT);
			if (version >= 5) {
				overridePendingTransition(R.anim.zoomloginin,
						R.anim.zoomstopmove);
			}

		}
	};

	// 一般點餐事件
	private void toOrder() {
		// 判斷網路是否連結
		if (!checkInternet(ActMain.this) == true) {
			MyToast.Show(ActMain.this, "偵測到網路連線狀況不穩！\n請檢查您的網路是否有連線或開啟了飛航模式。");
			return;
		}
		Intent intent = new Intent(ActMain.this, ActOrder.class);
		startActivity(intent);

		// 添加介面切換效果，注意只有Android的2.0(SdkVersion版本號為5)以後的版本才支援
		int version = Integer.valueOf(android.os.Build.VERSION.SDK_INT);
		if (version >= 5) {
			overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
			// 此為自訂的動畫效果，下面兩個為系統的動畫效果
			// overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
			// overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_righ);
		}
	}

	// 主題風格設定
	private void setBtnTheme(int btnMarkTheme, int btnSignTheme,
			int btnFansTheme, int btnCartTheme, int btnFindTheme,
			int btnEditTheme, int btnOrderTheme) {
		btnMark.setTextColor(0xFFFFFFFF);
		btnSign.setTextColor(0xFFFFFFFF);
		btnFans.setTextColor(0xFFFFFFFF);
		btnFind.setTextColor(0xFFFFFFFF);
		btnCart.setTextColor(0xFFFFFFFF);
		btnEdit.setTextColor(0xFFFFFFFF);
		btnOrder.setTextColor(0xFFFFFFFF);
		this.btnMark.setBackgroundResource(btnMarkTheme);
		this.btnSign.setBackgroundResource(btnSignTheme);
		this.btnFans.setBackgroundResource(btnFansTheme);
		this.btnCart.setBackgroundResource(btnCartTheme);
		this.btnFind.setBackgroundResource(btnFindTheme);
		this.btnEdit.setBackgroundResource(btnEditTheme);
		this.btnOrder.setBackgroundResource(btnOrderTheme);
	}

	// 主題選擇視窗秀出
	private void showThemeChoose() {
		Builder build = new AlertDialog.Builder(ActMain.this).setIcon(
				R.drawable.search2).setIcon(R.drawable.neulneul);// 設定"單選"的屬性
		final String[] strTheme = new String[] { "MarcOrder形象主題", "專業",
				"夏天陽光黃", "性感蜜蘋紅", "繽紛歡樂橘", "甜蜜戀愛粉", "薩爾達之綠", "沁涼天空藍",
				"自訂屬於自己的主題" };
		build.setItems(strTheme, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				SharedPreferences sharedTheme = getSharedPreferences("Theme", 0);
				Editor editor = sharedTheme.edit();
				editor.putInt("Theme", which).commit();
				MyToast.Show(ActMain.this, strTheme[which]);
				switchThemeMethod(which);
			}
		});
		build.setTitle("首頁風格選擇");
		// build.setItems(strOrderMethod, onOrder_click);
		Dialog dialog = build.create();
		dialog.show();
	}

	/**
	 * 參數傳目前選到的主題index，他就會判斷該換哪個主題了。
	 * */
	private void switchThemeMethod(int which) {
		switch (which) {
		case 0:
			setBtnTheme(R.drawable.btnmarcordertheme5,
					R.drawable.btnmarcordertheme2, R.drawable.btnblue3,
					R.drawable.btn3, R.drawable.btnsummer1,
					R.drawable.btnpink1, R.drawable.btngreen4);
			btnMark.setTextColor(0xFFDAA520);
			btnSign.setTextColor(0xFFDC0300);
			btnFans.setTextColor(0xFFFFFFFF);
			btnFind.setTextColor(0xFFFFFFFF);
			btnCart.setTextColor(0xFFFFFFFF);
			btnEdit.setTextColor(0xFFFFFFFF);
			btnOrder.setTextColor(0xFFFFFFFF);
			break;
		case 1:
			setBtnTheme(R.drawable.btn5, R.drawable.btn2, R.drawable.btn3,
					R.drawable.btn3, R.drawable.btn3, R.drawable.btn3,
					R.drawable.btn4);
			break;
		case 2:
			setBtnTheme(R.drawable.btnsummer1, R.drawable.btnsummer3,
					R.drawable.btnsummer2, R.drawable.btnsummer2,
					R.drawable.btnsummer2, R.drawable.btnsummer2,
					R.drawable.btnsummer4);
			break;
		case 3:
			setBtnTheme(R.drawable.btnred1, R.drawable.btnred3,
					R.drawable.btnred2, R.drawable.btnred2, R.drawable.btnred2,
					R.drawable.btnred2, R.drawable.btnred1);
			btnMark.setTextColor(0xFFDAA520);
			btnSign.setTextColor(0xFFDAA520);
			btnFans.setTextColor(0xFFDAA520);
			btnFind.setTextColor(0xFFDAA520);
			btnCart.setTextColor(0xFFDAA520);
			btnEdit.setTextColor(0xFFDAA520);
			btnOrder.setTextColor(0xFFDAA520);
			break;
		case 4:
			setBtnTheme(R.drawable.btnorange1, R.drawable.btnorange3,
					R.drawable.btnorange2, R.drawable.btnorange2,
					R.drawable.btnorange2, R.drawable.btnorange2,
					R.drawable.btnorange1);
			break;
		case 5:
			setBtnTheme(R.drawable.btnpink1, R.drawable.btnpink3,
					R.drawable.btnpink2, R.drawable.btnpink2,
					R.drawable.btnpink2, R.drawable.btnpink2,
					R.drawable.btnpink1);
			break;
		case 6:
			setBtnTheme(R.drawable.btngreen1, R.drawable.btngreen2,
					R.drawable.btngreen3, R.drawable.btngreen3,
					R.drawable.btngreen3, R.drawable.btngreen3,
					R.drawable.btngreen4);
			break;
		case 7:
			setBtnTheme(R.drawable.btnblue1, R.drawable.btnblue2,
					R.drawable.btnblue3, R.drawable.btnblue3,
					R.drawable.btnblue3, R.drawable.btnblue3,
					R.drawable.btnblue4);
			break;
		case 8:
			Dialog dialogTheme = new AlertDialog.Builder(ActMain.this)
					.setIcon(R.drawable.questionmark1)
					.setTitle("MarcOrder")
					.setMessage(
							"您的帳戶無法使用此功能！\n\n※想要擁有自訂屬於自己的主題風格嗎？\n趕快加入VIP會員！\n不但可以自訂主題！還可以抽好禮！大獎帶回家！")
					.setPositiveButton("確定",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
								}
							}).show();
			SharedPreferences sharedTheme = getSharedPreferences("Theme", 0);
			Editor editor = sharedTheme.edit();
			editor.putInt("Theme", 0).commit();
			break;
		}
	}

	// //掃描粉絲團網址
	// @Override
	// protected void onStart() {
	// super.onStart();
	//
	// // 判斷網路狀態，抓取粉絲團網址
	// if (checkInternet(ActMain.this) == true) {
	// new Thread() {
	// public void run() {
	// URL url;
	// try {
	// url = new URL(ActMain.webpage);
	// fansUrl = (new DataBaseManagement().findFansUrl(url))
	// .toString();
	// } catch (MalformedURLException e1) {
	// fansUrl = "";
	// }
	// }
	// }.start();
	// }
	// }

	// 初始化方法
	private void InitialComponent() {

		// 我知道你想問，標頭寫甚麼事件啊！？因為有使用者說，他一看到以為按這個才是點餐...T.T
		btnMark = (Button) findViewById(R.id.btnMark);
		btnMark.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				MyToast.Show(ActMain.this,
						"您好！\n歡迎使用MarcOrder系統\n點選\"一般點餐\"即可開始點餐。");
			}
		});

		btnOrder = (Button) findViewById(R.id.btnOrder);
		btnOrder.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				toOrder();
			}
		});

		btnFind = (Button) findViewById(R.id.btnFind);
		btnFind.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(ActMain.this, ActSerchOrders.class);
				startActivity(intent);
				// MyToast.Show(ActMain.this, "查詢功能準備中！\n敬請期待！");
				int version = Integer.valueOf(android.os.Build.VERSION.SDK_INT);
				if (version >= 5) {
					overridePendingTransition(R.anim.zoominfrombottom100,
							R.anim.zoomstopmove);
				}
			}

		});

		btnEdit = (Button) findViewById(R.id.btnEdit);
		btnEdit.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Builder build = new AlertDialog.Builder(ActMain.this).setIcon(
						R.drawable.search2).setIcon(R.drawable.gear2);// 設定"單選"的屬性
				build.setItems(new String[] { "主題更換" },
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								switch (which) {
								case 0:
									showThemeChoose();
									break;
								}
							}
						});
				build.setTitle("設定");
				Dialog dialog = build.create();
				dialog.show();
				// MyToast.Show(ActMain.this, "設定功能準備中！\n敬請期待！");
			}
		});

		btnFans = (Button) findViewById(R.id.btnFans);
		btnFans.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (!checkInternet(ActMain.this) == true) {
					MyToast.Show(ActMain.this,
							"偵測到網路連線狀況不穩！\n請檢查您的網路是否有連線或開啟了飛航模式。");
					return;
				}
				Intent intent = new Intent(ActMain.this, ActFans.class);
				startActivity(intent);
				int version = Integer.valueOf(android.os.Build.VERSION.SDK_INT);
				if (version >= 5) {
					overridePendingTransition(R.anim.zoomin2, R.anim.zoomout2);
				}
				// MyToast.Show(ActMain.this, "粉絲團功能準備中！\n敬請期待！");
			}
		});

		btnCart = (Button) findViewById(R.id.btnCart);
		// 夢幻餐車按鈕事件
		btnCart.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// 判斷網路狀態
				if (!checkInternet(ActMain.this) == true) {
					MyToast.Show(ActMain.this,
							"偵測到網路連線狀況不穩！\n請檢查您的網路是否有連線或開啟了飛航模式。");
					return;
				}

				SharedPreferences sharedPre = getSharedPreferences("T1", 0);
				Boolean google = sharedPre.getBoolean(googleLogInStr, false);
				String id = sharedPre.getString("id", "");
				String pwd = sharedPre.getString("pwd", "");
				if (google || (!id.equals("") && !pwd.equals(""))) {
					// 現在是登入狀態，顯示按鈕的TEXT為登出
					// 抓取該帳號的夢幻餐車字串
					myDreamCartThread = new Thread() {
						public void run() {
							try {
								// 1.先收尋雲端資料庫，找出這個會員的夢幻餐車資料
								URL url = new URL(ActMain.webpage);
								SharedPreferences sharePre = getSharedPreferences(
										"T1", 0);
								String account = sharePre.getString("id", "");
								dreamCartStr = new DataBaseManagement()
										.downLoadDreamCart(url, account)
										.toString().trim();

								if (run_Thread) {
									Message msg = new Message();
									msg.what = GUI_OK;
									ActMain.this.myDreamHandler
											.sendMessage(msg);
								}
							} catch (Exception e) {
							} finally {
								myDreamCartDialog.dismiss();
							}
						}
					};
					run_Thread = true;// 啟動Thread輸出控制，若中途停止了Thread，必須讓資料接收之後的動作停止
					myDreamCartDialog = ProgressDialog.show(ActMain.this,
							"連線中", "請稍後...", true, true,
							new DialogInterface.OnCancelListener() {
								public void onCancel(DialogInterface dialog) {
									run_Thread = false;
									myDreamCartThread.interrupt();
									myDreamCartDialog.dismiss();
								}
							});
					myDreamCartDialog.setCanceledOnTouchOutside(false);
					myDreamCartThread.start();

				} else if (!google || (id.equals("") && pwd.equals(""))) {
					// 但是現在是登出狀態
					MyToast.Show(ActMain.this, "您的帳戶無法使用此功能！\n請加入會員！");
				}
			}
		});

		btnSign = (Button) findViewById(R.id.btnSign);
		btnSign.setOnClickListener(signIn_Click);

		// 配置圖片給按鈕
		Drawable dOrder = getResources().getDrawable(R.drawable.order1);
		ChangeBtnImage(btnOrder, dOrder, "1一般點餐");
		Drawable dFind = getResources().getDrawable(R.drawable.search2);
		ChangeBtnImage(btnFind, dFind, "1紀錄");
		Drawable dEdit = getResources().getDrawable(R.drawable.gear1);
		ChangeBtnImage(btnEdit, dEdit, "1設定");
		Drawable dFans = getResources().getDrawable(R.drawable.facebook1);
		ChangeBtnImage(btnFans, dFans, "1粉絲團");
		Drawable dCart = getResources().getDrawable(R.drawable.cart3);
		ChangeBtnImage(btnCart, dCart, "1夢幻餐車");

		// 偵測該手機設定主題index為何
		SharedPreferences shareTheme = getSharedPreferences("Theme", 0);
		int theme = shareTheme.getInt("Theme", 0);
		switchThemeMethod(theme);
	}

	Handler myDreamHandler = new Handler() {// 夢幻餐車的handler
		@Override
		public void handleMessage(Message msg) {
			// super.handleMessage(msg);
			switch (msg.what) {
			case GUI_OK:
				if (!dreamCartStr.equals("")) {// 有夢幻餐車資料，就傳到下頁
					Intent intent = new Intent(ActMain.this, ActDreamCart.class);
					intent.putExtra("dreamCartStr", dreamCartStr);
					startActivity(intent);
				}
				break;
			}
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		// AccountManager accountManger = AccountManager.get(ActMain.this);
		// Account[ ] accounts = accountManger.getAccountsByType("com.google");
		// String google = accounts[0].name.toString();

		SharedPreferences sharedPre = getSharedPreferences("T1", 0);
		Boolean googleLogIn = sharedPre.getBoolean(googleLogInStr, false);
		String id = sharedPre.getString("id", "");
		String pwd = sharedPre.getString("pwd", "");
		if (googleLogIn) {// 若googleLogIn是true，就代表以google帳號登入
			btnSign.setText("登出");
			btnSign.setOnClickListener(signOut_Click);
			return;
		} else {
			// 如果googleLogIn是false，就以一般帳號登入
			if (!id.equals("") && !pwd.equals("")) {
				// 現在是登入狀態，顯示按鈕的TEXT為登出
				btnSign.setText("登出");
				btnSign.setOnClickListener(signOut_Click);
			} else if (id.equals("") && pwd.equals("")) {
				// 登入過，但是現在是登出狀態
				btnSign.setText("登入");
				btnSign.setOnClickListener(signIn_Click);
			}
		}
	}

	// 全域變數宣告區
	Button btnMark, btnOrder, btnFind, btnEdit, btnFans, btnCart, btnSign;
	public static Toast toast;// 為了避免吐司重複問題，大家可以多多使用這個土司，也可以使用MyToast這個類別;
	public static String fansUrl;// 指定粉絲團網址
}
