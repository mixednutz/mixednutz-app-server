package net.mixednutz.app.server.manager.post.journal.impl;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import net.mixednutz.app.server.entity.InternalTimelineElement;
import net.mixednutz.app.server.entity.Oembeds.Oembed;
import net.mixednutz.app.server.entity.post.journal.JournalComment;
import net.mixednutz.app.server.manager.post.impl.AbstractPostCommentEntityConverter;

@Component
public class JournalCommentEntityConverter extends AbstractPostCommentEntityConverter<JournalComment> {
	
	@Override
	public Oembed toOembed(String path, Integer maxwidth, Integer maxheight, String format, Authentication auth,
			String baseUrl) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canConvertOembed(String path) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void populatePostProperties(InternalTimelineElement api, JournalComment entity) {
		api.setInReplyToTitle(entity.getJournal().getSubject());
	}
	
	@Override
	public boolean canConvert(Class<?> entityClazz) {
		return JournalComment.class.isAssignableFrom(entityClazz);
	}

}
