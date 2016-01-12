package tw.com.gear.marcorder;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import at.technikum.mti.fancycoverflow.FancyCoverFlow;
import at.technikum.mti.fancycoverflow.FancyCoverFlowAdapter;

public class ImageAdapter extends FancyCoverFlowAdapter {

	private Context mContext ;
	private int width;
	private int height;
	private List<Bitmap> myImageList = new ArrayList<Bitmap>();
	private List<String> myTextList = new ArrayList<String>();
	private int index = 0;
	private int foodType = 0;
	private int singleFoodIndex = 0;
	
	public ImageAdapter(Context context){
		mContext = context;
	}
	@Override
	public int getCount() {
		return myImageList.size();
	}
	//設定現在的是哪種食物，在要顯示產品名字時用的
	public void SetFoodData(int foodtype, int singleFoodIndex){
		this.foodType = foodtype;
		this.singleFoodIndex = singleFoodIndex;
	}
	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

//	@Override
//	public View getView(int position, View convertView, ViewGroup arg2) {
//		ImageView imageView;
//		if(convertView == null){
//			imageView = new ImageView(mContext);
//			imageView.setLayoutParams(new Gallery.LayoutParams(width, height));
//			imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
//		}else{
//			imageView = (ImageView)convertView;
//		}
//		imageView.setImageBitmap(myImageList.get(position));
//		return imageView;
//	}

	public List<Bitmap> GetImageList(){
		return myImageList;
	}
	//設定imageAdapter的所有圖片
	public void SetImageList(List<Bitmap> bitmapList){
		myImageList = bitmapList;
	}
	public void SetTextList(List<String> textList){
		myTextList = textList;
	}
	public void SetWidth(int width){
		this.width = width;
	}
	public int GetWidth(){
		return this.width;
	}
	public void SetHeight(int height){
		this.height = height;
	}
	public int GetHeight(){
		return this.height;
	}
	//取得現在是第幾項
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	@Override
	public View getCoverFlowItem(int position, View convertView, ViewGroup viewGroup) {
		CustomViewGroup customViewGroup = null;

		if(convertView == null){
			customViewGroup = new CustomViewGroup(viewGroup.getContext());
			customViewGroup.setLayoutParams(new FancyCoverFlow.LayoutParams(width, height));
//			imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
		}else{
			customViewGroup = (CustomViewGroup)convertView;
		}
		
		customViewGroup.getImageView().setImageBitmap(myImageList.get(position));
		customViewGroup.getTextView().setText(myTextList.get(position));
		return customViewGroup;
	}
	
    private static class CustomViewGroup extends LinearLayout {

        // =============================================================================
        // Child views
        // =============================================================================

        private ImageView imageView;
        private TextView txtView;

        // =============================================================================
        // Constructor
        // =============================================================================

        private CustomViewGroup(Context context) {
            super(context);

            this.setOrientation(VERTICAL);
            this.setWeightSum(5);

            this.imageView = new ImageView(context);
            this.txtView = new TextView(context);
            
            LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            this.imageView.setLayoutParams(layoutParams);
            this.txtView.setLayoutParams(layoutParams);
            
            this.imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            this.imageView.setAdjustViewBounds(true);


            this.txtView.setTextSize(20);
            this.txtView.setGravity(Gravity.CENTER);

            this.addView(this.txtView);
            this.addView(this.imageView);
        }

        // =============================================================================
        // Getters
        // =============================================================================

        private ImageView getImageView() {
            return imageView;
        }
        private TextView getTextView(){
        	return txtView;
        }
    }
}