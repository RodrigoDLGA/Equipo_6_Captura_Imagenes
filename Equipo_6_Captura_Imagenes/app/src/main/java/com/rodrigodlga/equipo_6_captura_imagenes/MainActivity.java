package com.rodrigodlga.equipo_6_captura_imagenes;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
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
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    static String NombreCarpetaAlmacenamientoImagenes = "AppZ1_2021";

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

    Button start_activity;
    String imagePath;

    String fileName;
    TextView TV1;

    Spinner spinner_proyectos;
    EditText nombre_proyecto;

    Chronometer simpleChronometer;
    JSONArray jsonArray2;

    boolean BloqueaArranque;
    long timeWhenStopped = 0;
    int ConteoFotos = 0;

    File sdCard; // Apuntador al directorio principal
    File dir; // Apuntador al directorio específico

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        askPermissionOnly();

        sdCard = Environment.getExternalStorageDirectory();
        dir = new File(sdCard.getAbsolutePath() + "/" + NombreCarpetaAlmacenamientoImagenes);

        dir.mkdirs();

        ctx = this;
        act = this;

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        setContentView(R.layout.activity_main);

        spinner_proyectos = findViewById(R.id.spinner_proyectos);

        //ESTO SOLO ES PARA RELLENAR EL SPINNER:
        String[] arraySpinner = new String[] {
                "1", "2", "3", "4", "5", "6", "7"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, arraySpinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_proyectos.setAdapter(adapter);
        //-------------------------------------

        nombre_proyecto = findViewById(R.id.nombre_proyecto);

        preview = new Preview(this, (SurfaceView) findViewById(R.id.surfaceView));
        preview.setKeepScreenOn(true);

        start_activity = findViewById(R.id.start_activity); //BOTON PARA TOMAR CAPTURA
        start_activity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera.takePicture(shutterCallback, rawCallback, jpegCallback); // TOMA CAPTURA
            }
        });
    }

    public void onRadioButtonClicked(View view) {
        // Si el boton ya ha sido seleccionado?
        boolean checked = ((RadioButton) view).isChecked();

        // Revisa que boton ha sido seleccionado
        switch(view.getId()) {
            case R.id.seleccionar_ex:
                if (checked)
                    nombre_proyecto.setVisibility(View.GONE);
                    spinner_proyectos.setVisibility(View.VISIBLE);
                    break;
            case R.id.crear_nuevo:
                if (checked)
                    spinner_proyectos.setVisibility(View.GONE);
                    nombre_proyecto.setVisibility(View.VISIBLE);
                    break;
        }
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

                Toast.makeText(ctx, "Resolucion: ["+z.width+"x"+z.height+"]", Toast.LENGTH_LONG).show();

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
            new SaveImageTask().execute(data);
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

                fileName = String.format("%s_%s.jpg", Prefijo, ObtenHora());
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