var unreadContent=null;
var refreshId;
var snackbar;
var defaultTitleBar = document.title;
	

function startTimeout(readCallback, nextPage) {
	if (refreshId!=null) {
		console.log('[startTimeout] clearing Timeout '+refreshId);
  		clearTimeout(refreshId);	
	}
	refreshId = setTimeout(function(){
		console.log('[Timeout:'+refreshId+'] Timeout '+JSON.stringify(nextPage));
		refreshId = null;
		readCallback(nextPage);
	}, 30*1000);
	console.log('[startTimeout] Timeout '+refreshId+' set for 30 seconds');
}

function readTimeline(url, data, elementsSelector, renderCallback, readMoreCallback, readSinceCallback) {
	
	removePager();
	$(elementsSelector).empty();
 
  	buildLoading().appendTo(".timeline");
	
  	console.log(url);  
  	
	$.ajax({
		type: 'GET', 
		url: url, 
//		data: JSON.stringify(data),
		contentType: 'application/json; charset=utf-8',
		dataType: 'json',
		success: function(page){
			
			//Render
			renderCallback(page, 'append');
			$(elementsSelector+' .prerender').removeClass('prerender');
			  				
			$('.timeline .loading').remove();
			
			if (readMoreCallback!=null) {
				//Pager
				buildPager(page, function(timeline){
					readMoreCallback(timeline.nextPage);
				}).appendTo(".timeline");
			}
			
			if (readSinceCallback!=null) {
				//Timeout
				startTimeout(readSinceCallback, page.reversePage);
			}
		},
		timeout: 120*1000,
		error: function(jqXHR, textStatus, errorThrown) {
			if (textStatus==='timeout') {
				readTimeline(url, data, elementsSelector, renderCallback, readMoreCallback, readSinceCallback)
			} else {
				console.log('[readTimeline/error]: '+textStatus+' '+errorThrown);
				console.log(jqXHR);
			}
		}
	});
}

function readTimelineSince(url, data, elementsSelector, renderCallback, readSinceCallback) {
	var startTime = new Date();  		
	$.ajax({
		type: 'POST', 
		url: url, 
		data: JSON.stringify(data),
		contentType: 'application/json; charset=utf-8',
		dataType: 'json',
		success: function(page){
			var endTime = new Date();
			var timeDiff = (endTime-startTime)/1000;
			console.log('[readTimelineSince] Latency: '+timeDiff+' secs');
			if (page.items.length>0) {
				if (unreadContent==null || page.items.length>unreadContent.items.length) {
					console.log("Found feed items by looking for "+JSON.stringify(data));
					
					//Snackbar
					onNewItemsNotification(page, elementsSelector, renderCallback, readSinceCallback);
  				}
			}
			unreadContent = page;
			
			//Prerender content
			renderCallback(unreadContent, 'prepend');
			
			if (page.items.length<=200) {
  				//Repeat readSinceCallback with currentPage.
  				//The readSinceCallback with nextPage happens on snackbar close.
				startTimeout(readSinceCallback, page.pageRequest);	
			} else {
				console.log("There's more than 200 unread items! Is there anyone there?  I'm going to sleep now.");
			}
		},
		timeout: 120*1000,
		error: function(jqXHR, textStatus, errorThrown) {
			if (textStatus==='timeout') {
				//Try again
				readTimelineSince(url, data, renderCallback, readSinceCallback)
			} else {
				console.log('[readTimelineSince/error]: '+textStatus+' '+errorThrown);
				console.log(jqXHR);
			}
		}
	});
}

function readTimelineMore(url, data, elementsSelector, renderCallback, readMoreCallback) {

	removePager();
	
	buildLoading().appendTo(".timeline");
	
	$.ajax({
		type: 'POST', 
		url: url, 
		data: JSON.stringify(data),
		contentType: 'application/json; charset=utf-8',
		dataType: 'json',
		success: function(page){
			//Render
			renderCallback(page, 'append');
			$(elementsSelector+' .prerender').removeClass('prerender');
			
			$('.timeline .loading').remove();
			
			//Pager
			buildPager(page, function(timeline){
				readMoreCallback(timeline.nextPage);
			}).appendTo(".timeline");
							
		},
		timeout: 120*1000,
		error: function(jqXHR, textStatus, errorThrown) {
			console.log(jqXHR);
			console.log('[readTimelineMore/error]: '+textStatus+' '+errorThrown);
		}
	});
}

function onNewItemsNotification(page, elementsSelector, renderCallback, readSinceCallback) {
	if (snackbar!=null) {
		snackbar.remove();
	}
	document.title = '('+page.items.length+') '+defaultTitleBar;
	snackbar = $.snackbar({
 		content: '&#8593; You have '+page.items.length+' new items',
 		timeout: 0, 
 		style: "notificationbar",
 		onClose: function() {
 			document.title = defaultTitleBar;
 			
 			//Timeout
 			//Initiating this direction was prevPage, but continuing it is nextPage
 			console.log("next page is "+JSON.stringify(page.nextPage)+". is this right?")
 			startTimeout(readSinceCallback, page.nextPage);
							
 			//Render
 			var numOfPrerenders = $(elementsSelector+' .prerender').length;
 			var scrollToElement = $(elementsSelector+' .prerender').eq(numOfPrerenders-1);
 			$(elementsSelector+' .prerender').removeClass('prerender');
 			console.log(scrollToElement);
			$('html, body').animate({
				//51 is the size of the menu bar 60 is the height of input box
		        scrollTop: scrollToElement.offset().top - 51 - 60
		    	}, 500); 	
 			
 			unreadContent = null;
 		}});
}

