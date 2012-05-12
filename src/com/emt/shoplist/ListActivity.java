package com.emt.shoplist;

import com.emt.shoplist.R;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.SimpleCursorAdapter.ViewBinder;

public class ListActivity extends BaseActivity{ 
  static final String SEND_TIMELINE_NOTIFICATIONS = "com.marakana.yamba.SEND_TIMELINE_NOTIFICATIONS";

  Cursor cursor;
  ListView listShopping;
  SimpleCursorAdapter adapter;
  static final String[] FROM = { ShopListData.C_NAME, ShopListData.C_AMOUNT, ShopListData.C_USER, ShopListData.C_ID };
  static final int[] TO = { R.id.textItem, R.id.textAmount, R.id.textUser, R.id.textCreatedAt };
  TimelineReceiver receiver;
  IntentFilter filter; 
  
  EditText editText;
  Button updateButton;
  Button addButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.shoplist);

    // Find your views
    listShopping = (ListView) findViewById(R.id.listShoplist);
    editText = (EditText) findViewById(R.id.editText);
    updateButton = (Button) findViewById(R.id.buttonUpdate);
    updateButton.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            // Perform action on click
            ShopListApplication shopList = (ShopListApplication) getApplication();
            shopList.fetchListUpdates();
        }
    });
    addButton = (Button) findViewById(R.id.buttonAdd);
    addButton.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            // Perform action on click
            ShopListApplication shopList = (ShopListApplication) getApplication();
            ContentValues values = new ContentValues();
            values.put(ShopListData.C_NAME, editText.getText().toString());
	        values.put(ShopListData.C_AMOUNT, 0);
	        values.put(ShopListData.C_STATUS, 0);
	        values.put(ShopListData.C_USER, "lukasz");
	        values.put(ShopListData.C_ID, System.currentTimeMillis());
	        values.put(ShopListData.C_UPDATEFLAG, 1);
	        shopList.getShopListData().insertOrIgnore(values);
	        editText.setText(null);
        }
    });
    
    // Create the receiver
    receiver = new TimelineReceiver();
    filter = new IntentFilter( UpdaterService.NEW_STATUS_INTENT );
  }

  @Override
  protected void onResume() {
    super.onResume();

    this.setupList();
    
    // Register the receiver
    super.registerReceiver(receiver, filter,
        SEND_TIMELINE_NOTIFICATIONS, null);
  }
  
  @Override
  protected void onPause() {
    super.onPause();

    // UNregister the receiver
    unregisterReceiver(receiver); 
  }

  // View binder constant to inject business logic for timestamp to relative
  // time conversion
  static final ViewBinder VIEW_BINDER = new ViewBinder() { 

    public boolean setViewValue(View view, Cursor cursor, int columnIndex) { 
      if(view.getId() != R.id.textCreatedAt) return false; 
      
      // Update the created at text to relative time
      long timestamp = cursor.getLong(columnIndex); 
      CharSequence relTime = DateUtils.getRelativeTimeSpanString(view.getContext(), timestamp); 
      ((TextView)view).setText(relTime); 
      
      return true;  
    }

  };
  
  // Responsible for fetching data and setting up the list and the adapter
  private void setupList() {
    // Get the data from the database
    ShopListApplication shopList = (ShopListApplication) super.getApplication();
    this.cursor = shopList.getShopListData().getAllItems();
    startManagingCursor(this.cursor);

    // Setup the adapter
    adapter = new SimpleCursorAdapter(this, R.layout.row, cursor, FROM, TO);
    adapter.setViewBinder(VIEW_BINDER); 

    listShopping.setAdapter(adapter); 
  }

  // Receiver to wake up when UpdaterService gets a new status
  // It refreshes the timeline list by requerying the cursor
  class TimelineReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      setupList();
      Log.d("TimelineReceiver", "onReceived");
    }
  }

}
