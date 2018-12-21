package net.mixednutz.app.server.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(value=HttpStatus.FORBIDDEN, reason="The user is not authorized to view this resource")
public class NotAuthorizedException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1694611873240274824L;

	public NotAuthorizedException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public NotAuthorizedException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

	public NotAuthorizedException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public NotAuthorizedException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public NotAuthorizedException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
