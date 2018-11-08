package com.itsamsung.stdigor.peoplearoundyou;

import android.content.Context;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Random;

import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class People extends AppCompatActivity {

    ArrayList<Person> people;
    Person person;
    LinearLayout wall;
    TextView progress;
    Handler handler;
    Button load;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people);
        person = new Gson().fromJson(getIntent().getStringExtra("person"), Person.class);
        wall = findViewById(R.id.wall);
        load = findViewById(R.id.button2);
        progress = findViewById(R.id.progress);
        BackGround backGround = new BackGround();
        backGround.start();
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg){
                super.handleMessage(msg);
                initPersonList();
                progress.setText("Done!");
            }
        };
        load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wall.removeAllViews();
                new LoadPeople().execute();
                progress.setText("Loading...");
            }
        });
    }

    private void initPersonList(){
        Log.d("DEBUG", "PERSON LIST");
        Person pers;
        PersonView make;
        for(int i = 0; i < people.size(); i++){
            pers = people.get(i);
            Log.d("TAG_FROM_PEOPLE", new Gson().toJson(pers));
            make = new PersonView(this, pers);
            make.setId(i);
            Point size = new Point();
            getWindowManager().getDefaultDisplay().getSize(size);
            make.setLayoutParams(new LinearLayout.LayoutParams((int)(size.x * 0.95), 150));
            wall.addView(make);
        }
        progress.setText("Done!");
    }

    class LoadPeople extends AsyncTask<Void, Void, ArrayList<Person>>{

        @Override
        protected ArrayList<Person> doInBackground(Void... voids) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://frozen-badlands-67545.herokuapp.com")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            RequestSample requestSample = retrofit.create(RequestSample.class);
            try {
                Log.d("REQUEST", new Gson().toJson(person));
                Call<ArrayList<Person>> call = requestSample.lookForPeople(person);
                Response<ArrayList<Person>> response = call.execute();
                return response.body();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        public void onPostExecute(ArrayList<Person> personList){
            if(personList != null) {
                people = personList;
                handler.sendEmptyMessage(1);
            } else {
                progress.setText("Error");
            }
        }
    }

    class BackGround extends Thread {

        private LocationManager manager;

        public BackGround(){
            manager = (LocationManager) getSystemService(LOCATION_SERVICE);
            try {
                manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, listener);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run(){
        }

        private LocationListener listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                person.latitude = location.getLatitude();
                person.longitude = location.getLongitude();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {
                progress.setText("GPS provider enabled");
                manager = (LocationManager) getSystemService(LOCATION_SERVICE);
                try {
                    manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, listener);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onProviderDisabled(String provider) {
                progress.setText("GPS provider is unable");
            }
        };
    }


}
