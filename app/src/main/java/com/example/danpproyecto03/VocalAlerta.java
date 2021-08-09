package com.example.danpproyecto03;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class VocalAlerta extends Activity implements View.OnClickListener
        ,OnCompletionListener{
    private MediaPlayer miSonido;
    private Button bReturn;
    private Button bRestart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alerta);
        stopService(new Intent(getBaseContext(),VocalServicio.class));
        initialize();

        miSonido = MediaPlayer.create(this,R.raw.alarm);
        miSonido.setOnCompletionListener(this);
        miSonido.start();

    }
    private void initialize() {
        bReturn = (Button) findViewById(R.id.quitButton);
        bRestart = (Button) findViewById(R.id.restartButton);
        bReturn.setOnClickListener(this);
        bRestart.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch(v.getId()){
            case R.id.restartButton :
                miSonido.release();
                Intent i = new Intent(getBaseContext(),VocalServicio.class);
                startService(i);
                finish();
                break;
            case R.id.quitButton:
                miSonido.release();
                finish();
                break;
        }
    }
    @Override
    public void onCompletion(MediaPlayer arg0) {
        // TODO Auto-generated method stub
        arg0.start();
    }

}