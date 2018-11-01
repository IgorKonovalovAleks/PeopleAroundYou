package com.itsamsung.stdigor.peoplearoundyou;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import java.util.ArrayList;
import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

public class People extends AppCompatActivity {

    ArrayList<Person> people;
    Person person;
    LinearLayout wall;
    TextView progress;
    boolean ready;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people);
        person = new Gson().fromJson(getIntent().getStringExtra("person"), Person.class);
        wall = findViewById(R.id.wall);
        progress = findViewById(R.id.progress);
        progress.setText("Loading...");
        BackGround myLocationListener = new BackGround();
        ready = false;
        myLocationListener.start();
        while(!ready){}
        Person pers;
        PersonView make;
        for(int i = 0; i < people.size(); i++){
            pers = people.get(i);
            Log.d("TAG_FROM_PEOPLE", new Gson().toJson(pers));
            make = new PersonView(this, pers);
            make.setId(i);
            wall.addView(make);
        }
        progress.setText("Done!");
    }

    class BackGround extends Thread {

        private LocationManager manager;

        public BackGround(){
            manager = (LocationManager) getSystemService(LOCATION_SERVICE);
            try {
                manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, listener);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run(){
            while(true) {
                try {
                    Thread.sleep(5000);
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl("https://peoplearoundyou.herokuapp.com")                       //Don't know my project's URL on heroku yet
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                    RequestSample requestSample = retrofit.create(RequestSample.class);
                    //Maybe not ArrayList
                    Call<ArrayList<Person>> call = requestSample.lookForPeople(person);
                    //Response<ArrayList<Person>> response = call.execute();
                    //people = response.body();
                    people = new ArrayList<>();
                    for (int i = 0; i < 3; i++){
                        people.add(generate());
                    }
                    Log.d("TAG", Integer.toString(people.size()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ready = true;
            }
        }

        //returns random person
        private Person generate(){
            Person person = new Person();
            person.latitude = 56 + Math.random();
            person.longitude = 44 + Math.random();
            person.nickname = "Some nickname " + Double.toString(person.latitude);
            person.status = "Vsyo slozhno " + Double.toString(person.longitude);
            return person;
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
                manager = (LocationManager) getSystemService(LOCATION_SERVICE);
                try {
                    manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, listener);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
    }


}