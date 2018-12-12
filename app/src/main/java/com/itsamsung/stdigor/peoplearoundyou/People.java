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
    User usr;
    LinearLayout wall;
    TextView progress;
    Handler handler;
    Button load;
    boolean ready;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ready = false;
        usr = new Gson().fromJson(getIntent().getStringExtra("user"), User.class);
        wall = findViewById(R.id.wall);
        load = findViewById(R.id.button2);
        progress = findViewById(R.id.progress);
        BackGroundLocationListener backGround = new BackGroundLocationListener();
        backGround.start();
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg){
                super.handleMessage(msg);
                Log.d("HANDLER", "handled");
                initPersonList();
                if (!ready) {
                    setContentView(R.layout.activity_people);
                    ready = true;
                }
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
        new Remind().execute();
    }

    private void initPersonList(){
        Log.d("DEBUG", "PERSON LIST INITIALIZING...");
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
        Log.d("DEBUG", "DONE!");
        progress.setText("Done!");
    }

    class LoadPeople extends AsyncTask<Void, Void, ArrayList<Person>>{

        @Override
        protected ArrayList<Person> doInBackground(Void... voids) {
            Log.d("LOAD_PEOPLE", "Start...");
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(MainActivity.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            RequestSample requestSample = retrofit.create(RequestSample.class);
            try {
                Log.d("REQUEST_LOAD_PEOPLE", new Gson().toJson(usr));
                Call<ArrayList<Person>> call = requestSample.lookForPeople(usr);
                Response<ArrayList<Person>> response = call.execute();
                Log.d("LOAD_PEOPLE", new Gson().toJson(response.body()));
                return response.body();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(ArrayList<Person> personList){
            if(personList != null) {
                people = personList;
                handler.sendEmptyMessage(1);
            } else {
                progress.setText("Error");
            }
        }
    }

    class Remind extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(MainActivity.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            RequestSample requestSample = retrofit.create(RequestSample.class);
            try{
                Log.d("REMIND", new Gson().toJson(usr));
                Call<Integer> call = requestSample.remind(usr.id);
                Response<Integer> r = call.execute();
                usr.lastCall =  r.body();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v){
            new Remind().execute();
        }
    }

    class BackGroundLocationListener extends Thread {

        private LocationManager manager;

        public BackGroundLocationListener(){
            manager = (LocationManager) getSystemService(LOCATION_SERVICE);
            try {
                manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, listener);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run(){}

        private LocationListener listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                usr.person.latitude = location.getLatitude();
                usr.person.longitude = location.getLongitude();
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
