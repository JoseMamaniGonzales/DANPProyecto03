package com.example.danpproyecto03;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import static com.example.danpproyecto03.Camara.botonCamara;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class VocalServicio extends Service implements DetectarSonidoListener {
    private DetectorThread detectorThread;
    private RecorderThread recorderThread;
    private static final int NOTIFICATION_Id = 001;
    public static final int DETECT_NONE = 0;
    public static final int DETECT_WHISTLE = 1;
    public static int selectedDetection = DETECT_NONE;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        startDetection();
        return START_STICKY;
    }

    public void startDetection(){
        selectedDetection = DETECT_WHISTLE;
        recorderThread = new RecorderThread();
        recorderThread.start();
        detectorThread = new DetectorThread(recorderThread);
        detectorThread.setDetectarSonidoListener(this);
        detectorThread.start();
        Toast.makeText(this, "Servicio iniciado", Toast.LENGTH_LONG).show();
    }
    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (recorderThread != null) {
            recorderThread.stopRecording();
            recorderThread = null;
        }
        if (detectorThread != null) {
            detectorThread.stopDetection();
            detectorThread = null;
        }
        selectedDetection = DETECT_NONE;
        Toast.makeText(this, "Silbido detectado, detección detenida", Toast.LENGTH_LONG).show();
        botonCamara.performClick();
        //Toast.makeText(this, "Servicio detenido :D", Toast.LENGTH_LONG).show();
        stopNotification();
    }
    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onWhistleDetected() {
        stopService(new Intent(getBaseContext(),VocalServicio.class));
        /*Toast.makeText(this, "Silbido detectado", Toast.LENGTH_LONG).show();
        this.stopSelf();*/
    }
    public void stopNotification(){
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.cancel(NOTIFICATION_Id);
    }
}