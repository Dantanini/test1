package tw.com.gear.marcorder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import tw.com.gear.marcorder.R;
import android.R.color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;
import android.os.Build;

public class ActOrder extends Activity {

	public static Activity actOrderActivity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 設定此頁沒有標頭
		setContentView(R.layout.actorder);
		actOrderActivity = this;
		ActMain.myCart = new Cart();
		InitialComponent();// 初始化
	}

	// 圖片緊鄰文字用的
	/**
	 * 讓圖片的Image緊鄰文字的方法，第一個參數放你要改的按鈕，第二個放圖片，第三個放按鈕上的字　　 ※注意！這個方法目前會固定把圖片固定在左邊！
	 * 而且圖片會蓋掉第一個文字！所以請在第一個文字放隨便一個字母，以防影響文字原意！
	 */
	private void ChangeBtnImage(Button btn, Drawable d, String s) {
		SpannableString spanText = new SpannableString(s);
		int a = (int) btn.getTextSize();
		// d.setBounds(0, 0, a, a);//�]�w�Ϥ��r���j�p
		d.setBounds(0, 0, d.getIntrinsicWidth() * 4 / 5,
				d.getIntrinsicHeight() * 4 / 5);// ��l�j�p
		ImageSpan imageSpan = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);// 這邊可以更改圖片和文字的位置
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

	// 日期按鈕事件
	OnClickListener btnDate_Click = new OnClickListener() {
		public void onClick(View v) {
			// 判斷現在時間是否超過十二點，若超過會影響時間介面開啟的初始日期
			if (calendar.get(Calendar.HOUR_OF_DAY) >= 12)
				isThisTomorrow = TOMORROW;
			else
				isThisTomorrow = TODAY;

			// 判斷是不是第一次打開，若是也會影響初始的值
			if (btnDate.getText().toString().equals("選取時間")) {
				calendar.add(Calendar.MINUTE, +10);// 設定取餐的"分"一定是從現在的分加上10分鐘，以變產生馬上點餐便要取餐的矛盾
				TimePickerDialog dialog = new ActMyDatePicker(ActOrder.this,
						null, calendar.get(Calendar.HOUR_OF_DAY),
						calendar.get(Calendar.MINUTE), true, btnDate,
						isThisTomorrow);// 參數新增了四.按鈕和五.整數，分別用來傳遞目前按下去的按鈕以及現在是今天還是明天餐數用的
				dialog.show();
			} else {// 如果不是第一次打開，就會執行以下程式，以便將上一次已經取得的時間設定為時間介面的初始畫面時間
				int hour = Integer.parseInt(btnDate.getText().toString()
						.substring(0, 2));// 記錄上次選取的小時
				int minute = Integer.parseInt(btnDate.getText().toString()
						.substring(3, 5));// 記錄上次選取的分鐘
				TimePickerDialog dialog = new ActMyDatePicker(ActOrder.this,
						null, hour, minute, true, btnDate, isThisTomorrow);// 將小時和分鐘的參數改成我們設定的
				dialog.show();
			}
		}
	};

	// 取餐方式按鈕事件
	OnClickListener btnOrderMethod_Click = new OnClickListener() {
		public void onClick(View v) {
			Builder build = new AlertDialog.Builder(ActOrder.this).setIcon(
					R.drawable.search2).setIcon(R.drawable.ordermethod1);
			build.setSingleChoiceItems(new String[] { "外帶", "內用", "外送" },
					orderMethodIndex, onOrder_click);// 設定"單選"的屬性
			build.setPositiveButton("確定", null);
			build.setTitle("選擇取餐方式");
			// build.setItems(strOrderMethod, onOrder_click);
			Dialog dialog = build.create();
			dialog.show();
		}
	};

	// 地址選擇
	OnClickListener btnSendLocationChoose = new OnClickListener() {
		public void onClick(View v) {
			Button btn = (Button) v;
			// 判斷是不是第一次選地址，因為對後面的wheel會有嚴重的影響
			if (btn.getText().toString().equals("請選擇地址"))
				isFistTimeToOpen = FirstTime;// 是第一次選地址
			else
				isFistTimeToOpen = NotFirstTime;// 不是第一次選地址
			Intent intent = new Intent(ActOrder.this, CitiesActivity.class);
			startActivity(intent);
			// 添加介面切換效果，注意只有Android的2.0(SdkVersion版本號為5)以後的版本才支援
			int version = Integer.valueOf(android.os.Build.VERSION.SDK_INT);
			if (version >= 5) {
				overridePendingTransition(R.anim.zoominfrombottom,
						R.anim.zoomoutfrombottom);
			}
		}
	};

	// 取餐方式按鈕事件
	DialogInterface.OnClickListener onOrder_click = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			btnOrderMethod.setText(strOrderMethod[which]);
			orderMethodIndex = which;// 將已經選擇的方法index記錄下來，以便下次開啟的時候預設就是使用者上一次選好的index

			// 如果是外送，就要改變第三個按鈕為送達地址，因為外送要有送達地址，之後若不只一間店面，則是要多出一個按鈕才對
			if (which == 2) {
				btnStoreTitle.setText("送達地址");
				btnStore.setText("請選擇地址");
				btnStore.setOnClickListener(btnSendLocationChoose);
				btnStore.setTextColor(0xFFFA0300);
			}
			// 如果又選擇其他的，則要變回店家的選擇
			else {
				btnStoreTitle.setText("店家選擇");
				btnStore.setText("馬可早餐");
				btnStore.setTextColor(0xFF000000);
				btnStore.setOnClickListener(btnStore_Click);
			}
		}
	};

	// 店家選擇
	OnClickListener btnStore_Click = new OnClickListener() {
		public void onClick(View v) {
			startActivity(new Intent(
					Intent.ACTION_VIEW,
					Uri.parse("https://www.google.com.tw/maps/place/806%E9%AB%98%E9%9B%84%E5%B8%82%E5%89%8D%E9%8E%AE%E5%8D%80%E5%BB%BA%E8%8F%AF%E8%A1%972%E8%99%9F/@22.6141583,120.3238259,17z/data=!4m2!3m1!1s0x346e04a101211ec3:0xcd829b371704eb38")));

			MyToast.Show(ActOrder.this, "目前只有一間店家！\n即將擴張分店！\n敬請期待！");
		}
	};

	// 點選三大標題的時候要show的介紹文字的方法
	/** 秀標投文字的dialog，第一個參數填dialog的標題，第二個參數填內容，第三個參數填圖片 */
	private void titleDialog(String strTitle, String strMessage, int drawable) {
		Builder build = new AlertDialog.Builder(ActOrder.this)
				.setIcon(R.drawable.marcordericon2);
		build.setTitle(strTitle);
		build.setMessage(strMessage
				+ "\n\n\n※您也想要擁有類似系統嗎？\n請聯絡我們 Gear 團隊！\n對我們團隊而言，看到您滿意的表情，比甚麼都重要！"
				+ "\n請洽0972-067-287 王先生");
		build.setPositiveButton("確定", null);
		Dialog d = build.create();
		d.show();
	}

	// 更改回車鍵事件，讓使用者有安全感
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			doYouWantToLeave();// 詢問使用者是否確定離開
		}
		return super.onKeyDown(keyCode, event);
	}

	// 詢問使用者是否要離開的方法
	private void doYouWantToLeave() {
		Dialog dialog = new AlertDialog.Builder(ActOrder.this)
				.setIcon(R.drawable.questionmark1).setTitle("MarcOrder")
				.setMessage("要放棄此次訂餐嗎？\n※注意！若是回上一頁，您剛才所選的訂單資訊將會重置！")
				.setNegativeButton("取消", null)
				.setPositiveButton("確定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						ActMain.myCart = new Cart();

						finish();

						// 添加介面切換效果，注意只有Android的2.0(SdkVersion版本號為5)以後的版本才支援
						int version = Integer
								.valueOf(android.os.Build.VERSION.SDK_INT);
						if (version >= 5) {
							overridePendingTransition(R.anim.zoomin2,
									R.anim.zoomout2);
						}
					}
				}).show();
	}

	// 初始化
	private void InitialComponent() {
		btnReadyToOrder = (Button) findViewById(R.id.btnReadyToOrder);
		Drawable dRTOrder = getResources().getDrawable(R.drawable.hamburger1);
		ChangeBtnImage(btnReadyToOrder, dRTOrder, "1開始點餐");
		btnReadyToOrder.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				// 首先當然要判斷網路有沒有正常囉
				if (!checkInternet(ActOrder.this)) {
					MyToast.Show(ActOrder.this,
							"偵測到網路連線不太穩定噢。\n檢查一下你的網路有沒有問題呦~^.<");
					return;
				}

				// 然後再判斷有沒有必填蘭為沒有填寫
				if (btnDate.getText().toString().equals("選取時間")
						|| btnStore.getText().toString().equals("請選擇地址")) {
					MyToast.Show(ActOrder.this, "請完整填寫紅色欄位！");
					return;
				}

				// 下面這邊是要判斷"巷弄"有沒有填
				String strLittleLocation = "";
				for (int i = 3; i < aryLocation.length; i++)
					strLittleLocation += aryLocation[i];
				ActMain.myCart
						.setOrderType(btnOrderMethod.getText().toString());
				if (btnOrderMethod.getText().toString().equals("外送")) {
					if (!strLittleLocation.equals(""))
						ActMain.myCart.setCustomerAddress(btnStore.getTag()
								.toString());
					else {
						Dialog dialog = new AlertDialog.Builder(ActOrder.this)
								.setIcon(R.drawable.questionmark1)
								.setTitle("MarcOrder")
								.setMessage(
										"地址資訊填寫不完整呦！\n是不是忘記選擇正確的外送路段及填寫巷弄號樓的資訊呢?")
								.setPositiveButton("確定",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int which) {
												// 判斷是不是第一次選地址，因為對後面的wheel會有嚴重的影響
												if (btnStore.getText()
														.toString()
														.equals("請選擇地址"))
													isFistTimeToOpen = FirstTime;// 是第一次選地址
												else
													isFistTimeToOpen = NotFirstTime;// 不是第一次選地址
												Intent intent = new Intent(
														ActOrder.this,
														CitiesActivity.class);
												startActivity(intent);
												// 添加介面切換效果，注意只有Android的2.0(SdkVersion版本號為5)以後的版本才支援
												int version = Integer
														.valueOf(android.os.Build.VERSION.SDK_INT);
												if (version >= 5) {
													overridePendingTransition(
															R.anim.zoominfrombottom,
															R.anim.zoomoutfrombottom);
												}
											}
										}).show();
						return;
					}
				} else
					ActMain.myCart.setCustomerAddress(btnStore.getText()
							.toString());
				if (btnOrderMethod.getText().toString().equals("外送")) {
					Dialog dialog = new AlertDialog.Builder(ActOrder.this)
					.setIcon(R.drawable.questionmark1).setTitle("MarcOrder")
					.setMessage("很抱歉！\n目前外送功能尚在建構中，暫時無法使用！\n建議您選擇其他\"取餐方式\"\n\n\n如有任何疑問\n歡迎聯絡開發人員\n0972067287-王先生")
					.setPositiveButton("確定", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							
						}
					}).show();
					return;
				}
				Intent intent = new Intent(ActOrder.this, ActReadyToOrder.class);
				if (actionFlag == false) {// 判斷是否只傳一次，將歷史訂單傳入購物車
					intent.putExtra("action",
							getIntent().getStringExtra("action"));
					intent.putExtra("dreamCart",
							getIntent().getStringExtra("dreamCart"));
					actionFlag = true;
				}
				startActivity(intent);

				// 添加介面切換效果，注意只有Android的2.0(SdkVersion版本號為5)以後的版本才支援
				int version = Integer.valueOf(android.os.Build.VERSION.SDK_INT);
				if (version >= 5) {
					overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
				}
			}
		});

		btnBack = (Button) findViewById(R.id.btnBack);
		btnBack.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				doYouWantToLeave();// 詢問使用者是否確定離開
			}
		});

		btnHistory = (Button) findViewById(R.id.btnHistroy);
		btnHistory.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Dialog dialog = new AlertDialog.Builder(ActOrder.this)
						.setIcon(R.drawable.questionmark1)
						.setTitle("MarcOrder")
						.setMessage("要載入歷史訂單嗎？\n※注意！若是前往該頁，您剛才所選的訂單資訊將會重置！")
						.setNegativeButton("取消", null)
						.setPositiveButton("確定",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										Intent intent = new Intent(
												ActOrder.this,
												ActSerchOrders.class);
										startActivity(intent);
										finish();
										int version = Integer
												.valueOf(android.os.Build.VERSION.SDK_INT);
										if (version >= 5) {
											overridePendingTransition(
													R.anim.zoominfrombottom100,
													R.anim.zoomstopmove);
										}
									}
								}).show();

			}
		});

		btnDate = (Button) findViewById(R.id.btnDate);
		btnDate.setOnClickListener(btnDate_Click);
		btnDate.setTag("");

		btnDateTitle = (Button) findViewById(R.id.btnDateTitle);
		btnDateTitle.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				titleDialog("取餐時間", "這邊可以設置您欲取餐的日期。\n營業時間為每日的06:00-12:00"
						+ "\n若是時間超過12:00則只能指定明天以後的日期。"
						+ "\n※由於店家忙碌情況不同，取餐時間有時候可能會稍有延誤，造成您的不便敬請見諒！",
						R.drawable.marcordericon2);
			}

		});

		btnOrderMethod = (Button) findViewById(R.id.btnOrderMethod);
		btnOrderMethod.setOnClickListener(btnOrderMethod_Click);

		btnOrderMethodTitle = (Button) findViewById(R.id.btnOrderMethodTitle);
		btnOrderMethodTitle.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				titleDialog(
						"取餐方式",
						"在這邊您可以選擇您要的取餐方式。\n\n※注意！若是填寫外送，要記得填寫你的外送地址呦，這樣我們才能把美味的食物送至你指定的地點  ^.<。",
						R.drawable.marcordericon2);
			}
		});

		btnStore = (Button) findViewById(R.id.btnStore);
		btnStore.setOnClickListener(btnStore_Click);
		btnStoreTitle = (Button) findViewById(R.id.btnStoreTitle);
		btnStoreTitle.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Button btn = (Button) v;
				if (btn.getText().toString().equals("店家選擇"))
					titleDialog("店家選擇", "這邊可以設置您欲選購的店家。",
							R.drawable.marcordericon2);
				else
					titleDialog("送達地址", "這邊可以選擇您要餐點送達的地址。",
							R.drawable.marcordericon2);
			}
		});

		intLocation = new int[] { 0, 0, 0 };
		aryLocation = new String[] { "", "", "", "", "", "", "", "" };
		isFistTimeToOpen = FirstTime;
	}

	// **********************************//
	// ***********全域變數宣告區 ************//
	// **********************************//
	Button btnReadyToOrder, btnBack, btnDate, btnDateTitle, btnOrderMethod,
			btnOrderMethodTitle, btnStoreTitle, btnHistory;
	public static Button btnStore;

	// 選餐方式要用的方法
	String[] strOrderMethod = new String[] { "外帶", "內用", "外送" };

	// 取時間用的
	Calendar calendar = Calendar.getInstance();

	String orderMethod, Location;

	// 這個暫存是用來控制actionOrder頁面只會傳一次action的資料到actionReadyToOrder頁面
	// 避免多次將歷史訂單的資料重新載入購物車，始購物車產生重複資料
	boolean actionFlag = false;

	public static String date;

	// 呼叫時間選項用的，用來判斷現在打開的日期是今天可以點餐還是明天才能點餐的判斷用的
	int isThisTomorrow = 0, orderMethodIndex = 0;
	// 常數變數，以便程式碼閱讀
	final int TODAY = 0, TOMORROW = 1;

	final static boolean FirstTime = true, NotFirstTime = false;
	public static boolean isFistTimeToOpen;
	public static int[] intLocation;
	public static String[] aryLocation;
}
