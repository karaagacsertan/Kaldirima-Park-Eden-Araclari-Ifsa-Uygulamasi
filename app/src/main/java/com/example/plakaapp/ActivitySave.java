package com.example.plakaapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.plakaapp.Models.SelectedModel;
import com.example.plakaapp.Models.ifsaModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ActivitySave extends AppCompatActivity implements LocationListener {
    ImageView imageView;
    EditText  txtPlaka;
    EditText txtKonum;
    EditText txtTarih;
    ProgressDialog progDialog;
    String Sokak,Mahalle,Sehir;
    Button kaydetbuton,konumBul;
    Uri ImgUri;
    String Boylam="",Enlem="";
    LocationManager locationManager ;
    Bitmap selectedImage;
    private DatabaseReference myDatabase;
    StorageReference storageReference;
    FirebaseStorage storage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save);


        imageView = findViewById(R.id.imageView);
        txtPlaka= findViewById(R.id.txtPlaka);
        txtKonum = findViewById(R.id.txtKonum);
        txtTarih = findViewById(R.id.txtTarih);
        kaydetbuton= findViewById(R.id.savebutton);
        konumBul= findViewById(R.id.konumButton);
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        progDialog= new ProgressDialog(this);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        myDatabase = FirebaseDatabase.getInstance().getReference("ifsalar");

        if(ContextCompat.checkSelfPermission(ActivitySave.this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(ActivitySave.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},100);
        }
        konumBul.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                konumAl();
            }
        });
        Intent intent = getIntent();
        String info = intent.getStringExtra("info");

        if (info.matches("new")) {
            txtPlaka.setText("");
            txtKonum.setText("");
            SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd");
            Date date = new Date(System.currentTimeMillis());
            txtTarih.setText(formatter.format(date));
            kaydetbuton.setVisibility(View.VISIBLE);


            Bitmap selectImage = BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.galeri);
            imageView.setImageBitmap(selectImage);

        } else  {


            kaydetbuton.setVisibility(View.INVISIBLE);
            konumBul.setVisibility(View.INVISIBLE);
            imageView.setClickable(false);
            txtPlaka.setText(SelectedModel.Model.Plaka);
            txtKonum.setText(SelectedModel.Model.Sokak+" "+SelectedModel.Model.Mahalle+ ""+SelectedModel.Model.Sehir);
            txtTarih.setText(SelectedModel.Model.Tarih);
            storageReference=storage.getReference()
                    .child("images").child(SelectedModel.Model.ResimId);
            storageReference.getBytes(1024*1024).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    imageView.setImageBitmap(bitmap);
                }
            });
        }
    }

    @SuppressLint("MissingPermission")
    private void konumAl() {
        try {
            progDialog.setTitle("Konum ");
            progDialog.show();
            progDialog.setMessage("Alınıyor...");
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    5000,
                    5,
                    (LocationListener) ActivitySave.this);
        }
        catch(SecurityException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        Boylam=""+location.getLongitude();
        Enlem=""+location.getLatitude();


        try {
            Geocoder geo = new Geocoder(ActivitySave.this.getApplicationContext(), Locale.getDefault());
            double lati=Double.parseDouble(Enlem);
            double longu= Double.parseDouble(Boylam);
            List<Address> addresses = geo.getFromLocation(lati, longu, 1);
            if (addresses.isEmpty()) {
                Log.e("Adres","Konum Bekleniyor");
            }
            else {
                if (addresses.size() > 0) {
                    Sokak=addresses.get(0).getThoroughfare();
                    Mahalle=addresses.get(0).getSubLocality();
                    Sehir=addresses.get(0).getAdminArea();
                    String adres = (addresses.get(0).getThoroughfare() + ", " + addresses.get(0).getSubLocality() +", " + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName());
                    Log.e("Adres",adres);
                    txtKonum.setText(adres);
                }
            }
        }
        catch (Exception e){

        }
        progDialog.dismiss();
    }

    @Override
    public void onProviderDisabled(String provider) {
        progDialog.dismiss();
        Toast.makeText(ActivitySave.this, "Lütfen İnternet ve Konumunuzu Aktifleştiriniz", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    public void selectImage(View view){


        Intent cam=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cam,33);

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==1){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intentToGallery,2);
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode==33){
            try {
                selectedImage=(Bitmap)data.getExtras().get("data");
                ImgUri = data.getData();
                imageView.setImageBitmap(selectedImage);

            }
            catch (Exception e){
                Toast.makeText(this, "Resim Yüklenemedi", Toast.LENGTH_SHORT).show();
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void kaydet(View view){
        final  String rndkey= UUID.randomUUID().toString();



        if (selectedImage!=null && !txtPlaka.getText().toString().equals("") && !txtTarih.getText().toString().equals("")
                && !Boylam.equals("") && !Enlem.equals("") ) {
            ByteArrayOutputStream stream =  new ByteArrayOutputStream();
            selectedImage.compress(Bitmap.CompressFormat.JPEG,100,stream);
            byte []b=stream.toByteArray();
            ifsaModel model= new ifsaModel();

            progDialog.setTitle("Yükleniyor ...");
            progDialog.show();
            model.Enlem=Enlem;
            model.Boylam=Boylam;
            model.ResimId=rndkey;
            model.Sokak=Sokak;
            model.Mahalle=Mahalle;
            model.Sehir=Sehir;
            model.Plaka=txtPlaka.getText().toString();
            model.Tarih=txtTarih.getText().toString();




            StorageReference storageR= storageReference.child("images/"+ rndkey);
            storageR.putBytes(b).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progDialog.dismiss();

                    String userId = myDatabase.push().getKey();
                    myDatabase.child(userId).setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            Intent intent= new Intent(ActivitySave.this,MainActivity.class);
                            startActivity(intent);

                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progDialog.dismiss();

                    Toast.makeText(ActivitySave.this, "Hata Oluştu"+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double count=(100.00)*snapshot.getBytesTransferred()/snapshot.getTotalByteCount();
                    progDialog.setMessage("Yükleniyor " + (int)count + " %");
                }
            });
        }
        else {
            Toast.makeText(this, "Boş Alan Bırakmayınız", Toast.LENGTH_SHORT).show();
        }


    }




}