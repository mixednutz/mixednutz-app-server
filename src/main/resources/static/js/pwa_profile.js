var page = {
		externalFeeds: [],
		owner: {},
		ownerBundle: {},
		homeTimelineTemplate: $('#home_input_template'),
};

page.setup = function() {
	page.loadOwner().then(function(){
		page.updateExternalFeeds();
		page.updateTimelineInput();
		page.setupForms();
		page.setupProfile();
		page.postLoad();
	});
};

page.updateExternalFeeds = function() {
	var lastTab = '#home_tab';
	$("#newpost_form .externalFeedId").empty();
	$("#newjournal_form .externalFeedId").empty();
	$("#newphotos_form .externalFeedId").empty();
	$("#newpoll_form .externalFeedId").empty();

	for (var i=0; i< page.externalFeeds.length; i++) {
		var feed = page.externalFeeds[i];  			
		var template = $('#tabs_feed_template').clone();
		template.attr("id","externalFeed_"+feed.name); 
		template.find(".iconName").addClass("fa-"+feed.feedInfo.fontAwesomeIconName);
		template.find(".displayName").text(feed.feedInfo.displayName);
		for (var ii=0; ii<feed.accounts.length; ii++) {
			var account = feed.accounts[ii];
			var item = $("<li><a></a></li>");
			item.find("a")
				.attr("id", "externalFeedAccount_"+account.feedId)
				.attr("data-feed-id", account.feedId)
				.attr("data-feed-type", feed.name)
				.attr("data-image-url", account.image.src)
				.text(account.name)
				.attr("href", "#"+account.feedId);
			template.find(".dropdown-menu")
				.append(item);
			  				
			var crosspostItem = $('<div class="checkbox"><label><input name="externalFeedId" type="checkbox"/> <span></span></label></div>');
			crosspostItem.find("input").val(account.id);
			crosspostItem.find("span").text(account.name);
			var reshareItem = $('<label class="checkbox-inline"><input name="externalFeedId" type="checkbox"/> <span></span></label>');
			reshareItem.find("input").val(account.id);
			reshareItem.find("span").text(account.name);
			var accountOptItem = $('<option></option>');
			accountOptItem.val(account.id).text(account.nativeUsername);
			
			if (feed.canCrosspostTo!=null && feed.canCrosspostTo.includes("Msg")) {
				$("#newpost_form .externalFeedId").append(crosspostItem.clone());	
			}
			if (feed.canCrosspostTo!=null && feed.canCrosspostTo.includes("Journal")) {
				$("#newjournal_form .externalFeedId").append(crosspostItem.clone());	
			}
  			if (feed.canCrosspostTo!=null && feed.canCrosspostTo.includes("Album")) {
				$("#newphotos_form .Album_Crosspost .externalFeedId").append(crosspostItem.clone());	
			}
  	  		if (feed.canCrosspostTo!=null && feed.canCrosspostTo.includes("Photo")) {
				$("#newphotos_form .Photo_Crosspost .externalFeedId").append(crosspostItem.clone());	
			}
  	  		//TODO
	  	  	//if (feed.canCrosspostTo!=null && feed.canCrosspostTo.includes("Poll")) {
			//	$("#newpoll_form .externalFeedId").append(crosspostItem.clone());	
			//}
  	  		
  	  		$("#reshareFeedForm .externalFeedId").append(reshareItem.clone());	
  	  		if (feed.canCrosspostTo!=null && feed.canCrosspostTo.includes("twitter")) {
  	  			$("#twitterComposeForm .externalFeedId select[name=externalFeedId]").append(accountOptItem.clone());
  	  			$("#twitterReplyForm .externalFeedId select[name=externalFeedId]").append(accountOptItem.clone());
  	  		}
  	  		if (feed.canCrosspostTo!=null && feed.canCrosspostTo.includes("discord")) {
  	  			$("#discordComposeForm .externalFeedId select[name=externalFeedId]").append(accountOptItem.clone());
  	  			$("#discordReplyForm .externalFeedId select[name=externalFeedId]").append(accountOptItem.clone());
  	  		}
  	  		
			//clear memory
			crosspostItem.remove();
			reshareItem.remove();
			accountOptItem.remove();
		}
		
		
		template.insertAfter(lastTab);
		lastTab = "#"+template.attr("id");
	}
	$("#timeline_tabs").removeClass("hidden");
	
};	

page.updateTimelineInput = function() {
	if (app.user!=null) {
		
		//TODO we want to show this publicly if settings allow it
		var template = page.homeTimelineTemplate.clone();
  		template.attr("id","homeTimeline"+app.user.username); 
  		if (app.user.avatar!=null && app.user.avatar.src!=null) {
			template.find(".defaultPicture").attr("src", app.user.avatar.src);	
		} else {
			template.find(".defaultPicture").attr("src", defaultPicture);
		}
  		$("#timeline_inputs").append(template).removeClass("hidden");

  	}
	page.reloadTimeline();
}
  	
page.reloadTimeline = function() {
	if (app.hash){
  		var optionElement = $("#externalFeedAccount_"+app.hash);
        var feedId = optionElement.attr('data-feed-id');
        var feedType = optionElement.attr('data-feed-type');
        var imageUrl = optionElement.attr('data-image-url');
        var name = optionElement.text();
        
        //load feed 
        showFeedInput(feedId, feedType, name, imageUrl); 
		loadFeedTimeline(feedId);
	} else {
		//load timeline
		loadTimeline();
		showTimelineInput();
	}
}

page.setupForms = function() {
		
}

