package net.mixednutz.app.server.manager.post.journal.impl;

import java.time.LocalDate;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import net.mixednutz.api.core.model.NetworkInfo;
import net.mixednutz.app.server.entity.InternalTimelineElement;
import net.mixednutz.app.server.entity.InternalTimelineElement.Type;
import net.mixednutz.app.server.entity.Oembeds.Oembed;
import net.mixednutz.app.server.entity.Oembeds.OembedRich;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.journal.Journal;
import net.mixednutz.app.server.manager.ApiElementConverter;
import net.mixednutz.app.server.repository.JournalRepository;
import net.mixednutz.app.server.repository.UserRepository;

@Component
public class JournalEntityConverter implements ApiElementConverter<Journal> {
	
	private static final Pattern JOURNAL_PATTERN_REST=Pattern.compile(
			"^\\/(?<username>.*)\\/journal\\/(?<year>[0-9]*)\\/(?<month>[0-9]*)\\/(?<day>[0-9]*)\\/(?<subjectKey>.*)", 
			Pattern.CASE_INSENSITIVE);
	
	private static final String EMBED_BASE_URL = "/embed";
	
	@Autowired
	private NetworkInfo networkInfo;
	
	@Autowired
	private JournalRepository journalRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Override
	public InternalTimelineElement toTimelineElement(
			InternalTimelineElement api, Journal entity, User viewer) {
		api.setType(new Type("Journal",
				networkInfo.getHostName(),
				networkInfo.getId()+"_Journal"));
		api.setId(entity.getId());
		api.setTitle(entity.getSubject());
		return api;
	}

	@Override
	public boolean canConvert(Class<?> entityClazz) {
		return Journal.class.isAssignableFrom(entityClazz);
	}

	@Override
	public Oembed toOembed(String path, Integer maxwidth, Integer maxheight, String format, Authentication auth) {
		Matcher matcher = JOURNAL_PATTERN_REST.matcher(path);
		if (matcher.matches()) {
			String username = matcher.group("username");
			Integer year = Integer.parseInt(matcher.group("year"));
			Integer month = Integer.parseInt(matcher.group("month"));
			Integer day = Integer.parseInt(matcher.group("day"));
			String subjectKey = matcher.group("subjectKey");
			Optional<Journal> journal = get(username, year, month, day, subjectKey);
			if (journal.isPresent()) {
				return toOembedRich(journal.get(), maxwidth, maxheight);
			}
		}
		return null;
	}

	@Override
	public boolean canConvertOembed(String path) {
		Matcher matcher = JOURNAL_PATTERN_REST.matcher(path);
		return matcher.matches();
	}
	
	private Optional<Journal> get(String username, int year, int month, int day, String subjectKey) {
		Optional<User> profileUser = userRepository.findByUsername(username);
		
		if (profileUser.isPresent()) {
			return journalRepository.findByOwnerAndPublishDateKeyAndSubjectKey(
					profileUser.get(), LocalDate.of(year, month, day), subjectKey);
		}
		return Optional.empty();
	}
	
	private OembedRich toOembedRich(Journal journal, 
			int maxwidth, int maxheight) {
		int height = (maxheight > 270 || maxheight <=0) ? 270 : maxheight;
		int width = (maxwidth > 658 || maxwidth <= 0) ? 658 : maxwidth;
				
		OembedRich rich = new OembedRich();
		rich.setTitle(journal.getSubject());
		rich.setAuthorName(journal.getAuthor().getUsername());
		rich.setWidth(width);
		rich.setHeight(height);
		StringBuffer html = new StringBuffer();
		html.append("<iframe");
		html.append(" height=\""+height+"\"");
		html.append(" src=\""+networkInfo.getHostName()+EMBED_BASE_URL+journal.getUri()+"\"");
		html.append(" style=\"max-width: "+width+"px; width: calc(100% - 2px);\"");
		html.append(" frameborder=\"0\"></iframe>");
		rich.setHtml(html.toString());
		return rich;
	}

}
