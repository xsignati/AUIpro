<?php
include 'SessionHandlerInterface.php';
MySessionHandler::MSH_session_start();
//session_start();
$session = session_id();
$serverName = "localhost";
$userName = "root";
$password = "";
$dbName = "DatabaseC1";
$box = array();
//$conn = new mysqli($serverName, $userName, $password, $dbName);
?>