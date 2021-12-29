package net.mixednutz.app.server.manager.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import net.mixednutz.app.server.entity.Emoji;
import net.mixednutz.app.server.entity.EmojiCategory;
import net.mixednutz.app.server.entity.EmojiSubCategory;

public class EmojiManagerImplTest {

	@Test
	public void test() throws IOException {
		EmojiManagerImpl manager = new EmojiManagerImpl();
		Map<String, Emoji> emojis = new HashMap<>();
		for (Emoji emoji: manager.readEmojiData()) {
			System.out.println(emoji.getHtmlCode()+" "+emoji.getDescription()+" "+emoji.getText());
			emojis.put(emoji.getText(), emoji);
		}
		manager.readEmojiOrdering(emojis);
		for (Emoji emoji: emojis.values()) {
			System.out.println(emoji.getSortId()+" "+emoji.getHtmlCode()+" "+emoji.getDescription()+" "+emoji.getText());
		}
		for (EmojiCategory category: manager.readEmojiTest(emojis)) {
			System.out.println(category.getName());
			for (EmojiSubCategory subcategory: category.getSubCategories()) {
				System.out.println("\t"+subcategory.getName());
				for (Emoji emoji: subcategory.getEmoji()) {
					System.out.println("\t\t"+emoji.getSortId()+" "+emoji.getHtmlCode()+" "+emoji.getDescription()+" "+emoji.getText());
				}
			}
		}
	}
	
}
