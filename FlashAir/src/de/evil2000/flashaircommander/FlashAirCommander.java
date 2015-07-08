package de.evil2000.flashaircommander;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ProgressBar;

@SuppressWarnings("deprecation")
public class FlashAirCommander extends ActionBarActivity {
	private static final String APP_NAME = "FlashAirCommander";
	private static final String flashAirHost = "192.168.8.14";
	private FileListAdapter fileListAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_flash_air_commander);
		
		ProgressBar progressBar = new ProgressBar(this);
        progressBar.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        progressBar.setIndeterminate(true);
		
        ListView lstView = (ListView) findViewById(R.id.lstView);
        
		lstView.setEmptyView(progressBar);
		ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
        root.addView(progressBar);
        
        ArrayList<HashMap<String, String>> files = new ArrayList<HashMap<String,String>>();
        
        fileListAdapter = new FileListAdapter(this, files);
        lstView.setAdapter(fileListAdapter);
	}

	@Override
	protected void onStart() {
		super.onStart();
		updateFileListing("/DCIM/100CANON");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.flash_air_commander, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void updateFileListing(String dir) {
		// Create callback for httpTask
		OnTask callback = new OnTask() {
			@Override
			public void onTaskCompleted(String doc) {
				fileListAdapter.clear();
				ArrayList<HashMap<String, String>> files = parseFileListing(doc);
				fileListAdapter.addAll(files);
			}
		};
		
		if (dir == null) dir = "/";
		if (dir.equals("")) dir = "/";

		/*HttpData httpData = new HttpData();
		NetworkTask nt = new NetworkTask(httpData, callback);
		nt.execute("http://" + flashAirHost + "/command.cgi?op=100&DIR="+dir);*/
		
		ArrayList<HashMap<String, String>> files = parseFileListing(generateFakeFileListing(dir));
		fileListAdapter.clear();
		fileListAdapter.addAll(files);
		fileListAdapter.notifyDataSetChanged();
	}
	
	private ArrayList<HashMap<String,String>> parseFileListing(String raw) {
		ArrayList<HashMap<String,String>> filelist = new ArrayList<HashMap<String,String>>();
		
		String[] lines = raw.split("\r\n");
		for (String line : lines) {
			if (line.equals("WLANSD_FILELIST"))
				continue;
			String [] parts = line.split(",");
			HashMap<String, String> details = new HashMap<String,String>();
			details.put("directory", parts[0].equals("") ? "/" : parts[0]);
			details.put("filename", parts[1]);
			details.put("size", parts[2]);
			
			Integer roh = Integer.parseInt(parts[3]);
			String attribs = "";
			attribs += ((roh & 32) >> 5) > 0 ? "a" : ""; // Archive
			attribs += ((roh & 16) >> 4) > 0 ? "d" : ""; // Directory
			attribs += ((roh & 8) >> 3) > 0 ? "v" : ""; // Volume
			attribs += ((roh & 4) >> 2) > 0 ? "s" : ""; // System file
			attribs += ((roh & 2) >> 1) > 0 ? "h" : ""; // Hidden
			attribs += (roh & 1) > 0 ? "r" : ""; // Read only
			details.put("attributes",attribs);
			
			roh = Integer.parseInt(parts[4]);
			Integer year = ((roh & 32256) >> 9) + 1980;
			Integer month = (roh & 480) >> 5;
			Integer day = roh & 31;
			roh = Integer.parseInt(parts[5]);
			Integer hour = (roh & 63488) >> 11;
			Integer minute = (roh & 2016) >> 5;
			Integer second = (roh & 31) * 2;	
			details.put("date", year+"-"+(month < 10 ? "0" : "")+month+"-"+(day < 10 ? "0" : "")+day);
			details.put("time", (hour < 10 ? "0" : "")+hour+":"+(minute < 10 ? "0" : "")+minute+":"+(second < 10 ? "0" : "")+second);
			
			filelist.add(details);
		}
		
		return filelist;
	}
	
	
	private String generateFakeFileListing(String dir) {
		Random r = new Random();
		int numEntries = r.nextInt(20);
		
		String roh = "WLANSD_FILELIST\r\n";
		for (int i = 0; i < numEntries; i++) {
			roh += dir+",filename,"+String.valueOf(r.nextInt(Integer.MAX_VALUE))+","+String.valueOf(r.nextInt(32))+","+String.valueOf(r.nextInt(Integer.MAX_VALUE))+","+String.valueOf(r.nextInt(Integer.MAX_VALUE))+"\r\n";			
		}
		return roh;
	}
}
