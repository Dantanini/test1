package tw.com.gear.marcorder;


import java.util.ArrayList;

import tw.com.gear.marcorder.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.provider.CalendarContract.Colors;
import android.sax.StartElementListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
//購物車介面使用的adapter
//會連到ActOrderItemSetToEdit頁面(套餐編輯頁面)，或連到ActOrderItemToEdite頁面(單點編輯頁面)
public class ImageTextAdapterForSerchOrders extends BaseAdapter {
	private LayoutInflater layoutInflater;
	private Orders order;
			
	private ArrayList<Orders>orderList = new ArrayList<Orders>();
			private ArrayList<Bitmap> iconList = new ArrayList<Bitmap>();
			
	public ImageTextAdapterForSerchOrders(){
		
	}
	//購物車用的建構子
	public ImageTextAdapterForSerchOrders(Context context, ArrayList<Orders> orderlist, ArrayList<Bitmap> iconlist) {
		layoutInflater = (LayoutInflater)context.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE);
		this.orderList = orderlist;
		this.iconList = iconlist;
	}
	
	class ViewHolder {
		ImageView img;
		TextView txtlist, txtState;
		CheckBox ckbSelect;
	}

	@Override
	public int getCount() {
		return orderList.size();
	}

	@Override
	public Object getItem(int position) {
		return orderList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void select(int position){
		if(!orderList.get(position).isSelected()){
			orderList.get(position).setSelected(true);
			for(int i = 0; i < orderList.size(); i++){
				if(i != position){
					orderList.get(i).setSelected(false);
				}
			}
		}
		notifyDataSetChanged();
	}

	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.listview_item4, null);
            viewHolder = new ViewHolder();
            viewHolder.img = (ImageView) convertView.findViewById(R.id.imgLVI4);                
            viewHolder.txtlist = (TextView) convertView.findViewById(R.id.txtLVI4list);
            viewHolder.txtState = (TextView) convertView.findViewById(R.id.txtLVI4State);            
            viewHolder.ckbSelect = (CheckBox)convertView.findViewById(R.id.ckbLVI4Select);
            viewHolder.ckbSelect.setSelected(false);
            viewHolder.ckbSelect.setChecked(false);
            viewHolder.ckbSelect.setFocusable(false);
            convertView.setTag(viewHolder);
        } 
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        String totalItem = "";
        for(OrderItem item : orderList.get(position).getOrderList()){
        	if(item.getType().equals(ActMain.foodSetString)){
        		totalItem+=item.getName() + "x" + item.getQuantity() + "  " + item.getPrice() * item.getQuantity() + "元\r\n　";        		
        	}else{
        		if(item.getAttrsState()){
        			totalItem+=item.getName() + " " + item.getAttrs() + "x" + item.getQuantity() + "  " + (item.getPrice() * item.getQuantity()+item.getAttrsPrice()*item.getQuantity()) +"元\r\n　";      	        			
        		}else{
        			totalItem+=item.getName() + "x" + item.getQuantity() + "  " + item.getPrice() * item.getQuantity() +"元\r\n　";      	        			
        		}
        	}
        }
        viewHolder.img.setImageBitmap(iconList.get(position));
        viewHolder.txtState.setText(orderList.get(position).getOrdersState());
        if(orderList.get(position).getOrdersState().equals("待取"))
        	viewHolder.txtState.setTextColor(Color.rgb(255, 0, 0));
        else{
        	viewHolder.txtState.setTextColor(Color.rgb(0, 0, 0));        	
        }
        viewHolder.txtlist.setText("取餐時間：\r\n"+orderList.get(position).getTime()+
        		"\r\n取餐方式："+ orderList.get(position).getOrderType()+
        		"\r\n訂單編號：\r\n"+ orderList.get(position).getOrdersSerial()+
        		"\r\n訂單狀態："+ orderList.get(position).getOrdersState()+
        		"\r\n總計："+ orderList.get(position).GetTotalPrice()+"元"+
        		"\r\n點餐項目：\r\n"+"　"+totalItem);
        order = (Orders)getItem(position);
        viewHolder.ckbSelect.setChecked(order.isSelected());
        return convertView;
    }
	
}	