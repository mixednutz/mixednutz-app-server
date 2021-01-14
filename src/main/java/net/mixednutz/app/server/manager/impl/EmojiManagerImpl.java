package net.mixednutz.app.server.manager.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import net.mixednutz.app.server.entity.Emoji;
import net.mixednutz.app.server.entity.EmojiCategory;
import net.mixednutz.app.server.entity.EmojiSubCategory;
import net.mixednutz.app.server.manager.EmojiManager;
import net.mixednutz.app.server.repository.EmojiCategoryRepository;
import net.mixednutz.app.server.repository.EmojiRepository;

@Service
@Transactional
public class EmojiManagerImpl implements EmojiManager {
	
	private static final Logger LOG = LoggerFactory.getLogger(EmojiManagerImpl.class);

	private static final String emojiDataUrl = "http://www.unicode.org/Public/emoji/1.0/emoji-data.txt";
	private static final String emojiOrderingUrl = "http://unicode.org/emoji/charts-13.0/emoji-ordering.txt";
	private static final String emojiTestUrl = "http://unicode.org/Public/emoji/13.0/emoji-test.txt";

	@Autowired
	protected EmojiRepository emojiRepository;
	
	@Autowired
	protected EmojiCategoryRepository emojiCategoryRepository;
	
	@PostConstruct
	public void init() {
		if (emojiRepository.count()==0) {
			load();
		}
	}
	
	@Cacheable("emoji")
	public Map<EmojiCategory, List<Emoji>> findOrganizeByCategory() {
		Map<EmojiCategory, List<Emoji>> map = new LinkedHashMap<EmojiCategory, List<Emoji>>();
		for (Emoji emoji: emojiRepository.findAllByOrderBySortId()) {
			EmojiCategory cat = emoji.getSubCategory().getParentCategory();
			if (!map.containsKey(cat)) {
				map.put(cat, new ArrayList<Emoji>());
			}
			map.get(cat).add(emoji);
		}
		return map;
	}

	protected Iterable<Emoji> readEmojiData() throws IOException {
		String response = restTemplate().getForObject(emojiDataUrl, String.class);
		ArrayList<Emoji> emojis = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new StringReader(response))) {
			String line;
			while((line = reader.readLine()) !=null) {
				if (!line.startsWith("#")) {
					UnicodeData unicode = new UnicodeData();
					String[] token = line.split(";");
					unicode.code = token[0].trim();
					unicode.defaultEmojiStyle = token[1].trim();
					unicode.emojiLevel = token[2].trim();
					unicode.emojiModifierStatus = token[3].trim();
					int hashIndex = token[4].indexOf('#');
					unicode.emojiSources = token[4].substring(0,hashIndex).trim();
					unicode.comment = token[4].substring(hashIndex).trim();
					emojis.add(unicode.toEmoji());
				}
			}
		}
		return emojis;
	}
	
	protected void readEmojiOrdering(Map<String, Emoji> emojis) throws IOException {
		String response = restTemplate().getForObject(emojiOrderingUrl, String.class);

		try (BufferedReader reader = new BufferedReader(new StringReader(response))) {
			String line;
			int lineNumber = 0;
			while((line = reader.readLine()) !=null) {
				if (!line.startsWith("#")) {
					lineNumber++;
					UnicodeOrdering unicode = new UnicodeOrdering();
					unicode.lineNumber = lineNumber;
					String[] token = line.split(";");
					unicode.code = token[0].trim();
					
					String hashKey=Emoji.getText(unicode.code);
					if (emojis.containsKey(hashKey)) {
						unicode.addToEmoji(emojis.get(hashKey));
					}
				}
			}
		}
	}
	
	protected Iterable<EmojiCategory> readEmojiTest(Map<String, Emoji> emojis) throws IOException {
		String response = restTemplate().getForObject(emojiTestUrl, String.class);
		ArrayList<EmojiCategory> categories = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new StringReader(response))) {
			String line;
			EmojiCategory category = null;
			EmojiSubCategory subcategory = null;
			int categoryId = 0;
			int subcategoryId = 0;
			while((line = reader.readLine()) !=null) {
				if (line.startsWith("# group:")) {
					String[] token = line.split(":");
					category = new EmojiCategory();
					category.setId(++categoryId);
					category.setName(token[1].trim());
					category.setSubCategories(new ArrayList<>());
					categories.add(category);
				} else if (line.startsWith("# subgroup:")) {
					String[] token = line.split(":");
					subcategory = new EmojiSubCategory();
					subcategory.setId(++subcategoryId);
					subcategory.setName(token[1].trim());
					subcategory.setParentCategory(category);
					subcategory.setEmoji(new ArrayList<>());
					category.getSubCategories().add(subcategory);
				} else if (!line.startsWith("#")) {
					String[] token = line.split(";");
					String hashKey=Emoji.getText(token[0].trim());
					if (emojis.containsKey(hashKey)) {
						Emoji emoji = emojis.get(hashKey);
						emoji.setSubCategory(subcategory);
						subcategory.getEmoji().add(emoji);
					}
				}
			}
		}
		return categories;
	}
	
	

	protected RestTemplate restTemplate() {
		return new RestTemplate();
	}
	
	public void load() {
		Map<String, Emoji> emojiMap = new HashMap<>();
		try {
			for (Emoji emoji: readEmojiData()) {
				emojiMap.put(emoji.getText(), emoji);
			}
			readEmojiOrdering(emojiMap);
			Iterable<EmojiCategory> categoriesToSave = readEmojiTest(emojiMap);
			if (LOG.isDebugEnabled()) {
				for (EmojiCategory category: categoriesToSave) {
					LOG.debug(category.getName());
					for (EmojiSubCategory subcategory: category.getSubCategories()) {
						LOG.debug("\t"+subcategory.getName());
						for (Emoji emoji: subcategory.getEmoji()) {
							LOG.debug("\t\t"+emoji.getSortId()+" "+emoji.getHtmlCode()+" "+emoji.getDescription()+" "+emoji.getText());
						}
					}
				}
			}
			
			emojiCategoryRepository.saveAll(categoriesToSave);
		} catch (IOException e) {
			throw new RuntimeException("Unable to load emjoi.", e);
		}
	}
	
	private class UnicodeOrdering {
		int lineNumber;
		String code;
		
		void addToEmoji(Emoji emoji) {
			emoji.setSortId(lineNumber);
		}
	}
	class UnicodeData {
		String code;
		String defaultEmojiStyle;
		String emojiLevel;
		String emojiModifierStatus;
		String emojiSources;
		String comment;
		
		Emoji toEmoji() {
			Emoji emoji = new Emoji();
			emoji.setId(code);
			int paren2Index = comment.indexOf(')');
			emoji.setDescription(comment.substring(paren2Index+1).trim().toLowerCase());
			return emoji;
		}
	}

}
