package de.evil2000.flashaircommander;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import de.evil2000.flashaircommander.FileListAdapter.EditAdapterCallback;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

@SuppressWarnings("deprecation")
public class FlashAirCommander extends ActionBarActivity implements EditAdapterCallback {
	public static final String APP_NAME = "FlashAirCommander";
	private HashMap<String, String> dirUp = new HashMap<String, String>();
	private FileListAdapter fileListAdapter;
	private SharedPreferences sharedPrefs;
	private ListView lstView;
	private ProgressDialog prgDlg;
	private boolean downloadCanceled = false;
	public ArrayList<String> filesMarkedForDownload;
	public int filesMarkedForDownloadSize = 0;
	
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

		lstView = (ListView) findViewById(R.id.lstView);
		lstView.setEmptyView(findViewById(R.id.prgLoading));

		ArrayList<HashMap<String, String>> files = new ArrayList<HashMap<String, String>>();
		filesMarkedForDownload = new ArrayList<String>();

		fileListAdapter = new FileListAdapter(this, files);
		fileListAdapter.setNotifyOnChange(true);
		fileListAdapter.setEditAdapterCallback(this);
		lstView.setAdapter(fileListAdapter);
		lstView.setOnItemClickListener(onListItemClick());

		// Get the shared preferences from the Settings class by using PreferenceManager.
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		updateFileListing(null);
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
		if (id == R.id.menu_settings) {
			Intent intent = new Intent(this, Settings.class);
			startActivity(intent);
			return true;
		} else if (id == R.id.menu_download_selected_files) {
			prgDlg = new ProgressDialog(this);
			prgDlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			prgDlg.setTitle("Downloading files");
			prgDlg.setMessage("Please wait");
			prgDlg.setCancelable(true);
			prgDlg.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface arg0) {
					downloadCanceled = true;
				}
			});
			prgDlg.setIndeterminate(false);
			prgDlg.setProgress(0);
			prgDlg.setMax(filesMarkedForDownloadSize);
			prgDlg.show();
			downloadAndStoreSelectedFiles();
			return true;
		} else if (id == R.id.menu_select_all) {
			int c = fileListAdapter.getCount();
			for (int i = 0; i < c; i++) {
				HashMap<String, String> e = fileListAdapter.getItem(i);
				if (e.get("filename").equals(dirUp.get("filename")))
					continue;
				addFileToDownloadList(e.get("directory") + "/" + e.get("filename"), Long.valueOf(e.get("size")));
			}
			fileListAdapter.notifyDataSetChanged();
			return true;
		} else if (id == R.id.menu_deselect_all) {
			int c = fileListAdapter.getCount();
			for (int i = 0; i < c; i++) {
				HashMap<String, String> e = fileListAdapter.getItem(i);
				removeFileFromDownloadList(e.get("directory") + "/" + e.get("filename"), Long.valueOf(e.get("size")));
			}
			fileListAdapter.notifyDataSetChanged();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Returns the OnItemClickListener function.
	 * 
	 * @return
	 */
	private AdapterView.OnItemClickListener onListItemClick() {
		return new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> aview, View view, int position, long arg3) {
				HashMap<String, String> entry = fileListAdapter.getItem(position);
				if (entry.get("attributes").contains("d")) {
					// User selected a directory
					if (entry.get("filename").equals("..")) {
						// Go up one dir if user selected ".."
						updateFileListing(entry.get("directory").substring(0, entry.get("directory").lastIndexOf("/")));
					} else {
						// Dive into selected directory
						updateFileListing(entry.get("directory") + "/" + entry.get("filename"));
					}
				} else {
					// User selected a file
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
		if (dir == null)
			dir = "/";
		if (dir.equals(""))
			dir = "/";
		final String d = dir;
		
		// Create callback for httpTask
		OnTask callback = new OnTask() {
			@Override
			public void onTaskCompleted(String doc) {
				if (doc == null) {
					findViewById(R.id.prgLoading).setVisibility(View.GONE);
					lstView.setEmptyView(findViewById(R.id.lblError));
					showDialog(getString(R.string.dialog_title_error), getString(R.string.dialog_msg_error_connecting_to_flashair));
					return;
				}
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

		HttpData httpData = new HttpData();
		httpData.retries = 5;
		httpData.timeout = 5000;
		NetworkTask nt = new NetworkTask(httpData, callback);
		nt.execute("http://" + sharedPrefs.getString("flashAirHostname", "flashair") + "/command.cgi?op=100&DIR=" + dir);

		// callback.onTaskCompleted(generateFakeFileListing(dir));
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
			if (entry.get("attributes").contains("d"))
				continue;
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
				nt.execute("http://" + sharedPrefs.getString("flashAirHostname", "flashair") + "/thumbnail.cgi?" + path);
			}
		}
	}

	private void downloadAndStoreSelectedFiles() {
		if (filesMarkedForDownload.size() == 0 || downloadCanceled) {
			downloadCanceled = false;
			prgDlg.dismiss();
			fileListAdapter.notifyDataSetChanged();
			return;
		}
		
		HttpData httpData = new HttpData();
		httpData.retries = 0;

		final String fqFilename = filesMarkedForDownload.get(0);
		/*if (!fqFilename.startsWith("/")) {
			fqFilename = "/" + fqFilename;
		}*/

		final String prgMessage = fqFilename;

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				prgDlg.setMessage(prgMessage);
			}
		});

		String storageDir;
		if (sharedPrefs.getBoolean("dontStoreImagesInAlbumDir", false))
			storageDir = sharedPrefs.getString("alternativeImageStoreDirectory", getString(R.string.pref_alternative_image_store_directory_default_value));
		else
			storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();
		
		File storeFile = new File(storageDir + fqFilename);
		storeFile.getParentFile().mkdirs();
		httpData.writeInFile = storeFile.getAbsolutePath();

		OnTask callback = new OnTask() {
			@Override
			public void onProgress(Integer count) {
				prgDlg.incrementProgressBy(count);
			}
			
			@Override
			public void onTaskCompleted(byte[] img) {
				
				filesMarkedForDownload.remove(0);
				
				if (sharedPrefs.getBoolean("deleteFilesAfterDownload", true)) {
					HttpData httpData = new HttpData();
					httpData.retries = 0;
					NetworkTask nt = new NetworkTask(httpData, null);
					nt.execute("http://" + sharedPrefs.getString("flashAirHostname", "flashair") + "/upload.cgi?DEL=" + fqFilename);
				}
				
				downloadAndStoreSelectedFiles();

			}
		};

		NetworkTask nt = new NetworkTask(httpData, callback);
		if (!storeFile.exists() || sharedPrefs.getBoolean("overwriteTargetFiles", true)) {
			nt.execute("http://" + sharedPrefs.getString("flashAirHostname", "flashair") + fqFilename);
		} else {
			filesMarkedForDownload.remove(0);
			downloadAndStoreSelectedFiles();
		}
	}

	/**
	 * Parse the file listing returned from the FlashAir API according to https://flashair-developers.com/en/documents/api/commandcgi/#100
	 * 
	 * @param raw
	 * @return
	 */
	private ArrayList<HashMap<String, String>> parseFileListing(String raw) {
		Log.d("parseFileListing", raw);
		
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

	public void showDialog(String title, String msg) {
		showDialog(title, msg, 0, null, null, null);
	}

	public void showDialog(String title, String msg, int icon) {
		showDialog(title, msg, icon, null, null, null);
	}

	public void showDialog(String title, String msg, int icon, DialogInterface.OnClickListener yesHandler, DialogInterface.OnClickListener noHandler,
			DialogInterface.OnClickListener okHandler) {
		Builder a = new AlertDialog.Builder(this);

		/*
		 * new DialogInterface.OnClickListener() { public void onClick(DialogInterface dialog, int id) { // FIRE ZE MISSILES! } }
		 */

		a.setTitle(title);
		a.setMessage(msg);
		if (yesHandler != null)
			a.setPositiveButton(android.R.string.yes, yesHandler);
		if (noHandler != null)
			a.setNegativeButton(android.R.string.no, noHandler);
		if (yesHandler == null && noHandler == null)
			a.setNeutralButton(android.R.string.ok, okHandler);
		if (icon > 0)
			a.setIcon(icon);
		else
			a.setIcon(android.R.drawable.ic_dialog_alert);
		a.show();
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
			roh += dir + "," + (new BigInteger(130, r).toString(32)) + "," + String.valueOf(r.nextInt(Integer.MAX_VALUE)) + "," + String.valueOf(r.nextInt(32))
					+ "," + String.valueOf(r.nextInt(Integer.MAX_VALUE)) + "," + String.valueOf(r.nextInt(Integer.MAX_VALUE)) + "\r\n";
		}
		return roh;
	}

	@Override
	public void addFileToDownloadList(String filename, Long size) {
		if (!filesMarkedForDownload.contains(filename)) {
			filesMarkedForDownload.add(filename);
			filesMarkedForDownloadSize += size;
		}
	}

	@Override
	public boolean isFileInDownloadList(String filename) {
		return filesMarkedForDownload.contains(filename);
	}

	@Override
	public void removeFileFromDownloadList(String filename, Long size) {
		if (filesMarkedForDownload.contains(filename)) {
			filesMarkedForDownload.remove(filename);
			filesMarkedForDownloadSize += size;
		}
	}
}
