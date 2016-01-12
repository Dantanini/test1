package tw.com.gear.marcorder;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import tw.com.gear.marcorder.ActMain;
import tw.com.gear.marcorder.ActPageOrderItemList;
import tw.com.gear.marcorder.Food;
import tw.com.gear.marcorder.FoodFactory;
import tw.com.gear.marcorder.ImageAdapter;
import tw.com.gear.marcorder.MyGallery;
import tw.com.gear.marcorder.R;
import android.R.drawable;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import at.technikum.mti.fancycoverflow.FancyCoverFlow;

public class ActReadyToOrder extends Activity {
	String tmp = "";
	public static Activity actReadyTorder;
	private static final int GUI_OK = 0x101;// handler的驗證碼

	private Integer[] myImages1 = { R.drawable.p1_01, R.drawable.p1_02,
			R.drawable.p1_03, R.drawable.p1_04, R.drawable.p1_05,
			R.drawable.p1_06, R.drawable.p1_07 };

	// ==============================================================================================================

	private Integer[] myImages2 = { R.drawable.p01, R.drawable.p02,
			R.drawable.p03, R.drawable.p04, R.drawable.p05, R.drawable.p06 };

	// ================================================================================================================

	private ImageAdapter imageAdapterHttpList1;// 單點分類的imageAdapter(是從網路下載的資料)
	private ImageAdapter imageAdapterHttpList2;
	private int galleryWidth = 500;
	private int galleryHeight = 500;
	private int quantity = 0; // 數量
	private boolean imageSet = false; // 目前選中的是套餐(false)或單點(true)
	ProgressDialog myDialog;
	Thread foodThread;

	// private Bitmap icon = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.act_first_page);
		InitialComponent();
		final Bitmap icon = BitmapFactory.decodeResource(getResources(),
				R.drawable.no_file);
		actReadyTorder = this;
		foodThread = new Thread() {
			public void run() {
				try {
					// /////////////////////////////////////////////////
					URL url = new URL(ActMain.webpage);
					String jsonString = "";
					String jsonString2 = "";
					jsonString = new DataBaseManagement().loadFoodData(url)
							.toString().trim();
					jsonString2 = new DataBaseManagement().loadFoodSetData(url)
							.toString().trim();
					tmp = jsonString2;
					// ----------單點食物的下載和圖片的下載並建立集合---------
					ActMain.foodFactoryHttpListSetUse = new FoodFactory();
					ActMain.foodFactoryHttpListSetUse.CreateSubFoodList();
					ActMain.foodFactoryHttpList = new FoodFactory();
					ActMain.foodFactoryHttpList.CreateFoodList();
					ActMain.foodFactoryHttpList.setFoodMultiList(JsonFactory
							.GetFoodMultiList(jsonString));
					for (ArrayList<Food> fdl : ActMain.foodFactoryHttpList
							.getFoodMultiList()) {
						for (Food fd : fdl) {
							if (fd.getIconPath().trim().equals("")) {
								fd.setIcon(icon);
							} else {
								fd.setIcon(new DataBaseManagement()
										.GetIconFromURL(ActMain.webUrl
												+ fd.getIconPath().trim()
														.substring(1)));
							}
						}
					}
					// 套餐的副食，設定價差
					for (int i = 1; i < ActMain.foodFactoryHttpListSetUse
							.GetSubFoodList().size(); i++) {
						ActMain.foodFactoryHttpListSetUse
								.GetSubFoodList()
								.get(i)
								.setSpread(
										ActMain.foodFactoryHttpListSetUse
												.GetSubFoodList().get(i)
												.getPrice()
												- ActMain.foodFactoryHttpListSetUse
														.GetSubFoodList()
														.get(0).getPrice());
					}
					// 套餐的副食，設定Icon
					for (Food subFood : ActMain.foodFactoryHttpListSetUse
							.GetSubFoodList()) {
						if (subFood.getIconPath().equals("")) {
							subFood.setIcon(BitmapFactory.decodeResource(
									getResources(), myImages2[5]));
						} else {
							subFood.setIcon(new DataBaseManagement()
									.GetIconFromURL(ActMain.webUrl
											+ subFood.getIconPath()
													.substring(1)));

						}
					}
					// ----------套餐食物的下載和圖片的下載並建立集合--------------------
					ActMain.foodSetFactoryHttpList = new FoodFactory();
					ActMain.foodSetFactoryHttpList.CreateFoodSetList();
					ActMain.foodSetFactoryHttpList.SetFoodSetList(JsonFactory
							.GetFoodSetList(jsonString2));
					for (FoodSet fds : ActMain.foodSetFactoryHttpList
							.GetFoodSetList()) {
						if (fds.getIconPath().equals("")) {
							fds.setIcon(icon);
						} else {
							fds.setIcon(new DataBaseManagement()
									.GetIconFromURL(ActMain.webUrl
											+ fds.getIconPath().substring(1)));
						}
					}

					// ------------------------------

					Message msg = new Message();
					msg.what = GUI_OK;
					ActReadyToOrder.this.myMessageHandler.sendMessage(msg);

					// /////////////////////////////////////////////////
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					myDialog.dismiss();
				}
			}
		};

