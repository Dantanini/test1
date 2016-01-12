package tw.com.gear.marcorder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.integer;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
public class JsonFactory {

	public JsonFactory() {
		// TODO Auto-generated constructor stub
	}
	/**將購物車所有item轉成Json字串*/
	public static String GetJsonCodeFromCart(){
		JSONArray jsonArray = new JSONArray();
		List<OrderItem> list =  ActMain.myCart.getOrderList();
		try{
		for(int i = 0; i < list.size(); i++){
			JSONObject jsonObject = new JSONObject();
			if(list.get(i).getType().equals(ActMain.foodSetString)){
				//套餐
				jsonObject.put("id", list.get(i).getSerial());
				jsonObject.put("qty", list.get(i).getQuantity());
				jsonObject.put("type", list.get(i).getType());
				jsonObject.put("setDrink", list.get(i).getSubFoodSerial());
				jsonObject.put("attrs", "1008");
				jsonArray.put(jsonObject);
			}else if(!list.get(i).getType().equals(ActMain.foodSetString)){
				//單點
				jsonObject.put("id", list.get(i).getSerial());
				jsonObject.put("qty", list.get(i).getQuantity());
				jsonObject.put("type", list.get(i).getType());				
				jsonObject.put("setDrink", "0");
				if(list.get(i).getAttrsState()){
					jsonObject.put("attrs", list.get(i).getAttrs());
				}else{
					jsonObject.put("attrs", "1008");					
				}
				jsonArray.put(jsonObject);
			}
		}
		}catch(Exception e){
			e.printStackTrace();
		}
//		ActOrder.myCart.getOrderList().get(i)
		
		return jsonArray.toString();
	}
	//經由json字串轉換傳回ArrayList<OrderItem>物件(夢幻餐車使用)
	public static ArrayList<OrderItem> GetDreamCart(String jsonString){
		if(jsonString.trim().equals("")){
			return null;
		}
		ArrayList<OrderItem> cart = new ArrayList<OrderItem>();
		try {
			JSONArray jsonResult = new JSONArray(jsonString);
			for(int i = 0; i < jsonResult.length(); i++){
				OrderItem o = new OrderItem();
				JSONObject jsonObject = jsonResult.getJSONObject(i);
				o.setSerial(jsonObject.getString("id").toString().trim());
				o.setName(jsonObject.getString("name"));
				o.setQuantity(jsonObject.getInt("qty"));
				o.setType(jsonObject.getString("type"));
				o.setSubFoodSerial(jsonObject.getString("setDrink"));
				o.setSubFoodName(jsonObject.getString("setDrinkName"));
				o.setAttrsState(jsonObject.getBoolean("attrsState"));
				cart.add(o);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cart;
	}
	//經由ArrayList<OrderItem> 轉換成json字串送出(夢幻餐車使用)
	public static String GetJsonFromDreamCart(ArrayList<OrderItem> cart){
		JSONArray jsonArray = new JSONArray();
		try{
		for(int i = 0; i < cart.size(); i++){
			JSONObject jsonObject = new JSONObject();
			if(cart.get(i).getType().equals(ActMain.foodSetString)){
				jsonObject.put("id", cart.get(i).getSerial());
				jsonObject.put("name", cart.get(i).getName());
				jsonObject.put("qty", cart.get(i).getQuantity());
				jsonObject.put("type", cart.get(i).getType());
				jsonObject.put("setDrink", cart.get(i).getSubFoodSerial());
				jsonObject.put("setDrinkName", cart.get(i).getSubFoodName());	
				jsonObject.put("attrs", "1");//1指的是沒有加料
				jsonArray.put(jsonObject);
			}else if(!cart.get(i).getType().equals(ActMain.foodSetString)){
				jsonObject.put("id", cart.get(i).getSerial());
				jsonObject.put("name", cart.get(i).getName());
				jsonObject.put("qty", cart.get(i).getQuantity());
				jsonObject.put("type", cart.get(i).getType());				
				jsonObject.put("setDrink", "0");
				jsonObject.put("setDrinkName", "");	
				jsonObject.put("attrsState", cart.get(i).getAttrsState());
				jsonArray.put(jsonObject);
			}
		}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return jsonArray.toString();
	}
	//經由json字串轉換傳回food的分類集合List<List<Food>>
	public static ArrayList<ArrayList<Food>> GetFoodMultiList(String jsonString){
		ArrayList<ArrayList<Food>> multiList = new ArrayList<ArrayList<Food>>();
		ActMain.foodTypeArrayList.clear();
		ArrayList<Food> list = new ArrayList<Food>();
		boolean foodTypeIsMatch = false;//判斷是否已經有相同的食物類型
		try {
			JSONArray foodResult = new JSONArray(jsonString);
			for(int i = 0; i < foodResult.length(); i++){
				JSONObject foodObject = foodResult.getJSONObject(i);
				Food f = new Food();
				f.setName(foodObject.getString("fName").trim());
				f.setSerial(foodObject.getString("fId").trim());
				f.setPrice(Integer.valueOf(foodObject.getString("fPrice").trim()));
				f.setType(foodObject.getString("fType").trim());
				f.setIconPath(foodObject.getString("fPicPath"));
				f.setAttrs(foodObject.getString("fAnnotation").trim());
				f.setAttrsPrice(foodObject.getInt("aPrice"));
				if(f.getType().equals("套餐")){//如果是套餐，這類的食物會是套餐的副食，不會顯示在螢幕上
					ActMain.foodFactoryHttpListSetUse.GetSubFoodList().add(f);
					continue;
				}
				//判斷是否有相同的食物類型存在，如果有就存入，如果沒有就建立新的List集合存入MltiList
				for(List<Food> a : multiList){
					if(f.getType().equals(a.get(0).getType())){
						foodTypeIsMatch = true;
						a.add(f);
						break;
					}
				}		
				if(foodTypeIsMatch == false){
					ArrayList<Food> t = new ArrayList<Food>();
					ActMain.foodTypeArrayList.add(f.getType());
					t.add(f);
					multiList.add(t);
				}
				foodTypeIsMatch = false;
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return multiList;
	}
	//經由json字串轉換傳回FoodSet物件集合
	@SuppressLint("NewApi")
	public static List<FoodSet> GetFoodSetList(String jsonString){
		ArrayList<FoodSet> foodList = new ArrayList<FoodSet>();
		try {
			JSONArray foodResult = new JSONArray(jsonString);
			for(int i = 0; i < foodResult.length(); i++){
				JSONObject foodObject = foodResult.getJSONObject(i);
				FoodSet f = new FoodSet(foodObject.getString("fId").trim(), foodObject.getString("fName").trim(), new Food(), new Food());
				f.setType(ActMain.foodSetString);
				f.setIconPath(foodObject.getString("fPicPath"));
				f.getSetDetail().add(foodObject.getString("sDrink1").trim());
				f.getSetDetail().add(foodObject.getString("sDrink2").trim());
				f.getSetDetail().add(foodObject.getString("sDrink3").trim());
				f.getSetDetail().add(foodObject.getString("sDrink4").trim());
				f.getSetDetail().add(foodObject.getString("sDrink5").trim());
				//---------------------
				f.getMainFood().setSerial(foodObject.getString("mainFood").trim());
				f.getSubFood().setSerial(foodObject.getString("subFood").trim());
				Food mf = (Food)ActMain.foodFactoryHttpList.findBySerial(f.getMainFood().getSerial());
				Food sf = (Food)ActMain.foodFactoryHttpListSetUse.findBySerial(f.getSubFood().getSerial());
				f.getMainFood().setName(mf.getName());
				f.getMainFood().setPrice(mf.getPrice());
				f.getMainFood().setIcon(mf.getIcon());
				f.getSubFood().setName(sf.getName());
				f.getSubFood().setPrice(sf.getPrice());
				f.getSubFood().setIcon(sf.getIcon());
				//----------------------
				foodList.add(f);
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return foodList;
	}
	/**
	 * 這是舊的方法，新的是用GetFoodMultiList有分類的方法
	 * */
	//經由json字串轉換傳回food物件集合
	public static List<Food> GetFoodList(String jsonString){
		ArrayList<Food> foodList = new ArrayList<Food>();
		try {
			JSONArray foodResult = new JSONArray(jsonString);
			for(int i = 0; i < foodResult.length(); i++){
				JSONObject foodObject = foodResult.getJSONObject(i);
				Food f = new Food();
				f.setName(foodObject.getString("fName").trim());
				f.setSerial(foodObject.getString("fId").trim());
				f.setPrice(Integer.valueOf(foodObject.getString("fPrice").trim()));
				f.setType(foodObject.getString("fType").trim());
				f.setIconPath(foodObject.getString("fPicPath"));
				f.setAttrs(foodObject.getString("fAnnotation").trim());
				foodList.add(f);
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return foodList;
	}
}
