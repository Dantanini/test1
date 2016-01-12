package tw.com.gear.marcorder;

import android.content.Context;
import android.widget.Toast;

/**好用不會重複的吐司類別，送給大家使用，請使用類別方法"Show";*/
public class MyToast extends Toast{
	
	public MyToast(Context context) {
		super(context);
	}
	                     /**※使用方法※*/
	/**第一個看到context不用想了，就是你的那個act.this，第二個傳你要的字串*/
	public static void Show(Context context,String s) {
		if (ActMain.toast != null) {
			ActMain.toast.setText(s);
			ActMain.toast.show();
		} else {
			ActMain.toast = Toast.makeText(context, s, Toast.LENGTH_SHORT);
			ActMain.toast.show();
		}
	}
}
