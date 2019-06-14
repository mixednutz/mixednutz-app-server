package net.mixednutz.app.server.manager.impl;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
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
import net.mixednutz.app.server.entity.ExternalFeeds.Oauth1AuthenticatedFeed;
import net.mixednutz.app.server.entity.InternalTimelineElement;
import net.mixednutz.app.server.entity.InternalTimelineElement.Type;
import net.mixednutz.app.server.entity.NetworkInfo;
import net.mixednutz.app.server.entity.ReactionScore;
import net.mixednutz.app.server.entity.TagScore;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.UserProfile;
import net.mixednutz.app.server.entity.post.Post;
import net.mixednutz.app.server.entity.post.journal.Journal;
import net.mixednutz.app.server.manager.ApiManager;
import net.mixednutz.app.server.manager.ReactionManager;
import net.mixednutz.app.server.manager.TagManager;

@Service
public class ApiManagerImpl implements ApiManager{

	@Autowired
	private NetworkInfo networkInfo;
	
	@Autowired
	private HttpServletRequest request;
	
	@Autowired
	private TagManager tagManager;
	
	@Autowired
	private ReactionManager reactionManager;
	
	private static final String APPLICATION_JSON_OEMBED = "application/json+oembed";
	
	private static final String AVATARS_DIR = BasePhotoController.PHOTOS_STORAGE_DIR;
	private static final String AVATARS_QUERY = "size=avatar";
	private static final String DEFAULT_AVATAR_URI = "/img/nophoto.gif";
	
	public static String getAvatarUri(String avatarFilename) {
		if (avatarFilename!=null && !"".equals(avatarFilename.trim())) {
			return AVATARS_DIR + "/"
					+ avatarFilename + "?"+ AVATARS_QUERY;
		}
		return DEFAULT_AVATAR_URI;
	}
	
	@Override
	public UserWrapper toUser(User entity) {
		networkInfo.init(request);
		return new UserWrapper(entity, networkInfo);
	}
	
	public IUser toUser(User entity, UserProfile profile) {
		return toUser(entity).addProfile(profile);
	}
	
	@Override
	public InternalTimelineElement toTimelineElement(Journal entity, User viewer) {
		InternalTimelineElement api = toTimelineElement((Post<?>)entity);
		api.setType(new Type("Journal",
				networkInfo.getHostName(),
				networkInfo.getId()+"_Journal"));
		api.setId(entity.getId());
		api.setTitle(entity.getSubject());
		setTagCounts(api, tagManager.getTagScores(
				entity.getTags(), entity.getAuthor(), viewer));
		setReactionCounts(api, reactionManager.getReactionScores(
				entity.getReactions(), entity.getAuthor(), viewer));
		return api;
	}

	protected InternalTimelineElement toTimelineElement(Post<?> entity) {
		networkInfo.init(request);
		
		InternalTimelineElement api = new InternalTimelineElement();
		api.setUri(entity.getUri());
		api.setUrl(networkInfo.getBaseUrl()+entity.getUri());
		api.setPostedByUser(toUser(entity.getAuthor()));
		api.setPostedOnDate(entity.getDateCreated());
		api.setDescription(entity.getDescription());
		api.setAlternateLinks(new ArrayList<>());
		api.getAlternateLinks().add(new AlternateLink(
				networkInfo.getOembedBaseUrl()+"?url="+api.getUrl(), APPLICATION_JSON_OEMBED));
		return api;
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
	public static class UserWrapper implements IUser {

		final private User user;
		final private NetworkInfo networkInfo;
		private UserProfileWrapper profile;
		
		public UserWrapper(User user, NetworkInfo networkInfo) {
			super();
			this.user = user;
			this.networkInfo = networkInfo;
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
			return new Image(networkInfo.getBaseUrl()+getAvatarUri(user.getAvatarFilename()), user.getUsername()+"'s avatar");
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
			return networkInfo.getBaseUrl()+getUri();
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
