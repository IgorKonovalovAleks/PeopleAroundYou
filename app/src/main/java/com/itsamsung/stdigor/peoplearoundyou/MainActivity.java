package com.itsamsung.stdigor.peoplearoundyou;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;

import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class MainActivity extends AppCompatActivity implements LocationListenerActivity {

    User usr;
    Button button;
    Context context;
    EditText nameEdit, statusEdit;
    TextView statusView;
    public static final String BASE_URL = "https://peoplearoundyou.herokuapp.com";
    boolean ready, onSavingAwaiting;
    LocationManager LM;
    BackGroundLocationListener BGLL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ready = false;
        onSavingAwaiting = false;
        statusView = findViewById(R.id.StatusView);
        nameEdit = findViewById(R.id.NameEdit);
        statusEdit = findViewById(R.id.StatusEdit);
        button = findViewById(R.id.button);
        context = this.getApplicationContext();
        new LoadPerson().execute();
        initLocationListening();
    }

    private void initLocationListening(){
        LM = (LocationManager) getSystemService(LOCATION_SERVICE);
        BGLL = new BackGroundLocationListener(this);
        try {
            LM.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5, BGLL);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    //Lifecycle processors
    public void click(View view) {
        if (BGLL.able) {
            usr.person.nickname = nameEdit.getText().toString();
            usr.person.status = statusEdit.getText().toString();
            usr.lastCall = (int) (System.currentTimeMillis() / 1000);
            new Auth().execute();
            new SavePerson().execute();
        } else {
            statusView.setText("Switch on your GPS, please.");
        }
    }

    public void onAuthenticated() {
        if (ready) {
            Intent i = new Intent(MainActivity.this, People.class);
            i.putExtra("user", new Gson().toJson(usr));
            startActivity(i);
        } else {
            onSavingAwaiting = true;
        }
    }

    public void onSaved() {
        ready = true;
        if (onSavingAwaiting) {
            onSavingAwaiting = false;
            onAuthenticated();
        }
    }

    public void onLoaded(Person per) {
        if (per != null) {
            usr = new User();
            usr.person = per;
            nameEdit.setText(per.nickname);
            statusEdit.setText(per.status);
            Log.d("LOAD_PERSON", new Gson().toJson(usr));
        } else {
            Log.d("LOAD_PERSON", "using default settings");
            setDefault();
        }
    }

    private void setDefault() {
        nameEdit.setText("default user");
        statusEdit.setText("I'm a newcomer");
        usr = new User();
        usr.person = new Person();
        usr.person.nickname = "default user";
        usr.person.status = "I'm a newcomer";
        usr.person.latitude = 0;
        usr.person.longitude = 0;
    }

    // LocationListener methods
    @Override
    public void locationChanged(Location location) {
        Log.d("LOCATION_LISTENER", "location changed " + Double.toString(location.getLatitude()) + " " + Double.toString(location.getLongitude()));
        usr.person.latitude = location.getLatitude();
        usr.person.longitude = location.getLongitude();
    }

    @Override
    public void providerDisabled(String provider) {
        statusView.setText("Your GPS provider is unable");
    }

    @Override
    public void providerEnabled(String provider) {
        LM = (LocationManager) getSystemService(LOCATION_SERVICE);
        BGLL = new BackGroundLocationListener(this);
        try {
            LM.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, BGLL);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        statusView.setText("Your GPS provider is able");
    }

    class SavePerson extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            JSONFileLoader<Person> jfl = new JSONFileLoader<>("User.txt", Person.class, MainActivity.this);
            jfl.save(usr.person);
            return null;
        }

        protected void onPostExecute(Void v) {
            onSaved();
        }
    }

    class Auth extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(MainActivity.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            RequestSample requestSample = retrofit.create(RequestSample.class);
            try {
                Log.d("AUTH", new Gson().toJson(usr));
                Call<User> call = requestSample.auth(usr);
                Response<User> response = call.execute();
                usr = response.body();
                Log.d("AUTH_DONE", new Gson().toJson(usr));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void v) {
            onAuthenticated();
        }
    }

    class LoadPerson extends AsyncTask<Void, Void, Person> {

        @Override
        protected Person doInBackground(Void... voids) {
            JSONFileLoader<Person> jfl = new JSONFileLoader<>("User.txt", Person.class, MainActivity.this);
            Log.d("LOAD_PERSON_START", new Gson().toJson(usr));
            return jfl.get();
        }

        protected void onPostExecute(Person per){
            onLoaded(per);
        }
    }
}
