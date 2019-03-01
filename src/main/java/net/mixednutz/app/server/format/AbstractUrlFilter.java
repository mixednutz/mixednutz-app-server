package net.mixednutz.app.server.format;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractUrlFilter implements HtmlFilter {

	private static final Pattern URL_PATTERN=Pattern.compile(
			".?(\\A|<div>|<br ?>|<br ?\\/>|[\\r\\n])"
			+ "(?<url>https?:\\/\\/[\\w\\d:#@%\\/;$()~?\\\\+-=.\\/]*)"
			+ "(</div>|<br>|<br\\/>|[\\r\\n]|\\Z).?",
			Pattern.CASE_INSENSITIVE);
	private static final String URL_GROUP_NAME = "url";
	
	protected final List<UrlEntity> findUrls(String html) {

		Matcher matcher = URL_PATTERN.matcher(html);

		List<UrlEntity> entities = new ArrayList<UrlEntity>();
		
		while (matcher.find()) {
			entities.add(new UrlEntity(matcher.group(URL_GROUP_NAME),
					matcher.start(URL_GROUP_NAME), matcher.end(URL_GROUP_NAME)));
		}

		return entities;
	}
	
	static class UrlEntity {
		String text;
		int start;
		int end;
		public UrlEntity(String text, int start, int end) {
			super();
			this.text = text;
			this.start = start;
			this.end = end;
		}
	}
}
