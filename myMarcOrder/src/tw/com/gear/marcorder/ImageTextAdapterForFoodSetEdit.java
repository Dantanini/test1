package tw.com.gear.marcorder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import tw.com.gear.marcorder.ImageTextAdapterForFoodEdit.ViewHolder;
import tw.com.gear.marcorder.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ImageTextAdapterForFoodSetEdit extends BaseAdapter {
	
	private LayoutInflater layoutInflater;
	private List<Food>itemList = new ArrayList<Food>();
	private List<Bitmap> iconList = new ArrayList<Bitmap>();
	private Food food;
	public ImageTextAdapterForFoodSetEdit() {
		// TODO Auto-generated constructor stub
	}

	//載入所有飲料可供選擇的建構子
	public ImageTextAdapterForFoodSetEdit(Context context, List<Food> orderlist, List<Bitmap> iconlist) {
		layoutInflater = (LayoutInflater)context.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE);
		this.itemList = (ArrayList<Food>)orderlist;
		this.iconList = (ArrayList<Bitmap>)iconlist;
	}
	
	class ViewHolder {
		ImageView img;
		TextView txtlist;
		CheckBox ckbSelect;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return itemList.size();
	}
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return itemList.get(position);
	}
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	public void select(int position){
		if(!itemList.get(position).isSelected()){
			itemList.get(position).setSelected(true);
			for(int i = 0; i < itemList.size(); i++){
				if(i != position){
					itemList.get(i).setSelected(false);
				}
			}
		}
		notifyDataSetChanged();
	}

	ViewHolder viewHolder;
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.listview_item3, null);
            viewHolder = new ViewHolder();
            viewHolder.img = (ImageView) convertView.findViewById(R.id.imgLVI3);                
            viewHolder.txtlist = (TextView) convertView.findViewById(R.id.txtLVI3list);
            viewHolder.ckbSelect = (CheckBox) convertView.findViewById(R.id.ckbLVI3Select);
            viewHolder.ckbSelect.setSelected(false);
            viewHolder.ckbSelect.setChecked(false);
            viewHolder.ckbSelect.setFocusable(false);
            convertView.setTag(viewHolder);
        } 
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.img.setImageBitmap(iconList.get(position));
        if(itemList.get(position).getSpread() == 0){
        	viewHolder.txtlist.setText(itemList.get(position).getName());        	        	
        }else{
        	viewHolder.txtlist.setText(itemList.get(position).getName() + "  加" + itemList.get(position).getSpread() + "元");        	
        }
        food = (Food)getItem(position);
//        viewHolder.ckbSelect.setTag(position);
        viewHolder.ckbSelect.setChecked(food.isSelected());
        
		return convertView;
         
	}
}