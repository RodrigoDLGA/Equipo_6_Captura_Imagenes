<?php
/*  Observación:
PHP scripts run as the user 'nobody'. A typical directory on your account will be owned by your user ID, and you cannot change the owner or group of a directory (this can only be done by the 'root' superuser). To grant a PHP write permissions in a directory owned by you, the only option you have is to set the directory's permissions to 0777 (world-writeable). There is some security risk in this, so you should not set 0777 permissions on more directories than necessary, but this is a "necessary evil" in a shared server environment. 
*/

/* Observacion : instalar paquete php5-gd para Ubuntu... 	*/

	date_default_timezone_get();
	$timezone = date_default_timezone_get();
	
	if(isset($_POST['nombre_proyecto'])){
		$date = date('Y-m-d H:i:s');	
		$dato = $_POST['data'];
		$info = $_POST['info'];
		$nombre_proyecto = $_POST['nombre_proyecto'];
		echo "Hora Local: ".$date."\n";
		
		$cadena = "Archivos/";
		$cadena .= $dato;
		$cadena .= "_img.jpg";
		
		echo "Imagen Recibida y Decodificada en el Servidor... "."\n";
		
		$data = base64_decode($info);
		$im = imagecreatefromstring($data);

		// start buffering
		ob_start();
		// output jpeg (or any other chosen) format & quality
		imagejpeg($im, NULL, 85);
		// capture output to string
		$contents = ob_get_contents();
		// end capture
		ob_end_clean();

		// be tidy; free up memory
		imagedestroy($im);
		
		

		// lastly (for the example) we are writing the string to a file

		$fh = fopen($cadena, "w" );
		if ( !$fh )
			echo "fopen fallo ",$php_errormsg;
		//else
		//	echo "Jalo con MADRES ";

		fwrite( $fh, $contents );
		fclose( $fh );


	//	echo exec ('./Archivos/Programa');

		// lastly (for the example) we are writing the string to a file
		
		echo "Imagen Guadarda en el servidor "."\n";
		
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

		//Crea el proyecto si no existe
		$consulta="select * from proyectos where nombre_proyecto='$nombre_proyecto'";
		$registros=mysqli_query($conexion,$consulta);
		if($registros->num_rows==0){
			$consulta="INSERT INTO proyectos values(null, '$nombre_proyecto',now())";
			if(mysqli_query($conexion,$consulta)){
				echo "<br>Registro agregado exitosamente";
			}else{
				echo "<br>Ocurrio un fallo al momento de insertar registro";
			}
		}

		$consulta="SELECT id_proyecto from proyectos where nombre_proyecto = '".$nombre_proyecto."'";
		echo "Nombre proyecto: ".$nombre_proyecto;
		
		$registros=mysqli_query($conexion,$consulta);
		$id_proyecto=0;
		
		if($registros->num_rows>0){
			while($fila=$registros->fetch_assoc()){
				$id_proyecto = $fila["id_proyecto"];
			}
		} else {
			echo "Error al hacer la consulta a la BD";
		}
		
		echo "Id proyecto: ".$id_proyecto;

		

		//Inserta imagen al proyecto
		$consulta="INSERT INTO imagenes values(null, '".$nombre_proyecto.$date."_IMG',now(),'$cadena',$id_proyecto)";
		
		if(mysqli_query($conexion,$consulta)){
			echo "<br>Registro agregado exitosamente";
		}else{
			echo "<br>Ocurrio un fallo al momento de insertar registro";
		}

		desconectar($conexion);
	} else {
		echo "<h1>Ha accedido a este archivo de forma incorrecta</h1>";
	}
	
	
	
	
	//----------------------------------------
	die ('\nFinalizando...');

	// Checking
	if ($old != umask()) {
		die('An error occurred while changing back the umask');
}





	
?>

