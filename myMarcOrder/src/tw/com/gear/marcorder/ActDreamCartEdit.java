package tw.com.gear.marcorder;

import java.net.URL;
import java.util.ArrayList;

import tw.com.gear.marcorder.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.os.Build;
import at.technikum.mti.fancycoverflow.FancyCoverFlow;

public class ActDreamCartEdit extends Activity {
	
	private int galleryWidth = 500;
	private int galleryHeight = 500;
	public static Activity actDreamCartEdit;
	private static final int GUI_OK = 0x101;//handler的驗證碼
	private ImageAdapter imageAdapterHttpList1;//單點分類的imageAdapter(是從網路下載的資料)
	private ImageAdapter imageAdapterHttpList2;
	private int quantity = 0; // 數量
	Thread foodThread;
	ProgressDialog myDialog;
	String tmp = "";
	private boolean imageSet = false; // 目前選中的是套餐(false)或單點(true)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.act_dream_cart_edit);
		InitialComponent();
		ActMain.myCart = new Cart();
		actDreamCartEdit = this;
		foodThread = new Thread(){
			public void run(){
				try{
			///////////////////////////////////////////////////
						URL url = new URL(ActMain.webpage);
						String jsonString = "";
						String jsonString2 = "";
						jsonString = new DataBaseManagement().loadFoodData(url).toString().trim();
						jsonString2 = new DataBaseManagement().loadFoodSetData(url).toString().trim();
						tmp = jsonString2;
						//----------單點食物的下載和圖片的下載並建立集合---------
						ActMain.foodFactoryHttpListSetUse = new FoodFactory();
						ActMain.foodFactoryHttpListSetUse.CreateSubFoodList();
						ActMain.foodFactoryHttpList  = new FoodFactory();
						ActMain.foodFactoryHttpList.CreateFoodList();
						ActMain.foodFactoryHttpList.setFoodMultiList(JsonFactory.GetFoodMultiList(jsonString));
						for(ArrayList<Food> fdl : ActMain.foodFactoryHttpList.getFoodMultiList()){
							for(Food fd : fdl){
								if(fd.getIconPath().equals("")){
									fd.setIcon(BitmapFactory.decodeResource(getResources(), R.drawable.no_file));
								}else{
									fd.setIcon(new DataBaseManagement().GetIconFromURL(ActMain.webUrl + fd.getIconPath().substring(1)));
								}
							}
						}
						//套餐的副食，設定價差
						for(int i=1; i < ActMain.foodFactoryHttpListSetUse.GetSubFoodList().size(); i++){
							ActMain.foodFactoryHttpListSetUse.GetSubFoodList().get(i).setSpread(
									ActMain.foodFactoryHttpListSetUse.GetSubFoodList().get(i).getPrice() - 
									ActMain.foodFactoryHttpListSetUse.GetSubFoodList().get(0).getPrice());
							}
						//套餐的副食，設定Icon
						for(Food subFood : ActMain.foodFactoryHttpListSetUse.GetSubFoodList()){
							if(subFood.getIconPath().equals("")){
								subFood.setIcon(BitmapFactory.decodeResource(getResources(), R.drawable.p06));
							}else{
								subFood.setIcon(new DataBaseManagement().GetIconFromURL(ActMain.webUrl + subFood.getIconPath().substring(1)));

							}
						}
						//----------套餐食物的下載和圖片的下載並建立集合--------------------
						ActMain.foodSetFactoryHttpList = new FoodFactory();
						ActMain.foodSetFactoryHttpList.CreateFoodSetList();
						ActMain.foodSetFactoryHttpList.SetFoodSetList(JsonFactory.GetFoodSetList(jsonString2));
						for(FoodSet fds : ActMain.foodSetFactoryHttpList.GetFoodSetList()){
							if(fds.getIconPath().equals("")){
								fds.setIcon(BitmapFactory.decodeResource(getResources(), R.drawable.no_file));
							}else{
								fds.setIcon(new DataBaseManagement().GetIconFromURL(ActMain.webUrl + fds.getIconPath().substring(1)));
							}
						}
						
						//------------------------------

						Message msg = new Message();
						msg.what = GUI_OK;
						ActDreamCartEdit.this.myMessageHandler.sendMessage(msg);
						
			///////////////////////////////////////////////////
				}catch(Exception e){
					e.printStackTrace();
				}finally{

					myDialog.dismiss();
				}
			}
		};
		myDialog = ProgressDialog.show(ActDreamCartEdit.this, "連線中", "請稍後...", true, true, new DialogInterface.OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				foodThread.interrupt();
				myDialog.dismiss();
				ActDreamCartEdit.this.finish();
			}
		});
		myDialog.setCanceledOnTouchOutside(false);
		foodThread.start();
	}
	
	//下載食物清單時的thread的handler，用來從new Thread()中取出msg傳回main Thread，並處理下載的資料轉存成食物物件
	Handler myMessageHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			//super.handleMessage(msg);
			switch(msg.what){
			case GUI_OK:
//				Builder b = new AlertDialog.Builder(ActDreamCartEdit.this).setMessage(getIntent().getStringExtra("jsonStr"));
//				Dialog d = b.create();
//				d.show();
				//將該筆餐車資料jsonCode轉成物件
				if(getIntent().getStringExtra("jsonStr") != null){
					ArrayList<OrderItem> tmpCart = new ArrayList<OrderItem>();
					if(getIntent().getStringExtra("jsonStr").equals("0"))
						tmpCart = JsonFactory.GetDreamCart(getIntent().getStringExtra("str1"));
					if(getIntent().getStringExtra("jsonStr").equals("1"))
						tmpCart = JsonFactory.GetDreamCart(getIntent().getStringExtra("str2"));
					if(getIntent().getStringExtra("jsonStr").equals("2"))
						tmpCart = JsonFactory.GetDreamCart(getIntent().getStringExtra("str3"));
					for(OrderItem o : tmpCart){
						OrderItem newItem = new OrderItem();
						if(!o.getType().equals(ActMain.foodSetString)){//設定訂單的單點項目
							Food f = (Food)ActMain.foodFactoryHttpList.findBySerial(o.getSerial());
							if(f != null){
								newItem = new OrderItem(f.getSerial(), f.getName(), o.getQuantity(), f.getPrice());
								newItem.setAttrs(f.getAttrs());
								newItem.setAttrsPrice(f.getAttrsPrice());
								newItem.setAttrsState(o.getAttrsState());
								newItem.setType(o.getType());								
								ActMain.myCart.add(newItem);
							}
						}else{//設定訂單的套餐項目
							FoodSet f = (FoodSet)ActMain.foodSetFactoryHttpList.findBySerial(o.getSerial());
							if(f != null){
								Food mainFood = (Food)ActMain.foodFactoryHttpList.findBySerial(f.getMainFood().getSerial());
								Food subFood = (Food)ActMain.foodFactoryHttpListSetUse.findBySerial(o.getSubFoodSerial());
								newItem = new OrderItem(f.getSerial(), f.getName(), o.getQuantity(), mainFood.getPrice());
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
					lblTotal.setText("總計：" + ActMain.myCart.GetTotalPrice() + " 元");
				}
			
//				MyToast.Show(ActDreamCartEdit.this, ActMain.foodFactoryHttpList.getFoodMultiList().size()+"@"+ActMain.foodSetFactoryHttpList.GetFoodSetList().size());
				imageAdapterHttpList1 = new ImageAdapter(ActDreamCartEdit.this);
				imageAdapterHttpList1.SetImageList(ActMain.foodFactoryHttpList.GetBitmapMultiList(0));
				imageAdapterHttpList1.SetTextList(ActMain.foodFactoryHttpList
						.GetTextMultiList(0));
				SetGallery(imageAdapterHttpList1);
				
				imageAdapterHttpList2 = new ImageAdapter(ActDreamCartEdit.this);// 建立Gallery用的adapter
				imageAdapterHttpList2.SetImageList(ActMain.foodSetFactoryHttpList.GetBitmapMultiList(0));
				imageAdapterHttpList2.SetTextList(ActMain.foodSetFactoryHttpList
						.GetTextMultiList(0));
				SetGallery(imageAdapterHttpList2);
				gallery.setAdapter(imageAdapterHttpList2);
//				lblName.setText(ActMain.foodSetFactoryHttpList.GetFoodItem(imageAdapterHttpList2.getIndex()).getName());
	

				
				break;
			}
		}
	};
//	 設定gallery的圖片長寬尺寸符合手機尺寸
	private void SetGallery(ImageAdapter imageAdapter) {
       DisplayMetrics metrics = new DisplayMetrics();
       getWindowManager().getDefaultDisplay().getMetrics(metrics);
//       galleryWidth = metrics.widthPixels;
       galleryHeight = metrics.heightPixels;
       imageAdapter.SetWidth(galleryHeight/2);
		imageAdapter.SetHeight(galleryHeight/2);
		
	}
	@Override
	protected void onResume() {
		lblTotal.setText("總計：" + ActMain.myCart.GetTotalPrice() + " 元");
		super.onResume();
	}
	// -----------------------UI和事件-----------------------------------------------

	// gallery的按鈕事件
	OnItemClickListener gallery_Click = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long arg3) {
			if(imageSet==false){
				MyToast.Show(ActDreamCartEdit.this, ActMain.foodSetFactoryHttpList.GetFoodItem(position).getType());				
			}else{
				MyToast.Show(ActDreamCartEdit.this, ActMain.foodFactoryHttpList.GetFoodItem(position).getAttrs());				
			}

		}

	};
	// 圖片在gallery選取時的事件(分成選中和沒選)
	OnItemSelectedListener gallery_OnSelected = new OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1,
				int position, long arg3) {

			if (imageSet == false) {// 顯示圖片對應的foodName
				//顯示套餐的
				imageAdapterHttpList2.setIndex(position);
//				lblName.setText(ActMain.foodSetFactoryHttpList.GetFoodItem(
//						imageAdapterHttpList2.getIndex()).getName()
//						+ "\r\n"+((FoodSet)(ActMain.foodSetFactoryHttpList.GetFoodItem(imageAdapterHttpList2.getIndex()))).getMainFood().getName()+"+"
//						+((FoodSet)(ActMain.foodSetFactoryHttpList.GetFoodItem(imageAdapterHttpList2.getIndex()))).getSubFood().getName()+"  "
//						+ ActMain.foodSetFactoryHttpList.GetFoodItem(imageAdapterHttpList2.getIndex())
//								.getPrice() + "元");
				// lblName.setText(foodFactory1.GetFoodItem(imageAdapter1.getIndex()).getName()+"  "+foodFactory1.GetFoodItem(imageAdapter1.getIndex()).getPrice()+"元");
			} else {
				//顯示單點的
				imageAdapterHttpList1.setIndex(position);
//				lblName.setText(ActMain.foodFactoryHttpList.getFoodMultiList().get(ActMain.foodFactoryHttpList.getFoodTypeIndex()).get(imageAdapterHttpList1.getIndex()).getName()+"  " + 
//						ActMain.foodFactoryHttpList.getFoodMultiList().get(ActMain.foodFactoryHttpList.getFoodTypeIndex()).get(imageAdapterHttpList1.getIndex()).getPrice() + "元");

			}
			quantity = 0;
			lblQuantity.setText("數量： " + quantity);

		}
		//gallery在沒有選擇時發生的event
		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// 顯示圖片對應的foodName
			if (imageSet == false) {
				imageAdapterHttpList2.setIndex(0);
//				lblName.setText(ActMain.foodSetFactoryHttpList.GetFoodItem(
//						imageAdapterHttpList2.getIndex()).getName());
				// lblName.setText(foodFactory1.GetFoodItem(imageAdapter1.getIndex()).getName());
			} else {
				imageAdapterHttpList1.setIndex(0);
//				lblName.setText(foodFactoryHttp.GetFoodItem(
//						imageAdapterHttpList1.getIndex()).getName());
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
					foodSet = (FoodSet) ActMain.foodSetFactoryHttpList.GetFoodItem(imageAdapterHttpList2.getIndex());
					Intent intent = new Intent(ActDreamCartEdit.this, ActPageOrderItemList.class);
					intent.putExtra("listData", FoodFactory.getListDataToIntent(foodSet, quantity));
					quantity = 0;
					lblQuantity.setText("數量： " + quantity);
					startActivityForResult(intent, 1);
				} else {// 單點就直接加入購物車，但要判斷購物車如果有同樣商品就直接將數量增加
					Food food = new Food();
					food = ActMain.foodFactoryHttpList.GetFoodListItem(imageAdapterHttpList1.getIndex());
					if (ActMain.myCart.getOrderList().size() > 0) {
						boolean isEqual = false;
						// int index = 0;
						for (int o = 0; o < ActMain.myCart.getOrderList().size(); o++) {
							if (food.getSerial().equals(ActMain.myCart.getOrderList().get(o).getSerial()) 
									&& !ActMain.myCart.getOrderList().get(o).getType().equals(ActMain.foodSetString)
									&& !ActMain.myCart.getOrderList().get(o).getAttrsState()) {
								isEqual = true;
								ActMain.myCart.getOrderList().get(o)
										.setQuantity(
												ActMain.myCart.getOrderList().get(o)
														.getQuantity()
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
						OrderItem item = new OrderItem(food.getSerial(),
								food.getName(), quantity, food.getPrice());
						item.setType(food.getType());
						item.setAttrs(food.getAttrs());
						item.setAttrsPrice(food.getAttrsPrice());
						ActMain.myCart.add(item);
					}
					MyToast.makeText(ActDreamCartEdit.this, "成功加入購物車",
							Toast.LENGTH_SHORT).show();
					;
					quantity = 0;
					lblQuantity.setText("數量： " + quantity);
					lblTotal.setText("總計：" + ActMain.myCart.GetTotalPrice() + " 元");
				}
			}
		}
	};
	// 套餐按鈕事件
	OnClickListener btnPackage_Click = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if(ActMain.foodSetFactoryHttpList ==null)
				return;
