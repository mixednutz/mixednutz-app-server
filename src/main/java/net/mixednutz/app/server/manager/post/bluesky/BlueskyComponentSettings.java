package net.mixednutz.app.server.manager.post.bluesky;

import java.util.Collections;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import net.mixednutz.app.server.entity.ComponentSettings;

@Component
@ConditionalOnClass(name="net.mixednutz.api.BlueskyConfig")
public class BlueskyComponentSettings implements ComponentSettings {
	
	@Override
	public boolean includeNewFormModal() {
		return true;
	}
	
	@Override
	public String includeNewFormModalContentFragmentName() {
		return "bluesky/fragments_bluesky :: blueskyCompose_form";
	}
	
	@Override
	public String newFormModalId() {
		return "blueskyCompose";
	}

	@Override
	public Map<String, ?> getSettings() {
		return Collections.emptyMap();
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
	public boolean includeScriptFragment() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String includeScriptFragmentName() {
		// TODO Auto-generated method stub
		return null;
	}

}
