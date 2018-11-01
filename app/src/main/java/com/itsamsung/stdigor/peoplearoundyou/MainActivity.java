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
    TextView text;
    Context context;
    private static final String TAG = "TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.button);
        text = findViewById(R.id.textView);
        context = this.getApplicationContext();

        //Just an example of user's settings
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
