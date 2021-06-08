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
import android.graphics.Color;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import java.time.LocalDateTime;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    Spinner spinner_proyectos;
    EditText nombre_proyecto;
    Button btn_siguiente;
    boolean nuevoProyecto;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nuevoProyecto=false;

        setContentView(R.layout.activity_main);

        spinner_proyectos = findViewById(R.id.spinner_proyectos);


        //ESTO SOLO ES PARA RELLENAR EL SPINNER:

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, this.cargarSpinner());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_proyectos.setAdapter(adapter);


        nombre_proyecto = findViewById(R.id.nombre_proyecto);


    }

    public void onRadioButtonClicked(View view) {
        btn_siguiente = findViewById(R.id.btn_siguiente);
        btn_siguiente.setEnabled(true);
        // Si el boton ya ha sido seleccionado?
        boolean checked = ((RadioButton) view).isChecked();


        // Revisa que boton ha sido seleccionado
        switch(view.getId()) {
            case R.id.seleccionar_ex:
                if (checked)
                    nombre_proyecto.setVisibility(View.GONE);
                    spinner_proyectos.setVisibility(View.VISIBLE);
                    nuevoProyecto=false;
                    break;
            case R.id.crear_nuevo:
                if (checked)
                    spinner_proyectos.setVisibility(View.GONE);
                    nombre_proyecto.setVisibility(View.VISIBLE);
                    nuevoProyecto=true;
                    break;
        }
    }

    //En este método se creara un array con todos los proyectos existentes
    public String[] cargarSpinner(){
        String[] arraySpinner = new String[] {
                "1", "2", "3", "4", "5", "6", "7"
        };
        return arraySpinner;
    }

    public void siguiente(View view) {
        if(nuevoProyecto){
            if(!nombre_proyecto.getText().toString().isEmpty()){
                String nombreProyecto = nombre_proyecto.getText().toString();

                Intent intent = new Intent(MainActivity.this, ProcesoCapturar.class);
                intent.putExtra("nombreProyecto", nombreProyecto);
                startActivity(intent);


            } else {
                nombre_proyecto.setError("Complete el campo");
            }
        } else {
            String nombreProyecto = spinner_proyectos.getSelectedItem().toString();

            Intent intent = new Intent(MainActivity.this, ProcesoCapturar.class);
            intent.putExtra("nombreProyecto", nombreProyecto);
            startActivity(intent);

        }
    }
}