		// 連線下載食物清單
		myDialog = ProgressDialog.show(ActReadyToOrder.this, "掃描最新食物清單中",
				"請稍後...", true, true, new DialogInterface.OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						foodThread.interrupt();
						myDialog.dismiss();
						ActReadyToOrder.this.finish();
					}
				});
		myDialog.setCanceledOnTouchOutside(false);

		foodThread.start();
	}

	// 下載食物清單時的thread的handler，用來從new Thread()中取出msg傳回main Thread，並處理下載的資料轉存成食物物件
	Handler myMessageHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// super.handleMessage(msg);
			switch (msg.what) {
			case GUI_OK:

				imageAdapterHttpList2 = new ImageAdapter(ActReadyToOrder.this);// 建立Gallery用的adapter
				imageAdapterHttpList2
						.SetImageList(ActMain.foodSetFactoryHttpList
								.GetBitmapMultiList(0));
				imageAdapterHttpList2
						.SetTextList(ActMain.foodSetFactoryHttpList
								.GetTextMultiList(0));
				SetGallery(imageAdapterHttpList2);

				imageAdapterHttpList1 = new ImageAdapter(ActReadyToOrder.this);
				imageAdapterHttpList1.SetImageList(ActMain.foodFactoryHttpList
						.GetBitmapMultiList(0));
				imageAdapterHttpList1.SetTextList(ActMain.foodFactoryHttpList
						.GetTextMultiList(0));
				SetGallery(imageAdapterHttpList1);

				gallery.setAdapter(imageAdapterHttpList2);
				singleOrder();
				// lblName.setText(ActMain.foodSetFactoryHttpList.GetFoodItem(
				// imageAdapterHttpList2.getIndex()).getName());

				// 判斷是否從夢幻餐車頁面，帶餐車資料過來
				if (getIntent().getStringExtra("action") != null
						&& getIntent().getStringExtra("action").equals(
								"dreamCart")) {
					ArrayList<OrderItem> tmpCart = new ArrayList<OrderItem>();
					tmpCart = JsonFactory.GetDreamCart(getIntent()
							.getStringExtra("dreamCart"));
					for (OrderItem o : tmpCart) {
						OrderItem newItem = new OrderItem();
						if (!o.getType().equals(ActMain.foodSetString)) {
							Food f = (Food) ActMain.foodFactoryHttpList
									.findBySerial(o.getSerial());
							if (f != null) {// 如果沒有找到相同資料就找下一筆細項，產品有可能已經下架

								newItem = new OrderItem(f.getSerial(),
										f.getName(), o.getQuantity(),
										f.getPrice());
								newItem.setAttrs(f.getAttrs());
								newItem.setAttrsPrice(f.getAttrsPrice());
								newItem.setAttrsState(o.getAttrsState());
								newItem.setType(o.getType());
								ActMain.myCart.add(newItem);
							}

						} else {
							FoodSet f = (FoodSet) ActMain.foodSetFactoryHttpList
									.findBySerial(o.getSerial());
							if (f != null) {// 如果沒有找到相同資料就找下一筆細項，產品有可能已經下架
								Food mainFood = (Food) ActMain.foodFactoryHttpList
										.findBySerial(f.getMainFood()
												.getSerial());
								Food subFood = (Food) ActMain.foodFactoryHttpListSetUse
										.findBySerial(o.getSubFoodSerial());
								newItem = new OrderItem(f.getSerial(),
										f.getName(), o.getQuantity(),
										mainFood.getPrice());
								newItem.setType(o.getType());
								newItem.setMainFoodName(mainFood.getName());
								newItem.setMainFoodSerial(mainFood.getSerial());
								newItem.setSubFoodName(subFood.getName());
								newItem.setSubFoodSerial(subFood.getSerial());
								newItem.setSubFoodPrice(subFood.getPrice());
								ActMain.myCart.add(newItem);
							}
						}
					}
					lblTotal.setText("總計：" + ActMain.myCart.GetTotalPrice()
							+ " 元");
				}

				// 判斷是否是從歷史訂單頁面啟動這頁的，action有資料就是，null就是沒有
				// 載入歷史訂單的資料，從sqlite找尋serial，依照serial找尋食物，再重新加入購物車
				if (getIntent().getStringExtra("action") != null
						&& !getIntent().getStringExtra("action").equals(
								"dreamCart")) {
					String serial = getIntent().getStringExtra("action");
					SQLiteMenagement sqlManger = new SQLiteMenagement(
							ActReadyToOrder.this);
					Cursor table2 = sqlManger
							.Select("SELECT * FROM OrderDetail");
					table2.moveToFirst();
					for (int j = 0; j < table2.getCount(); j++) {
						if (serial.equals(table2
								.getString(ActSerchOrders.detailSerial))) {
							OrderItem item = null;
							// 如果相同，代表是套餐的食物
							if (table2.getString(ActSerchOrders.detailType)
									.equals(ActMain.foodSetString)) {
								String sId = table2
										.getString(ActSerchOrders.detailId);
								int qty = table2
										.getInt(ActSerchOrders.detailQty);
								FoodSet f = (FoodSet) ActMain.foodSetFactoryHttpList
										.findBySerial(sId);
								if (f != null) {// 如果沒有找到相同資料就找下一筆細項，產品有可能已經下架
									Food mainFood = (Food) ActMain.foodFactoryHttpList
											.findBySerial(f.getMainFood()
													.getSerial());
									Food subFood = (Food) ActMain.foodFactoryHttpListSetUse
											.findBySerial(table2
													.getString(ActSerchOrders.detailSfId));
									item = new OrderItem(f.getSerial(),
											f.getName(), qty,
											mainFood.getPrice());
									item.setType(ActMain.foodSetString);
									item.setMainFoodSerial(mainFood.getSerial());
									item.setMainFoodName(mainFood.getName());
									item.setSubFoodSerial(subFood.getSerial());
									item.setSubFoodName(subFood.getName());
									item.setSubFoodPrice(subFood.getPrice());
									if (item != null)
										ActMain.myCart.add(item);
								}
							} else {
								String sId = table2
										.getString(ActSerchOrders.detailId);
								int qty = table2
										.getInt(ActSerchOrders.detailQty);
								Food f = (Food) ActMain.foodFactoryHttpList
										.findBySerial(sId);
								if (f != null) {// 如果沒有找到相同資料就找下一筆細項，產品有可能已經下架
									item = new OrderItem(f.getSerial(),
											f.getName(), qty, f.getPrice());
									item.setAttrs(f.getAttrs());
									item.setAttrsPrice(f.getAttrsPrice());
									if (table2
											.getInt(ActSerchOrders.detailAttrState) == 0) {
										item.setAttrsState(false);
									} else {
										item.setAttrsState(true);
									}
									item.setType(table2
											.getString(ActSerchOrders.detailType));
									if (item != null)
										ActMain.myCart.add(item);
								}
							}
						}
						table2.moveToNext();
					}
					lblTotal.setText("總計：" + ActMain.myCart.GetTotalPrice()
							+ " 元");
				}
				break;
			}
		}
	};

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onResume() {
		lblTotal.setText("總計：" + ActMain.myCart.GetTotalPrice() + " 元");
		super.onResume();
	}

	// 套餐完成加入時回傳的字串
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1) {
			if (data != null) {
				MyToast.Show(ActReadyToOrder.this,
						data.getExtras().getString("kk"));
				lblTotal.setText("總計：" + ActMain.myCart.GetTotalPrice() + " 元");
			}
		}
	}

	// 設定gallery的圖片長寬尺寸符合手機尺寸
	private void SetGallery(ImageAdapter imageAdapter) {
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		// galleryWidth = metrics.widthPixels;
		galleryHeight = metrics.heightPixels;
		imageAdapter.SetWidth(galleryHeight / 2);
		imageAdapter.SetHeight(galleryHeight / 2);

	}

	// -----------------------UI和事件-----------------------------------------------

	// gallery的按鈕事件
	OnItemClickListener gallery_Click = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long arg3) {

			// if(imageSet==false){
			// MyToast.Show(ActReadyToOrder.this,
			// ActMain.foodSetFactoryHttpList.GetFoodItem(position).getType());
			// }else{
			// MyToast.Show(ActReadyToOrder.this,
			// ActMain.foodFactoryHttpList.GetFoodItem(position).getAttrs().toString());
			// }

		}

	};
	// 圖片在gallery選取時的事件(分成選中和沒選)
	OnItemSelectedListener gallery_OnSelected = new OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1,
				int position, long arg3) {

			if (imageSet == false) {// 顯示圖片對應的foodName
				// 顯示套餐的
				imageAdapterHttpList2.setIndex(position);
				// lblName.setText(ActMain.foodSetFactoryHttpList.GetFoodItem(
				// imageAdapterHttpList2.getIndex()).getName()
				// + "\r\n"
				// + ((FoodSet) (ActMain.foodSetFactoryHttpList
				// .GetFoodItem(imageAdapterHttpList2.getIndex())))
				// .getMainFood().getName()
				// + "+"
				// + ((FoodSet) (ActMain.foodSetFactoryHttpList
				// .GetFoodItem(imageAdapterHttpList2.getIndex())))
				// .getSubFood().getName()
				// + "  "
				// + ActMain.foodSetFactoryHttpList.GetFoodItem(
				// imageAdapterHttpList2.getIndex()).getPrice()
				// + "元");
				// lblName.setText(foodFactory1.GetFoodItem(imageAdapter1.getIndex()).getName()+"  "+foodFactory1.GetFoodItem(imageAdapter1.getIndex()).getPrice()+"元");
			} else {
				// 顯示單點的
				imageAdapterHttpList1.setIndex(position);
				// lblName.setText(ActMain.foodFactoryHttpList.getFoodMultiList()
				// .get(ActMain.foodFactoryHttpList.getFoodTypeIndex())
				// .get(imageAdapterHttpList1.getIndex()).getName()
				// + "  "
				// + ActMain.foodFactoryHttpList
				// .getFoodMultiList()
				// .get(ActMain.foodFactoryHttpList
				// .getFoodTypeIndex())
				// .get(imageAdapterHttpList1.getIndex())
				// .getPrice() + "元");

			}
			quantity = 0;
			lblQuantity.setText("數量： " + quantity);

		}

		// gallery在沒有選擇時發生的event
		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// 顯示圖片對應的foodName
			if (imageSet == false) {
				imageAdapterHttpList2.setIndex(0);
				// lblName.setText(ActMain.foodSetFactoryHttpList.GetFoodItem(
				// imageAdapterHttpList2.getIndex()).getName());
			} else {
				imageAdapterHttpList1.setIndex(0);
			}
			quantity = 0;
			lblQuantity.setText("數量： " + quantity);
		}

	};
	// 增加數量的按鈕事件
	OnClickListener btnOrder_Add_Click = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			quantity++;
			if (quantity > 99)
				quantity = 99;
			lblQuantity.setText("數量： " + quantity);
		}
	};
	// /減少數量的按鈕事件
	OnClickListener btnOrder_Sub_Click = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			quantity--;
			if (quantity < 0)
				quantity = 0;
			lblQuantity.setText("數量： " + quantity);

		}
	};
	// 加入購物車按鈕事件
	OnClickListener btnAddToCart_Click = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			if (quantity > 0) {
				if (imageSet == false) {
					FoodSet foodSet = new FoodSet();
					foodSet = (FoodSet) ActMain.foodSetFactoryHttpList
							.GetFoodItem(imageAdapterHttpList2.getIndex());
					Intent intent = new Intent(ActReadyToOrder.this,
							ActPageOrderItemList.class);
					intent.putExtra("listData",
							FoodFactory.getListDataToIntent(foodSet, quantity));
					quantity = 0;
					lblQuantity.setText("數量： " + quantity);
					startActivityForResult(intent, 1);
				} else {// 單點就直接加入購物車，但要判斷購物車如果有同樣商品就直接將數量增加
					Food food = new Food();
					food = ActMain.foodFactoryHttpList
							.GetFoodListItem(imageAdapterHttpList1.getIndex());
					if (ActMain.myCart.getOrderList().size() > 0) {
						// 第二次以上加入購物車，需要判斷是否已經有相同食物在車中
						boolean isEqual = false;
						// int index = 0;
						for (int o = 0; o < ActMain.myCart.getOrderList()
								.size(); o++) {
							if (food.getSerial().equals(
									ActMain.myCart.getOrderList().get(o)
											.getSerial())// 判斷購物車中是否有相同食物
									&& !ActMain.myCart.getOrderList().get(o)
											.getType()
											.equals(ActMain.foodSetString)
									&& !ActMain.myCart.getOrderList().get(o)
											.getAttrsState()) {
								isEqual = true;
								ActMain.myCart
										.getOrderList()
										.get(o)
										.setQuantity(
												ActMain.myCart.getOrderList()
														.get(o).getQuantity()
														+ quantity);
								break;// ***很重要***不可以用return必須用break是指跳出當前迴圈
							} else {
								isEqual = false;
							}
						}
						if (isEqual == false) {
							OrderItem item = new OrderItem(food.getSerial(),
									food.getName(), quantity, food.getPrice());
							item.setType(food.getType());
							item.setAttrs(food.getAttrs());
							item.setAttrsPrice(food.getAttrsPrice());
							ActMain.myCart.add(item);
						}
					} else {
						// 第一次把訂單加入購物車
						OrderItem item = new OrderItem(food.getSerial(),
								food.getName(), quantity, food.getPrice());
						item.setType(food.getType());
						item.setAttrs(food.getAttrs());
						item.setAttrsPrice(food.getAttrsPrice());
						ActMain.myCart.add(item);
					}
					MyToast.Show(ActReadyToOrder.this, "成功加入購物車");
					;
					quantity = 0;
					lblQuantity.setText("數量： " + quantity);
					lblTotal.setText("總計：" + ActMain.myCart.GetTotalPrice()
							+ " 元");
				}
			}
		}
	};
	// 套餐按鈕事件
	OnClickListener btnPackage_Click = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (ActMain.foodSetFactoryHttpList == null)
				return;
			// MyToast.Show(ActReadyToOrder.this,
			// ""+foodSetFactoryHttpList.GetFoodSetList().size()+"@"+foodFactoryHttpListSetUse.GetSubFoodList().size()+"@"+
			// foodFactoryHttpList.getFoodMultiList().get(0).size()+"\r\n"+tmp);
			gallery.setAdapter(imageAdapterHttpList2);
			gallery.setSelection(imageAdapterHttpList2.getIndex(), false);// 設定顯示在剛剛選擇的項目
			imageSet = false;
			quantity = 0;
			lblQuantity.setText("數量： " + quantity);
		}

	};
	// 單點類型選擇Dialog的按鈕事件，按下會回傳某個類型，主頁面要跟著換imageAdapter
	DialogInterface.OnClickListener dialog_click = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			ActMain.foodFactoryHttpList.setFoodTypeIndex(which);
			imageAdapterHttpList1.SetImageList(ActMain.foodFactoryHttpList
					.GetBitmapMultiList(which));
			imageAdapterHttpList1.SetTextList(ActMain.foodFactoryHttpList
					.GetTextMultiList(which));
			imageAdapterHttpList1.setIndex(0);
			// SetGallery(imageAdapterHttpList1);
			gallery.setAdapter(imageAdapterHttpList1);
			gallery.setSelection(imageAdapterHttpList1.getIndex(), false);// 設定顯示在剛剛選擇的項目
			imageSet = true;
			quantity = 0;
			lblQuantity.setText("數量： " + quantity);
		}
	};
	// 單點按鈕事件
	OnClickListener btnSingle_Click = new OnClickListener() {
		@Override
		public void onClick(View v) {
			singleOrder();
		}
	};

	private void singleOrder() {
		if (ActMain.foodTypeArrayList.size() == 0)
			return;
		String[] list = new String[ActMain.foodTypeArrayList.size()];
		for (int a = 0; a < ActMain.foodTypeArrayList.size(); a++) {
			list[a] = ActMain.foodTypeArrayList.get(a);
		}
		Builder build = new AlertDialog.Builder(ActReadyToOrder.this)
				.setIcon(R.drawable.marcordericon2).setTitle("請選擇")
				.setItems(list, dialog_click);
		Dialog d = build.create();
		d.show();
		gallery.setAdapter(imageAdapterHttpList1);
		gallery.setSelection(imageAdapterHttpList1.getIndex(), false);// 設定顯示在剛剛選擇的項目
		imageSet = true;
		quantity = 0;
		lblQuantity.setText("數量： " + quantity);
	}

	// 查看購物車按鈕事件
	OnClickListener btnCart_Click = new OnClickListener() {
		@Override
		public void onClick(View v) {

			Button btn = (Button) v;

			if (btn == btnCart) {
				Intent intent = new Intent(ActReadyToOrder.this,
						ActPageCart.class);
				intent.putExtra("upOrDown", "down");
				startActivity(intent);
				// 動畫下至上
				int version = Integer.valueOf(android.os.Build.VERSION.SDK_INT);
				if (version >= 5) {
					overridePendingTransition(R.anim.zoominfrombottom100,
							R.anim.zoomstopmove);
				}
			} else if (btn == btnCheckOut) {
				Intent intent = new Intent(ActReadyToOrder.this,
						ActPageCart.class);
				intent.putExtra("upOrDown", "up");
				startActivity(intent);
				// 動畫右至左
				int version = Integer.valueOf(android.os.Build.VERSION.SDK_INT);
				if (version >= 5) {
					overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
					// 此為自訂的動畫效果，下面兩個為系統的動畫效果
					// overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
					// overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_righ);
				}
			}
		}
	};

	private void backMethod() {
		Dialog dialog = new AlertDialog.Builder(ActReadyToOrder.this)
				.setIcon(R.drawable.questionmark1).setTitle("確定要離開嗎？")
				.setMessage("要放棄此次訂餐嗎？\n※注意！若是回首頁，您剛才所選的訂單資訊將會重置！")
				.setPositiveButton("確定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

						ActMain.myCart = new Cart();

						finish();
						ActOrder.actOrderActivity.finish();
						// 添加介面切換效果，注意只有Android的2.0(SdkVersion版本號為5)以後的版本才支援
						int version = Integer
								.valueOf(android.os.Build.VERSION.SDK_INT);
						if (version >= 5) {
							overridePendingTransition(R.anim.zoomin2,
									R.anim.zoomout2);
						}
					}
				}).setNegativeButton("取消", null).show();
	}

	// 回首頁的按鈕事件
	OnClickListener btnBack_Click = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			backMethod();
		}
	};

	// 實體返回的按鍵事件
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && myDialog.isShowing()) {
			myDialog.dismiss();
			foodThread.interrupt();
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	void InitialComponent() {
		btnHome = (Button) findViewById(R.id.btnHome);
		btnHome.setOnClickListener(btnBack_Click);
		gallery = (FancyCoverFlow) findViewById(R.id.galleryFPG1);
		gallery.setReflectionEnabled(true);
		gallery.setReflectionRatio(0.5f);
		gallery.setReflectionGap(0);
		gallery.setOnItemClickListener(gallery_Click);
		gallery.setOnItemSelectedListener(gallery_OnSelected);
		btnOrderAdd = (Button) findViewById(R.id.btnFPAdd);
		btnOrderAdd.setOnClickListener(btnOrder_Add_Click);
		btnOrderSub = (Button) findViewById(R.id.btnFPSub);
		btnOrderSub.setOnClickListener(btnOrder_Sub_Click);
		btnAddToCart = (Button) findViewById(R.id.btnAddToCart);
		btnAddToCart.setOnClickListener(btnAddToCart_Click);
		// btnPackage = (Button) findViewById(R.id.btnFPPackage);
		// btnPackage.setOnClickListener(btnPackage_Click);
		btnSingle = (Button) findViewById(R.id.btnFPSingle);
		btnSingle.setOnClickListener(btnSingle_Click);
		lblQuantity = (TextView) findViewById(R.id.lblQuantity);
		// lblName = (TextView) findViewById(R.id.lblFoodName);
		lblTotal = (TextView) findViewById(R.id.lblAFPTotal);
		btnCheckOut = (Button) findViewById(R.id.btnCheckOut);
		btnCheckOut.setOnClickListener(btnCart_Click);
		btnCart = (Button) findViewById(R.id.btnCart);
		btnCart.setOnClickListener(btnCart_Click);

	}

	Button btnHome;
	Button btnCheckOut;
	Button btnCart;
	Button btnAddToCart;
	Button btnOrderAdd;
	Button btnOrderSub;
	Button btnPackage;
	Button btnSingle;
	FancyCoverFlow gallery;
	TextView lblQuantity;
	// TextView lblName;
	TextView lblTotal;
}
