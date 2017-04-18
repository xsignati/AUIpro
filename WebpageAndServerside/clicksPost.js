var clicksBuffer = [];
var bufferLimit = 100;
var bufferIterator = 0;

function clicksPost(cursorX, cursorY, moveTime, actionType){
	
	if(clicksBuffer.length >= bufferLimit || actionType == 'URLclicked'){
		//save current data
		clicksBuffer[bufferIterator] = {x: cursorX, y: cursorY, time: moveTime, action: actionType};
		//send data to server
		$.ajax({
			type: "POST",
			url: "MouseMovementToDatabase.php",
			contentType: 'application/json; charset=UTF-8', //pure JSON
			/* data: {bufferData : JSON.stringify(clicksBuffer)}, */ //not pure JSON
			data: JSON.stringify(clicksBuffer),
			complete: function() {
				//$("#loading").hide();

			},
			success: function(msg) {
				//alert(msg);
				//$("div").append(msg + ", ");
			},
			error: function(msg) {
				//alert("error");
			}
		});
	
		//release buffer, reset iterator
		clicksBuffer = [];
		bufferIterator = 0;
	}
	else{
		//$("div").append(actionType);
		clicksBuffer[bufferIterator] = {x: cursorX, y: cursorY, time: moveTime, action: actionType};
		//$("div").append(clicksBuffer[bufferIterator].[0]);
		//alert(JSON.stringify({bufferData: clicksBuffer}));
		//alert(JSON.stringify(clicksBuffer));
		bufferIterator++;
	}
	
	//no JS buffer type
/* 	$.ajax({
		type: "POST",
		url: "MouseMovementToDatabase.php",
		data: {clicksBuffer: },
		complete: function() {
				$("#loading").hide();
		},
		success: function(msg) {
			//$("div").append(msg + ", ");
		},
		error: function() {
				alert( "Err");
		}
	}); */
}


