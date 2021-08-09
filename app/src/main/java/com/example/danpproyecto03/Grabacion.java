package com.example.danpproyecto03;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.HashMap;

public class Grabacion extends AppCompatActivity {
    ImageButton btnStopRecord, btnStop; // Botones de grabar audio, parar grabación, Iniciar reproducción y Parar reproducción
    ImageButton btnRecord, btnPlay;
    EditText txtNombreSonido;
    Spinner selectSOnidos;
    HashMap<String,String> sonidos_guardados = new HashMap<String,String>();
    String[] sonidosToSpinner = {"Seleccione"};
    String pathNew =""; //
    String pathSave = ""; // Dirección del sonido guardado a utilizar por todos
    MediaRecorder mediaRecorder;
    MediaPlayer mediaPlayer;
    final int REQUEST_PERMISSION_CODE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grabacion);
        if (!checkPermissionFromDevice())
            requestPermission();
        init_GRAVAR_ESCUCHAR_SONIDO();



    }
    private void init_GRAVAR_ESCUCHAR_SONIDO(){
        //INICIALIZANDO COMPONENTES - GRABAR AUDIO Y REPRODUCIR
        btnPlay =  findViewById(R.id.btn_play);
        btnStop = findViewById(R.id.btn_stop);
        btnRecord = findViewById(R.id.btn_StartRecord);
        btnStopRecord =findViewById(R.id.btn_StopRecord);
        txtNombreSonido = findViewById(R.id.txt_new_sound);
        selectSOnidos = findViewById(R.id.select_sounds);

        selectSOnidos.setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item,sonidosToSpinner));

        selectSOnidos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // Your code here
                //actualizarSonidos();
                pathSave = sonidos_guardados.get(selectSOnidos.getSelectedItem());
            }
            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });

        // Evento Botón iniciar grabación
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (checkPermissionFromDevice())
                {
                    pathNew = Environment.getExternalStorageDirectory()
                            .getAbsolutePath()+"/"
                            + txtNombreSonido.getText().toString()+".3gp"; //_audio_record antes UUID.randomUUID().toString()+
                    System.out.println("PATH - "+ pathNew); // impresión de la dirección del audio guardado
                    setupMediaRecorder();
                    try {
                        mediaRecorder.prepare();
                        mediaRecorder.start();
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                    btnPlay.setEnabled(false);
                    btnStop.setEnabled(false);
                    btnStopRecord.setEnabled(true);
                    Toast.makeText(Grabacion.this, "Grabando audio..", Toast.LENGTH_SHORT).show();

                }
                else{
                    requestPermission();
                }
            }
        });
        // Evento Botón Parar grabación
        btnStopRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mediaRecorder.stop();
                sonidos_guardados.put(String.valueOf(txtNombreSonido.getText()),pathNew);
                pathSave = pathNew; // se guarda la dirección guardada del audio ACTUAL
                btnStopRecord.setEnabled(false);
                btnPlay.setEnabled(true);
                btnRecord.setEnabled(true);
                btnStop.setEnabled(false);
                actualizarSonidos();
                Toast.makeText(Grabacion.this, ".. Se paró grabar", Toast.LENGTH_SHORT).show();
            }
        });

        // Evento Botón Reproducir lo grabado
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnStop.setEnabled(true);
                btnStopRecord.setEnabled(false);
                // btnRecord.setEnabled(false);

                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(pathSave);//se recurre a un audio previamente guardado
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mediaPlayer.start();
                Toast.makeText(Grabacion.this, "Reproduciendo..", Toast.LENGTH_SHORT).show();

            }
        });
        // Evento Botón Parar reproductor
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnStopRecord.setEnabled(false);
                btnRecord.setEnabled(true);
                btnStop.setEnabled(false);
                btnPlay.setEnabled(true);

                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    setupMediaRecorder();
                    Toast.makeText(Grabacion.this, ".. Se paró de reproducir", Toast.LENGTH_SHORT).show();

                }
            }

        });
    }
    private void actualizarSonidos(){
        sonidosToSpinner = sonidos_guardados.keySet().toArray(new String[sonidos_guardados.keySet().size()]);
        selectSOnidos.setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item,sonidosToSpinner));
    }
    // configurar grabadora multimedia
    private void setupMediaRecorder() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(pathNew); //el nuevo
    }

    // Requerir permisos
    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO

        }, REQUEST_PERMISSION_CODE);
    }

    //
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this, "Permiso obtenido..", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, "Permiso denegado..", Toast.LENGTH_SHORT).show();

            }
            break;
        }
    }

    //Verificar los permisos del Dispositivo
    private boolean checkPermissionFromDevice() {
        int record_audio_result = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        int write_external_storage_result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return write_external_storage_result == PackageManager.PERMISSION_GRANTED &&
                record_audio_result == PackageManager.PERMISSION_GRANTED;
    }


}