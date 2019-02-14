var page = {};

page.setup = function() {
	page.updateForm();
	
	//void saved bundle
	app.removeBundleFromStorage();
};

page.updateForm = function() {
	$('[name=showCombinedExternalFeedsOnProfile]').prop('checked',app.settings.showCombinedExternalFeedsOnProfile);
	console.log(app.settings.indexPage);
	$('[name=indexPage]').val(app.settings.indexPage);
	page.updateExternalFeeds();
}

page.updateExternalFeeds = function() {	
	for (var i=0; i< app.externalFeeds.length; i++) {
		var feed = app.externalFeeds[i]; 
		for (var ii=0; ii<feed.accounts.length; ii++) {
			var account = feed.accounts[ii];
			var template = $('#feed_visibility_template').clone();
			template.attr("id","externalFeedAccount_"+account.feedId); 
			template.find(".iconName").addClass("fa-"+feed.feedInfo.fontAwesomeIconName);
			template.find(".displayName").text(feed.feedInfo.displayName);
			template.find("[name=feedId]").val(account.feedId);
			template.find("[name=visibility]").val(account.visibility);
		}
		template.appendTo("#feed_visibility");
	}
}