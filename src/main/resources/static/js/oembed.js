$(document).ready(function(){
	
	$('.oembed').each(function(i, obj) {
		var sourceType = $(this).data('sourcetype');
		var sourceId = $(this).data('sourceid');
		var thisOembed = $(this);
		
		var url = '/internal/lookup?url='+sourceId;
		$.ajax({
			url: url,
			type: "GET", 
			success: function(data){
				thisOembed.empty();
				thisOembed.append($(data.oembed.html));
			}
		});
	});
});