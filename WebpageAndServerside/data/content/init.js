/**
*a timestamp variable
*/ 
var pageLoadedTime;

$(document).ready(function(){
	/**
	* timestamp of the timer
	*/ 
	pageLoadedTime = performance.now();
	
	/**
	* init the PHP script
	*/ 
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