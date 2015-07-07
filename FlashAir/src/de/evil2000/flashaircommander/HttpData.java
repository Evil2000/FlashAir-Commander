package de.evil2000.flashaircommander;

import java.util.Collections;
import java.util.Map;

import org.jsoup.Connection.Method;

/**
 * @author dave
 * 
 */
public class HttpData {
	public String userAgent = "Mozilla/5.0 (X11; Linux x86_64; rv:31.0) Gecko/20100101 Firefox/31.0";
	public String referrer = "";
	public String url = "";

	public Integer timeout = 60000; // ms
	public Integer retries = 10;
	
	public Boolean followRedirects = true;

	public Map<String, String> cookies = Collections.emptyMap();
	public Map<String, String> payload = Collections.emptyMap();

	public Method method = Method.GET;

	/**
	 * 
	 */
	public HttpData() {

	}

	public HttpData(Boolean clear) {
		if (clear)
			this.clear();
	}

	public HttpData(String url) {
		this.clear();
		this.url = url;
	}

	public HttpData(String url, Map<String, String> payload) {
		this.clear();
		this.url = url;
		this.payload = payload;
	}

	public HttpData(String url, Map<String, String> payload, Map<String, String> cookies) {
		this.clear();
		this.url = url;
		this.payload = payload;
		this.cookies = cookies;
	}

	public HttpData clear() {
		userAgent = null;
		referrer = null;
		url = null;
		timeout = null;
		followRedirects = null;
		cookies = null;
		payload = null;
		method = null;
		return this;
	}

	/**
	 * @return the userAgent
	 */
	public String getUserAgent() {
		return userAgent;
	}

	/**
	 * @param userAgent
	 *            the userAgent to set
	 */
	public HttpData setUserAgent(String userAgent) {
		this.userAgent = userAgent;
		return this;
	}

	/**
	 * @return the referrer
	 */
	public String getReferrer() {
		return referrer;
	}

	/**
	 * @param referrer
	 *            the referrer to set
	 */
	public HttpData setReferrer(String referrer) {
		this.referrer = referrer;
		return this;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url
	 *            the url to set
	 */
	public HttpData setUrl(String url) {
		this.url = url;
		return this;
	}

	/**
	 * @return the timeout
	 */
	public Integer getTimeout() {
		return timeout;
	}

	/**
	 * @param timeout
	 *            the timeout to set
	 */
	public HttpData setTimeout(Integer timeout) {
		this.timeout = timeout;
		return this;
	}

	/**
	 * @return the followRedirects
	 */
	public Boolean getFollowRedirects() {
		return followRedirects;
	}

	/**
	 * @param followRedirects
	 *            the followRedirects to set
	 */
	public HttpData setFollowRedirects(Boolean followRedirects) {
		this.followRedirects = followRedirects;
		return this;
	}

	/**
	 * @return the cookies
	 */
	public Map<String, String> getCookies() {
		return cookies;
	}

	/**
	 * @param cookies
	 *            the cookies to set
	 */
	public HttpData setCookies(Map<String, String> cookies) {
		this.cookies = cookies;
		return this;
	}

	/**
	 * @return the payload
	 */
	public Map<String, String> getPayload() {
		return payload;
	}

	/**
	 * @param payload
	 *            the payload to set
	 */
	public HttpData setPayload(Map<String, String> payload) {
		this.payload = payload;
		return this;
	}

	/**
	 * @return the method
	 */
	public String getMethod() {
		return method.toString();
	}

	/**
	 * @param method
	 *            the method to set
	 */
	public HttpData setMethod(String method) {
		if (method.equalsIgnoreCase("GET"))
			this.method = Method.GET;
		else
			this.method = Method.POST;
		return this;
	}

	@Override
	public String toString() {
		return this.url;
	}

	/*

	@Override
	public int describeContents() {
		return hashCode();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(userAgent);
		dest.writeString(referrer);
		dest.writeString(url);
		dest.writeInt(timeout);
		dest.writeValue(followRedirects);
		dest.writeMap(cookies);
		dest.writeMap(payload);
		switch (method) {
		case GET:
			dest.writeString("GET");
			break;
		case POST:
			dest.writeString("POST");
			break;
		}
	}

	public HttpData(Parcel src) {
		userAgent = src.readString();
		referrer = src.readString();
		url = src.readString();
		timeout = src.readInt();
		followRedirects = (Boolean) src.readValue(Boolean.class.getClassLoader());
		cookies = src.readHashMap(Map.class.getClassLoader());
		payload = src.readHashMap(Map.class.getClassLoader());
		String m = src.readString(); 
		if (m.equalsIgnoreCase("GET"))
			method = Method.GET;
		else if (m.equalsIgnoreCase("POST"))
			method = Method.POST;
		else
			method = null;
	}

	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public HttpData createFromParcel(Parcel in) {
			return new HttpData(in);
		}

		public HttpData[] newArray(int size) {
			return new HttpData[size];
		}
	};
	
	*/
}