package com.itsamsung.stdigor.peoplearoundyou;

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
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import java.util.ArrayList;

import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class People extends AppCompatActivity implements LocationListenerActivity {

    ArrayList<Person> people;
    User usr;
    LinearLayout wall;
    TextView progress;
    Button load;
    Handler handler;
    boolean ready;
    boolean awaiting;
    BackGroundLocationListener listener;
    LocationManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people);
        ready = false;
        awaiting = false;
        usr = new Gson().fromJson(getIntent().getStringExtra("user"), User.class);
        wall = findViewById(R.id.wall);
        load = findViewById(R.id.button2);
        progress = findViewById(R.id.progress);
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                super.handleMessage(msg);
                onPeopleLoaded();
            }
        };
        initLocationListenning();
        new Remind().execute();
    }

    //Lifecycle methods
    public void onLoadButtonClicked(View v){
        if (listener.able) {
            awaiting = false;
            wall.removeAllViews();
            new LoadPeople().execute();
            progress.setText("Loading...");
        } else {
            awaiting = true;
            Log.d("ON_LOAD_CLICKED", "start awaiting");
        }
    }

    public void onPeopleLoaded(){
        Log.d("HANDLER", "handled");
        initPersonList();
        if (!ready) {
            ready = true;
        }
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

    void initLocationListenning(){
        listener = new BackGroundLocationListener(this);
        manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        try {
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
            manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        progress.setText("Your GPS in unable");
        if (awaiting) {
            awaiting = false;
            onLoadButtonClicked(load);
        }
    }

    //Interface methods
    @Override
    public void locationChanged(Location location) {
        Log.d("LOCATION_LISTENER", "location changed " + Double.toString(location.getLatitude()) + " " + Double.toString(location.getLongitude()));
        usr.person.latitude = location.getLatitude();
        usr.person.longitude = location.getLongitude();
    }

    @Override
    public void providerDisabled(String provider) {
        progress.setText("Switct on your GPS");
    }

    @Override
    public void providerEnabled(String provider) {
        Log.d("LOCATION_LISTENER", "enabled");
        initLocationListenning();
    }

    //Requests to server
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
                onPeopleLoaded();
            } else {
                progress.setText("Error");
            }
        }
    }

    //Does queries per 20 seconds
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
}
