package net.mixednutz.app.server.manager.post.discord;

import java.util.Collections;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import net.mixednutz.app.server.entity.ComponentSettings;

@Component
@ConditionalOnClass(name="net.mixednutz.api.DiscordConfig")
public class DiscordComponentSettings implements ComponentSettings {

	@Override
	public boolean includeNewFormModal() {
		return true;
	}
	
	@Override
	public String includeNewFormModalContentFragmentName() {
		return "discord/fragments_discord :: discordCompose_form";
	}
	
	@Override
	public String newFormModalId() {
		return "discordCompose";
	}
	
	
	@Override
	public Map<String, ?> getSettings() {
		return Collections.emptyMap();
	}

	@Override
	public boolean includeTimelineTemplateHtmlFragment() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String includeTimelineTemplateHtmlFragmentName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean includeHtmlFragment() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String includeHtmlFragmentName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean includeTimelineTemplateScriptFragment() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String includeTimelineTemplateScriptFragmentName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean includeScriptFragment() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String includeScriptFragmentName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean css() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String cssHref() {
		// TODO Auto-generated method stub
		return null;
	}
}