//			MyToast.Show(ActReadyToOrder.this, ""+foodSetFactoryHttpList.GetFoodSetList().size()+"@"+foodFactoryHttpListSetUse.GetSubFoodList().size()+"@"+
//					foodFactoryHttpList.getFoodMultiList().get(0).size()+"\r\n"+tmp);
			gallery.setAdapter(imageAdapterHttpList2);
			gallery.setSelection(imageAdapterHttpList2.getIndex(), false);// 設定顯示在剛剛選擇的項目
			imageSet = false;
			quantity = 0;
			lblQuantity.setText("數量： " + quantity);
		}

	};
	//單點類型選擇Dialog的按鈕事件，按下會回傳某個類型，主頁面要跟著換imageAdapter
	DialogInterface.OnClickListener dialog_click = new DialogInterface.OnClickListener(){
		@Override
		public void onClick(DialogInterface dialog, int which) {
			ActMain.foodFactoryHttpList.setFoodTypeIndex(which);
			imageAdapterHttpList1.SetImageList(ActMain.foodFactoryHttpList.GetBitmapMultiList(which));
			imageAdapterHttpList1.setIndex(0);
//			SetGallery(imageAdapterHttpList1);
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
			if(ActMain.foodTypeArrayList.size() == 0)
				return;
			String[ ] list = new String[ActMain.foodTypeArrayList.size()];
			for(int a =0; a < ActMain.foodTypeArrayList.size(); a++){
				list[a]=ActMain.foodTypeArrayList.get(a);
			}
			Builder build = new AlertDialog.Builder(ActDreamCartEdit.this).setIcon(R.drawable.marcordericon2).setTitle("請選擇").setItems(list, dialog_click);
			Dialog d = build.create();
			d.show();
			
			gallery.setAdapter(imageAdapterHttpList1);
			gallery.setSelection(imageAdapterHttpList1.getIndex(), false);// 設定顯示在剛剛選擇的項目
			imageSet = true;
			quantity = 0;
			lblQuantity.setText("數量： " + quantity);
		}
	};
	// 查看購物車按鈕事件
	OnClickListener btnCart_Click = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(ActDreamCartEdit.this, ActDreamCartFinish.class);
			intent.putExtra("jsonStr", getIntent().getStringExtra("jsonStr"));
			intent.putExtra("str1", getIntent().getStringExtra("str1"));
			intent.putExtra("str2", getIntent().getStringExtra("str2"));
			intent.putExtra("str3", getIntent().getStringExtra("str3"));
			startActivity(intent);
		}
	};
	
	private void backMethod(){
			Dialog dialog = new AlertDialog.Builder(ActDreamCartEdit.this)
			.setIcon(R.drawable.questionmark1).setTitle("確定要離開嗎？")
			.setMessage("要放棄此次選擇嗎？\n※注意！若是回首頁，您剛才所選的餐點資訊將會清除！")
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
			}).setNegativeButton("取消", null).show();		
	}
	// 回首頁的按鈕事件
	OnClickListener btnBack_Click = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			backMethod();
		}
	};
	//實體返回的按鍵事件
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK && !myDialog.isShowing())
			backMethod();
		if(keyCode == KeyEvent.KEYCODE_BACK && myDialog.isShowing()){
			ActMain.myCart = new Cart();
			myDialog.dismiss();
			foodThread.interrupt();
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	void InitialComponent() {
		btnBack = (Button) findViewById(R.id.btnADCEHome);
		btnBack.setOnClickListener(btnBack_Click);
		gallery = (FancyCoverFlow) findViewById(R.id.galleryADCEG1);
		gallery.setReflectionEnabled(true);
        gallery.setReflectionRatio(0.5f);
        gallery.setReflectionGap(0);
		gallery.setOnItemClickListener(gallery_Click);
		gallery.setOnItemSelectedListener(gallery_OnSelected);
		btnOrderAdd = (Button) findViewById(R.id.btnADCEAdd);
		btnOrderAdd.setOnClickListener(btnOrder_Add_Click);
		btnOrderSub = (Button) findViewById(R.id.btnADCESub);
		btnOrderSub.setOnClickListener(btnOrder_Sub_Click);
		btnAddToCart = (Button) findViewById(R.id.btnADCEAddToCart);
		btnAddToCart.setOnClickListener(btnAddToCart_Click);
		btnPackage = (Button) findViewById(R.id.btnADCEPackage);
		btnPackage.setOnClickListener(btnPackage_Click);
		btnSingle = (Button) findViewById(R.id.btnADCESingle);
		btnSingle.setOnClickListener(btnSingle_Click);
		lblQuantity = (TextView) findViewById(R.id.lblADCEQuantity);
//		lblName = (TextView) findViewById(R.id.lblADCEFoodName);
		lblTotal = (TextView) findViewById(R.id.lblADCETotal);
		btnCheckOut = (Button) findViewById(R.id.btnADCECheckOut);
		btnCheckOut.setOnClickListener(btnCart_Click);
		btnCart = (Button) findViewById(R.id.btnADCECart);
		btnCart.setOnClickListener(btnCart_Click);
	}
	Button btnBack;
	Button btnCheckOut;
	Button btnCart;
	Button btnAddToCart;
	Button btnOrderAdd;
	Button btnOrderSub;
	Button btnPackage;
	Button btnSingle;
	FancyCoverFlow gallery;
	TextView lblQuantity;
//	TextView lblName;
	TextView lblTotal;
}
