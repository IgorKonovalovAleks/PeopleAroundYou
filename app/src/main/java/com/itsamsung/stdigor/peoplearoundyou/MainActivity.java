package com.itsamsung.stdigor.peoplearoundyou;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.util.ArrayList;
import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class MainActivity extends AppCompatActivity {

    Person person;
    Button button;
    TextView text;
    String latitude, longitude;
    Context context;
    private static final String TAG = "TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.button);
        text = findViewById(R.id.textView);
        MyLocationListener myLocationListener = new MyLocationListener();
        myLocationListener.start();
        context = this.getApplicationContext();

        //Just an example
        person = new Person();
        Log.i("Normal", "first");
        person.nickname = "root";
        person.longitude = 44.23423;
        person.latitude = 56.23421;
        person.status = "Vsyo slozhno";
        new LoadPerson("write").execute();


        Log.i("Normal", "executing");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text.setText("Latitude: " + latitude + "\n" + "Longitude: " + longitude);
                if(latitude == null || longitude == null){
                    text.setText("Check your connection to the Internet!");
                }
            }
        });
    }

    class MyLocationListener extends Thread {

        private LocationManager manager;

        public MyLocationListener(){
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
                    Response<ArrayList<Person>> response = call.execute();
                    /*

                    DO NEXT

                     */
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private LocationListener listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                latitude = Double.toString(location.getLatitude());
                longitude = Double.toString(location.getLongitude());
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

    class LoadPerson extends AsyncTask<Void, String, Person>{

        String mode;

        public LoadPerson(String mode){
            this.mode = mode;
        }

        @Override
        protected Person doInBackground(Void... voids) {
            JSONBaseModule jbm = new JSONBaseModule("User.txt", context);
            Person per = new Person();
            Log.d(TAG, person.status);
            if (mode == "write"){
                jbm.saveData(person);
                Log.d(TAG, person.status);
            } else if (mode == "read"){
                per = jbm.getData();
                Log.d(TAG, person.status);
            }
            return per;
        }

        public void onPostExecute(Person result){
            if (mode == "write") {
                Log.d("TAG", Double.toString(person.latitude));
                //Temporary. It's just a test
                new LoadPerson("read").execute();
                Log.i("Normal", Double.toString(person.latitude));
            } else {
                person = result;
                text.setText(person.status + person.nickname + person.latitude + person.longitude);
                Log.i("Normal", Double.toString(person.latitude));
            }
        }
    }
}
