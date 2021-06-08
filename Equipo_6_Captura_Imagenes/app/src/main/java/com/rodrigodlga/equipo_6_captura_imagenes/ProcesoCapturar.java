package com.rodrigodlga.equipo_6_captura_imagenes;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;

public class ProcesoCapturar extends AppCompatActivity {

    static String NombreCarpetaAlmacenamientoImagenes;

    private static final int SEGUNDOS_SEPARACION_ENTRE_FOTOGRAFIAS = 10;

    int TamañoCadenaImagen=0;
    private static final int REQUEST_ID_CALLPHONE_PERMISSION = 100;
    private static final int REQUEST_ID_READPHONE_PERMISSION = 200;
    private static final int REQUEST_ID_VIBRATE_PERMISSION = 300;
    private static final int REQUEST_ID_READCONTACTS_PERMISSION = 400;
    private static final int REQUEST_ID_ACCESS_NOTIFICATION = 500;
    int ResolucionHorizontal;
    int ResolucionVertical;

    private static final String TAG = "CamTestActivity";
    Preview preview;
    Camera camera;
    Activity act;
    Context ctx;

    String imagePath;

    String fileName, nombreProyecto;
    TextView tv_fotosRestantes, comienzo_num, cantfot_num, intervalo_num, estado_textv;
    TextView segundos_timer, tv_finaliza;
    CountDownTimer CDT_segRestantes, CDT_intervalo;

    int ConteoFotos = 0;

    File sdCard; // Apuntador al directorio principal
    File dir; // Apuntador al directorio específico

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        nombreProyecto= extras.getString("nombreProyecto");
        this.setTitle("Proyecto: "+nombreProyecto);

        askPermissionOnly();
        NombreCarpetaAlmacenamientoImagenes = "Proyecto "+nombreProyecto;
        sdCard = Environment.getExternalStorageDirectory();
        dir = new File(sdCard.getAbsolutePath() + "/" + NombreCarpetaAlmacenamientoImagenes);

        dir.mkdirs();

        ctx = this;
        act = this;

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        setContentView(R.layout.activity_proceso_capturar);

        //---------------spinner_proyectos = findViewById(R.id.spinner_proyectos);

        //ESTO ES PARA TENER VALORES INICIALES EN PARÁMETROS RECIBIDOS
        this.inicializarParametros();

        //------------nombre_proyecto = findViewById(R.id.nombre_proyecto);

        preview = new Preview(this, (SurfaceView) findViewById(R.id.surfaceView));
        preview.setKeepScreenOn(true);

    }

    //En este método se recibirán los parámetros y se asignarán en la vista.
    public void inicializarParametros(){
        CDT_intervalo=null;
        CDT_segRestantes=null;
        this.cambiarEstado(0);

        cantfot_num = findViewById(R.id.cantfot_num); //Cantidad de fotos
        intervalo_num = findViewById(R.id.intervalo_num); //Intervalo de segundos
        comienzo_num = findViewById(R.id.comienzo_num); //Fecha de inicio de la captura de fotos

        //Las siguientes líneas son de prueba.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            comienzo_num.setText(LocalDateTime.now()+"");
        }
        cantfot_num.setText("5");
        intervalo_num.setText("6");
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

                int cantidadFotosRestante = Integer.parseInt(tv_fotosRestantes.getText().toString());
                ConteoFotos++;
                cantidadFotosRestante=cantidadFotosRestante-1;
                tv_fotosRestantes.setText(cantidadFotosRestante+"");

                //Si aún hay fotos restantes, se repite el contador.
                if(cantidadFotosRestante>0){
                    CDT_intervalo.start();
                } else {
                    segundos_timer.setText("0 seg.");
                    cambiarEstado(3);
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

    public void tomarFoto(){
        camera.takePicture(shutterCallback, rawCallback, jpegCallback); // TOMA CAPTURA
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
                estado_textv.setTextColor(Color.GREEN);
                break;
        }
    }

    //Este método es para que el boton (que posteriormente se borrara) funcione.
    public void probarContadores(View view) {
        this.inicializarContadores();
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
        public void onPictureTaken(byte[] data, Camera camera) {
            new ProcesoCapturar.SaveImageTask().execute(data);
            resetCam();
            Log.d(TAG, "onPictureTaken - jpeg");
        }
    };

    private class SaveImageTask extends AsyncTask<byte[], Void, String> {

        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(byte[]... data) {
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

                fileName = String.format("%s%s_%s.jpg", NombreCarpetaAlmacenamientoImagenes, Prefijo, ObtenHora());
                File outFile = new File(dir, fileName);

                outStream = new FileOutputStream(outFile);
                outStream.write(data[0]);
                outStream.flush();
                outStream.close();

                Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length + " to " + outFile.getAbsolutePath());

                // Enviar al servidor !!!
                imagePath =  outFile.getAbsolutePath();
                refreshGallery(outFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
            }
            //return ""+faces.size();
            return Information;
        }

        @Override
        protected void onPostExecute(String x) {
            Toast.makeText(getApplicationContext(), "ImageAdquirida"+imagePath+x, Toast.LENGTH_SHORT).show();
        }

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

    // When you have the request results
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //
        // Note: If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0) {
            switch (requestCode) {
                case REQUEST_ID_CALLPHONE_PERMISSION: {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        //cargarControles();
                        Toast.makeText(getApplicationContext(), "Permission REQUEST_ID_CALLPHONE_PERMISSION Concedido!", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
                case REQUEST_ID_READPHONE_PERMISSION: {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        //writeFile();
                        //
                        Toast.makeText(getApplicationContext(), "Permission REQUEST_ID_READPHONE_PERMISSION Concedido!", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
                case REQUEST_ID_VIBRATE_PERMISSION: {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        //writeFile();
                        //
                        Toast.makeText(getApplicationContext(), "Permission REQUEST_ID_VIBRATE_PERMISSION Concedido!", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
                case REQUEST_ID_READCONTACTS_PERMISSION: {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        //writeFile();
                        //
                        Toast.makeText(getApplicationContext(), "Permission REQUEST_ID_READCONTACTS_PERMISSION Concedido!", Toast.LENGTH_SHORT).show();
                    }
                }
                break;

                case REQUEST_ID_ACCESS_NOTIFICATION: {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        //writeFile();
                        //
                        Toast.makeText(getApplicationContext(), "Permission REQUEST_ID_ACCESS_NOTIFICATION Concedido!", Toast.LENGTH_SHORT).show();
                    }

                }
                break;
            }
        } else {
            Toast.makeText(getApplicationContext(), "Permission Cancelled!", Toast.LENGTH_SHORT).show();
        }
    }


}