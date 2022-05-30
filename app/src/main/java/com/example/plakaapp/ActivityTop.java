package com.example.plakaapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.annotation.RequiresApi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.os.Build;

import com.example.plakaapp.Models.ifsaModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ActivityTop extends AppCompatActivity implements LocationListener {
    ArrayList<String> AdresArray;
    ArrayList<ifsaModel> models;
    private DatabaseReference mDatabase;
    ArrayAdapter arrayAdapter;
    ArrayAdapter arrayAdapterPlate;
    ArrayList<String> plakaArray;
    ArrayList<ifsaModel> modeller;
    ArrayList<Integer> idArray;
    ListView listView;
    ListView listView2;
    List<String> selectedAdress;
    ProgressDialog progDialog;
    String Sokak="",Mahalle="",Sehir="";
    String Boylam="",Enlem="";
    LocationManager locationManager ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top);

        listView2 = findViewById(R.id.listview2);
        plakaArray=new ArrayList<String>();
        idArray = new ArrayList<Integer>();
        models= new ArrayList<>();
        modeller= new ArrayList<>();
        listView = findViewById(R.id.listview);
        progDialog= new ProgressDialog(this);
        selectedAdress=new ArrayList<>();
        AdresArray=new ArrayList<String>();
        mDatabase = FirebaseDatabase.getInstance().getReference("ifsalar");
        if(ContextCompat.checkSelfPermission(ActivityTop.this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(ActivityTop.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},100);
        }
        getLocation();

        mDatabase.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
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
                if(plakaArray.size()<=0){
                    Toast.makeText(ActivityTop.this, "Liste Boş ... ", Toast.LENGTH_LONG).show();
                }
                else {
                    Map<String, Long> result =
                            plakaArray.stream()
                                    .sorted(String::compareTo)
                                    .collect(
                                            Collectors.groupingBy(
                                                    Function.identity(), Collectors.counting()
                                            )
                                    );
                    LinkedHashMap<String, Long> sortedMap = new LinkedHashMap<>();
                    result.entrySet()
                            .stream()
                            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                            .forEachOrdered(x -> sortedMap.put(x.getKey(), x.getValue()));

                    System.out.println("Listeyi Ters Cevir   : " + sortedMap);

                    List<String> strlist= new ArrayList<>(sortedMap.keySet());
                    strlist=strlist.size()>3?strlist.subList(0,3):strlist.subList(0,strlist.size());
                    arrayAdapterPlate = new ArrayAdapter(ActivityTop.this,android.R.layout.simple_list_item_1,strlist);
                    listView2.setAdapter(arrayAdapterPlate);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }
    @Override
    public void onLocationChanged(Location location) {
        Boylam=""+location.getLongitude();
        Enlem=""+location.getLatitude();

        try {
            Geocoder geo = new Geocoder(ActivityTop.this.getApplicationContext(), Locale.getDefault());
            double lati=Double.parseDouble(Enlem);
            double longu= Double.parseDouble(Boylam);
            List<Address> addresses = geo.getFromLocation(lati, longu, 1);
            if (addresses.isEmpty()) {
                Log.e("Adres","Waiting for Location");
            }
            else {
                if (addresses.size() > 0 && (Mahalle.equals("") && Sokak.equals("") && Sehir.equals("") )) {
                    Sokak=addresses.get(0).getThoroughfare();
                    Mahalle=addresses.get(0).getSubLocality();
                    Sehir=addresses.get(0).getAdminArea();
                    String adres = (addresses.get(0).getThoroughfare() + ", " + addresses.get(0).getSubLocality() +", " + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName());
                    Log.e("Adres",adres);
                    mDatabase.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            AdresArray.clear();
                            models.clear();

                            for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                                ifsaModel k = postSnapshot.getValue(ifsaModel.class);
                                k.Id=postSnapshot.getKey().toString();
                                AdresArray.add(k.Sokak +" - "+k.Mahalle +" - "+k.Sehir);
                                models.add(k);
                            }
                            if(AdresArray.size()<=0){
                                Toast.makeText(ActivityTop.this, "Liste Boş ... ", Toast.LENGTH_LONG).show();
                            }
                            else if(AdresArray.size()<=3)
                            {
                                arrayAdapter = new ArrayAdapter(ActivityTop.this,android.R.layout.simple_list_item_1,AdresArray);
                                listView.setAdapter(arrayAdapter);
                            }
                            else if(AdresArray.size()>3) {
                               selectedAdress.clear();
                                for (String s:AdresArray){
                                    if(s.contains(Sokak) && s.contains(Mahalle) && s.contains(Sehir) && selectedAdress.size()<4){
                                        if(selectedAdress.indexOf(s)<0){selectedAdress.add(s);}
                                    }
                                }
                                for (String s:AdresArray){
                                    if(s.contains(Mahalle) && s.contains(Sehir) && selectedAdress.size()<4){
                                        if(selectedAdress.indexOf(s)<0){selectedAdress.add(s);}
                                    }
                                }

                                for (String s:AdresArray){
                                    if(s.contains(Sokak)  && s.contains(Sehir) && selectedAdress.size()<4 ){
                                        if(selectedAdress.indexOf(s)<0){selectedAdress.add(s);}
                                    }
                                }
                                for (String s:AdresArray){
                                    if(s.contains(Sokak) && s.contains(Mahalle)&& selectedAdress.size()<4 ){
                                        if(selectedAdress.indexOf(s)<0){selectedAdress.add(s);}
                                    }
                                }

                                for (String s:AdresArray){
                                    if(s.contains(Mahalle) ){
                                        if(selectedAdress.indexOf(s)<0){selectedAdress.add(s);}
                                    }
                                }
                                for (String s:AdresArray){
                                    if(s.contains(Sehir) ){
                                        if(selectedAdress.indexOf(s)<0){selectedAdress.add(s);}
                                    }
                                }
                                Toast.makeText(ActivityTop.this, ""+Sehir+Mahalle+Sokak, Toast.LENGTH_SHORT).show();
                                arrayAdapter = new ArrayAdapter(ActivityTop.this,android.R.layout.simple_list_item_1,selectedAdress);
                                listView.setAdapter(arrayAdapter);
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });



                }
            } progDialog.dismiss();
        }
        catch (Exception e){

        }
        progDialog.dismiss();
    }

    @Override
    public void onProviderDisabled(String provider) {
        progDialog.dismiss();
        Toast.makeText(ActivityTop.this, "Lütfen İnternet ve Konumunuzu Aktifleştiriniz", Toast.LENGTH_SHORT).show();

    }


    @SuppressLint("MissingPermission")
    private void getLocation() {
        try {
            progDialog.setTitle("Konum Servisi");
            progDialog.show();
            progDialog.setMessage("Alınıyor...");
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    5000,
                    5,
                    (LocationListener) ActivityTop.this);
        }
        catch(SecurityException e) {
            e.printStackTrace();
        }
    }

}