function buildLoading() {
	return $('<div class="loading"><i class="fa fa-spinner fa-pulse fa-3x fa-fw"></i><span class="sr-only">Loading...</span></div>');
}
function buildPager(timeline, loadTimelineFunction) {
	var pager = $('<nav id="timeline_pager"><ul class="pager"></ul></nav>');
	if (timeline.hasNext) {
		$('<li class="pager-prev"><a>Load More</a></li>').appendTo(pager.find('ul'))
	}
	pager.find('.pager-prev a').click(function(e){
		pager.remove();
		loadTimelineFunction(timeline);
	});
	return pager;
}
function removePager() {
	$('#timeline_pager').remove();
}

function formatDateTodayYesterday(date, callback) {
	var now = new Date();
	if (now.toDateString()==date.toDateString()) {
		return "<span class=\"today\">Today</span>";
	}
	var yesterday = new Date(now-1);
	if (yesterday.toDateString()==date.toDateString()) {
		return "Yesterday";
	}
	if (callback!=null) {
		return callback(date);
	}
	return date.toLocaleDateString();
}

function formatDateTimeTodayYesterday(date, dateCallback, timeCallback) {
	var time = date.toLocaleTimeString();
	if (timeCallback!=null) {
		time = timeCallback(date);
	}
	return formatDateTodayYesterday(date, dateCallback) + " at " + time;
}

function buildExternalFeedTimelineElement(element, feedId) {
	var type = element.type.id;
	var id = type+"_"+element.providerId;
	if ($("#"+id).length>0) {
		console.log(id+" already exists");
		return null;
	}
	var template = $('#template_oembed').clone();
	template.attr("id",id); 	
	
	var oembedHref = null;
	if (element.alternateLinks!=null) {
		for (altLinkIdx in element.alternateLinks) {
			if (element.alternateLinks[altLinkIdx].type=="application/json+oembed") {
				oembedHref = element.alternateLinks[altLinkIdx].href;
			}
		}	
	}
	//Default stuff (avatar/username/date)
	template.find(".avatar img").attr({
		'src':element.postedByUser.avatar.src,
		'title':element.postedByUser.avatar.title});
	template.find(".avatar a").attr({
		'href':element.postedByUser.url,
		'target':'_new'});
	template.find(".username .name").text(element.postedByUser.displayName);
	template.find(".username a")
		.attr('href',element.postedByUser.url)
		.attr('target','_new');
	template.find(".username .screenname").text(element.postedByUser.username);
	template.find(".createdAt span").text(new Date(element.postedOnDate).toLocaleString());
	//Preview
	var preview = template.find(".preview");
	//preview.css('display','none');
	var paneBody = preview.find(".pane-body");
	var oembedDiv = preview.find(".oembed");
	paneBody.find(".title").html(element.title);
 	paneBody.find(".description").html(element.description);
 	preview.find("a.pane").attr({
			"href":element.url});
	if (oembedHref!=null) {
		
		preview.find('.oembedToggle button').on('click', function(){
			if (oembedDiv.attr('hidden')) {
				oembedDiv.removeAttr('hidden');	
			} else {
				oembedDiv.attr('hidden', true);
			}
  			
  		});
		
  		$.ajax({
			type: 'GET', 
			url: oembedHref+"&amp;align=center",
			dataType: "jsonp",
			success: function(data){
				if (data.type=="rich") {
					oembedDiv.html(data.html);
				} else if (data.type="photo") {
					oembedDiv.remove();
					paneBody.find(".image")
						.attr('href', data.url);
				}
			},
			error: function() {
				console.log('[oembed] error:'+oembedHref);
				preview.remove();
			}
		});
	} else {
		oembedDiv.remove();
	}
	
	return template;
}

function buildInternalTimelineElement(element, feedId) {
	var type = element.type.id;
	var id = type+"_"+element.providerId;
	if ($("#"+id).length>0) {
		console.log(id+" already exists");
		return null;
	}
	
	console.log('#template_'+element.type.name); 
	var template = $('#template_'+element.type.name).clone();
	template.attr("id",id); 	
	
	//Default stuff (avatar/username/date)
	template.find(".avatar img").attr({
		'src':element.postedByUser.avatar.src,
		'title':element.postedByUser.avatar.title});
	template.find(".avatar a").attr({
		'href':element.postedByUser.url});
	console.log(element.postedByUser);
	template.find(".username .name").text(
			element.postedByUser.displayName!=null?element.postedByUser.displayName:element.postedByUser.username);
	template.find(".username a")
		.attr('href',element.postedByUser.url);
	if (element.postedByUser.displayName!=null) {
		template.find(".username .screenname").text(element.postedByUser.username);
	} else {
		template.find(".username .screenname").remove();
	}
	
	if (element.postedToGroup!=null) {
		
	} else {
		template.find(".owner").remove();
	}
	template.find(".createdAt span").text(new Date(element.postedOnDate).toLocaleString());
	template.find(".subject a")
		.attr('href',element.url)
		.text(element.title);
	template.find(".preview a.readmore").attr('href',getRelativePath(element.uri));
	//Preview
	var preview = template.find(".preview");
	//preview.css('display','none');
	var paneBody = preview.find(".pane-body");
	var oembedDiv = preview.find(".oembed").remove();
	paneBody.find(".title").html(element.title);
 	paneBody.find(".description").html(element.description);
 	preview.find("a.pane").attr({
			"href":element.url});
 	//TODO re-implement resharing
 	template.find(".reshared").remove();
	
	return template;
}
