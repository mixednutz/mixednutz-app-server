package net.mixednutz.app.server.manager.impl;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonInclude;

import net.mixednutz.api.core.model.Action;
import net.mixednutz.api.core.model.AlternateLink;
import net.mixednutz.api.core.model.Image;
import net.mixednutz.api.core.model.Link;
import net.mixednutz.api.core.model.ReactionCount;
import net.mixednutz.api.core.model.TagCount;
import net.mixednutz.api.model.IAction;
import net.mixednutz.api.model.IImage;
import net.mixednutz.api.model.IUser;
import net.mixednutz.api.model.IUserProfile;
import net.mixednutz.app.server.controller.BasePhotoController;
import net.mixednutz.app.server.controller.api.OembedController;
import net.mixednutz.app.server.entity.CommentsAware;
import net.mixednutz.app.server.entity.ExternalFeeds.Oauth1AuthenticatedFeed;
import net.mixednutz.app.server.entity.InternalTimelineElement;
import net.mixednutz.app.server.entity.ReactionScore;
import net.mixednutz.app.server.entity.ReactionsAware;
import net.mixednutz.app.server.entity.TagScore;
import net.mixednutz.app.server.entity.TagsAware;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.UserProfile;
import net.mixednutz.app.server.entity.post.Post;
import net.mixednutz.app.server.entity.post.PostComment;
import net.mixednutz.app.server.manager.ApiElementConverter;
import net.mixednutz.app.server.manager.ApiManager;
import net.mixednutz.app.server.manager.ReactionManager;
import net.mixednutz.app.server.manager.TagManager;

@Service
public class ApiManagerImpl implements ApiManager{
	
	@Autowired
	private HttpServletRequest request;
	
	@Autowired
	private TagManager tagManager;
	
	@Autowired
	private ReactionManager reactionManager;
	
	@Autowired
	private Collection<ApiElementConverter<?>> apiElementConverters;
	
	private static final String APPLICATION_JSON_OEMBED = "application/json+oembed";
	
	private static final String AVATARS_DIR = BasePhotoController.PHOTOS_STORAGE_DIR;
	private static final String AVATARS_QUERY = "size=avatar";
	private static final String DEFAULT_AVATAR_URI = "/img/nophoto.gif";
	private static final String OEMBED_DIR = OembedController.OEMBED_DIR;
	
	private static String getAvatarUri(String avatarFilename) {
		if (avatarFilename!=null && !"".equals(avatarFilename.trim())) {
			return AVATARS_DIR + "/"
					+ avatarFilename + "?"+ AVATARS_QUERY;
		}
		return DEFAULT_AVATAR_URI;
	}
	
	private static String getOembedUri(String url) {
		return OEMBED_DIR + "?url=" + url;
	}
	
	private String getBaseUrl() {
		try {
			URL baseUrl = new URL(
					request.getScheme(), 
					request.getServerName(), 
					request.getServerPort(), 
					"");
			return baseUrl.toExternalForm();
		} catch (MalformedURLException e) {
			throw new RuntimeException("Something's wrong with creating the baseUrl!", e);
		}
	}
	
	@Override
	public UserWrapper toUser(User entity) {
		if (entity!=null) {
			return new UserWrapper(entity);
		}
		return null;
		
	}
	
