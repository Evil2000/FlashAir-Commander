package de.evil2000.flashaircommander;

import java.util.Map;

import org.jsoup.nodes.Document;

import android.os.Handler;

/**
 * @author dave
 *
 */
public class OnTask extends Handler {
	public static final int MSG_PROGRESS = 0;
	public static final int MSG_COMPLETE = 1;
	
	void onTaskCompleted(String doc) {
	}
	void onTaskCompleted(byte[] respBytes) {
	}
	void onProgress(Integer progress) {
	}
	void onCookiesChanged(Map<String, String> cks) {
	}
}
