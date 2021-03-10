package net.mixednutz.app.server.controller.web;

import java.util.function.Supplier;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestParameterPropertyValues;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import net.mixednutz.api.model.IPost;
import net.mixednutz.app.server.controller.exception.ResourceNotFoundException;
import net.mixednutz.app.server.entity.ExternalFeeds.AbstractFeed;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.NewPostFactory;
import net.mixednutz.app.server.manager.ExternalFeedManager;
import net.mixednutz.app.server.repository.ExternalFeedRepository;

@Controller
public class ExternalContentController {

	@Autowired
	private ExternalFeedRepository externalFeedRepository;
	
	@Autowired
	private ExternalFeedManager externalFeedManager;
	
	protected AbstractFeed loadFeed(Long id) {
		return externalFeedRepository.findById(id)
			.orElseThrow(new Supplier<ResourceNotFoundException>() {
				@Override
				public ResourceNotFoundException get() {
					throw new ResourceNotFoundException("Feed not found");
				}
			});
	}
	
	@RequestMapping(value="/feed/post", method = RequestMethod.POST, params="submit")
	public String postNew(@ModelAttribute(ExternalContentComposeFormFactory.MODEL_ATTRIBUTE) ExternalContentComposeForm content,
			@RequestParam(value="externalFeedId") Long externalFeedId,
			@AuthenticationPrincipal User user, Model model, Errors errors,
			HttpServletRequest request
			) {
		
		AbstractFeed feed = loadFeed(externalFeedId);
		
		IPost post = externalFeedManager.instantiatePost(feed)
			.orElseThrow(() -> new IllegalArgumentException("Feed doesn't support posting"));
		
		//Copy Request into post
		final WebDataBinder binder = new WebDataBinder(post);
		ServletRequestParameterPropertyValues values = new ServletRequestParameterPropertyValues(request);
		binder.bind(values);
		
		externalFeedManager.post(feed, post);
		
		return "redirect:/main#"+externalFeedId;
	}
	
	public static class ExternalContentComposeForm {
		String composeBody;

		public String getComposeBody() {
			return composeBody;
		}

		public void setComposeBody(String composeBody) {
			this.composeBody = composeBody;
		}
	}
	
	
	@Component
	public static class ExternalContentComposeFormFactory implements NewPostFactory<ExternalContentComposeForm> {

		public static final String MODEL_ATTRIBUTE = "newExternalPost";
		
		@Override
		public ExternalContentComposeForm newPostForm(Model model, User owner) {
			ExternalContentComposeForm externalContent = new ExternalContentComposeForm();
			model.addAttribute(MODEL_ATTRIBUTE, externalContent);
			return externalContent;
		}
		
	}
	
}
