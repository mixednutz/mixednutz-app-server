package net.mixednutz.app.server.manager.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
import net.mixednutz.app.server.entity.InternalTimelineElement;
import net.mixednutz.app.server.entity.InternalTimelineElement.Type;
import net.mixednutz.app.server.entity.NetworkInfo;
import net.mixednutz.app.server.entity.ReactionScore;
import net.mixednutz.app.server.entity.TagScore;
import net.mixednutz.app.server.entity.User;
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
	private TagManager tagManager;
	
	@Autowired
	private ReactionManager reactionManager;
	
	private static final String APPLICATION_JSON_OEMBED = "application/json+oembed";
	
	@Override
	public IUser toUser(User entity) {
		return new UserWrapper(entity, networkInfo);
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
	
	public static class UserWrapper implements IUser {

		final private User user;
		final private NetworkInfo networkInfo;
		
		public UserWrapper(User user, NetworkInfo networkInfo) {
			super();
			this.user = user;
			this.networkInfo = networkInfo;
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
			return new Image(user.getAvatarSrc(), user.getUsername()+"'s avatar");
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
			// TODO Auto-generated method stub
			return null;
		}
		
	}


}
