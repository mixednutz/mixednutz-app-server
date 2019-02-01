var profile = {
		homeTimelineTemplate: $('#home_input_template'),
};

/*profile.updateTimelineInput = function() {
	console.log(app.ownerBundle);
	if (app.user!=null) {
		if (app.ownerBundle.following) {
			var template = app.homeTimelineTemplate.clone();
		  	template.attr("id","homeTimeline"+app.user.username); 
		  	if (app.user.imageUrl) {
					template.find(".defaultPicture").attr("src", app.user.imageUrl.href);	
			}
		  	$("#timeline_inputs").append(template).removeClass("hidden");	
		}
		
	}
	
	app.reloadTimeline();
};*/


profile.setupProfile = function() {
	if (app.owner.imageUrl!=null) {
		$('.profile .avatar img')
			.attr("src", app.owner.imageUrl.href);
	}
	$('.profile .displayName').text(app.owner.displayName);
	$('.profile .username').text(app.owner.username);
	if (app.ownerBundle.friendPath!=null) {
		if (app.ownerBundle.friendPath.length==0) {
			$('.userpath .path').html('You Are '+app.owner.displayName);
		} else if (app.ownerBundle.friendPath.length==1) {
			$('.userpath .path').html(app.owner.displayName+' is one of your Nutsterz');
		} else if (app.ownerBundle.friendPath.length>1) {
			var path = "";
			for (var index in app.ownerBundle.friendPath) {
				path += ' <a href="'+app.ownerBundle.friendPath[index].url+'">'+
					app.ownerBundle.friendPath[index].username+'</a> <i class="fa fa-caret-right" aria-hidden="true"></i>';
			}
			path += ' <a href="'+app.owner.url+'">'+app.owner.displayName+'</a>'
			$('.userpath .path').html(path);
		}
	} else {
		$('.userpath .path').html('This person is not in your Nutster Network.');
	}
	
	if (app.ownerBundle.friendPath!=null && app.ownerBundle.friendPath.length>1) {
		if (app.ownerBundle.pending!=null) {
			if (app.ownerBundle.pending) {
				$('#requestFriend-status').html('Pending '+app.owner.displayName+'\'s approval');
			}
		} else {
			$('#requestFriend-status').html('<a id="requestFriend-btn" href="#" class="btn btn-default">Add '+app.owner.displayName+' as a Nutster</a>');
			$('#requestFriend-btn').on('click', requestFriendBtnClick);
		}
	}
	if (app.ownerBundle.profile!=null) {
		if (app.ownerBundle.profile.aboutMe!=null) {
			$('.profile .aboutme').html(app.ownerBundle.profile.aboutMe);
		} else {
			$('.profile .aboutme').remove();
		}
		if (app.ownerBundle.profile.location!=null) {
			$('.profile .location').text(app.ownerBundle.profile.location.city+", "+
					app.ownerBundle.profile.location.state+" "+app.ownerBundle.profile.location.country);
		} else {
			$('.profile .location').remove();
		}
		if (app.ownerBundle.profile.memberSince!=null) {
			$('.profile .membersince time') 
				.attr('datetime', app.ownerBundle.profile.memberSince)
				.text(new Date(app.ownerBundle.profile.memberSince).toLocaleDateString());
		} else {
			$('.profile .membersince').remove();
		}
		if (app.ownerBundle.profile.lastlogin!=null) {
			$('.profile .lastlogin time') 
				.attr('datetime', app.ownerBundle.profile.lastlogin)
				.text(new Date(app.ownerBundle.profile.lastlogin).toLocaleDateString());
		} else {
			$('.profile .lastlogin').remove();
		}
		
		$('.profile .followers .followingCount').text(app.ownerBundle.followingCount);
		$('.profile .followers .followerCount').text(app.ownerBundle.followerCount);
		$('.profile .followers').attr('href',app.owner.url+'/friends');
		if (app.ownerBundle.profile.twitterAccount!=null) {
			$('.socmed-author-twitter a').attr('href',app.ownerBundle.profile.twitterAccount.userProfileUrl.href);
		} else {
			$('.socmed-author-twitter').remove();
		}
		if (app.ownerBundle.profile.instagramAccount!=null) {
			$('.socmed-author-instagram a').attr('href',app.ownerBundle.profile.instagramAccount.userProfileUrl.href);
		} else {
			$('.socmed-author-instagram').remove();
		}
	}
	
};
profile.postLoad = function() {
	console.log('postLoad');
	$('.profile').removeAttr("hidden");
	$('.userpath').removeAttr("hidden");
	$('.extended-profile').removeAttr("hidden");
	$('#requestFriend-status').remove();
	if (app.user!=null && app.user.id==app.owner.id) {
		$('#viewusermenu a').attr('href',app.user.url+'/edit');
		$('#viewusermenu a').removeAttr("hidden");
	} else {
		$('#viewusermenu a').remove();
	}
}