	public IUser toUser(User entity, UserProfile profile) {
		return toUser(entity).addProfile(profile);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <E> void copyWithApiElementConverters(InternalTimelineElement api, E entity, User viewer) {
		for (ApiElementConverter<?> converter: apiElementConverters) {
			if (converter.canConvert(entity.getClass())) {
				((ApiElementConverter)converter).toTimelineElement(api, entity, viewer);
			}
		}
	}
	
	@Override
	public <E> InternalTimelineElement toTimelineElement(E entity, User viewer) {
		InternalTimelineElement api = toTimelineElement((Post<?>)entity);
		if (entity instanceof CommentsAware) {
			CommentsAware<?> hasComments = (CommentsAware<?>)entity;
			setComments(api, hasComments.getComments());
		}
		if (entity instanceof TagsAware) {
			TagsAware<?> hasTags = (TagsAware<?>) entity;
			setTagCounts(api, tagManager.getTagScores(
					hasTags.getTags(), ((Post<?>)entity).getAuthor(), viewer));
		}
		if (entity instanceof ReactionsAware) {
			ReactionsAware<?> hasReactions = (ReactionsAware<?>) entity;
			setReactionCounts(api, reactionManager.getReactionScores(
					hasReactions.getReactions(), ((Post<?>)entity).getAuthor(), viewer));
		}
		this.copyWithApiElementConverters(api, entity, viewer);
		return api;
	}

	protected InternalTimelineElement toTimelineElement(Post<?> entity) {
		InternalTimelineElement api = new InternalTimelineElement();
		api.setUri(entity.getUri());
		api.setUrl(getBaseUrl()+entity.getUri());
		api.setPostedByUser(toUser(entity.getAuthor()));
		api.setPostedOnDate(entity.getDatePublished());
		api.setDescription(entity.getDescription());
		api.setAlternateLinks(new ArrayList<>());
		api.getAlternateLinks().add(new AlternateLink(
				getOembedUri(api.getUrl()), APPLICATION_JSON_OEMBED));
		return api;
	}
	
	protected InternalTimelineElement toTimelineElement(PostComment entity) {
		InternalTimelineElement api = new InternalTimelineElement();
		api.setUri(entity.getUri());
		api.setUrl(getBaseUrl()+entity.getUri());
		api.setPostedByUser(toUser(entity.getAuthor()));
		api.setPostedOnDate(entity.getDateCreated());
		api.setDescription(entity.getBody());
		api.setAlternateLinks(new ArrayList<>());
		api.getAlternateLinks().add(new AlternateLink(
				getOembedUri(api.getUrl()), APPLICATION_JSON_OEMBED));
		return api;
	}
	
	protected void setComments(InternalTimelineElement api, Iterable<? extends PostComment> comments) {
		List<InternalTimelineElement> apiComments = new ArrayList<>();
		for (PostComment comment : comments) {
			apiComments.add(toTimelineElement(comment));
		}
		api.setComments(apiComments);
	}
	
	protected void setTagCounts(InternalTimelineElement api, Iterable<TagScore> tagScores) {
		List<TagCount> tags = new ArrayList<>();
		for (TagScore tag : tagScores) {
			tags.add(toTagCount(tag, api.getUrl()));
		}
		api.setTags(tags);
	}
	
	protected void setReactionCounts(InternalTimelineElement api, Iterable<ReactionScore> reactionScores) {
		List<ReactionCount> reactions = new ArrayList<>();
		for (ReactionScore reaction : reactionScores) {
			reactions.add(toReactionCount(reaction, api.getUrl()));
		}
		api.setReactions(reactions);
	}
	
	protected TagCount toTagCount(TagScore tagScore, String baseUrl) {
		TagCount api = new TagCount();
		api.setName(tagScore.getTag());
		api.setDisplayName(tagScore.getTag());
		api.setCount(tagScore.getScore());
		api.setUserIncluded(tagScore.isUserIncluded());
		api.setToggleAction(new Action(
				new Link(baseUrl+"/tag/toggle?tag="+api.getName()), 
				"tag_"+api.getName(), 
				api.getName()));
		return api;
	}
	
	protected ReactionCount toReactionCount(ReactionScore reactionScore, String baseUrl) {
		ReactionCount api = new ReactionCount();
		api.setId(reactionScore.getEmoji().getId());
		api.setUnicode(reactionScore.getEmoji().getHtmlCode());
		api.setDescription(reactionScore.getEmoji().getDescription());
		api.setCount(reactionScore.getScore());
		api.setUserIncluded(reactionScore.isUserIncluded());
		api.setToggleAction(new Action(
				new Link(baseUrl+"/reaction/toggle?emojiId="+api.getId()),
				"emoji_"+api.getId(),
				api.getUnicode(), 
				api.getDescription()));
		return api;
	}
	
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public class UserWrapper implements IUser {

		final private User user;
		private UserProfileWrapper profile;
		
		public UserWrapper(User user) {
			super();
			this.user = user;
		}
		
		public UserWrapper addProfile(UserProfile profile) {
			if (profile!=null) {
				this.profile = new UserProfileWrapper(profile);
			}
			return this;
		}

		@Override
		public String getUsername() {
			return user.getUsername();
		}

		@Override
		public String getDisplayName() {
			return user.getDisplayName();
		}

		@Override
		public IImage getAvatar() {
			return new Image(getBaseUrl()+getAvatarUri(user.getAvatarFilename()), user.getUsername()+"'s avatar");
		}

		@Override
		public boolean isPrivate() {
			return user.isPrivate();
		}

		@Override
		public Serializable getProviderId() {
			return user.getProviderId();
		}

		@Override
		public String getUri() {
			return "/"+user.getUsername();
		}

		@Override
		public String getUrl() {
			return getBaseUrl()+getUri();
		}

		@Override
		public List<? extends IAction> getActions() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public IUserProfile getProfileData() {
			return profile;
		}

		public LocalDate getMemberSince() {
			return user.getMemberSince().toLocalDate();
		}
		
	}
	
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class UserProfileWrapper extends UserProfile implements IUserProfile {
		
		final private UserProfile userProfile;

		public UserProfileWrapper(UserProfile userProfile) {
			super();
			this.userProfile = userProfile;
		}

		public String getLocation() {
			return userProfile.getLocation();
		}

		public String getBio() {
			return userProfile.getBio();
		}

		public String getPronouns() {
			return userProfile.getPronouns();
		}

		public String getWebsite() {
			return userProfile.getWebsite();
		}

		public Oauth1AuthenticatedFeed getTwitterAccount() {
			return userProfile.getTwitterAccount();
		}
				
	}

}
