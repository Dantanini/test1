package tw.com.gear.marcorder;

import java.util.List;

import android.graphics.Bitmap;

//食物類別，App進入點餐畫面時會去載入伺服器的資料來產生食物物件
public class Food {

	private String serial = "";// 該食物物件的編號
	private String name = "";// 名稱
	private String attrs = "";// 食物的附加條件(，例如:加起司)
	private int attrsPrice = 0;// 附加條件的價格
	private int price = 0;// 價格
	private String type = "";// 食物的分類
	private Bitmap icon;// 圖片
	private String iconPath = "";// 圖片下載入徑
	private int spread = 0; // 價差，在附餐顯示時需要顯示出來

	private boolean isSelected = false;

	public Food() {

	}

	public Food(String serial, String name) {
		this.serial = serial;
		this.name = name;
	}

	public String getIconPath() {
		return iconPath;
	}

	public void setIconPath(String path) {
		iconPath = path;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAttrs() {
		return attrs;
	}

	public void setAttrs(String attrs) {
		this.attrs = attrs;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public String getSerial() {
		return serial;
	}

	public void setSerial(String serial) {
		this.serial = serial;
	}

	public Bitmap getIcon() {
		return icon;
	}

	public void setIcon(Bitmap icon) {
		this.icon = icon;
	}

	public String getType() {
		return type;
	}

	public void setType(String _type) {
		this.type = _type;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	/**
	 * 獲取價差
	 */
	public int getSpread() {
		return spread;
	}

	/**
	 * 設定價差
	 */
	public void setSpread(int spread) {
		this.spread = spread;
	}

	public int getAttrsPrice() {
		return attrsPrice;
	}

	public void setAttrsPrice(int attrsPrice) {
		this.attrsPrice = attrsPrice;
	}

}
