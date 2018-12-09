package net.mixednutz.app.server.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.BAD_REQUEST, reason="The provided parameters are incorrect or missing")
public class BadParametersException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2818145500395545510L;

	public BadParametersException(String message) {
		super(message);
	}

}
