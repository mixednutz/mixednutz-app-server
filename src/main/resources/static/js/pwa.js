var app = {
  			externalFeeds: [],
  			user: {},
  			owner: {},
  			friends: [],
  			fgroups: [],
  			settings: {},
  			homeTimelineTemplate: $('#home_input_template'),
  			swRegistration: {},
  			notificationsEnabled: false,
  			notificationsDenied: false,
  			bundle: {}, // for persisting to local storage
  			pushNotificationSettings: null, // for settings before bundle load
  			hash: null
  	};

(function() {
	'user strict';
	
	/*****
	 * Event Listeners
	 *****/
	
	/*****
	 * UI Methods
	 *****/
	
	app.setup = function() {
		app.updateExternalFeeds();
		app.updateTimelineInput();
		//app.setupForms();
	}
	
	app.updateExternalFeeds = function() {
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
//  					.on('click', {
//  						feedId: account.id, feedType: feed.name, name: account.name, imageUrl: account.image.src
//  					}, function (event) {
//  						showFeedInput(event.data.feedId, event.data.feedType, event.data.name, event.data.imageUrl); 
//  						loadFeedTimeline(event.data.feedId);
//  					});
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
  	
  	app.updateTimelineInput = function() {
  		var template = app.homeTimelineTemplate.clone();
	  	template.attr("id","homeTimeline"+app.user.username); 
	  	if (app.user.imageUrl) {
  			template.find(".defaultPicture").attr("src", app.user.imageUrl.href);	
  		}
	  	$("#timeline_inputs").append(template).removeClass("hidden");
	  	
  		app.reloadTimeline();
  	}
  	
  	app.reloadTimeline = function() {
  		if (app.hash){
	  		var optionElement = $("#externalFeedAccount_"+app.hash);
	        var feedId = optionElement.attr('data-feed-id');
	        var feedType = optionElement.attr('data-feed-type');
	        var imageUrl = optionElement.attr('data-image-url');
	        var name = optionElement.text();
	        
	        //load feed 
	        //showFeedInput(feedId, feedType, name, imageUrl); 
			loadFeedTimeline(feedId);
  		} else {
  			//load timeline
  			loadTimeline();
  			showTimelineInput();
  		}
  	}
	
  	/*****
	 * Data Methods
	 *****/
	
	app.loadBundle = function() {
  		return new Promise(function(resolve, reject){
  			$.ajax({
  				type: 'GET', 
  				url: getRelativePath('/api/timeline/bundle'),
  				dataType: "json",
  				success: function(data){
  					console.log("loadBundle");
  					console.log(data);
  					app.bundle = data;
  					app.externalFeeds = data.externalFeeds;
  					app.user = data.user;
  					app.owner = data.user;
  					app.friends = data.following;
  					app.fgroups = data.categories;
  					app.settings = data.settings;
  					if (app.pushNotificationSettings) {
  						app.settings.pushNotificationSettings = app.pushNotificationSettings;
  						app.pushNotificationSettings = null;
  					}
  					
  					app.saveBundle();
  					app.setup();
  					resolve(app.bundle);
  				}
  			});
  		});
  		
  	};
	
	app.loadBundleFromStorage = function() {
  		return idbKeyval.get('bundle').then(function(data){
  			if (data) {
  				console.log("Bundle retrieved from localStorage");
  	  			
  				app.bundle = data;
  	  			app.externalFeeds = data.externalFeeds;
  				app.user = data.user;
  				app.owner = data.user;
  				app.friends = data.following;
  				app.fgroups = data.categories;
  				app.settings = data.settings;
  				if (app.pushNotificationSettings) {
					app.settings.pushNotificationSettings = app.pushNotificationSettings;
					app.pushNotificationSettings = null;
					app.saveBundle();
				}
  				
  				app.setup();
  				return true;
  			}
  			return false;
  		});
  	}
  	 
  	app.saveBundle = function() {
  		console.log("saveBundle");
  		idbKeyval.set('bundle', app.bundle);
  	}
  	
  	app.removeBundleFromStorage = function() {
  		idbKeyval.del('bundle');
  	}
  	
  	/**
  	 *  Startup routine once we have the bundle loaded
  	 */
  	app.postLoad = function()  {
		if (app.user!=null) {
  			app.loadNotifications();
  			
  			if (app.settings.pushNotificationSettings.pushNotificationsEnabled) {
				app.subscribeNotifications();
			} else {
				console.log("User does not want push notificatins from this device.")
			}
  		}
		console.log(app.subscribePromise);
	}
	
	//Start-up
  	$(document).ready(function() {
	  	var hash = window.location.hash;
	    if(hash) {		
	    	app.hash = hash.substring(1);
	    }
    });
  	app.loadBundleFromStorage().then(function(wasCached){
  		if (!wasCached) {
  			// First time
  	  		app.loadBundle().then(function(){
  	  			app.postLoad();
  	  		});	
  		} else {
  			app.postLoad();
  		}
  		
  	});  
  	
	
	//Service worker
//    if ('serviceWorker' in navigator && 'PushManager' in window) {
//    	navigator.serviceWorker
//    		.register('./service-worker.js')
//    		.then(function(registration) {
//    			console.log('Service Worker Registered with scope: ', registration.scope); 
//    			
//    			app.swRegistration = registration;
//    			
//    			app.setupPushMessaging();
//    			    			
//    		}).catch(function(error){console.log('Service worker failed: ', error)});
//    } else {
//    	console.warn('ServiceWorkers and Push messaging are not supported');
//    }
})();