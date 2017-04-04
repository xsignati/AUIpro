<?php
include 'index.php';
include 'library.php';

if(session_id() == '' || !isset($_SESSION['currentSessionId']) || session_id() != $_SESSION['currentSessionId']) {
	$dbInit = init($serverName, $userName, $password, $dbName);
}

if(isset($_SESSION['currentSessionId']) && session_id() == $_SESSION['currentSessionId']) {
	userInfo($serverName, $userName, $password, $dbName, $session);
}

function init($serverName, $userName, $password, $dbName){

	// Create connection
	$conn = new mysqli($serverName, $userName, $password);
	// Check connection
	if ($conn->connect_error) {
		die("Connection failed: " . $conn->connect_error);
		return false;
	} 

	// Check if database exist, select target database
	$db_selected = mysqli_select_db($conn,$dbName);

	if(!$db_selected){
		// Create database
		$sql = "CREATE DATABASE `$dbName`";
		if ($conn->query($sql) === TRUE) {
			echo "Database created successfully";
		} else {
			echo "Error creating database: " . $conn->error;
			return false;
		}
		mysqli_select_db($conn,$dbName);
		
		$sql = "CREATE TABLE General (
		ID int NOT NULL AUTO_INCREMENT,
		sessionID VARCHAR(40) NOT NULL,
		IPaddress VARCHAR(45),
		serverTime VARCHAR(25),
		userAgent VARCHAR(255),
		agentName VARCHAR(255),
		agentVersion VARCHAR(255),
		platform VARCHAR(50),
		agentPattern VARCHAR(255),
		PRIMARY KEY (ID)
		)";

		if ($conn->query($sql) === TRUE) {
			echo "Table created successfully";
		} else {
			echo "Error creating table General: " . $conn->error;
			return false;
		}
		
		//create mouse-click table
		$sql = "CREATE TABLE MouseTracks (
		ID int NOT NULL AUTO_INCREMENT,
		sessionID VARCHAR(40) NOT NULL,
		action VARCHAR(12),
		time BIGINT(60),
		x INT(255),
		y INT(255),
		PRIMARY KEY (ID)
		)";
		if ($conn->query($sql) === TRUE) {
			echo "Table MouseTracks created successfully";
		} else {
			echo "Error creating table MouseTracks: " . $conn->error;
			return false;
		}
		
		//create cursor-curves table
		$sql = "CREATE TABLE MouseCurves (
		ID int NOT NULL AUTO_INCREMENT,
		sessionID VARCHAR(40) NOT NULL,
		curveID VARCHAR(40),
		action VARCHAR(12),
		time BIGINT(60),
		x INT(255),
		y INT(255),
		PRIMARY KEY (ID)
		)";
		if ($conn->query($sql) === TRUE) {
			echo "Table MouseCurves created successfully";
		} else {
			echo "Error creating table MouseCurves: " . $conn->error;
			return false;
		}
		
		//create curveParameters table
		$sql = "CREATE TABLE CurveParameters  (
		ID int NOT NULL AUTO_INCREMENT,
		sessionID VARCHAR(40) NOT NULL,
		curveID VARCHAR(40),
		angleAB DECIMAL(6,2),
		angleABC DECIMAL(6,2),
		ratioBAC DECIMAL(10,3),
		firstPointTime BIGINT(60),
		PRIMARY KEY (ID)
		)";
		if ($conn->query($sql) === TRUE) {
			echo "Table MouseCurves created successfully";
		} else {
			echo "Error creating table MouseCurves: " . $conn->error;
			return false;
		}
		//create inputs table
		$sql = "CREATE TABLE blocks  (
		ID int NOT NULL AUTO_INCREMENT,
		sessionID VARCHAR(40) NOT NULL,
		blockID INT(40),
		feature DECIMAL(4,3),
		featID INT(3),
		PRIMARY KEY (ID)
		)";
		if ($conn->query($sql) === TRUE) {
			echo "Table blocks created successfully";
		} else {
			echo "Error creating table blocks: " . $conn->error;
			return false;
		}	

		
	}
	
	

	$_SESSION['currentSessionId'] = session_id();
	$conn->close();
	return true;
}

function userInfo($serverName, $userName, $password, $dbName, $session){
	
	$conn = new mysqli($serverName, $userName, $password, $dbName);
	
	//get userInfo
	$IPaddress = mysqli_real_escape_string($conn,get_ip_address());
	$serverTime = mysqli_real_escape_string($conn,getServerTime());
	$agentInfo = getBrowser();
	$userAgent = mysqli_real_escape_string($conn,$agentInfo['userAgent']);
	$agentName = mysqli_real_escape_string($conn,$agentInfo['name']);
	$agentVersion = mysqli_real_escape_string($conn,$agentInfo['version']);
	$platform = mysqli_real_escape_string($conn,$agentInfo['platform']);
	$agentPattern = mysqli_real_escape_string($conn,$agentInfo['pattern']);

	
	//write userInfo to database
/* 	$conn->query("INSERT INTO General (sessionID, IPaddress, serverTime, userAgent, agentName, agentVersion
	platform, agentPattern)
	VALUES ( '$session', '$IPaddress', '$serverTime', '$userAgent', '$agentName', '$agentVersion', '$platform', '$agentPattern' )");
	$conn->close(); */
	$sql=	"INSERT INTO General 
				(sessionID, IPaddress, serverTime, userAgent, agentName, agentVersion,
				platform, agentPattern)
			SELECT * FROM
			(SELECT
				'$session', '$IPaddress', '$serverTime', '$userAgent', '$agentName', '$agentVersion', '$platform', '$agentPattern') as tmp
			WHERE NOT EXISTS (SELECT *
							 FROM General
							 WHERE  sessionID = '$session'
							 AND	IPaddress = '$IPaddress'
							 AND	agentName = '$agentName'
							 AND	userAgent = '$userAgent'
							 AND	agentVersion = '$agentVersion'
							 AND	platform = '$platform'
							 AND	agentPattern = '$agentPattern')
			";
	if ($conn->query($sql) === TRUE) {
		echo ("zapisano nowa osobe");
	} else {
		echo "Error inserting into General: " . $conn->error;
		return false;
	}
}
?>