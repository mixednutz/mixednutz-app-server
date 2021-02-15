var page = {
		
  	};

page.setup = function() {
	page.updateExternalFeeds();
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
		var composeFormDropdown = $('#'+feed.name+'ComposeForm').find("select[name=externalFeedId]");
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
			crosspostItem.find("input").val(account.feedId);
			crosspostItem.find("span").text(account.name);
			var reshareItem = $('<label class="checkbox-inline"><input name="externalFeedId" type="checkbox"/> <span></span></label>');
			reshareItem.find("input").val(account.feedId);
			reshareItem.find("span").text(account.name);
			var accountOptItem = $('<option></option>');
			accountOptItem.val(account.feedId).text(account.username);
			
			if (feed.compatibleMimeTypes!=null) {
				$(".externalFeedId").append(crosspostItem.clone());	
			}
			
			composeFormDropdown.append(accountOptItem.clone());
			
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



$( window ).on( 'hashchange', function( e ) {
	var hash = window.location.hash;
	if(hash.length>1) {		
		app.hash = hash.substring(1);
	} else {
		app.hash = null;
	}
	page.reloadTimeline();
});

	