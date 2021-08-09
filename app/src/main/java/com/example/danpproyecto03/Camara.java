package com.example.danpproyecto03;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.Arrays;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class Camara extends AppCompatActivity {
    private Button start,stop;
    public static boolean bandera = false;

    TextureView vistaCamara;
    ImageView foto;
    ImageView foto2;
    static Button botonCamara;
    private String camaraId;
    private Size imagenDimen;
    private ImageReader imageReader;
    CameraDevice camaraDev;
    CaptureRequest.Builder captureRequestBuilder;
    CameraCaptureSession cameraCaptureSession;
    CaptureRequest captureRequest;
    Handler mHandler;
    HandlerThread mHandlerThread;
    private File file;

    private static  final SparseIntArray ORIENTACION = new SparseIntArray();
    static {
        ORIENTACION.append(Surface.ROTATION_0,90);
        ORIENTACION.append(Surface.ROTATION_90,0);
        ORIENTACION.append(Surface.ROTATION_180,270);
        ORIENTACION.append(Surface.ROTATION_270,180);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camara);
        vistaCamara = (TextureView)findViewById(R.id.vistaCamara);
        foto = (ImageView)findViewById(R.id.foto);
        foto2 = (ImageView)findViewById(R.id.foto2);
        botonCamara = (Button)findViewById(R.id.tomarFoto);

        vistaCamara.setSurfaceTextureListener(textureListener);
        botonCamara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    tomarFoto();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
        });
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1000);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 1000);
        }
        start = (Button) findViewById(R.id.btnIniciar);
        stop = (Button) findViewById(R.id.btnDetener);
        start.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startService(new Intent(getBaseContext(),VocalServicio.class));
            }
        });
        stop.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                stopService(new Intent(getBaseContext(),VocalServicio.class));
            }
        });
    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
            try {
                abrirCamara();
            } catch (@SuppressLint("NewApi") CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {

        }
    };

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 101){
            if(grantResults[0]==PackageManager.PERMISSION_DENIED){
                Toast.makeText(getApplicationContext(),"Disculpa, es encesario dar permisos a la camara",Toast.LENGTH_LONG).show();
            }
        }

    }

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback(){

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onOpened(CameraDevice cameraDevice) {
            camaraDev = cameraDevice;
            try {
                createCameraPreview();
            } catch (@SuppressLint("NewApi") CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            camaraDev.close();
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onError(CameraDevice cameraDevice, int i) {
            camaraDev.close();
            camaraDev = null;
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void createCameraPreview() throws CameraAccessException {
        SurfaceTexture textura = vistaCamara.getSurfaceTexture();
        textura.setDefaultBufferSize(imagenDimen.getWidth(),imagenDimen.getHeight());
        Surface surface = new Surface(textura);

        captureRequestBuilder = camaraDev.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        captureRequestBuilder.addTarget(surface);
        camaraDev.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(CameraCaptureSession session) {
                if(camaraDev == null){
                    return;
                }
                cameraCaptureSession = session;
                try {
                    updatePreview();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onConfigureFailed(CameraCaptureSession session) {
                Toast.makeText(getApplicationContext(),"Configuracion cambiada",Toast.LENGTH_LONG).show();
            }
        },null);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void updatePreview() throws CameraAccessException {
        if(camaraDev == null){
            return;
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(),null,mHandler);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void abrirCamara() throws CameraAccessException {
        CameraManager manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        camaraId = manager.getCameraIdList()[0];
        CameraCharacteristics caracteristicas = manager.getCameraCharacteristics(camaraId);
        StreamConfigurationMap map = caracteristicas.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        imagenDimen = map.getOutputSizes(SurfaceTexture.class)[0];

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(Camara.this,new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE},101);
            return;
        }
        manager.openCamera(camaraId,stateCallback,null);
    }

    public void tomarFoto() throws CameraAccessException {
        foto.setImageBitmap(vistaCamara.getBitmap());
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected void onResume(){
        super.onResume();
        inciarThread();
        if(vistaCamara.isAvailable()){
            try {
                abrirCamara();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }else{
            vistaCamara.setSurfaceTextureListener(textureListener);
        }
    }

    private void inciarThread(){
        mHandlerThread = new HandlerThread("Camara de fondo");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onPause() {
        try {
            stopThread();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.onPause();
    }

    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    protected void stopThread() throws InterruptedException {
        mHandlerThread.quitSafely();
        mHandlerThread.join();
        mHandlerThread = null;
        mHandler = null;


    }
}