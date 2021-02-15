package net.mixednutz.app.server.manager.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.mixednutz.app.server.entity.TagScore;
import net.mixednutz.app.server.entity.TagsAware;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.AbstractTag;
import net.mixednutz.app.server.manager.TagManager;


@Transactional
@Service
public class TagManagerImpl implements TagManager{
	
	
	@Override
	public String[] splitTags(String tagString) {
		String step1 = tagString.replaceAll("^[,\\s]+", "");
		String[] step2 = step1.split("[,\\s]+");
		if (step2.length==1 && step2[0].length()==0) {
			//spliting an empty string returns original empty string in an array.
			return new String[0];
		}
		return step2;
	}
	
	@Override
	public String[] getTagsArray(Collection<? extends AbstractTag> tags) {
		List<String> tagsList = new ArrayList<>();
		for (Iterator<? extends AbstractTag> it = tags.iterator(); it.hasNext();) {
			AbstractTag tag = it.next();
			tagsList.add(tag.getTag());
		}
		String[] array = new String[tags.size()];
		return tagsList.toArray(array);
	}

	public String getTagsString(Collection<? extends AbstractTag> tags) {
		StringBuffer tagsString = new StringBuffer();
		Collection<AbstractTag> filteredTags = new ArrayList<>();
		//Filter and only show own tags.
		for (AbstractTag tag: tags) {
			if (tag.getTaggerId()==null || tag.getTaggerId()==0){
				filteredTags.add(tag);
			}
		}
		for (Iterator<? extends AbstractTag> it = filteredTags.iterator(); it.hasNext();) {
			AbstractTag tag = it.next();
			tagsString.append(tag.getTag());
			if (it.hasNext()) {
				tagsString.append(",");
			}
		}
		return tagsString.toString();
	}
	
	public <T extends AbstractTag> void mergeTags(String[] tagArray, 
			Set<T> tags, NewTagCallback<T> callback) {
		
		Collection<AbstractTag> tagsToRemove = new ArrayList<>(tags);
		for (String tagString : tagArray) {
			AbstractTag existingTag = null;
			for (AbstractTag tag: tags) {
				if (tagString.equals(tag.getTag())  // Existing tag.  keep it.
						|| (tag.getTaggerId()!=null && tag.getTaggerId()!=0)) //This one isn't owned by the author
				{ 
					existingTag = tag;
					tagsToRemove.remove(existingTag);
					break;
				}
			}
			if (existingTag==null) {
				//New tag
				tags.add(callback.createTag(tagString));
			}
		}
		//Removed tags
		if (!tagsToRemove.isEmpty()) {
			tags.removeAll(tagsToRemove);
			//In order to delete the @OneToMany annotation needs orphanRemoval=true
			//delete(copy); 
		}
	}
	
	public <T extends AbstractTag> Collection<T> addTags(String[] tagArray, Set<T> tags, 
			User author, User currentUser, NewTagCallback<T> callback) {
		List<T> addedTags = new ArrayList<T>();
		for (String tagString : tagArray) {
			AbstractTag existingTag = null;
			for (AbstractTag tag: tags) {
				if (tagString.equals(tag.getTag())
						&& userOwnsTag(tag, author, currentUser)) {
					existingTag = tag;
					break;
				}
			}
			if (existingTag==null) {
				final T newtag = callback.createTag(tagString);
				tags.add(newtag);
				addedTags.add(newtag);
			}
		}
		return addedTags;
	}
	
	public <T extends AbstractTag> T toggleTag(String tagString, Set<T> tags, 
			User author, User currentUser, NewTagCallback<T> callback) {
		for (AbstractTag tag: tags) {
			if (tag.getTag().equals(tagString)
					&& userOwnsTag(tag, author, currentUser)) {
				tags.remove(tag);
				return null;
			}
		}
		T addedTag = callback.createTag(tagString);
		tags.add(addedTag);
		return addedTag;
	}
	
	public <T extends AbstractTag> List<TagScore> getTagScores(Set<T> tags, User author, User currentUser) {
		Map<String, TagScore> tagScores = new HashMap<String, TagScore>();
		for (T tag : tags) {
			if (!tagScores.containsKey(tag.getTag())) {
				tagScores.put(tag.getTag(), new TagScore(tag.getTag()));
			}
			TagScore tagScore = tagScores.get(tag.getTag());
			if (!tagScore.isUserIncluded()) {
				//This eliminates duplicate votes too!
				tagScore.setUserIncluded(userOwnsTag(tag, author, currentUser));
				tagScore.incrementScore();
			}
		}
		List<TagScore> list = new ArrayList<TagScore>(tagScores.values());
		Collections.sort(list);
		return list;
	}
	
	@Override
	public <P extends TagsAware<T>, T extends AbstractTag> List<TagScore> getTagScores(Set<P> posts) {
		Map<String, TagScore> tagScores = new HashMap<String, TagScore>();
		for (P post: posts) {
			for (T tag : post.getTags()) {
				if (!tagScores.containsKey(tag.getTag())) {
					tagScores.put(tag.getTag(), new TagScore(tag.getTag()));
				}
				TagScore tagScore = tagScores.get(tag.getTag());
				tagScore.incrementScore();
			}
		}
		List<TagScore> list = new ArrayList<TagScore>(tagScores.values());
		Collections.sort(list);
		return list;
	}
	
	/**
	 * If current user is the Author the taggerId must be equal 0 or null.  
	 * Else taggerId must be current user's id.
	 * @param tag
	 * @param author
	 * @param currentUser
	 * @return
	 */
	protected boolean userOwnsTag(AbstractTag tag, User author, User currentUser) {
		return (author!=null && author.equals(currentUser) && (tag.getTaggerId()==null || tag.getTaggerId().equals(0L))) ||
				(currentUser!=null && currentUser.getUserId().equals(tag.getTaggerId()));
	}
}
