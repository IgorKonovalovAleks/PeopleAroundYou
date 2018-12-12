package com.itsamsung.stdigor.peoplearoundyou;

import android.content.Context;
import android.content.Intent;
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

import com.google.gson.Gson;

import java.util.ArrayList;
import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class MainActivity extends AppCompatActivity {

    User usr;
    Button button;
    Context context;
    private static final String TAG = "TAG";
    public static final String BASE_URL = "https://peoplearoundyou.herokuapp.com";
    boolean ready;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ready = false;
        button = findViewById(R.id.button);
        context = this.getApplicationContext();
        usr = new User();
        new LoadPerson("auth").execute();
    }


    public void click(View view){
        Intent i = new Intent(MainActivity.this, People.class);
        i.putExtra("user", new Gson().toJson(usr));
        startActivity(i);
    }


    class LoadPerson extends AsyncTask<Void, Void, Void> {

        String mode;

        public LoadPerson(String mode){
            this.mode = mode;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            JSONBaseModule<Person> jbm = new JSONBaseModule<>("User.txt", Person.class, MainActivity.this);
            switch (mode){
                case "write":
                    jbm.save(usr.person);
                    Log.d("WRITE", "done");
                    break;
                case "read":
                    usr.person = jbm.get();
                    Log.d("READ", new Gson().toJson(usr));
                    break;
                case "auth":
                    usr.person = jbm.get();

                    if(usr.person == null) {
                        usr.person = new Person();
                        usr.person.nickname = "newcomer";
                        usr.person.longitude = 0;
                        usr.person.status = "Vsyo slozhno" + Long.toString((long) (System.currentTimeMillis() % 10000));
                        usr.person.latitude = 0;
                        Log.d(TAG, "config file not found, using default settings");
                    }

                    usr.lastCall = (int)(System.currentTimeMillis() / 1000);
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
                    break;
            }
            Log.d(TAG, new Gson().toJson(usr));
            ready = true;
            return null;
        }

        protected void onPostExecute(Void voids){
            setContentView(R.layout.activity_main);
        }
    }
}
