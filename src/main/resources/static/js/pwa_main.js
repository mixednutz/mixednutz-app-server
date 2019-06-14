var page = {
		homeTimelineTemplate: $('#home_input_template'),
  	};

page.setup = function() {
	page.updateExternalFeeds();
	page.updateTimelineInput();
	page.setupForms();
};

	
page.updateExternalFeeds = function() {
	var lastTab = '#home_tab';
	$("#newpost_form .externalFeedId").empty();
	$("#newjournal_form .externalFeedId").empty();
	$("#newphotos_form .externalFeedId").empty();
	$("#newpoll_form .externalFeedId").empty();

	for (var i=0; i< app.externalFeeds.length; i++) {
		var feed = app.externalFeeds[i];  			
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

$( window ).on( 'hashchange', function( e ) {
	var hash = window.location.hash;
	if(hash.length>1) {		
		app.hash = hash.substring(1);
	} else {
		app.hash = null;
	}
	page.reloadTimeline();
});

	