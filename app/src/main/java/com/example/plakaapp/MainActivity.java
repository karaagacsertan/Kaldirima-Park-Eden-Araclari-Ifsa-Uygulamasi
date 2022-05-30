package com.example.plakaapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    Button kaydet;
    Button kaydedilenler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        kaydet = findViewById(R.id.ifsaKaydet);
        kaydedilenler = findViewById(R.id.kayıtlar);



    }


    public void ifsakaydet(View view)
    {
        Intent intent = new Intent(MainActivity.this, ActivitySave.class);
        intent.putExtra("info","new");
        startActivity(intent);
    }

    public void kaydedilenler(View view){
        Intent intent = new Intent(MainActivity.this, ActivityList.class);
        startActivity(intent);
    }


}