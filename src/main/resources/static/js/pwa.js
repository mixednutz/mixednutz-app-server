var app = {
  			externalFeeds: [],
  			user: {},
  			networkInfo: {},
  			friends: [],
  			fgroups: [],
  			settings: {},
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
  	
  	
	
  	
  	/*****
	 * Data Methods
	 *****/
	
  	/**
  	 * Check to see if the user is currently logged in.
  	 */
  	app.verifyAuthentication = function() {
  		return new Promise(function(resolve, reject){
	  		$.ajax({
					type: 'GET', 
					url: getRelativePath('/internal/loggedin/user/'),
					dataType: "json",
					success: function(data){
						resolve(data);
					},
					error: function(jqXHR, textStatus, errorThrown){
						if (jqXHR.status==200) {
							resolve();
						} else {
							reject();
						}
					}
				});
  		});
  	};
  		
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
  					app.networkInfo = data.networkInfo;
  					app.friends = data.following;
  					app.fgroups = data.categories;
  					app.settings = data.settings;
  					if (app.pushNotificationSettings) {
  						app.settings.pushNotificationSettings = app.pushNotificationSettings;
  						app.pushNotificationSettings = null;
  					}
  					
  					app.saveBundle();
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
  				app.networkInfo = data.networkInfo;
  				app.friends = data.following;
  				app.fgroups = data.categories;
  				app.settings = data.settings;
  				if (app.pushNotificationSettings) {
					app.settings.pushNotificationSettings = app.pushNotificationSettings;
					app.pushNotificationSettings = null;
					app.saveBundle();
				}
  				
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
  	
  	
	
	//Start-up
  	$(document).ready(function() {
	  	var hash = window.location.hash;
	    if(hash) {		
	    	app.hash = hash.substring(1);
	    }
    });
  	app.verifyAuthentication().then(
  			//authenticated
  			function(user){
  				console.log("Authenticated as "+user.username);
		  		app.loadBundleFromStorage().then(function(wasCached){
		  			if (!wasCached) {
		  				// First time
		  	  			app.loadBundle().then(function(){
		  	  				page.setup();
		  	  			});	
		  			} else {
		  				page.setup();
		  			}
		  		
		  		});  
  			  	
  			},
  			//Not authenticated
  			function(){
  				console.log("Not Authenticated");
  				app.removeBundleFromStorage();
  				//load other components not related to the current user:
  				page.setup();
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