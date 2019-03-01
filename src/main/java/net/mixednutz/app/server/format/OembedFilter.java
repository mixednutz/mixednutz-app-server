package net.mixednutz.app.server.format;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class OembedFilter extends AbstractUrlFilter {
		
//	protected ExternalContentManager externalContentManager;
//	
//	public OembedFilter(ExternalContentManager externalContentManager) {
//		super();
//		this.externalContentManager = externalContentManager;
//	}


	@Override
	public String filter(String html) {
		List<OembedEntity> oembeds = lookupOembeds(findUrls(html));
		if (oembeds.isEmpty()) {
			return html;
		}
		StringBuffer newtext = new StringBuffer();
		int lastIdx = 0;
		for (OembedEntity entity: oembeds) {
			newtext.append(html.substring(lastIdx, entity.start))
				.append("<div class=\"oembed\" data-sourceType=\""+entity.type+"\" data-sourceId=\""+entity.text+"\">")
				.append(entity.text)
				.append("</div>");
  			lastIdx = entity.end;
		}
		if (lastIdx<html.length()) {
			newtext.append(html.substring(lastIdx, html.length()));	
  		}
		return newtext.toString();
	}
	
	
	protected List<OembedEntity> lookupOembeds(List<UrlEntity> urlEntities) {
		List<OembedEntity> entities = new ArrayList<OembedEntity>();
		for (UrlEntity urlEntity: urlEntities) {
//			String sourceType = externalContentManager.deriveSourceType(urlEntity.text);
//			if (!"url".equals(sourceType)) {
//				entities.add(new OembedEntity(urlEntity, sourceType));
//			}
		}
		return entities;
	}
	
	static class OembedEntity extends UrlEntity {
		String type;

		public OembedEntity(UrlEntity urlEntity, String type) {
			super(urlEntity.text, urlEntity.start, urlEntity.end);
			this.type = type;
		}
	}



}
