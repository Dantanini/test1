package tw.com.gear.marcorder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteMenagement extends SQLiteOpenHelper {

	public SQLiteMenagement(Context context) {
		super(context, "dbOrder.db", null, 1);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String strSql = "CREATE TABLE Ordered  (";
		strSql += "_id INTEGER PRIMARY KEY,  ";
		strSql += " oSerial TEXT, ";		
		strSql += " oGatTime TEXT, ";
		strSql += " oGatWay TEXT, ";
		strSql += " oName TEXT, ";
		strSql += " oPhone TEXT, ";
		strSql += " oState TEXT, ";
		strSql += " oAddress TEXT)";
		
		String strSql2 = "CREATE TABLE OrderDetail (";
		strSql2 += "_id INTEGER PRIMARY KEY, ";
		strSql2 += " oSerial TEXT,";
		strSql2 += " dPId TEXT,";
		strSql2 += " dPName TEXT,";
		strSql2 += " dPType TEXT,";
		strSql2 += " dPMfId TEXT,";
		strSql2 += " dPMfName TEXT,";
		strSql2 += " dPSfId TEXT,";
		strSql2 += " dPSfName TEXT,";
		strSql2 += " dPQty INTEGER,";
		strSql2 += " dPPrice INTEGER,";
		strSql2 += " dPAttrState INTEGER,";
		strSql2 += " dPAttrs TEXT,";
		strSql2 += " dPAttrsPrice TEXT)";
		
		db.execSQL(strSql);
		db.execSQL(strSql2);
	}
	public void Update(String strTable, ContentValues values, String where){
		getWritableDatabase().update(strTable, values, "oSerial='" + where + "'", null);
	}
//	public void Update(String strTable, ContentValues values, int intPk){
//		getWritableDatabase().update(strTable, values, "_id="+String.valueOf(intPk), null);
//	}
	public void Delete(String strTable, String oSerial){
		getWritableDatabase().delete(strTable, "oSerial='" + oSerial + "'", null);
	}
//	public void Delete(String strTable, int intPk){
//		getWritableDatabase().delete(strTable, "_id="+String.valueOf(intPk), null);
//	}
	public void Insert(String strTable, ContentValues values){
		getWritableDatabase().insert(strTable, null, values);
	}
	public Cursor Select(String str){
		return getReadableDatabase().rawQuery(str, null);
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

}