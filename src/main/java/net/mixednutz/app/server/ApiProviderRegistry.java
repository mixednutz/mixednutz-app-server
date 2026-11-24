package net.mixednutz.app.server;

import java.util.HashMap;
import java.util.Map;

import net.mixednutz.api.provider.ApiProvider;

public class ApiProviderRegistry {

	private Map<String, ApiProvider<?,?>> socialNetworkClients = new HashMap<>();
	private final Map<Class<?>, String> apiTypeIndex = new HashMap<Class<?>, String>();

	public void addSocialNetworkClient(ApiProvider<?,?> socialManager) {
		socialNetworkClients.put(socialManager.getProviderId(), socialManager);
		apiTypeIndex.put(socialManager.getClass(), socialManager.getProviderId());
	}

	public ApiProvider<?,?> getSocialNetworkClient(String providerId) {
		return socialNetworkClients.get(providerId);
	}

	@SuppressWarnings("unchecked")
	public <Api> ApiProvider<Api, ?> getSocialNetworkClient(Class<Api> clazz) {
		String providerId = apiTypeIndex.get(clazz);
		return (ApiProvider<Api, ?>) socialNetworkClients.get(providerId);
	}

	public Iterable<ApiProvider<?,?>> getProviders() {
		return socialNetworkClients.values();
	}

}
