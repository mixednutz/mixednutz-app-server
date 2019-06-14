var page = {};

page.setup = function() {
	page.updateForm();
	
	//void saved bundle
	app.removeBundleFromStorage();
};

page.updateForm = function() {
	page.updateExternalFeeds();
}

page.updateExternalFeeds = function() {	
	for (var i=0; i< app.externalFeeds.length; i++) {
		var feed = app.externalFeeds[i]; 
		var select = $('.'+feed.feedInfo.id+"AccountId select")
		for (var ii=0; ii<feed.accounts.length; ii++) {
			var account = feed.accounts[ii];
			
			console.log(feed.feedInfo.id+"AccountId");
			console.log(account.feedId);
			console.log(account.name);
			
			var option = $('<option></option>')
				.attr("value",account.feedId)
				.text(account.name);
			
			select.append(option);
//			var template = $('#feed_visibility_template').clone();
//			template.attr("id","externalFeedAccount_"+account.feedId); 
//			template.find(".iconName").addClass("fa-"+feed.feedInfo.fontAwesomeIconName);
//			template.find(".displayName").text(feed.feedInfo.displayName);
//			template.find("[name=feedId]").val(account.feedId);
//			template.find("[name=visibility]").val(account.visibility);
		}
		//template.appendTo("#feed_visibility");
	}
}