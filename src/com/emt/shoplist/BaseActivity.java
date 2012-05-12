package com.emt.shoplist;

import com.emt.shoplist.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * The base activity with common features
 */
public class BaseActivity extends Activity { // <1>
  ShopListApplication shopListApp; // <2>

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    shopListApp = (ShopListApplication) getApplication(); // <3>
  }

  // Called only once first time menu is clicked on
  @Override
  public boolean onCreateOptionsMenu(Menu menu) { // <4>
    getMenuInflater().inflate(R.menu.menu, menu);
    return true;
  }

  // Called every time user clicks on a menu item
  @Override
  public boolean onOptionsItemSelected(MenuItem item) { // <5>

    switch (item.getItemId()) {
    case R.id.itemPrefs:
      startActivity(new Intent(this, PrefsActivity.class)
          .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
      break;
    case R.id.itemToggleService:
      if (shopListApp.isServiceRunning()) {
        stopService(new Intent(this, UpdaterService.class));
      } else {
        startService(new Intent(this, UpdaterService.class));
      }
      break;
    case R.id.itemPurge:
      ((ShopListApplication) getApplication()).getShopListData().delete();
      Toast.makeText(this, R.string.msgAllDataPurged, Toast.LENGTH_LONG).show();
      // Let the world know status has changed - Timeline cares
      sendBroadcast( new Intent(UpdaterService.NEW_STATUS_INTENT) );
      break;
    case R.id.itemShoplist:
      startActivity(new Intent(this, ListActivity.class).addFlags(
          Intent.FLAG_ACTIVITY_SINGLE_TOP).addFlags(
          Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
      break;
    }
    return true;
  }

  // Called every time menu is opened
  @Override
  public boolean onMenuOpened(int featureId, Menu menu) { // <6>
    MenuItem toggleItem = menu.findItem(R.id.itemToggleService); // <7>
    if (shopListApp.isServiceRunning()) { // <8>
      toggleItem.setTitle(R.string.titleServiceStop);
      toggleItem.setIcon(android.R.drawable.ic_media_pause);
    } else { // <9>
      toggleItem.setTitle(R.string.titleServiceStart);
      toggleItem.setIcon(android.R.drawable.ic_media_play);
    }
    return true;
  }

}
