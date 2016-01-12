package tw.com.gear.marcorder;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import tw.com.gear.marcorder.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
//套餐的預覽畫面，會連到ImageTextAdapter產生List
public class ActPageOrderItemList extends Activity {
	//OrderItem item = new OrderItem();
	FoodSet tmpFoodSet = new FoodSet();

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.act_preview);
		InitialComponent();
		
		HashMap<String, Object> data =(HashMap<String, Object>)getIntent().getSerializableExtra("listData");
		
		ActMain.previewFood.setSerial((String)data.get("serial"));
		tmpFoodSet = (FoodSet)ActMain.foodSetFactoryHttpList.findBySerial(ActMain.previewFood.getSerial());
		ActMain.previewFood.setQuantity((int)data.get("quantity"));
		ActMain.previewFood.setName(tmpFoodSet.getName());
		ActMain.previewFood.setType(tmpFoodSet.getType());
		ActMain.previewFood.setMainFoodSerial(tmpFoodSet.getMainFood().getSerial());
		ActMain.previewFood.setMainFoodName(tmpFoodSet.getMainFood().getName());
		ActMain.previewFood.setPrice(tmpFoodSet.getMainFood().getPrice());		
		ActMain.previewFood.setSubFoodSerial(tmpFoodSet.getSubFood().getSerial());
		ActMain.previewFood.setSubFoodName(tmpFoodSet.getSubFood().getName());
		ActMain.previewFood.setSubFoodPrice(tmpFoodSet.getSubFood().getPrice());		
		
		findViews();
	}

	
	private void findViews() {
		listItem = (ListView) findViewById(R.id.listItem2);
		listItem.setAdapter(new ImageTextAdapter(this, ActMain.previewFood, tmpFoodSet.getIcon())); 
		listItem.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				TextView textView = (TextView) ((LinearLayout) view).getChildAt(1);
				MyToast.makeText(getApplicationContext(), textView.getText(),	Toast.LENGTH_SHORT).show();
			}
		});
	}
	//重新返回本頁時，將所有listview更新
	protected void onResume() {
		listItem.invalidateViews();
		super.onResume();
	}
	//取消
	OnClickListener btnCancel_Click = new OnClickListener() {
		public void onClick(View arg0) {
			
			ActMain.previewFood.Clear();
			finish();
		}
	};
	//把套餐加入購物車
	OnClickListener btnOk_Click = new OnClickListener() {
		public void onClick(View arg0) {
			if(listItem.getChildCount() != 0){
				ActMain.myCart.add(new OrderItem().Clone(ActMain.previewFood));
				ActMain.previewFood.Clear();
				Bundle bund = new Bundle();
				bund.putString("kk", "成功加入購物車");
				Intent intent = new Intent();
				intent.putExtras(bund);
				setResult(0, intent);
			
				finish();
			}
		}
	};
	private void InitialComponent() {
		btnCancel = (Button)findViewById(R.id.btnOILCencel);
		btnCancel.setOnClickListener(btnCancel_Click);
		btnOk = (Button)findViewById(R.id.btnOILOk);
		btnOk.setOnClickListener(btnOk_Click);		
		txtMsg = (TextView)findViewById(R.id.txtMsg2);
	}
	Button btnCancel;
	Button btnOk;
	TextView txtMsg;
	ListView listItem;
}
