$(".generatesTitleKey").change(function(){
	var str = $(this).val();
	str = str
		.toLowerCase()
		.trim()
		.replace(/[^\w\s]|_/g, "")
		.replace(/\s+/g, '-');
	$(".generatedTitleKey").val(str);
});