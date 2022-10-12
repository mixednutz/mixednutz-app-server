package net.mixednutz.app.server.manager;

import java.util.Optional;

public interface OembedFilterWhitelistManager {

	Optional<String> deriveSourceType(String sourceId);
	
}
