package com.example.plakaapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.plakaapp.Models.SelectedModel;
import com.example.plakaapp.Models.ifsaModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ActivityList extends AppCompatActivity {
        ListView listView;
        ArrayList<String> plakaArray;
        ArrayList<ifsaModel> modeller;
        ArrayList<Integer> idArray;
        Button bolge;
    private DatabaseReference myDatabase;
    ArrayAdapter arrayAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        bolge = findViewById(R.id.konum);
        modeller= new ArrayList<>();
        listView = findViewById(R.id.listView);
        plakaArray=new ArrayList<String>();
        idArray = new ArrayList<Integer>();
        myDatabase = FirebaseDatabase.getInstance().getReference("ifsalar");
        arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,plakaArray);
        listView.setAdapter(arrayAdapter);

        myDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                plakaArray.clear();
                modeller.clear();
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    ifsaModel k = postSnapshot.getValue(ifsaModel.class);
                    k.Id=postSnapshot.getKey().toString();
                    plakaArray.add(k.Plaka);
                    modeller.add(k);
                }
                if(plakaArray.size()<1){
                    Toast.makeText(ActivityList.this, "Boş Liste. ", Toast.LENGTH_LONG).show();
                }
                arrayAdapter = new ArrayAdapter(ActivityList.this,android.R.layout.simple_list_item_1,plakaArray);
                listView.setAdapter(arrayAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ActivityList.this, ActivitySave.class);
                intent.putExtra("plakaId",modeller.get(position).Id);
                intent.putExtra("info","old");
                SelectedModel.Model=modeller.get(position);
                startActivity(intent);
            }
        });
    }

    public void topList(View view){
        Intent intent = new Intent(ActivityList.this, ActivityTop.class);
        startActivity(intent);
    }
}