package net.mixednutz.app.server.format;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import net.mixednutz.app.server.format.FormattingUtils;

public class FormattingUtilsImpl implements FormattingUtils {
	
	HttpServletRequest request;
	
	public FormattingUtilsImpl(HttpServletRequest request) {
		super();
		this.request = request;
	}

	/**
	 * Returns an absolute URL based on a relative one.
	 * 
	 * @param relativeUrl
	 * @return
	 */
	public String formatAbsoluteUrl(String relativeUrl) {
		return this.formatAbsoluteUrl(relativeUrl, true);
	}
	
	public String formatAbsoluteUrl(String relativeUrl, boolean includeContextPath) {
		try {
			int port = request.getServerPort();
			if (request.getScheme().equals("http") && port == 80) {
			    port = -1;
			} else if (request.getScheme().equals("https") && port == 443) {
			    port = -1;
			}
			return new URL(request.getScheme(), 
					request.getServerName(), 
					port, 
					(includeContextPath?request.getContextPath():"")+relativeUrl).toExternalForm();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public String formatDateTodayYesterday(Date date, String datePattern) {
		SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
		Date now = new Date();
		String dateStr = sdf.format(date);
		if (dateStr.equals(sdf.format(now))) {
			return "<span class=\"today\">Today</span>";
		} 
		Date yesterday = new Date(System.currentTimeMillis()-(1000*60*60*24));
		if (dateStr.equals(sdf.format(yesterday))) {
			return "Yesterday";
		} 
		return dateStr;
	}
	
	public String formatDateTimeTodayYesterday(Date date, String datePattern, String timePattern) {
		SimpleDateFormat sdf = new SimpleDateFormat(timePattern);
		StringBuffer buffer = new StringBuffer();
		buffer.append(this.formatDateTodayYesterday(date, datePattern));
		buffer.append(" at ");
		buffer.append(sdf.format(date));
		return buffer.toString();
	}
	
	public String formatTimeSince(long timestamp) {
		long now = System.currentTimeMillis();
		long diff = now - timestamp;
		long secs = diff / 1000;
		if (secs < 60) {
			return String.valueOf(secs)+"s";
		}
		long mins = secs / 60;
		if (mins < (60*1.5)) {
			int secsRemaining = (int)secs % 60;
			return String.valueOf(mins)+"m "+String.valueOf(secsRemaining)+"s";
		}
		if (mins < (60*2)) {
			return String.valueOf(mins)+"m";
		}
		long hours = mins / 60;
		if (hours < (24*1.5)) {
			int minsRemaining = (int)mins % 60;
			return String.valueOf(hours)+"h "+String.valueOf(minsRemaining)+"m";
		}
		if (hours < (24*2)) {
			return String.valueOf(hours)+"h";
		}
		int days = (int)hours / 24;
		if (days < (365*1.5)) {
			int hoursRemaining = (int)hours % 24;
			return String.valueOf(days)+"d "+String.valueOf(hoursRemaining)+"h";
		}
		if (days < (365*2)) {
			return String.valueOf(days)+"d";
		}
		int years = days / 365;
		return String.valueOf(years)+"y";
	}
	
	public String removeSpaces(String s) {
		return s.replaceAll(" ", "");
	}

}
