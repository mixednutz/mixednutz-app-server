package net.mixednutz.app.server.controller.rss;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.rometools.rome.feed.rss.Channel;
import com.rometools.rome.feed.rss.Description;
import com.rometools.rome.feed.rss.Item;

import net.mixednutz.api.core.model.PageRequest;
import net.mixednutz.api.model.IPage;
import net.mixednutz.api.model.IPageRequest.Direction;
import net.mixednutz.api.model.ITimelineElement;
import net.mixednutz.app.server.controller.exception.UserNotFoundException;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.format.FormattingUtils;
import net.mixednutz.app.server.format.FormattingUtilsImpl;
import net.mixednutz.app.server.manager.TimelineManager;
import net.mixednutz.app.server.repository.UserRepository;

@Controller
@RequestMapping({"/rss"})
public class RssTimelineController {
	
	public static final String USER_TIMELINE_ENDPOINT = 
			"/{username}";
	
	public static final String PAGE_SIZE_STR = "20";
	public static final int PAGE_SIZE = Integer.parseInt(PAGE_SIZE_STR);
	
	@Autowired
	private TimelineManager timelineManager;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
    private MessageSource messageSource;
	
	private MessageSourceAccessor accessor;

    @PostConstruct
    private void init() {
        accessor = new MessageSourceAccessor(messageSource, Locale.ENGLISH);
    }

	@RequestMapping(value={USER_TIMELINE_ENDPOINT}, 
			method = RequestMethod.GET)
	public @ResponseBody Channel getUserTimeline(
			@PathVariable String username,
			@RequestParam(value="pageSize", defaultValue=PAGE_SIZE_STR) int pageSize,
			@AuthenticationPrincipal User user, HttpServletRequest request) {
		
		return getUserTimeline(username, 
				PageRequest.first(pageSize, Direction.LESS_THAN, String.class),
				pageSize, user, request);
	}
	
	protected @ResponseBody Channel getUserTimeline(
			@PathVariable String username,
			@RequestBody PageRequest<String> prevPage, 
			@RequestParam(value="pageSize", defaultValue=PAGE_SIZE_STR) int pageSize,
			@AuthenticationPrincipal User user, HttpServletRequest request) {
		
		Optional<User> profileUser = userRepository.findByUsername(username);
		if (!profileUser.isPresent()) {
			throw new UserNotFoundException("User "+username+" not found");
		}
		
		//If pageSize is null, grab default
		if (prevPage.getPageSize()==null) {
			prevPage.setPageSize(pageSize);
		}
				
		//Get local data
		final IPage<? extends ITimelineElement,Instant> internalContent = 
				timelineManager.getUserTimeline(profileUser.get(), user, prevPage);
		
		return toChannel(request, internalContent, username);
	}
	
	protected Channel toChannel(HttpServletRequest request, IPage<? extends ITimelineElement,Instant> page, 
			String username) {
		FormattingUtils formatter = new FormattingUtilsImpl(request);
				
		Channel channel = new Channel();
        channel.setFeedType("rss_2.0");
        channel.setTitle(accessor.getMessage("site.title"));
        channel.setDescription(accessor.getMessage("rss.description"));
        channel.setLink(formatter.formatAbsoluteUrl("/rss/"+username));
        channel.setCopyright("Copyright "+ZonedDateTime.now().getYear()+" "+
        		accessor.getMessage("site.title"));
        channel.setLanguage("en");
//        channel.setWebMaster(webMaster);
        
        for (ITimelineElement element: page.getItems()) {
        	Item item = new Item();
            item.setAuthor(element.getPostedByUser().getUsername());
            item.setLink(element.getUrl());
            item.setTitle(element.getTitle());
            item.setPubDate(Date.from(element.getPostedOnDate().toInstant()));
            item.setComments(element.getUrl()+"#comments");
            item.setDescription(new Description());
            item.getDescription().setValue(element.getDescription());
            channel.getItems().add(item);
        }
        return channel;
	}
		
}
