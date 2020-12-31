package net.mixednutz.app.server.entity.post.journal;

import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.NewPostFactory;

@Component
public class JournalFactory implements NewPostFactory<Journal> {

	@Override
	public Journal newPostForm(Model model, User owner) {
		final Journal journal = new Journal();
		model.addAttribute("newpost", journal);
		journal.setOwnerId(owner!=null?owner.getUserId():null);
		return journal;
	}

}
