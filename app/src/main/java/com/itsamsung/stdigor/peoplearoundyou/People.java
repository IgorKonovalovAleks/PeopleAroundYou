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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import java.util.ArrayList;

import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class People extends AppCompatActivity implements LocationListenerActivity, OnMapReadyCallback {

    ArrayList<Person> people;
    User usr;
    LinearLayout wall;
    TextView progress;
    Button load;
    GoogleMap gmap;
    MapView mapView;
    Handler handler;
    boolean ready, awaiting, mapReady, waitingForMap;
    BackGroundLocationListener listener;
    LocationManager manager;

    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people_new);
        ready = awaiting = mapReady = waitingForMap = false;
        usr = new Gson().fromJson(getIntent().getStringExtra("user"), User.class);
        //wall = findViewById(R.id.wall); for old version
        mapView = findViewById(R.id.mapView);
        load = findViewById(R.id.button2);
        progress = findViewById(R.id.progress);
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                super.handleMessage(msg);
                onPeopleLoaded();
            }
        };
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);
        initLocationListening();
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
        if (mapReady) {
            initMapView();
        } else {
            waitingForMap = true;
        }
        ready = true;
    }

    private void initMapView(){
        Person make;
        for (int i = 0; i < people.size(); i++) {
            make = people.get(i);
            Log.d("INIT_MAP", new Gson().toJson(make));
            LatLng coords = new LatLng(make.latitude, make.longitude);
            gmap.addMarker(new MarkerOptions()
                    .position(coords)
                    .title(make.nickname)
                    .snippet(make.status)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        }
        gmap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(usr.person.latitude, usr.person.longitude)));
        Log.d("INIT_MAP", "DONE!");
    }

    private void initPersonList(){
        Log.d("DEBUG", "PERSON LIST INITIALIZING...");
        Person pers;
        PersonView make;
        for(int i = 0; i < people.size(); i++){
            pers = people.get(i);
            Log.d("INIT_LIST", new Gson().toJson(pers));
            make = new PersonView(this, pers);
            make.setId(i);
            Point size = new Point();
            getWindowManager().getDefaultDisplay().getSize(size);
            make.setLayoutParams(new LinearLayout.LayoutParams((int)(size.x * 0.95), 150));
            wall.addView(make);
        }
        Log.d("INIT_LIST", "DONE!");
        progress.setText("Done!");
    }

    void initLocationListening(){
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

    //Implemented methods
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
        initLocationListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }
    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("MAP_INIT", "map is ready");
        gmap = googleMap;
        mapReady =true;
        if (waitingForMap) {
            waitingForMap = false;
            initMapView();
        }
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
