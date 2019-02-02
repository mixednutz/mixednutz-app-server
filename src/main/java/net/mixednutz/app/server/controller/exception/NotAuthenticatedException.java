package net.mixednutz.app.server.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(value=HttpStatus.UNAUTHORIZED, reason="This resource requires to user to be authenticated")
public class NotAuthenticatedException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1694611873240274824L;

	public NotAuthenticatedException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public NotAuthenticatedException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

	public NotAuthenticatedException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public NotAuthenticatedException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public NotAuthenticatedException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
