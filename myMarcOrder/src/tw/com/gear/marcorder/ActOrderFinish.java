package tw.com.gear.marcorder;


import tw.com.gear.marcorder.R;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.Build;

@SuppressLint("NewApi")
public class ActOrderFinish extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.act_order_finish);
		txt = (TextView)findViewById(R.id.txtAOF1);
		img = (ImageView)findViewById(R.id.imgAOF1);
		btnconfirm = (Button)findViewById(R.id.btnAOF1);
		btnconfirm.setOnClickListener(confirm_click);
		String s = getIntent().getStringExtra("result");
		String s1 = getIntent().getStringExtra("ans");
		//控制QR碼長寬
		WindowManager Wmanger = (WindowManager) getSystemService(WINDOW_SERVICE);
		Display display = Wmanger.getDefaultDisplay();
		Point point  = new Point();
		display.getSize(point);
		int width = point.x;
		int height = point.y;
		int smallerDimension = width < height ? width : height;
		smallerDimension = smallerDimension*3/4;
		txt.setText(s1+"\r\n您可以到查詢頁面找到這筆訂單\r\n這是您的QR碼");
		Bitmap bitmap = null;
		QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(s, null, Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(), smallerDimension);
		try{
			bitmap = qrCodeEncoder.encodeAsBitmap();
		}catch(WriterException e){
			e.printStackTrace();
		}
		
		Orders o  = new Orders(ActMain.myCart);
		o.setOrdersSerial(s);
		//這裡要寫入SQLite，儲存這筆訂單再手機內

		ContentValues data = new ContentValues();
		data.put("oSerial", o.getOrdersSerial());
		data.put("oGatTime", o.getTime());
		data.put("oGatWay", o.getOrderType());
		data.put("oName", o.getCustomerName());
		data.put("oPhone", o.getCustomerPhone());
		data.put("oAddress", o.getCustomerAddress());
		data.put("oState", ActSerchOrders.newOrderState);//代表新的訂單
		SQLiteMenagement manger = new SQLiteMenagement(ActOrderFinish.this);
		manger.Insert("Ordered", data);
		ContentValues data2 = new ContentValues();
		for(OrderItem oi : ActMain.myCart.getOrderList()){
			data2.put("oSerial", o.getOrdersSerial());
			data2.put("dPId", oi.getSerial());
			data2.put("dPName", oi.getName());
			data2.put("dPType", oi.getType());
			data2.put("dPMfId", oi.getMainFoodSerial());
			data2.put("dPMfName", oi.getMainFoodName());
			data2.put("dPSfId", oi.getSubFoodSerial());
			data2.put("dPSfName", oi.getSubFoodName());
			data2.put("dPQty", oi.getQuantity());
			data2.put("dPPrice", oi.getPrice());
			if(oi.getAttrsState() == false){
				data2.put("dPAttrState", 0);				
			}else{
				data2.put("dPAttrState", 1);								
			}
			data2.put("dPAttrs", oi.getAttrs());
			data2.put("dPAttrsPrice", oi.getAttrsPrice());
			manger.Insert("OrderDetail", data2);
		}
		//顯示QR碼
		img.setImageBitmap(bitmap);
	}

	OnClickListener confirm_click = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
//			ActOrder.myCart.clear();
			//結束自己頁面
			finish();
			//結束點餐頁面
			ActReadyToOrder.actReadyTorder.finish();
			ActOrder.actOrderActivity.finish();
		}
	};
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			finish();
			ActReadyToOrder.actReadyTorder.finish();
			ActOrder.actOrderActivity.finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	Button btnconfirm;
	ImageView img;
	TextView txt;
}
