<?php
include 'index.php';
//session_write_close();
ignore_user_abort(true);
set_time_limit(0);

if(isset($_SESSION['currentSessionId']) && session_id() == $_SESSION['currentSessionId']) {
	$conn = new mysqli($serverName, $userName, $password, $dbName);

	$session = mysqli_real_escape_string($conn, $session);
	/* $bData = json_decode($_POST['bufferData']); */
	$bData = json_decode(file_get_contents("php://input"));
	 foreach($bData as $key => $value){
		 if($value){
			$conn->query("INSERT INTO MouseTracks (sessionID, action, time, x, y)
			VALUES ( '$session', '$value->action', '$value->time', '$value->x', '$value->y')");
		 }
	 }

/* 	$file = 'ubi.txt';
	$current = file_get_contents($file);
	$current .= "done\n";
	file_put_contents($file, $current); */
	//old method
/* 	$xxx = mysqli_real_escape_string($conn, $_POST['x']);
	$yyy = mysqli_real_escape_string($conn, $_POST['y']);
	$time = mysqli_real_escape_string($conn, $_POST['time']);
	$action = mysqli_real_escape_string($conn, $_POST['action']);
	$session = mysqli_real_escape_string($conn, $session); */
	
/* 	$sql2 = "INSERT INTO MouseTracks (sessionID, action, time, x, y)
	VALUES ( '$session', '$action', '$time', '$xxx', '$yyy')"; */
	//$conn->query($sql2);
/* 	if ($conn->query($sql2) === TRUE) {
		echo "query succesfull";
	} else {
		echo "Error  " . $conn->error;
		return false;
	} */
	$conn->close();
}


?>