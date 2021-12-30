package net.mixednutz.app.server.controller.web;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.UserProfile;
import net.mixednutz.app.server.io.domain.PersistableMultipartFile;
import net.mixednutz.app.server.io.manager.PhotoUploadManager;
import net.mixednutz.app.server.io.manager.PhotoUploadManager.Size;
import net.mixednutz.app.server.repository.UserProfileRepository;
import net.mixednutz.app.server.repository.UserRepository;

@Controller
public class UserProfileController {
	
	@Autowired
	private UserProfileRepository profileRepository;
	
	@Autowired
	private UserRepository userRepository;
		
	@Autowired
	protected PhotoUploadManager photoUploadManager;
	
	private String profileForm(@AuthenticationPrincipal User user, Model model) {
		final UserProfile profile =
				profileRepository.findById(user.getUserId()).orElse(new UserProfile());
		
		ProfileForm form = new ProfileForm(user, profile);
		model.addAttribute("form", form);
				
//		final Map<INetworkInfoSmall, List<AbstractFeed>> externalFeeds = 
//				externalFeedManager.feedsForUser(user);
//		model.addAttribute("externalFeeds", externalFeeds);
		
		return "profile/edit";
	}
	
	@RequestMapping(value="/{username}/edit", method = RequestMethod.GET)
	public String editProfile(@PathVariable String username, @AuthenticationPrincipal User user, Model model) {				
		if (!username.equals(user.getUsername())) {
			throw new AccessDeniedException("That's not yours to edit!");
		}
		
		return profileForm(user, model);
	}
	
	@RequestMapping(value="/{username}/edit", method = RequestMethod.POST)
	public String saveProfile(ProfileForm form, Errors errors,
			@PathVariable String username,
			@RequestParam("avatar") MultipartFile avatar,
			@AuthenticationPrincipal final User currentUser, Model model) {			
		
		if (!username.equals(currentUser.getUsername())) {
			throw new AccessDeniedException("That's not yours to edit!");
		}
		
		if (errors.hasErrors()) {
			return profileForm(currentUser, model);
		}
		
		String avatarFilename = null;
		if (avatar!=null && !avatar.getOriginalFilename().equals("")) {
			avatarFilename = uploadPhoto(currentUser, avatar);
		}
		
		// Load entities so we can change them:
		User user = userRepository.findById(currentUser.getUserId()).get();
		UserProfile profile = profileRepository.findById(currentUser.getUserId())
				.orElse(new UserProfile(user));
		
		if (notEquals(form.getDisplayName(), user.getDisplayName())) {
			user.setDisplayName(form.getDisplayName());
		}
		if (avatarFilename!=null) {
			user.setAvatarFilename(avatarFilename);
		} else if (form.isClearAvatar()) {
			user.setAvatarFilename(null);
		}
		user = userRepository.save(user);
		
		//update cached current user:
		Authentication authentication = 
				new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);
				
		if (notEquals(form.getBio(), profile.getBio())) {
			profile.setBio(form.getBio());
		}
		if (notEquals(form.getLocation(), profile.getLocation())) {
			profile.setLocation(form.getLocation());
		}
		if (notEquals(form.getPronouns(), profile.getPronouns())) {
			profile.setPronouns(form.getPronouns());
		}
		if (notEquals(form.getWebsite(), profile.getWebsite())) {
			profile.setWebsite(form.getWebsite());
		}
		if (notEquals(form.getTwitterAccountId(), profile.getTwitterAccountId())) {
			profile.setTwitterAccountId(form.getTwitterAccountId());
		}
		if (notEquals(form.getDiscordInviteCode(), profile.getDiscordInviteCode())) {
			profile.setDiscordInviteCode(form.getDiscordInviteCode());
		}
		if (notEquals(form.getDeviantArtUsername(), profile.getDeviantArtUsername())) {
			profile.setDeviantArtUsername(form.getDeviantArtUsername());
		}
		profileRepository.save(profile);
				
		return "redirect:/"+currentUser.getUsername();
	}
	
	/**
	 * Null-safe notEquals method
	 * 
	 * @param value1
	 * @param value2
	 * @return
	 */
	private <O> boolean notEquals(O value1, O value2) {
		return (value1!=null && !value1.equals(value2)) ||
				(value2!=null && !value2.equals(value1));
	}
	
	private String uploadPhoto(User user, MultipartFile file) {
		PersistableMultipartFile pFile = new PersistableMultipartFile();
		pFile.setFile(file);
		try {
			return photoUploadManager.uploadFile(user, pFile, Size.AVATAR);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public static class ProfileForm {
		private boolean clearAvatar = false;
		private String currentAvatar;
		private String displayName;
		private String location;
		private String bio;
		private String pronouns;
		private String website;
		private Integer twitterAccountId;
		private String discordInviteCode;
		private String deviantArtUsername;
		
		public ProfileForm() {
			super();
		}
		public ProfileForm(User user, UserProfile profile) {
			super();
			currentAvatar = user.getAvatarFilename();
			displayName = user.getDisplayName();
			location = profile.getLocation();
			bio = profile.getBio();
			pronouns = profile.getPronouns();
			website = profile.getWebsite();
			twitterAccountId = profile.getTwitterAccountId();
			discordInviteCode = profile.getDiscordInviteCode();
			deviantArtUsername = profile.getDeviantArtUsername();
		}
		public boolean isClearAvatar() {
			return clearAvatar;
		}
		public void setClearAvatar(boolean clearAvatar) {
			this.clearAvatar = clearAvatar;
		}
		public String getCurrentAvatar() {
			return currentAvatar;
		}
		public void setCurrentAvatar(String currentAvatar) {
			this.currentAvatar = currentAvatar;
		}
		public String getDisplayName() {
			return displayName;
		}
		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}
		public String getLocation() {
			return location;
		}
		public void setLocation(String location) {
			this.location = location;
		}
		public String getBio() {
			return bio;
		}
		public void setBio(String bio) {
			this.bio = bio;
		}
		public String getPronouns() {
			return pronouns;
		}
		public void setPronouns(String pronouns) {
			this.pronouns = pronouns;
		}
		public String getWebsite() {
			return website;
		}
		public void setWebsite(String website) {
			this.website = website;
		}
		public Integer getTwitterAccountId() {
			return twitterAccountId;
		}
		public void setTwitterAccountId(Integer twitterAccountId) {
			this.twitterAccountId = twitterAccountId;
		}
		public String getDiscordInviteCode() {
			return discordInviteCode;
		}
		public void setDiscordInviteCode(String discordInviteCode) {
			this.discordInviteCode = discordInviteCode;
		}
		public String getDeviantArtUsername() {
			return deviantArtUsername;
		}
		public void setDeviantArtUsername(String deviantArtUsername) {
			this.deviantArtUsername = deviantArtUsername;
		}
		
	}

}
