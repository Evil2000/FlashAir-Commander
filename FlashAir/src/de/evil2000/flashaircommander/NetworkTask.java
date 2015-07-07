package de.evil2000.flashaircommander;

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
		this.callback = callback;
	}

	@Override
	protected String doInBackground(String... urls) {
		try {
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
			// When fetching jpegs ignore content type
			connection.ignoreContentType(true);

			publishProgress(1);
			Response response = connection.execute();

			Map<String, String> retCookies = response.cookies();
			if (!retCookies.equals(httpData.cookies))
				returnCookies(retCookies);
			Log.d("Jsoup", "Response: " + response.contentType());
			if (response.contentType().split(";")[0].equalsIgnoreCase("text/plain"))
				return response.body();
			else 
				returnByteArray(response.bodyAsBytes());
			// response.headers();
			// response.statusCode();
			// response.statusMessage();
			// httpData.referrer = response.url().toString();
		/*} catch (SocketTimeoutException e) {
			Log.e("Jsoup", "ERROR: Socket timeout while trying to connect to" + httpData.url);
			e.printStackTrace();*/
		} catch (Exception e) {
			e.printStackTrace();
			if (httpData.retries > 0) {
				httpData.retries--;
				Log.e("Jsoup", "Retries left: " + httpData.retries);
				return doInBackground(httpData.url);
			}
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
