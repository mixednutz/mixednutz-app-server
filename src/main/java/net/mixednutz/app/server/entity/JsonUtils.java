package net.mixednutz.app.server.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import net.mixednutz.api.core.model.TimelineElement;
import net.mixednutz.api.model.ITimelineElement;

public class JsonUtils {
	
	private static final ObjectMapper objectMapper = new ObjectMapper();
	static {
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
	}
	
	public static ObjectMapper getObjectMapper() {
		return objectMapper;
	}
	
	public static <T> ITimelineElement fromJson(String elementJson, Class<? extends ITimelineElement> elementClass) {
		try {
			System.out.println(elementClass+"\n"+elementJson);
			return objectMapper.readValue(elementJson, elementClass);
		} catch (Exception e) {
			e.printStackTrace();
			return new TimelineElement();
		} 
	}
	
	public static String toJson(Object element) {
		try {
			return objectMapper.writeValueAsString(element);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return "{}";
		}
	}
	
}
