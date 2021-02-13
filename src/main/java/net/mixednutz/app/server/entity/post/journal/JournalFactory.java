package net.mixednutz.app.server.entity.post.journal;

import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.NewCommentFactory;
import net.mixednutz.app.server.entity.post.NewPostFactory;

@Component
public class JournalFactory implements NewPostFactory<Journal>, NewCommentFactory<JournalComment> {

	public static final String MODEL_ATTRIBUTE = "newpost";
	public static final String MODEL_ATTRIBUTE_COMMENT = "newComment";
	
	@Override
	public Journal newPostForm(Model model, User owner) {
		final Journal journal = new Journal();
		model.addAttribute(MODEL_ATTRIBUTE, journal);
		journal.setOwnerId(owner!=null?owner.getUserId():null);
		return journal;
	}

	@Override
	public JournalComment newCommentForm(Model model) {
		final JournalComment comment = new JournalComment();
		model.addAttribute(MODEL_ATTRIBUTE_COMMENT, comment);
		return comment;
	}

}
