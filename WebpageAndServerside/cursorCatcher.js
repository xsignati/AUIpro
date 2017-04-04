 $(document).ready(function(){
	 $(document).on({
		mousemove: function(event){
			if (enableHandler){
				clicksPost(event.pageX, event.pageY, (performance.now() - pageLoadedTime) * 1000, 'Moved');
				enableHandler = false;
			}
		},
		click: function(event){
			if($(event.target).closest('a').is('a')){
				event.preventDefault();
				clicksPost(event.pageX, event.pageY, (performance.now() - pageLoadedTime) * 1000, 'URLclicked');
				safeDelay(event);
				
			}
			else{
				clicksPost(event.pageX, event.pageY, (performance.now() - pageLoadedTime) * 1000, 'Clicked');
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
