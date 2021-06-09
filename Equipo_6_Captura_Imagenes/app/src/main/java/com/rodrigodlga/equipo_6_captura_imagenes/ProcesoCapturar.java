package com.rodrigodlga.equipo_6_captura_imagenes;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.threetenabp.AndroidThreeTen;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.threeten.bp.Duration;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.temporal.ChronoUnit;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProcesoCapturar extends AppCompatActivity {

    static String NombreCarpetaAlmacenamientoImagenes;
    String strdate = null;
    int diferenciaFecha;
    List<String> fotosCapturadas, fotosCodificadas, fotosFechas;
    int contadorError=0;
    int TamañoCadenaImagen=0;
    private static final int REQUEST_ID_CALLPHONE_PERMISSION = 100;
    private static final int REQUEST_ID_READCONTACTS_PERMISSION = 400;
    private static final int STORAGE_PERMISSION_CODE = 101;
    int ResolucionHorizontal;
    int ResolucionVertical;

    GetMethodDemo task;
    private static final String TAG = "CamTestActivity";
    Preview preview;
    Camera camera;
    Activity act;
    Context ctx;
    int parametrosIniciales;
    boolean finalizado;
    String imagePath, cargaParametros, ImagenCodificada;
    Bitmap bMap;
    Bitmap myBitMap;
    Button btn_intervalo;

    String fileName, nombreProyecto;
    TextView tv_fotosRestantes, comienzo_num, cantfot_num, intervalo_num, estado_textv;
    TextView segundos_timer, tv_finaliza;
    CountDownTimer CDT_segRestantes, CDT_intervalo, CDT_Fecha;

    int ConteoFotos = 0;

    File sdCard; // Apuntador al directorio principal
    File dir; // Apuntador al directorio específico

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidThreeTen.init(this);
        fotosCapturadas = new ArrayList<String>();
        fotosCodificadas = new ArrayList<String>();
        fotosFechas = new ArrayList<String>();
        finalizado=false;
        parametrosIniciales = contabilizarParametrosPrevios();
        task.cancel(true);

        Log.d("consola","Parametros iniciales encontrados: "+parametrosIniciales);
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        nombreProyecto= extras.getString("nombreProyecto");
        this.setTitle("Proyecto: "+nombreProyecto);

        askPermissionOnly();
        NombreCarpetaAlmacenamientoImagenes = "Proyecto_"+nombreProyecto;
        sdCard = Environment.getExternalStorageDirectory();
        dir = new File(sdCard.getAbsolutePath() + "/" + NombreCarpetaAlmacenamientoImagenes);

        dir.mkdirs();

        ctx = this;
        act = this;

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_proceso_capturar);

        preview = new Preview(this, (SurfaceView) findViewById(R.id.surfaceView));
        preview.setKeepScreenOn(true);

        checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                STORAGE_PERMISSION_CODE);

        //Mantener actividad en estado de espera de una orden
        //this.esperandoConsulta();
        //new EsperandoConsulta().execute();


    }

    public void esperandoConsulta(){

        while(!(contabilizarParametrosPrevios()>parametrosIniciales)){
            btn_intervalo = findViewById(R.id.btn_intervalo);
            btn_intervalo.setEnabled(false);
            Log.d("consola","Inicial: "+parametrosIniciales+", actual: "+contabilizarParametrosPrevios());
            task.cancel(true);
            Log.d("consola","Esperando parametros");
            //Esperar 5 segundos
            try {
                Thread.sleep(5*1000);
            } catch (Exception e) {
            }
        }
        Log.d("consola","-- Parametros encontrados");
        task.cancel(true);

        task = (GetMethodDemo) new GetMethodDemo().execute("http://www.gloperenab.me/Proyecto1Equipo/parametros_entrada.php");
        String cargaParametros2=cargaParametros.substring(cargaParametros.indexOf("+")+1);
        String[] parametros = cargaParametros2.split("\\|");
        Log.d("consola", parametros[0]+" "+parametros[1]+" "+parametros[2]);
        inicializarParametros(parametros[0],parametros[1],parametros[2]);
        inicializarEnFecha();
    }

    public int contabilizarParametrosPrevios(){
        task = (GetMethodDemo) new GetMethodDemo().execute("http://www.gloperenab.me/Proyecto1Equipo/consulta_parametros.php");
        while(!finalizado){
            Log.d("casio","a");
            //Esperando a que finalizado cambie a true
        }
        if(finalizado){
            Log.d("casio","Cambio a true");
        }
        Log.d("casio",cargaParametros+" <<<");
        String cargaParametros2=cargaParametros.substring(0,cargaParametros.indexOf("+"));
        String[] arraySpinner = cargaParametros2.split("\\|");
        int cantidad = arraySpinner.length;
        return cantidad;
    }

    //En este método se recibirán los parámetros y se asignarán en la vista.
    @SuppressLint("NewApi")
    public void inicializarParametros(String fecha_inicio, String intervalo, String cantidad){
        CDT_intervalo=null;
        CDT_segRestantes=null;
        CDT_Fecha=null;
        this.cambiarEstado(0);

        cantfot_num = findViewById(R.id.cantfot_num); //Cantidad de fotos
        intervalo_num = findViewById(R.id.intervalo_num); //Intervalo de segundos
        comienzo_num = findViewById(R.id.comienzo_num); //Fecha de inicio de la captura de fotos

        //Las siguientes líneas son de prueba.
        comienzo_num.setText(fecha_inicio);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime comienzo = LocalDateTime.parse(fecha_inicio, formatter);

        Log.d("Horas","comienzo: "+comienzo);

        ZoneId zona = ZoneId.of("America/Mexico_City");
        LocalDateTime ahora = LocalDateTime.now(zona);
        String actualS=""+ahora;
        actualS = actualS.substring(0, actualS.indexOf("."));
        actualS = actualS.replace("T"," ");
        LocalDateTime actual = LocalDateTime.parse(actualS, formatter);
        Log.d("Horas","actual: "+actual);

        Duration diferencia = Duration.between(actual, comienzo);
        diferenciaFecha = (int) diferencia.get(ChronoUnit.SECONDS);
        //diferenciaFecha = diferenciaFecha - (60*60);
        Log.d("Horas","diferencia: "+diferencia.get(ChronoUnit.SECONDS));
        cantfot_num.setText(cantidad);
        intervalo_num.setText(intervalo);
    }

    public void inicializarEnFecha(){
        btn_intervalo = findViewById(R.id.btn_intervalo);
        btn_intervalo.setEnabled(false);
        if(diferenciaFecha<=0){
            inicializarContadores();
        } else {
            this.cambiarEstado(3);
            //Iniciar contador para iniciar captura de fotos
            int Valor = diferenciaFecha*1000;
            //CrearCDTIniciarCaptura(Valor);
            //CDT_restanteCaptura.start();
            CrearCDTFecha(Valor);
            CDT_Fecha.start();
        }
    }

    public void inicializarContadores(){
        this.cambiarEstado(1);
        cantfot_num = findViewById(R.id.cantfot_num);
        intervalo_num = findViewById(R.id.intervalo_num);
        tv_fotosRestantes = findViewById(R.id.tv_fotosRestantes);
        tv_fotosRestantes.setText(cantfot_num.getText().toString());

        //Iniciar contador intervalo de captura de fotos
        if(!intervalo_num.getText().toString().isEmpty()){
            int Valor = Integer.parseInt(intervalo_num.getText().toString())*1000;
            CrearCDTIntervalo (Valor);
            CDT_intervalo.start();
        } else {
            //Ocurrió algún error
        }

        //Iniciar contador de segundos restantes
        if(!(cantfot_num.getText().toString().isEmpty() && intervalo_num.getText().toString().isEmpty())){
            int tiempo = Integer.parseInt(cantfot_num.getText().toString()) * Integer.parseInt(intervalo_num.getText().toString());
            int Valor = tiempo*1000;
            CrearCDTFinaliza (Valor);
            CDT_segRestantes.start();
        } else {
            //Ocurrió algún error
        }
    }

    void CrearCDTIntervalo (int ValorCuenta) {
        cantfot_num = findViewById(R.id.cantfot_num);
        segundos_timer = findViewById(R.id.segundos_timer);
        tv_finaliza = findViewById(R.id.tv_finaliza);
        CDT_intervalo = new CountDownTimer(ValorCuenta,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                segundos_timer.setText(millisUntilFinished / 1000+" seg.");
            }

            @Override
            public void onFinish() {
                //Llamar método para tomar la foto.
                tomarFoto();
                //aca

                int cantidadFotosRestante = Integer.parseInt(tv_fotosRestantes.getText().toString());
                ConteoFotos++;
                cantidadFotosRestante=cantidadFotosRestante-1;
                tv_fotosRestantes.setText(cantidadFotosRestante+"");

                //Si aún hay fotos restantes, se repite el contador.
                if(cantidadFotosRestante>0){
                    CDT_intervalo.start();
                } else {
                    segundos_timer.setText("0 seg.");
                    cambiarEstado(2);
                    Log.d("consola","Fotos capturadas correctamente: "+fotosCapturadas.size());
                }

            }
        };
    }

    void CrearCDTFinaliza (int ValorCuenta) {
        cantfot_num = findViewById(R.id.cantfot_num);
        intervalo_num = findViewById(R.id.intervalo_num);
        tv_finaliza = findViewById(R.id.tv_finaliza);

        CDT_segRestantes = new CountDownTimer(ValorCuenta,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tv_finaliza.setText("Finaliza en: "+millisUntilFinished / 1000+" seg.");
            }

            @Override
            public void onFinish() {
                tv_finaliza.setText("Proceso finalizado.");
            }
        };
    }

    void CrearCDTFecha (int ValorCuenta){
        CDT_Fecha = new CountDownTimer(ValorCuenta, 1000) {

            public void onTick(long millisUntilFinished) {
                long secondsInMilli = 1000;
                long minutesInMilli = secondsInMilli * 60;
                long hoursInMilli = minutesInMilli * 60;

                long elapsedHours = millisUntilFinished / hoursInMilli;
                millisUntilFinished = millisUntilFinished % hoursInMilli;

                long elapsedMinutes = millisUntilFinished / minutesInMilli;
                millisUntilFinished = millisUntilFinished % minutesInMilli;

                long elapsedSeconds = millisUntilFinished / secondsInMilli;

                String hms = String.format("%02d:%02d:%02d", elapsedHours, elapsedMinutes, elapsedSeconds);

                estado_textv.setText("Estado: Pendiente, restan "+hms);
            }

            public void onFinish() {
                inicializarContadores();
            }
        };
    }

    public void tomarFoto(){
        camera.takePicture(shutterCallback, rawCallback, jpegCallback); // TOMA CAPTURA
    }

    public void subirFoto(){
        Log.d("consola",""+imagePath);
        new GetMethodDemo().execute("http://gloperenab.me/Proyecto1Equipo/img_servidor.php");
    }

    public void cambiarEstado(int  estado){
        estado_textv = findViewById(R.id.estado_textv);
        switch (estado){
            case 0:
                estado_textv.setText("Estado: En espera de instrucción");
                estado_textv.setTextColor(Color.GRAY);
                break;
            case 1:
                estado_textv.setText("Estado: En progreso.");
                estado_textv.setTextColor(Color.rgb(255, 164, 032));
                break;
            case 2:
                estado_textv.setText("Estado: Finalizado");
                estado_textv.setTextColor(Color.rgb(45,87,44));
                break;
            case 3:
                estado_textv.setText("Estado: Cargando.");
                estado_textv.setTextColor(Color.rgb(87, 35, 100));
                break;
        }
    }

    //Este método es para que el boton (que posteriormente se borrara) funcione.
    public void probarContadores(View view) {
        esperandoConsulta();
    }

    String ObtenHora () {
        //DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm:ss");
        DateFormat df = new SimpleDateFormat("yyyy:MM:dd,HH:mm:ss");
        String date = df.format(Calendar.getInstance().getTime());
        return date;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Inicializar cameras
        int numCams = Camera.getNumberOfCameras();
        if(numCams > 0){
            try{
                camera = Camera.open(0); // Camara Posterior
                //camera = Camera.open(1); // Camara fontal_

                Camera.Parameters p = camera.getParameters();
                Camera.Size z = p.getPictureSize();

                //Toast.makeText(ctx, "Resolucion: ["+z.width+"x"+z.height+"]", Toast.LENGTH_LONG).show();

                ResolucionHorizontal = z.width;
                ResolucionVertical = z.height;

                camera.setDisplayOrientation(90);
                camera.startPreview();

                preview.setCamera(camera);

            } catch (RuntimeException ex){
                Toast.makeText(ctx, "R.string.camera_not_found", Toast.LENGTH_LONG).show();
            }
        }

        //FUNCION DEL CRONOMETRO

        /*
        Se usa de la siguiente manera:

        simpleChronometer.setBase(SystemClock.elapsedRealtime());
        simpleChronometer.start();
        */

        /* // *** Descomenta esta sección para hacer pruebas ***
        simpleChronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                long elapsedMillis = SystemClock.elapsedRealtime() - chronometer.getBase();
                int Multiplicador = (int) (elapsedMillis/1000);
                if ((Multiplicador % SEGUNDOS_SEPARACION_ENTRE_FOTOGRAFIAS)==1) {
                    //ConteoFotos = 0;

                    //if (ConteoFotos>0) { // Tomar FOTO
                    camera.takePicture(shutterCallback, rawCallback, jpegCallback);
                    Toast.makeText(ctx, " Tiempo " + elapsedMillis + "Captutar Foto [" + ConteoFotos + "]", Toast.LENGTH_LONG).show();
                    //}
                    // Guardar conteo de fotos
                    ConteoFotos++;
                }
                return;
            }
        });*/
    }

    @Override
    protected void onPause() {
        if(camera != null) {
            //cancelTimer();
            camera.stopPreview();
            preview.setCamera(null);
            camera.release();
            camera = null;
        }
        super.onPause();
        //simpleChronometer.stop();


    }

    private void resetCam() {
        camera.startPreview();
        preview.setCamera(camera);
    }

    private void refreshGallery(File file) {
        Intent mediaScanIntent = new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(file));
        sendBroadcast(mediaScanIntent);
    }

    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
            //			 Log.d(TAG, "onShutter'd");
        }
    };

    Camera.PictureCallback rawCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            //			 Log.d(TAG, "onPictureTaken - raw");
        }
    };

    Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        public void onPictureTaken(byte[] data, Camera camera) {
            /*
            SaveImageTask a = (SaveImageTask) CompletableFuture.supplyAsync(() -> {
                    return new SaveImageTask().execute(data);
                }).get(2500, TimeUnit.MILLISECONDS);
             */
            SaveImageTask(data);

            if(imagePath!=null){
                fotosCapturadas.add(imagePath);
                fotosCodificadas.add(ImagenCodificada);
                fotosFechas.add(strdate);
            }
            Log.d("consola","fotos->"+fotosCapturadas.size());

            resetCam();
            Log.d(TAG, "onPictureTaken - jpeg");
        }
    };

    public void SaveImageTask(byte[]... data){
        FileOutputStream outStream = null;

        //Frame frame;
        //SparseArray<Face> faces = null;
        String Information = "";

        // Write to SD Card
        try {
            //File sdCard = Environment.getExternalStorageDirectory();
            //File dir = new File (sdCard.getAbsolutePath() + "/"+NombreCarpetaAlmacenamientoImagenes);
            //dir.mkdirs();

            String Prefijo="";
            if ((ConteoFotos-1)<10) {
                Prefijo="000"+(ConteoFotos-1);
            }
            else {
                if ((ConteoFotos-1)<100) {
                    Prefijo="00"+(ConteoFotos-1);
                }
                else {
                    if ((ConteoFotos-1)<1000) {
                        Prefijo="0"+(ConteoFotos-1);
                    }
                    else {
                        Prefijo=""+(ConteoFotos-1);
                    }
                }

            }

            Calendar c = Calendar.getInstance();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");

            if (c!= null) {
                strdate = sdf.format(c.getTime());
            }
            fileName = String.format("%s%s_%s.jpg", NombreCarpetaAlmacenamientoImagenes, Prefijo, ObtenHora());

            File outFile = new File(dir,fileName);

            outStream = new FileOutputStream(outFile);
            outStream.write(data[0]);
            outStream.flush();
            imagePath =  outFile.getAbsolutePath();
            outStream.close();

            Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length + " to " + outFile.getAbsolutePath());

            // Enviar al servidor !!!
            //imagePath =  dir+"/"+fileName;
            refreshGallery(outFile);

            CargarFotografiaGaleria C = new CargarFotografiaGaleria ();
            C.execute(outFile.getAbsolutePath());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }

        //OnPost
        contadorError++;
        String ruta = ""+imagePath;
        Log.d("consola","ruta "+contadorError+" "+ruta);
        Toast.makeText(getApplicationContext(), contadorError+":"+ruta, Toast.LENGTH_SHORT).show();
    }

    private void askPermissionOnly() {

        this.askPermission(REQUEST_ID_READCONTACTS_PERMISSION, Manifest.permission.CAMERA);

        this.askPermission(REQUEST_ID_CALLPHONE_PERMISSION, Manifest.permission.WRITE_EXTERNAL_STORAGE);

    }

    // With Android Level >= 23, you have to ask the user
    // for permission with device (For example read/write data on the device).
    private boolean askPermission(int requestId, String permissionName) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {


            // Check if we have permission
            //int permission = android.support.v4.app.ActivityCompat.checkSelfPermission(this, permissionName);
            int permission = ActivityCompat.checkSelfPermission(this, permissionName);


            if (permission != PackageManager.PERMISSION_GRANTED) {
                // If don't have permission so prompt the user.
                this.requestPermissions(new String[]{permissionName}, requestId);
                return false;
            }
        }
        return true;
    }

    public void actualizar(View view) {
        Intent intent = new Intent(ProcesoCapturar.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private class CargarFotografiaGaleria extends AsyncTask<String, Void, Bitmap> {
        //String CadenaServidor;
        //String Servidor;
        //String imagePath;
        //Bitmap bMap;

        @Override
        protected Bitmap doInBackground(String... params) {
            String response = "";
            Log.d("consola","a: "+imagePath);
            bMap= BitmapFactory.decodeFile(imagePath); // ORIGINAL
            myBitMap = decodeScaledBitmapFromSdCard(imagePath, 400, 400); // REDIMENSIONADO
            /*                IM1.setImageBitmap(myBitMap);
             */
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            //myBitMap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            myBitMap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] bitmapdata = stream.toByteArray();
            // get the base 64 string
            ImagenCodificada = Base64.encodeToString(bitmapdata, Base64.NO_WRAP);
            TamañoCadenaImagen = ImagenCodificada.length();

            //return imagePath;
            return myBitMap;

        }

        @SuppressLint("WrongThread")
        @Override
        protected void onPostExecute(Bitmap result) {
            if(fotosCapturadas.size()==Integer.parseInt(cantfot_num.getText().toString())){
                for (int i = 0; i<fotosCapturadas.size();i++){
                    imagePath=fotosCapturadas.get(i);
                    strdate = fotosFechas.get(i);

                    bMap= BitmapFactory.decodeFile(imagePath);
                    myBitMap = decodeScaledBitmapFromSdCard(imagePath, 400, 400);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    myBitMap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byte[] bitmapdata = stream.toByteArray();
                    ImagenCodificada = Base64.encodeToString(bitmapdata, Base64.NO_WRAP);
                    TamañoCadenaImagen = ImagenCodificada.length();

                    Log.d("consola","Subiendo imagen "+i+": "+imagePath);
                    Log.d("consola","- Fecha: "+strdate);
                    Log.d("consola","- Codif: "+ImagenCodificada);
                    subirFoto();
                }
            }
        }

    }

    public static Bitmap decodeScaledBitmapFromSdCard(String filePath,
                                                      int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }


    public class GetMethodDemo extends AsyncTask<String , Void ,String> {
        String server_response;
        Map<String,Object> params;

        @Override
        protected void onPreExecute() {

            params = new LinkedHashMap<>();

            params.put("nombre_proyecto", nombreProyecto);
            params.put("data", nombreProyecto+"_"+strdate); // Nombre con el que se guardara la imagen
            params.put("info", ImagenCodificada); // Imagen codificada en BASE64

        }

        @Override
        protected String doInBackground(String... strings) {


            URL url = null;
            try {
                url = new URL(strings[0]);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }


            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String,Object> param : params.entrySet()) {
                if (postData.length() != 0) postData.append('&');
                try {
                    postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                postData.append('=');
                try {
                    postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            byte[] postDataBytes = new byte[0];
            try {
                postDataBytes = postData.toString().getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection)url.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                conn.setRequestMethod("POST");
            } catch (ProtocolException e) {
                e.printStackTrace();
            }
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            conn.setDoOutput(true);
            try {
                conn.getOutputStream().write(postDataBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Reader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }

            StringBuilder sb = new StringBuilder();
            int c = 100;
            try {
                c = in.read();
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (c >= 0) {
                sb.append((char)c);
                try {
                    c = in.read();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //for (int c; (c = in.read()) >= 0;)

            server_response = sb.toString();
            cargaParametros=server_response;
            finalizado=true;

            //server_response = Cadena;
            return (server_response);
        }

        @Override
        protected void onPostExecute(String s) {

            Log.e("Response-s", "" + s);
            Log.e("Response-server", "" + server_response);

        }
    }

    // Solicitar Permiso de Acceso a Archivos
    // Function to check and request permission
    public void checkPermission(String permission, int requestCode)
    {

        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(ProcesoCapturar.this, permission)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(
                    ProcesoCapturar.this,
                    new String[] { permission },
                    requestCode);
        }
        else {
            Toast.makeText(ProcesoCapturar.this,
                    "Permission already granted",
                    Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super
                .onRequestPermissionsResult(requestCode,
                        permissions,
                        grantResults);

        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(ProcesoCapturar.this,
                        "Storage Permission Granted",
                        Toast.LENGTH_SHORT)
                        .show();
            }
            else {
                Toast.makeText(ProcesoCapturar.this,
                        "Storage Permission Denied",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

}