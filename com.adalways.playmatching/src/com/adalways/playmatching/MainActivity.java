package com.adalways.playmatching;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.adalways.playmatching.R;
import com.plter.lib.android.java.controls.ArrayAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

public class MainActivity extends ListActivity {

	
	private ArrayAdapter<GameListCellData> adapter;
	private ProgressDialog dialog=null;
	

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
        checkVersion(); 
		
		 AdView mAdView = (AdView) findViewById(R.id.adView);
	     AdRequest adRequest = new AdRequest.Builder().build();
	     mAdView.loadAd(adRequest);
		
		adapter=new ArrayAdapter<MainActivity.GameListCellData>(this,R.layout.game_list_cell) {
			
			@Override
			public void initListCell(int position, View listCell, ViewGroup parent) {
				ImageView iconIv = (ImageView) listCell.findViewById(R.id.iconIv);
				TextView labelTv=(TextView) listCell.findViewById(R.id.labelTv);
				
				GameListCellData data = getItem(position);
				iconIv.setImageResource(data.iconResId);
				labelTv.setText(data.label);
			}
		};
		
		setListAdapter(adapter);
		
		adapter.add(new GameListCellData("Fruit Matching", R.drawable.sg_icon, "sg_config.json"));
		adapter.add(new GameListCellData("Vegetable Matching", R.drawable.sc_icon, "sc_config.json"));
		adapter.add(new GameListCellData("Animal Matching", R.drawable.dw_icon, "dw_config.json"));
		adapter.add(new GameListCellData("Heart Matching", R.drawable.love_icon, "love_config.json"));
		adapter.add(new GameListCellData("Gem Matching", R.drawable.coin_icon, "coin_config.json"));
		adapter.add(new GameListCellData("Digit Matching", R.drawable.digit_icon, "digit_config.json"));
		adapter.add(new GameListCellData("Character Matching", R.drawable.char_icon, "char_config.json"));
	}
	
	public void checkVersion() { 
		try {
            String packageName = getPackageName();
            String url = "https://play.google.com/store/apps/details?id="+packageName;
            Log.d("main",url);

            PackageInfo info = getPackageManager().getPackageInfo(packageName, 0);

            new CheckVersionAsyncTask(MainActivity.this, info.versionName).execute(url);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
 
       
    } 
	
	
	@Override
	protected void onPause() {
		
		if (dialog!=null) {
			dialog.dismiss();
			dialog=null;
		}
		
		super.onPause();
	}
	
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		
		

		
		dialog=ProgressDialog.show(this, "Pleasing wait", "Loading...");
		
		GameListCellData data = adapter.getItem(position);
		Intent i = new Intent(this, LinkGameActivity.class);
		i.putExtra("configFile", data.gameConfigFile);
		
		
		startActivity(i);
	    
	    
		super.onListItemClick(l, v, position, id);
	}
	
	
	public static class GameListCellData{
		
		public GameListCellData(String label,int iconResId,String gameConfigFile) {
			this.label=label;
			this.iconResId=iconResId;
			this.gameConfigFile=gameConfigFile;
		}
		
		public String label=null;
		public int iconResId=0;
		public String gameConfigFile=null;
	}
}