page.setupProfile = function() {
	if (page.owner.avatar!=null && page.owner.avatar.src!=null) {
		$('.profile .avatar img')
			.attr("src", page.owner.avatar.src);
	}
	$('.profile .displayName').text(page.owner.displayName);
	$('.profile .username').text(page.owner.username);
	if (page.ownerBundle.friendPath!=null) {
		if (page.ownerBundle.friendPath.length==0) {
			$('.userpath .path').html('You Are '+page.owner.displayName);
		} else if (page.ownerBundle.friendPath.length==1) {
			$('.userpath .path').html(page.owner.displayName+' is one of your Nutsterz');
		} else if (page.ownerBundle.friendPath.length>1) {
			var path = "";
			for (var index in page.ownerBundle.friendPath) {
				path += ' <a href="'+page.ownerBundle.friendPath[index].url+'">'+
					page.ownerBundle.friendPath[index].username+'</a> <i class="fa fa-caret-right" aria-hidden="true"></i>';
			}
			path += ' <a href="'+page.owner.url+'">'+page.owner.displayName+'</a>'
			$('.userpath .path').html(path);
		}
	} else {
		$('.userpath .path').html('This person is not in your Nutster Network.');
	}
	
	if (page.ownerBundle.friendPath!=null && page.ownerBundle.friendPath.length>1) {
		if (page.ownerBundle.pending!=null) {
			if (page.ownerBundle.pending) {
				$('#requestFriend-status').html('Pending '+page.owner.displayName+'\'s approval');
			}
		} else {
			$('#requestFriend-status').html('<a id="requestFriend-btn" href="#" class="btn btn-default">Add '+page.owner.displayName+' as a Nutster</a>');
			$('#requestFriend-btn').on('click', requestFriendBtnClick);
		}
	}
	console.log(page.ownerBundle);
	if (page.ownerBundle.user.profileData !=null) {
		if (page.ownerBundle.user.profileData.bio!=null) {
			$('.profile .bio').html(page.ownerBundle.user.profileData.bio);
		} else {
			$('.profile .bio').remove();
		}
		if (page.ownerBundle.user.profileData.pronouns!=null) {
			$('.profile .pronouns').html(page.ownerBundle.user.profileData.pronouns);
		} else {
			$('.profile .pronouns').remove();
		}
		if (page.ownerBundle.user.profileData.location!=null) {
			$('.profile .location').text(page.ownerBundle.user.profileData.location);
		} else {
			$('.profile .location').remove();
		}
		if (page.ownerBundle.user.memberSince!=null) {
			$('.profile .membersince time') 
				.attr('datetime', page.ownerBundle.user.memberSince)
				.text(new Date(page.ownerBundle.user.memberSince).toLocaleDateString());
		} else {
			$('.profile .membersince').remove();
		}
		/*if (page.ownerBundle.page.lastlogin!=null) {
			$('.profile .lastlogin time') 
				.attr('datetime', page.ownerBundle.page.lastlogin)
				.text(new Date(page.ownerBundle.page.lastlogin).toLocaleDateString());
		} else {
			$('.profile .lastlogin').remove();
		}*/
		
		$('.profile .followers .followingCount').text(page.ownerBundle.followingCount);
		$('.profile .followers .followerCount').text(page.ownerBundle.followerCount);
		$('.profile .followers').attr('href',page.owner.url+'/friends');
		
		$('.socmed-author-rss a').attr('href','/rss/'+page.owner.username);
		if (page.ownerBundle.user.profileData.twitterAccount!=null) {
			$('.socmed-author-twitter a').attr('href','https://twitter.com/'+page.ownerBundle.user.profileData.twitterAccount.username);
		} else {
			$('.socmed-author-twitter').remove();
		}
		if (page.ownerBundle.user.profileData.discordInviteCode!=null) {
			$('.socmed-author-discord a').attr('href','https://discord.gg/'+page.ownerBundle.user.profileData.discordInviteCode);
		} else {
			$('.socmed-author-discord').remove();
		}
		if (page.ownerBundle.user.profileData.deviantArtUsername!=null) {
			$('.socmed-author-deviantart a').attr('href','https://deviantart.com/'+page.ownerBundle.user.profileData.deviantArtUsername);
		} else {
			$('.socmed-author-deviantart').remove();
		}
		//if (page.ownerBundle.page.instagramAccount!=null) {
		//	$('.socmed-author-instagram a').attr('href',page.ownerBundle.page.instagramAccount.userProfileUrl.href);
		//} else {
			$('.socmed-author-instagram').remove();
		//}
	} else {
		$('.extended-profile').remove();
	}
	
};
page.postLoad = function() {
	console.log('postLoad');
	$('.profile').removeAttr("hidden");
	$('.userpath').removeAttr("hidden");
	$('.extended-profile').removeAttr("hidden");
	$('#requestFriend-status').remove();
	if (app.user!=null && app.user.providerId==page.owner.providerId) {
		$('#viewusermenu a').attr('href',getRelativePath(app.user.uri+'/edit'));
		$('#viewusermenu').removeAttr("hidden");
	} else {
		$('#viewusermenu').remove();
	}
}

page.loadOwner = function() {
	return new Promise(function(resolve, reject){
		
		$.ajax({
			type: 'GET', 
			url: getRelativePath('/internal/'+loadOwnerUserName+'/bundle'),
			dataType: "json",
			success: function(data){
				page.owner = data.user;
				page.ownerBundle = data;
				page.externalFeeds = data.externalFeeds;
				resolve(page.owner);
			}
		});
		
	});
};

$( window ).on( 'hashchange', function( e ) {
	var hash = window.location.hash;
	if(hash.length>1) {		
		app.hash = hash.substring(1);
	} else {
		app.hash = null;
	}
	page.reloadTimeline();
});

