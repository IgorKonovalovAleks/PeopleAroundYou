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

    Person person;
    Button button;
    Context context;
    private static final String TAG = "TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.button);
        context = this.getApplicationContext();

        person = new Person();

        new LoadPerson("read").execute();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, People.class);
                i.putExtra("person", new Gson().toJson(person));
                startActivity(i);
            }
        });
    }


    class LoadPerson extends AsyncTask<Void, String, Person> {

        String mode;

        public LoadPerson(String mode){
            this.mode = mode;
        }

        @Override
        protected Person doInBackground(Void... voids) {
            JSONBaseModule jbm = new JSONBaseModule("User.txt", context);
            Person per = new Person();
            if (mode == "write"){
                jbm.saveData(person);
            } else if (mode == "read"){
                per = jbm.getData();
            }
            Log.d(TAG, new Gson().toJson(per));
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
                Log.i("Normal", Double.toString(person.latitude));
            }
        }
    }
}
