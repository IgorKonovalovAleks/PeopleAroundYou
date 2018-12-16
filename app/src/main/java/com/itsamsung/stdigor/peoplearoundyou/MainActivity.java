package com.itsamsung.stdigor.peoplearoundyou;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;

import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class MainActivity extends AppCompatActivity {

    User usr;
    Button button;
    Context context;
    EditText nameEdit, statusEdit;
    private static final String TAG = "TAG";
    public static final String BASE_URL = "https://peoplearoundyou.herokuapp.com";
    boolean ready;
    boolean onSavingAwaiting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ready = false;
        onSavingAwaiting = false;
        nameEdit = findViewById(R.id.NameEdit);
        statusEdit = findViewById(R.id.StatusEdit);
        button = findViewById(R.id.button);
        context = this.getApplicationContext();
        new LoadPerson().execute();
    }

    public void click(View view){
        usr.person.nickname = nameEdit.getText().toString();
        usr.person.status = statusEdit.getText().toString();
        usr.lastCall = (int)(System.currentTimeMillis() / 1000);
        new Auth().execute();
        new SavePerson().execute();
    }

    public void onAuthenticated(){
        if (ready) {
            Intent i = new Intent(MainActivity.this, People.class);
            i.putExtra("user", new Gson().toJson(usr));
            startActivity(i);
        } else {
            onSavingAwaiting = true;
        }
    }

    public void onSaved(){
        ready = true;
        if (onSavingAwaiting) {
            onSavingAwaiting = false;
            onAuthenticated();
        }
    }

    public void onLoaded(Person per){
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

    private void setDefault(){
        nameEdit.setText("default user");
        statusEdit.setText("I'm a newcomer");
        usr = new User();
        usr.person = new Person();
        usr.person.nickname = "default user";
        usr.person.status = "I'm a newcomer";
        usr.person.latitude = 0;
        usr.person.longitude = 0;
    }

    class SavePerson extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            JSONFileLoader<Person> jfl = new JSONFileLoader<>("User.txt", Person.class, MainActivity.this);
            jfl.save(usr.person);
            return null;
        }

        protected void onPostExecute(Void v){
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

        protected void onPostExecute(Void v){
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
