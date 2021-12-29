package net.mixednutz.app.server.repository;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import net.mixednutz.app.server.entity.Emoji;
import net.mixednutz.app.server.entity.EmojiCategory;
import net.mixednutz.app.server.manager.EmojiManager;
import net.mixednutz.app.server.manager.impl.EmojiManagerImpl;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("jpa-dev")
//@DataJpaTest
@Tag("IntegrationTest")
@Import(EmojiManagerImpl.class)
@Disabled
public class EmojiCategoryRepositoryIntegrationTest {
	
	private static final Logger LOG = LoggerFactory.getLogger(EmojiCategoryRepositoryIntegrationTest.class);
	
	@Autowired
	EmojiManager emojiManager;
	
	@Autowired
	EmojiCategoryRepository emojiCategoryRepository;
	
	@PersistenceContext
	EntityManager em;
	
	@Test
	public void test() {
		Map<EmojiCategory, List<Emoji>> emojis = emojiManager.findOrganizeByCategory();
		
		for (Entry<EmojiCategory, List<Emoji>> entry: emojis.entrySet()) {
			LOG.info("{}: {}",entry.getKey().getId(), entry.getKey().getName());
			for (Emoji emoji: entry.getValue()) {
				LOG.info("\t"+emoji.getSortId()+" "+emoji.getHtmlCode()+" "+emoji.getDescription()+" "+emoji.getText());
			}
		}
		
	}

}
