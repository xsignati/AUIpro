/* $(document).ready(function(){
 $(document).on({
	mousemove: function(event){
	 $("div").append(event.pageX + ", ");
	},
	click: function(){
	$("div").append("click");
	}
 });
});
 */

 $(document).ready(function(){
	 $(document).on({
		mousemove: function(event){
			if (enableHandler){
				clicksPost(event.pageX, event.pageY, (performance.now() - pageLoadedTime), 'Moved');
				enableHandler = false;
			}
		},
		click: function(event){
			if($(event.target).closest('a').is('a')){
				event.preventDefault();
				clicksPost(event.pageX, event.pageY, (performance.now() - pageLoadedTime), 'URLclicked');
				safeDelay(event);
				
			}
			else{
				clicksPost(event.pageX, event.pageY, (performance.now() - pageLoadedTime), 'Clicked');
			}
		}
	 });
});

timer = window.setInterval(function(){
    enableHandler = true;
}, 5);


function safeDelay(e){
    //var link = this;
	var AJAXSnumber = getActiveAJAXs();
	var numberOfChecks = 0;
	var path = $(event.target).closest('a').attr('href');
	
	var waitForAJAXs = function(){
		if((AJAXSnumber < 5) || (numberOfChecks > 50)){	
			window.location = path;
		}
		else{
			AJAXSnumber = getActiveAJAXs();
			numberOfChecks++;
			setTimeout(waitForAJAXs, 200);
		}
	}
	waitForAJAXs();
}

function getActiveAJAXs(){
	return $.active;
}

/* $('a').click(function(){
    return confirm("Are you sure you want to delete?");
}) */
