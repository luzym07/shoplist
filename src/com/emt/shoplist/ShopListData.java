package com.emt.shoplist;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ShopListData {
  private static final String TAG = ShopListData.class.getSimpleName();

  private static final int VERSION = 1;
  private static final String DATABASE = "shopList.db";
  private static final String TABLE = "shoplist";

  public static final String C_ID = "_id"; //col 0 int TIMESTAMP
  public static final String C_NAME = "name"; //col 1 string
  public static final String C_AMOUNT = "amount";//col 2 int
  public static final String C_STATUS = "status";//col 3 int
  public static final String C_USER = "user";//col 4 text
  public static final String C_UPDATEFLAG = "update_flag";//col 5 int

  private static final String GET_ALL_ORDER_BY = C_NAME + " DESC";

  //private static final String[] MAX_CREATED_AT_COLUMNS = { "max("
    //  + ShopListData.C_CREATED_AT + ")" };

  private static final String[] DB_NAME_COLUMNS = { C_NAME };

  private class DbHelper extends SQLiteOpenHelper {

    public DbHelper(Context context) {
      super(context, DATABASE, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
      Log.i(TAG, "Creating database: " + DATABASE);
      db.execSQL("create table " + TABLE + " (" + C_ID + " integer primary key, " + C_NAME + " text, " 
    		  + C_AMOUNT + " int, " + C_STATUS + " int, " + C_USER + " text, "
    		  + C_UPDATEFLAG + " int)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      db.execSQL("drop table " + TABLE);
      this.onCreate(db);
    }
  }

  private final DbHelper dbHelper;

  public ShopListData(Context context) {
    this.dbHelper = new DbHelper(context);
    //this.dbHelper.onCreate(null);
    Log.i(TAG, "Initialized data");
  }

  public void close() {
    this.dbHelper.close();
  }

  public void insertOrIgnore(ContentValues values) {
    Log.d(TAG, "insertOrIgnore on " + values);
    SQLiteDatabase db = this.dbHelper.getWritableDatabase();
    try {
      db.insertWithOnConflict(TABLE, null, values,
          SQLiteDatabase.CONFLICT_IGNORE);
    } finally {
      db.close();
    }
  }
  public void insertOrUpdate(ContentValues values) {
	    Log.d(TAG, "insertOrUpdate on " + values);
	    SQLiteDatabase db = this.dbHelper.getWritableDatabase();
	    try {
	      db.insertWithOnConflict(TABLE, null, values,
	          SQLiteDatabase.CONFLICT_REPLACE);
	    } finally {
	      db.close();
	    }
	  }
  
  public void update(long rowID, ContentValues values) {
	    Log.d(TAG, "update on " + values);
	    SQLiteDatabase db = this.dbHelper.getWritableDatabase();
	    try {
	      db.updateWithOnConflict(TABLE, values, C_ID + "="+ rowID, null, SQLiteDatabase.CONFLICT_IGNORE);
	    } finally {
	      db.close();
	    }
	  }
  
  public List<ShopItem> getAllToUpdate(){
	  List<ShopItem> items = new ArrayList<ShopItem>();
	  SQLiteDatabase db = this.dbHelper.getReadableDatabase();  
	  try {
		  Cursor cursor = db.query(TABLE, null, C_UPDATEFLAG + "= 1" , null,
          null, null, null); 
		  try {
              cursor.moveToFirst();
              while (!cursor.isAfterLast()) {
      			ShopItem item = cursorToItem(cursor);
      			items.add(item);
      			cursor.moveToNext();
      		  }
              return items;
          } finally {
            cursor.close();
          }
	  } finally {
	    db.close();
	  }
  }
  
  private ShopItem cursorToItem(Cursor cursor) {
		ShopItem item = new ShopItem();
		Log.d(TAG, "CursorToItem cursor col: " + cursor.getColumnCount() + " rows: "+ cursor.getCount()
				+ " col_names: "+ cursor.getColumnName(0));
		item.setTimestamp(cursor.getLong(0));
		item.setName(cursor.getString(1));
		item.setAmount(cursor.getLong(2));
		item.setStatus(cursor.getLong(3));
		item.setUser(cursor.getString(4));
		item.setUpdate(cursor.getLong(5));
		return item;
	}

  /**
   * 
   * @return Cursor where the columns are sorted by name descending
   */
  public Cursor getAllItems() {
    SQLiteDatabase db = this.dbHelper.getReadableDatabase();
    return db.query(TABLE, null, null, null, null, null, GET_ALL_ORDER_BY);
  }

  public String getStatusTextById(long id) {
    SQLiteDatabase db = this.dbHelper.getReadableDatabase();
    try {
      Cursor cursor = db.query(TABLE, DB_NAME_COLUMNS, C_ID + "=" + id, null,
          null, null, null);
      try {
        return cursor.moveToNext() ? cursor.getString(0) : null;
      } finally {
        cursor.close();
      }
    } finally {
      db.close();
    }
  }
  
  /**
   * Deletes ALL the data
   */
  public void delete() {
    // Open Database
    SQLiteDatabase db = dbHelper.getWritableDatabase();

    // Delete the data
    db.delete(TABLE, null, null);

    // Close Database
    db.close();
  }

}
