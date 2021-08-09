package com.example.danpproyecto03;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void pasar(View view){
        Intent intent = new Intent(this, Camara.class);
        startActivity(intent);
    }
}