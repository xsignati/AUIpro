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
			data: JSON.stringify(clicksBuffer),
			complete: function() {
				//$("#loading").hide();
			}
		});
		//release the buffer, reset iterator
		clicksBuffer = [];
		bufferIterator = 0;
	}
	else{
		clicksBuffer[bufferIterator] = {x: cursorX, y: cursorY, time: moveTime, action: actionType};
		bufferIterator++;
	}
}


