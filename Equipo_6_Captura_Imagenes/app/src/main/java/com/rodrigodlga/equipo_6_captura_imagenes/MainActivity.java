package com.rodrigodlga.equipo_6_captura_imagenes;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    RadioButton seleccionar_ex;
    String todosLosProyectos;
    Spinner spinner_proyectos;
    EditText nombre_proyecto;
    Button btn_siguiente;
    boolean nuevoProyecto, finalizado;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nuevoProyecto=false;
        finalizado=false;

        setContentView(R.layout.activity_main);

        spinner_proyectos = findViewById(R.id.spinner_proyectos);
        seleccionar_ex = findViewById(R.id.seleccionar_ex);

        //ESTO SOLO ES PARA RELLENAR EL SPINNER:
        String[] datosCargados = this.cargarSpinner();
        if(datosCargados.length==1&&datosCargados[0].contains("Error al hacer la consulta a la BD")){
            seleccionar_ex.setEnabled(false);
        } else {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, datosCargados);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner_proyectos.setAdapter(adapter);
        }
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
        new GetMethodDemo().execute("http://www.gloperenab.me/Proyecto1Equipo/consulta_proyectos.php");

        while(!finalizado){
            Log.d("casio","a");
            //Esperando a que finalizado cambie a true
        }
        if(finalizado){
            Log.d("casio","Cambio a true");
        }
        String[] arraySpinner = todosLosProyectos.split("\\|");
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

    public class GetMethodDemo extends AsyncTask<String , Void ,String> {
        String server_response;
        Map<String,Object> params;

        @Override
        protected void onPreExecute() {
            params = new LinkedHashMap<>();
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
            finalizado=true;
            todosLosProyectos = server_response;

            //server_response = Cadena;
            return (server_response);
        }

        @Override
        protected void onPostExecute(String s) {
            finalizado=true;
            Log.d("Response-s", "" + s);
            Log.d("Response-server", "" + server_response);
        }
    }

}