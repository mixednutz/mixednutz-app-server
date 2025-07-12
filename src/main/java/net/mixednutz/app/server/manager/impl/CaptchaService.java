package net.mixednutz.app.server.manager.impl;

import java.net.URI;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import net.mixednutz.app.server.manager.CaptchaException;

@Service
public class CaptchaService {
	
	private static final String BASE_URL="https://www.google.com/recaptcha/api/siteverify";
	
	private static Pattern RESPONSE_PATTERN = Pattern.compile("[A-Za-z0-9_-]+");
	
	@Autowired
    private MessageSource messageSource;
	
	private RestTemplate restTemplate;
		
	private CaptchaSettings settings;
	
	@Autowired
    public CaptchaService(Optional<RestTemplate> restTemplate) {
		super();
		this.restTemplate = restTemplate.orElse(new RestTemplate());
	}

	@PostConstruct
    private void init() {
    	MessageSourceAccessor accessor = new MessageSourceAccessor(messageSource, Locale.ENGLISH);
        settings = new CaptchaSettings(
        		accessor.getMessage("google.recaptcha.key.site"),
        		accessor.getMessage("google.recaptcha.key.secret"));
    }
    
	public CaptchaSettings getSettings() {
		return settings;
	}
	
    public void processResponse(String response) {
        if(!responseSanityCheck(response)) {
            throw new CaptchaException("Response contains invalid characters");
        }

        
        URI verifyUri = UriComponentsBuilder.fromUriString(BASE_URL)
        	.queryParam("secret", settings.getSecret())
        	.queryParam("response", response)
        	.queryParam("remoteip", getClientIP())
        	.build().toUri();
        
        GoogleResponse googleResponse = restTemplate.getForObject(verifyUri, GoogleResponse.class);

        if(!googleResponse.isSuccess()) {
            throw new CaptchaException("reCaptcha was not successfully validated");
        }
    }
    
    private String getClientIP() {
    	return "";
    }

    private boolean responseSanityCheck(String response) {
        return StringUtils.hasLength(response) && RESPONSE_PATTERN.matcher(response).matches();
    }
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPropertyOrder({
        "success",
        "challenge_ts",
        "hostname",
        "error-codes"
    })
    static class GoogleResponse {
    	
    	@JsonProperty("success")
        private boolean success;
        
        @JsonProperty("challenge_ts")
        private String challengeTs;
        
        @JsonProperty("hostname")
        private String hostname;
        
        @JsonProperty("error-codes")
        private ErrorCode[] errorCodes;
        
        @JsonIgnore
        public boolean hasClientError() {
            ErrorCode[] errors = getErrorCodes();
            if(errors == null) {
                return false;
            }
            for(ErrorCode error : errors) {
                switch(error) {
                    case InvalidResponse:
                    case MissingResponse:
                        return true;
                }
            }
            return false;
        }

        static enum ErrorCode {
            MissingSecret,     InvalidSecret,
            MissingResponse,   InvalidResponse;

            private static Map<String, ErrorCode> errorsMap = new HashMap<String, ErrorCode>(4);

            static {
                errorsMap.put("missing-input-secret",   MissingSecret);
                errorsMap.put("invalid-input-secret",   InvalidSecret);
                errorsMap.put("missing-input-response", MissingResponse);
                errorsMap.put("invalid-input-response", InvalidResponse);
            }

            @JsonCreator
            public static ErrorCode forValue(String value) {
                return errorsMap.get(value.toLowerCase());
            }
        }

		public boolean isSuccess() {
			return success;
		}

		public void setSuccess(boolean success) {
			this.success = success;
		}

		public String getChallengeTs() {
			return challengeTs;
		}

		public void setChallengeTs(String challengeTs) {
			this.challengeTs = challengeTs;
		}

		public String getHostname() {
			return hostname;
		}

		public void setHostname(String hostname) {
			this.hostname = hostname;
		}

		public ErrorCode[] getErrorCodes() {
			return errorCodes;
		}

		public void setErrorCodes(ErrorCode[] errorCodes) {
			this.errorCodes = errorCodes;
		}
    	
    }
    
   
    static class CaptchaSettings {

    	private String site;
    	private String secret;
    	
    	public CaptchaSettings(String site, String secret) {
			super();
			this.site = site;
			this.secret = secret;
		}
		public String getSite() {
    		return site;
    	}
    	public void setSite(String site) {
    		this.site = site;
    	}
    	public String getSecret() {
    		return secret;
    	}
    	public void setSecret(String secret) {
    		this.secret = secret;
    	}
    	
    }
    
    static enum ErrorCode {
        MissingSecret,     InvalidSecret,
        MissingResponse,   InvalidResponse;

        private static Map<String, ErrorCode> errorsMap = new HashMap<String, ErrorCode>(4);

        static {
            errorsMap.put("missing-input-secret",   MissingSecret);
            errorsMap.put("invalid-input-secret",   InvalidSecret);
            errorsMap.put("missing-input-response", MissingResponse);
            errorsMap.put("invalid-input-response", InvalidResponse);
        }

        @JsonCreator
        public static ErrorCode forValue(String value) {
            return errorsMap.get(value.toLowerCase());
        }
    }

}
