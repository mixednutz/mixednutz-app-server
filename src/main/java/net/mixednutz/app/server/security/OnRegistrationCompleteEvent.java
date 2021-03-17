package net.mixednutz.app.server.security;

import org.springframework.context.ApplicationEvent;

import net.mixednutz.app.server.entity.UserEmailAddress;

public class OnRegistrationCompleteEvent extends ApplicationEvent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5070417739629829667L;
	
	private UserEmailAddress userEmailAddress;

	public OnRegistrationCompleteEvent(UserEmailAddress userEmailAddress) {
		super(userEmailAddress);
		this.userEmailAddress = userEmailAddress;
	}

	public UserEmailAddress getUserEmailAddress() {
		return userEmailAddress;
	}

	public void setUserEmailAddress(UserEmailAddress userEmailAddress) {
		this.userEmailAddress = userEmailAddress;
	}

}
