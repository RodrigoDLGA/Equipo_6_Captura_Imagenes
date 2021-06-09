<?php
/*  Observación:
PHP scripts run as the user 'nobody'. A typical directory on your account will be owned by your user ID, and you cannot change the owner or group of a directory (this can only be done by the 'root' superuser). To grant a PHP write permissions in a directory owned by you, the only option you have is to set the directory's permissions to 0777 (world-writeable). There is some security risk in this, so you should not set 0777 permissions on more directories than necessary, but this is a "necessary evil" in a shared server environment. 
*/

/* Observacion : instalar paquete php5-gd para Ubuntu... 	*/

	
	$servidor="localhost";
	$usuario="id15584989_ger_admin";
	$contrasena='F@%$awUNsv45Px[^';
	$bd="id15584989_libreria2";
	$conexion;
	
	function conectar($servidor,$usuario,$contrasena,$bd){
		$conexion=mysqli_connect($servidor,$usuario,$contrasena,$bd);
	
		if(!$conexion){
			die('Ocurrió un error al hacer la conexión con la bd');
		} else {
		}
		return $conexion;
	}
	
	function desconectar($conexion){
		mysqli_close($conexion);
	}

	
	$conexion=conectar($servidor,$usuario,$contrasena,$bd);

	$consulta="SELECT id_proyecto, nombre_proyecto, fecha_creacion_proyecto from proyectos";
	$registros=mysqli_query($conexion,$consulta);
	
	if($registros->num_rows>0){
		while($fila=$registros->fetch_assoc()){
			echo $fila["nombre_proyecto"]."|";
		}
	} else {
		echo "Error al hacer la consulta a la BD";
	}

	desconectar($conexion);
	
	
	
	
	//----------------------------------------
	die ('');

	// Checking
	if ($old != umask()) {
		die('An error occurred while changing back the umask');
}





	
?>

