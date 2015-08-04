package de.evil2000.flashaircommander;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import android.os.AsyncTask;
import android.util.Log;

public class NetworkTask extends AsyncTask<String, Integer, String> {
	private HttpData httpData;
	private OnTask callback;

	public NetworkTask(HttpData httpData, OnTask callback) {
		this.httpData = httpData;
		this.callback = callback != null ? callback : new OnTask();
	}

	@Override
	protected String doInBackground(String... urls) {
		try {
			if (urls.length > 0)
				httpData.url = urls[0];

			URL url = new URL(httpData.url);
			Log.d("HTTP", "Executing: " + httpData.url);
			HttpURLConnection conection = (HttpURLConnection) url.openConnection();
			conection.connect();
			
			// getting file length
			int lengthOfFile = conection.getContentLength();
			
			Log.d("HTTP", "lengthOfFile: " + lengthOfFile);

			// input stream to read file - with 8k buffer
			InputStream input = new BufferedInputStream(conection.getInputStream());

			FileOutputStream outFile = null;
			ByteArrayOutputStream outByte = new ByteArrayOutputStream();
			if (httpData.writeInFile != null && !httpData.writeInFile.equals("")) {
				// Output stream to write file
				outFile = new FileOutputStream(httpData.writeInFile);
			}
			
			byte buffer[] = new byte[0x20000]; // ~130K.
			
	        int read;
	        while (true) {
	            read = input.read(buffer);
	            if (read == -1) break;
	            if (outFile != null) {
	            	outFile.write(buffer, 0, read);
	            } else {
	            	outByte.write(buffer, 0, read);
	            }
	            publishProgress(read);
	        }

	        input.close();
			if (outFile != null) {
				outFile.flush();
				outFile.close();
				returnByteArray(outByte.toByteArray());
				outByte.close();
				return null;
			}

			if (conection.getContentType().equalsIgnoreCase("text/plain")) {
				return outByte.toString().trim();
			} else {
				returnByteArray(outByte.toByteArray());
			}

		} catch (Exception e) {
			Log.e("Error: ", e.getMessage());
		}
		return null;
	}

	protected void returnByteArray(final byte[] b) {
		callback.post(new Runnable() {
			@Override
			public void run() {
				callback.onTaskCompleted(b);
			}
		});
	}

	protected void returnCookies(final Map<String, String> cookies) {
		callback.post(new Runnable() {
			@Override
			public void run() {
				callback.onCookiesChanged(cookies);
			}
		});
	}

	protected void onProgressUpdate(Integer... progress) {
		callback.onProgress(progress[0]);
	}

	protected void onPostExecute(String result) {
		callback.onTaskCompleted(result);
	}

}
