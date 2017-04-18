//save time of loaded page
var pageLoadedTime;
//ajax call
$(document).ready(function(){
	//first timestamp of timer, after page is loaded
	pageLoadedTime = performance.now();
	
	//init php
    $.ajax({
        type: "POST",
        url: "init.php",
		complete: function() {
		$("#loading").hide();
		},
		success: function(msg) {
		},
		error: function() {
		}
	});
    
});