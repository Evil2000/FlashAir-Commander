package de.evil2000.flashaircommander;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.Map;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;

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
		int count;
		try {
			if (urls.length > 0)
				httpData.url = urls[0];

			URL url = new URL(httpData.url);
			Log.d("HTTP", "Executing: " + httpData.url);
			HttpURLConnection conection = (HttpURLConnection) url.openConnection();
			conection.connect();
			
			// getting file length
			int lenghtOfFile = conection.getContentLength();
			
			Log.d("HTTP", "lenghtOfFile: " + lenghtOfFile);

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
	            }
	            outByte.write(buffer, 0, read);
	            publishProgress(read);
	        }

			if (outFile != null) {
				outFile.flush();
				outFile.close();
			}
			input.close();

			if (conection.getContentType().equalsIgnoreCase("text/plain")) {
				return outByte.toString().trim();
			} else {
				returnByteArray(outByte.toByteArray());
			}

		} catch (Exception e) {
			Log.e("Error: ", e.getMessage());
		}

		/*try {
			if (urls.length > 0)
				httpData.url = urls[0];

			Log.d("Jsoup", "Executing: " + httpData.url);

			Connection connection = Jsoup.connect(httpData.url);

			if (httpData.cookies != null)
				connection.cookies(httpData.cookies);
			if (httpData.payload != null)
				connection.data(httpData.payload);
			if (httpData.followRedirects != null)
				connection.followRedirects(httpData.followRedirects);
			if (httpData.method != null)
				connection.method(httpData.method);
			if (httpData.referrer != null)
				connection.referrer(httpData.referrer);
			if (httpData.timeout != null)
				connection.timeout(httpData.timeout);
			if (httpData.url != null)
				connection.url(httpData.url);
			if (httpData.userAgent != null)
				connection.userAgent(httpData.userAgent);
			// When fetching jpegs ignore content type and body size
			connection.ignoreContentType(true);
			connection.maxBodySize(0);

			publishProgress(1);
			Response response = connection.execute();

			Map<String, String> retCookies = response.cookies();
			if (!retCookies.equals(httpData.cookies))
				returnCookies(retCookies);
			Log.d("Jsoup", "Response: " + response.contentType() + " Size: " + response.header("Content-Length"));
			if (response.contentType().split(";")[0].equalsIgnoreCase("text/plain")) {
				return response.body();
			} else {
				returnByteArray(response.bodyAsBytes());
			}
			// response.headers();
			// response.statusCode();
			// response.statusMessage();
			// httpData.referrer = response.url().toString();
			
		} catch (Exception e) {
			e.printStackTrace();
			if (httpData.retries > 0) {
				httpData.retries--;
				Log.e("Jsoup", "Retries left: " + httpData.retries);
				return doInBackground(httpData.url);
			}
		}*/
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
