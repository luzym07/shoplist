package com.emt.shoplist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.pras.SpreadSheet;
import com.pras.SpreadSheetFactory;
import com.pras.WorkSheet;
import com.pras.WorkSheetCell;
import com.pras.WorkSheetRow;
import com.pras.sp.Field;
import com.pras.table.Record;
import com.prasanta.auth.AndroidAuthenticator;

import android.app.Activity;
import android.app.Application;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class ShopListApplication extends Application implements
    OnSharedPreferenceChangeListener {
  private static final String TAG = ShopListApplication.class.getSimpleName();
  
  public SpreadSheet ss;
  private SharedPreferences prefs;
  private ShopListData shopListData;
  private boolean serviceRunning;
  private boolean inShopList;

  @Override
  public void onCreate() {
    super.onCreate();
    this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
    this.prefs.registerOnSharedPreferenceChangeListener(this);
    this.shopListData = new ShopListData(this);
    Log.i(TAG, "Application started");
  }
  
  /*class GetAsyncSpreadSheetFactory extends AsyncTask<Application, Void, ArrayList<SpreadSheet> >{

  	
  	// This method runs in a separate thread
		@Override
		protected ArrayList<SpreadSheet> doInBackground(Application... params) {
			
			SpreadSheetFactory f;
			//f = SpreadSheetFactory.getInstance(new AndroidAuthenticator(params[0]));//use existing google account
	        f= SpreadSheetFactory.getInstance("lukaszymla@gmail.com","niekumaty1");
			// Retrieve a list with all spreadsheets with names starting with "World"
			ArrayList<SpreadSheet> alss;
			alss = f.getAllSpreadSheets(true, "World", false);

			// When we return here from the background thread, the method onPostExecute() is automatically called in the main/UI thread  
			return alss;
			
		}
		
		
		// This method runs in the main UI thread after the background thread is finished
		@Override
		protected void onPostExecute(ArrayList<SpreadSheet> alss){

				
			// Catch if something went wrong with Google Spreadsheet communcation, or there was no matching spreadsheets found.
			// - in that case just return.
			if ( (alss == null) || (alss.size() == 0) ) return;

			// Make a toast showing how many spreadsheets we have found
			Toast.makeText(getApplicationContext(), "Found " + alss.size() + " spreadsheets", Toast.LENGTH_LONG).show();

			// Store the first matching spreadsheet in local variable ...
			SpreadSheet ss = alss.get(0);

			// Get the first worksheet in that spreadsheet ...
			WorkSheet ws = ss.getAllWorkSheets().get(0);

			// Store all rows in that worksheet in an ArrayList of
			// WorkSheetRow's ...
			ArrayList<WorkSheetRow> rows = ws.getData(false);

			// Lets look at the first row (index 0)
			// - Note that this row 2 in the worksheet. Row 1 is reserved for the column names and can not be accessed directly.
			WorkSheetRow row = rows.get(0);

			// Store all cells in the row we are looking at, in an ArrayList of WorkSheetCell's
			ArrayList<WorkSheetCell> cells = row.getCells();

			// Lets look at the cell in column B ( 0=A, 1=B, ... )
			WorkSheetCell cell = cells.get(1);

			// Show our result
			String str = "cell = " + cell.getValue();
			Toast.makeText(getApplicationContext(),	str, Toast.LENGTH_LONG).show();
			Log.v(TAG, str);
			//((TextView)findViewById(R.id.tv)).setText(str);
			
			
		}
  };*/


  //get GoogleSpreadSheet
  public synchronized SpreadSheet getSpreadSheet() {
    if (this.ss == null) {
      String username = this.prefs.getString("username", "student");
      String password = this.prefs.getString("password", "password" );
      //String url = this.prefs.getString("url", "http://yamba.marakana.com/api");
      if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)){
    	  	SpreadSheetFactory f;
			//f = SpreadSheetFactory.getInstance(new AndroidAuthenticator(params[0]));//use existing google account
	        f= SpreadSheetFactory.getInstance("","");
			// Retrieve a list with all spreadsheets with names starting with "World"
			ArrayList<SpreadSheet> alss;
			alss = f.getAllSpreadSheets(true, "World", false);
			if ( (alss == null) || (alss.size() == 0) ){
				Log.d(TAG, "No World spreadSheet or connection failed");
				return null;
			}
			this.ss = alss.get(0);
      }
    }
    return this.ss;
  }

  public boolean startOnBoot() {
    return this.prefs.getBoolean("startOnBoot", false);

  }

  public ShopListData getShopListData() {
    return shopListData;
  }

  public synchronized int fetchListUpdates() {
    Log.d(TAG, "Fetching shoppingList updates");
    SpreadSheet ss = this.getSpreadSheet();
    
    if (ss == null) {
      Log.d(TAG, "gSpreadSheet connection failed!");
      return 0;
    }
    try {
      //List<Status> statusUpdates = twitter.getFriendsTimeline();
      int count = 0;
      ContentValues values = new ContentValues();
	  HashMap<String, String> rs = new HashMap<String, String>();
      //ss.addWorkSheet("db", new String[]{"name", "amount", "status", "user", "timestamp"});
	   // Get the first worksheet in that spreadsheet  ...
		WorkSheet ws = ss.getAllWorkSheets().get(0);
			
		  //******Update spreadsheet*******
		  //Google spreadsheet: 0 Name, 1 Amount, 2 status, 3 user, 4 created_at
		  //1 Query DB for upload=true, get all db entries/cursor
			List<ShopItem> items = this.getShopListData().getAllToUpdate();
			Log.d(TAG, "items to update: "+ items.size());
			for (ShopItem item: items){
				//2 get current id (timestamp) and update it with new (upload) timestamp
				long rowID = item.getTimestamp();
				item.setTimestamp(System.currentTimeMillis());
				//3 add gs row (if not already in gs, otherwise update)
			    rs = new HashMap<String, String>();
				rs.put("name", item.getName());
				rs.put("amount", "0");
				rs.put("status", "0"); 
				rs.put("user", "lukasz");
				rs.put("timestamp", String.valueOf(item.getTimestamp()));
				ws.addRecord(ss.getKey(), rs); //what if failed?
				
				//4 update db with timestamp and update flag getWriteableDatabase()
				values = new ContentValues();
				values.put(ShopListData.C_ID, item.getTimestamp());
				values.put(ShopListData.C_UPDATEFLAG, 0);
				this.getShopListData().update(rowID, values);//udpadet id (timestamp) in db
			}
			//***Update DB****
			ArrayList<Record> records = ws.getRecords(ss.getKey());
			for(int i=0; i<records.size(); i++){
				Record r=records.get(i);
				rs = r.getData();
				//Log.d(TAG,"hash data: " + rs);
				//values.put(ShopListData.C_ID, value);
		        values.put(ShopListData.C_ID, rs.get("timestamp"));
		        values.put(ShopListData.C_NAME, rs.get("name"));
		        values.put(ShopListData.C_AMOUNT, rs.get("amount"));
		        values.put(ShopListData.C_STATUS, rs.get("status"));
		        values.put(ShopListData.C_USER, rs.get("user"));
		        values.put(ShopListData.C_UPDATEFLAG, 0);
		        this.getShopListData().insertOrUpdate(values);
		        //if (latestStatusCreatedAtTime < createdAt) {
		          //count++;
		        //}
		      }
		      Log.d(TAG, count > 0 ? "Got " + count + " status updates"
		          : "No new status updates");
      return count;
    } catch (RuntimeException e) {
      Log.e(TAG, "Failed to fetch status updates", e);
      return 0;
    }
  }

  public synchronized void onSharedPreferenceChanged(
      SharedPreferences sharedPreferences, String key) {
    //this.twitter = null;
  }

  public boolean isServiceRunning() {
    return serviceRunning;
  }

  public void setServiceRunning(boolean serviceRunning) {
    this.serviceRunning = serviceRunning;
  }

  public boolean isInTimeline() {
    return inShopList;
  }

  public void setInTimeline(boolean inTimeline) {
    this.inShopList = inTimeline;
  }

  @Override
  public void onTerminate() {
    super.onTerminate();
    this.shopListData.close();
    Log.i(TAG, "Application terminated");
  }
}
