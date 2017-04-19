<?php
include 'index.php';
//session_write_close();
ignore_user_abort(true);
set_time_limit(0);

if(isset($_SESSION['currentSessionId']) && session_id() == $_SESSION['currentSessionId']) {
	$conn = new mysqli($serverName, $userName, $password, $dbName);
	$session = mysqli_real_escape_string($conn, $session);
	$bData = json_decode(file_get_contents("php://input"));
	 foreach($bData as $key => $value){
		 if($value){
			$conn->query("INSERT INTO MouseTracks (sessionID, action, time, x, y)
			VALUES ( '$session', '$value->action', '$value->time', '$value->x', '$value->y')");
		 }
	 }
	$conn->close();
}


?>