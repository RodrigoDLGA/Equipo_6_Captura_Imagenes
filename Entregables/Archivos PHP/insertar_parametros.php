<?php
/*  Observación:
PHP scripts run as the user 'nobody'. A typical directory on your account will be owned by your user ID, and you cannot change the owner or group of a directory (this can only be done by the 'root' superuser). To grant a PHP write permissions in a directory owned by you, the only option you have is to set the directory's permissions to 0777 (world-writeable). There is some security risk in this, so you should not set 0777 permissions on more directories than necessary, but this is a "necessary evil" in a shared server environment. 
*/

/* Observacion : instalar paquete php5-gd para Ubuntu... 	*/
	
	if(isset($_POST['entrada'])){
		
		$entrada = $_POST['entrada'];
		$salida = $_POST['salida'];
		$intervalo = $_POST['intervalo'];	
		$cantidad = $_POST['cantidad'];	
		
		//A base de datos-------------------------
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
		
		//Inserta parametros
		$consulta="INSERT INTO `parametros` (`id_parametro`, `entrada`, `salida`, `intervalo`, `cantidad`) VALUES
		(null, '$entrada', null, $intervalo, $cantidad);";
		
		if(mysqli_query($conexion,$consulta)){
			
		}else{
			echo "ERROR:Ocurrio un fallo al momento de insertar registro";
		}

		desconectar($conexion);
	} else {
		echo "ERROR:Ha accedido a este archivo de forma incorrecta</h1>";
	}
	
	
	
	
	//----------------------------------------
	die ('');

	// Checking
	if ($old != umask()) {
		die('An error occurred while changing back the umask');
}





	
?>

