package tw.com.gear.marcorder;


import java.util.ArrayList;

import tw.com.gear.marcorder.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.sax.StartElementListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
//購物車介面使用的adapter
//會連到ActOrderItemSetToEdit頁面(套餐編輯頁面)，或連到ActOrderItemToEdite頁面(單點編輯頁面)
public class ImageTextAdapter extends BaseAdapter {
	private LayoutInflater layoutInflater;
		Context context;

			private ArrayList<OrderItem>itemList = new ArrayList<OrderItem>();
			private ArrayList<Bitmap> iconList = new ArrayList<Bitmap>();
			
	//套餐訂購預覽頁面使用的建構子
	public ImageTextAdapter(Context context, OrderItem i, Bitmap b) {
		layoutInflater = (LayoutInflater)context.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE);
		itemList.add(i);
		iconList.add(b);
		this.context = context;
		
	}
	//購物車用的建構子
	public ImageTextAdapter(Context context, ArrayList<OrderItem> orderlist, ArrayList<Bitmap> iconlist) {
		layoutInflater = (LayoutInflater)context.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE);
		this.itemList = orderlist;
		this.iconList = iconlist;
		this.context = context;
	}
	
	class ViewHolder {
		ImageView img;
		TextView txtlist;
		Button btnChange, btnDel;
	}

	@Override
	public int getCount() {
		return itemList.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	//更改
	OnClickListener viewHolderBtnChange_CLick = new OnClickListener(){
		@Override
		public void onClick(View v) {
			int position = ((ListView)(v.getParent().getParent().getParent())).getPositionForView(v);
			if(ActMain.previewFood.getName().equals("")){//這行判斷，是要執行預覽頁面來的資料，還是購物車頁面來的資料
				if(itemList.get(position).getType().equals(ActMain.foodSetString)){//執行購物車來的資料
					//轉到套餐的編輯頁面
					Intent intent = new Intent(v.getContext(), ActOrderItemSetToEdit.class);
					intent.putExtra("serial", itemList.get(position).getSerial().toString());
					v.getContext().startActivity(intent);
				}else{
					//	轉到單點的編輯頁面
					Intent intent = new Intent(v.getContext(), ActOrderItemToEdit.class);
					intent.putExtra("serial", itemList.get(position).getSerial().toString());				
					v.getContext().startActivity(intent);
				}
			}else{
				//執行預覽頁面來的資料
				Intent intent = new Intent(v.getContext(), ActOrderItemSetToEdit.class);
				intent.putExtra("serial", itemList.get(position).getSerial().toString());
				v.getContext().startActivity(intent);
			}
		}
	};
	//刪除
	OnClickListener viewHolderBtnDel_CLick = new OnClickListener(){
		
		@Override
		public void onClick(View v) {
			//抓取在listView中的view的position
			//用position找購物車中的項目，並刪除,也將在orderedIconList中的該項目圖片刪除
			if(ActMain.previewFood.getName().equals("")){
				ActMain.myCart.getOrderList().remove(((ListView)(v.getParent().getParent().getParent())).getPositionForView(v));
				ActMain.orderedIconList.remove(((ListView)(v.getParent().getParent().getParent())).getPositionForView(v));
				//用Position找ListView中的項目，並刪除
				((ListView)(v.getParent().getParent().getParent())).invalidateViews();
				int total=0;
				for(OrderItem o : ActMain.myCart.getOrderList()){
					total += o.getPrice()*o.getQuantity();
				}
				if(ActMain.whoIsActivity.equals("Cart")){
					((TextView)(((Activity)v.getContext()).findViewById(R.id.txtAPCMsg1))).setText(
							"您的取餐方式："+ActMain.myCart.getOrderType()+
							"\r\n您的預計取餐時間："+ActMain.myCart.getTime().substring(5, 16)+
							"\r\n總計："+total+"元");					
				}else if(ActMain.whoIsActivity.equals("dreamCart")){
					((TextView)(((Activity)v.getContext()).findViewById(R.id.txtADCFMsg1))).setText(
							"以下是您的餐點"+
							"\r\n總計："+total+"元");						
				}
			}else{
				ActMain.previewFood.Clear();
				((Activity)v.getContext()).finish();
			}
		}	
	};
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.listview_item, null);
            viewHolder = new ViewHolder();
            viewHolder.img = (ImageView) convertView.findViewById(R.id.img);                
            viewHolder.txtlist = (TextView) convertView.findViewById(R.id.txtlist);
            viewHolder.btnChange = (Button) convertView.findViewById(R.id.btnChange);
            viewHolder.btnChange.setOnClickListener(viewHolderBtnChange_CLick);
            viewHolder.btnChange.setFocusable(false);//把Button的focus設為false才能抓到他的爸爸物件(ListView)的focus
            viewHolder.btnDel = (Button) convertView.findViewById(R.id.btnDel);
            viewHolder.btnDel.setFocusable(false);
            viewHolder.btnDel.setOnClickListener(viewHolderBtnDel_CLick);
            convertView.setTag(viewHolder);
        } 
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.img.setImageBitmap(iconList.get(position));
        if(itemList.get(position).getType().equals(ActMain.foodSetString)){//如果是套餐
        	viewHolder.txtlist.setText(itemList.get(position).getName()+"\r\n"+itemList.get(position).getMainFoodName()+
        			"+"+itemList.get(position).getSubFoodName()+"\r\n單價: "+
        			itemList.get(position).getPrice()+"\r\n數量: "+itemList.get(position).getQuantity()+
        			"\r\n小計: "+(itemList.get(position).getPrice()*itemList.get(position).getQuantity()));        	
        }else{//如果是單點
        	if(itemList.get(position).getAttrsState()){
        		viewHolder.txtlist.setText(itemList.get(position).getName()+"\r\n單價: "+
        				itemList.get(position).getPrice()+"\r\n數量: "+itemList.get(position).getQuantity()+
        				"\r\n" + itemList.get(position).getAttrs() +
        				"\r\n小計: "+(itemList.get(position).getPrice() * itemList.get(position).getQuantity() +
        						itemList.get(position).getAttrsPrice() * itemList.get(position).getQuantity()));        	        	        		        		
        	}else{
        		viewHolder.txtlist.setText(itemList.get(position).getName()+"\r\n單價: "+
        				itemList.get(position).getPrice()+"\r\n數量: "+itemList.get(position).getQuantity()+
        				"\r\n小計: "+(itemList.get(position).getPrice()*itemList.get(position).getQuantity()));        	        	        		
        	}
        }
        return convertView;
    }
	
}	