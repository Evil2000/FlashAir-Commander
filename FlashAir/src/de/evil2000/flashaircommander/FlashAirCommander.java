package de.evil2000.flashaircommander;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ProgressBar;

@SuppressWarnings("deprecation")
public class FlashAirCommander extends ActionBarActivity {
	private static final String APP_NAME = "FlashAirCommander";
	private static final String flashAirHost = "192.168.8.14";
	private FileListAdapter fileListAdapter;
	private HashMap<String, String> dirUp = new HashMap<String, String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_flash_air_commander);

		dirUp.put("directory", "/");
		dirUp.put("filename", "..");
		dirUp.put("size", "0");
		dirUp.put("attributes", "d");
		dirUp.put("date", "");
		dirUp.put("time", "");
		
		ProgressBar progressBar = new ProgressBar(this);
		progressBar.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER));
		progressBar.setIndeterminate(true);

		ListView lstView = (ListView) findViewById(R.id.lstView);

		lstView.setEmptyView(progressBar);
		ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
		root.addView(progressBar);

		ArrayList<HashMap<String, String>> files = new ArrayList<HashMap<String, String>>();

		fileListAdapter = new FileListAdapter(this, files);
		fileListAdapter.setNotifyOnChange(true);
		lstView.setAdapter(fileListAdapter);
		lstView.setOnItemClickListener(onListItemClick());
	}

	@Override
	protected void onStart() {
		super.onStart();
		updateFileListing("/");
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

	private AdapterView.OnItemClickListener onListItemClick() {
		return new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> aview, View view, int position, long arg3) {
				HashMap<String,String> entry = fileListAdapter.getItem(position);
				if (entry.get("attributes").contains("d")) {
					// User selected a directory
					if (entry.get("filename").equals("..")) {
						// Go up one dir if user selected ".."
						updateFileListing(entry.get("directory").substring(0, entry.get("directory").lastIndexOf("/")));
					} else {
						// Dive into selected directory
						updateFileListing(entry.get("directory") + "/" + entry.get("filename"));
					}
				}
			}

		};
	}

	/**
	 * Download the file listing from FlashAir via API, parse it and store the result in fileListAdapter. Also triggers the download of the thumbnails.
	 * 
	 * @param dir
	 */
	private void updateFileListing(String dir) {
		final String d = dir;
		// Create callback for httpTask
		OnTask callback = new OnTask() {
			@Override
			public void onTaskCompleted(String doc) {
				fileListAdapter.clear();
				if (!(d.equals("") || d.equals("/"))) {
					dirUp.put("directory", d);
					fileListAdapter.add(dirUp);
				}
				ArrayList<HashMap<String, String>> files = parseFileListing(doc);
				fileListAdapter.addAll(files);
				downloadAndCacheThumbnails();
			}
		};

		if (dir == null)
			dir = "/";
		if (dir.equals(""))
			dir = "/";

		HttpData httpData = new HttpData();
		NetworkTask nt = new NetworkTask(httpData, callback);
		nt.execute("http://" + flashAirHost + "/command.cgi?op=100&DIR=" + dir);
	}

	/**
	 * Download the thumbnails for each entry stored in the fileListAdapter and store it in the applications cache folder.
	 * 
	 */
	private void downloadAndCacheThumbnails() {
		HttpData httpData = new HttpData();
		httpData.retries = 0;
		int c = fileListAdapter.getCount();
		for (int i = 0; i < c; i++) {
			HashMap<String, String> entry = fileListAdapter.getItem(i);
			String directory = entry.get("directory");
			String filename = entry.get("filename");
			String path = directory + "/" + filename;
			final String cacheFilename = getCacheDir().getAbsolutePath() + File.separator + directory.replace("/", ".") + filename + entry.get("size");
			File cachedFile = new File(cacheFilename);

			OnTask callback = new OnTask() {
				@Override
				public void onTaskCompleted(byte[] img) {
					try {
						FileOutputStream fos = new FileOutputStream(cacheFilename);
						fos.write(img);
						fos.close();
						fileListAdapter.notifyDataSetChanged();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}

				}
			};
			NetworkTask nt = new NetworkTask(httpData, callback);
			if (!cachedFile.exists()) {
				nt.execute("http://" + flashAirHost + "/thumbnail.cgi?" + path);
			}
		}
	}

	/**
	 * Parse the file listing returned from the FlashAir API according to https://flashair-developers.com/en/documents/api/commandcgi/#100
	 * 
	 * @param raw
	 * @return
	 */
	private ArrayList<HashMap<String, String>> parseFileListing(String raw) {
		ArrayList<HashMap<String, String>> filelist = new ArrayList<HashMap<String, String>>();
		if (raw == null)
			return filelist;
		
		String[] lines = raw.split("\r\n");
		for (String line : lines) {
			if (line.equals("WLANSD_FILELIST"))
				continue;
			String[] parts = line.split(",");
			HashMap<String, String> details = new HashMap<String, String>();
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
			details.put("attributes", attribs);

			roh = Integer.parseInt(parts[4]);
			Integer year = ((roh & 32256) >> 9) + 1980;
			Integer month = (roh & 480) >> 5;
			Integer day = roh & 31;
			roh = Integer.parseInt(parts[5]);
			Integer hour = (roh & 63488) >> 11;
			Integer minute = (roh & 2016) >> 5;
			Integer second = (roh & 31) * 2;
			details.put("date", year + "-" + (month < 10 ? "0" : "") + month + "-" + (day < 10 ? "0" : "") + day);
			details.put("time", (hour < 10 ? "0" : "") + hour + ":" + (minute < 10 ? "0" : "") + minute + ":" + (second < 10 ? "0" : "") + second);

			filelist.add(details);
		}

		return filelist;
	}

	/**
	 * For debugging.
	 * 
	 * @param dir
	 * @return
	 */
	private String generateFakeFileListing(String dir) {
		Random r = new Random();
		int numEntries = r.nextInt(20);

		String roh = "WLANSD_FILELIST\r\n";
		for (int i = 0; i < numEntries; i++) {
			roh += dir + ",filename," + String.valueOf(r.nextInt(Integer.MAX_VALUE)) + "," + String.valueOf(r.nextInt(32)) + ","
					+ String.valueOf(r.nextInt(Integer.MAX_VALUE)) + "," + String.valueOf(r.nextInt(Integer.MAX_VALUE)) + "\r\n";
		}
		return roh;
	}
}
