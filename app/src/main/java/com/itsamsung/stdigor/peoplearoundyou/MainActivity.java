package com.itsamsung.stdigor.peoplearoundyou;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    Button button;
    TextView text;
    String place;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.button);
        text = findViewById(R.id.textView);
        MyLocationListener myLocationListener = new MyLocationListener();
        myLocationListener.start();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text.setText(place);
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

        }

        private LocationListener listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                place = "Latitude: " + location.getLatitude() + "\n" + "Longitude: " + location.getLongitude();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
    }
}
