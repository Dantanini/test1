package tw.com.gear.marcorder;


import tw.com.gear.marcorder.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.os.Build;

public class ActOrderItemToEdit extends Activity {

	OrderItem item = new OrderItem();
	Food food = new Food();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.act_order_item_to_edit);
		InitialComponent();
		String data = getIntent().getStringExtra("serial");
//		txtMsg.setText("yes"+data);
		food = (Food)ActMain.foodFactoryHttpList.findBySerial(data);
		item = ActMain.myCart.findBysingleSerial(data);
		findViews();
	}

	
	private void findViews() {
		listItem = (ListView) findViewById(R.id.listOITEItem2);
		listItem.setAdapter(new ImageTextAdapterForFoodEdit(ActOrderItemToEdit.this, item, food.getIcon())); 
		listItem.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//				MyToast.Show(ActOrderItemToEdit.this, item.getAttrs()+"/"+item.getName());
			}
		});
	}
	//取消編輯
	OnClickListener btnCancel_Click = new OnClickListener() {
		public void onClick(View arg0) {
			finish();
		}
	};
	//編輯完成
	OnClickListener btnOk_Click = new OnClickListener() {
		public void onClick(View arg0) {
			String a = ((TextView)((LinearLayout)(arg0.getParent().getParent())).findViewById(R.id.txtLVI2_1)).getText().toString().substring(3).trim();
			boolean ckbState = ((CheckBox)((LinearLayout)(arg0.getParent().getParent())).findViewById(R.id.ckbLI2Other)).isChecked();
			//			((Button)arg0).setText(a);
			item.setQuantity(Integer.valueOf(a));
			item.setAttrsState(ckbState);
			finish();
		}
	};
	private void InitialComponent() {
		btnCancel = (Button)findViewById(R.id.btnOITECencel);
		btnCancel.setOnClickListener(btnCancel_Click);
		btnOk = (Button)findViewById(R.id.btnOITEOk);
		btnOk.setOnClickListener(btnOk_Click);		
		txtMsg = (TextView)findViewById(R.id.txtOITEMsg2);
	}
	Button btnCancel;
	Button btnOk;
	TextView txtMsg;
	ListView listItem;
}