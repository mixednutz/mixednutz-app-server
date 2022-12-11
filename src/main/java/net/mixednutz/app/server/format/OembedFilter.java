package net.mixednutz.app.server.format;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.mixednutz.app.server.manager.ExternalContentManager;

@Component
public class OembedFilter extends AbstractUrlFilter {
		
	protected final ExternalContentManager oembedFilterAllowlistManager;
	
	@Autowired
	public OembedFilter(ExternalContentManager oembedFilterWhitelistManager) {
		super();
		this.oembedFilterAllowlistManager = oembedFilterWhitelistManager;
	}


	@Override
	public String filter(String html) {
		List<UrlEntity> oembeds = lookupOembeds(findUrls(html));
		if (oembeds.isEmpty()) {
			return html;
		}
		StringBuffer newtext = new StringBuffer();
		int lastIdx = 0;
		for (UrlEntity entity: oembeds) {
			if (entity instanceof OembedEntity) {
				OembedEntity oEntity = (OembedEntity) entity;
				newtext.append(html.substring(lastIdx, entity.start))
					.append("<div class=\"oembed\" data-sourceType=\""+oEntity.type+"\" data-sourceId=\""+entity.text+"\">")
					.append(entity.text)
					.append("</div>");
			} else {
				newtext.append(html.substring(lastIdx, entity.start))
					.append("<a target=\"_blank\" rel=\"noopener noreferrer\" href=\""+entity.text+"\">")
					.append(entity.text)
					.append("</a>");
			}
  			lastIdx = entity.end;
		}
		if (lastIdx<html.length()) {
			newtext.append(html.substring(lastIdx, html.length()));	
  		}
		return newtext.toString();
	}
	
	
	protected List<UrlEntity> lookupOembeds(List<UrlEntity> urlEntities) {
		List<UrlEntity> entities = new ArrayList<>();
		for (UrlEntity urlEntity: urlEntities) {
			oembedFilterAllowlistManager.deriveSourceType(urlEntity.text)
				.ifPresentOrElse(
						sourceType->entities.add(new OembedEntity(urlEntity, sourceType)),
						()->entities.add(urlEntity));
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
