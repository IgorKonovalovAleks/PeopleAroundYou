package com.itsamsung.stdigor.peoplearoundyou;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public final class JSONBaseModule {

    private File file;
    private Context context;
    private Gson gson;

    public JSONBaseModule(String name, Context ctx){
        this.context = ctx;
        this.file = new File(context.getFilesDir().toString(), name);
        gson = new Gson();
    }


    public void saveData(Person person) {
        String data = gson.toJson(person);
        Log.d("TAG_WRITE", data);
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(file));
            writer.print(data);
            writer.close();
        } catch (IOException e) {
            Log.e("TAG", "Error in Writing: " + e.getLocalizedMessage());
        }
    }

    public Person getData() {
        try {
            Scanner scanner = new Scanner(file);
            String str = "";
            Person person;
            while(scanner.hasNext()){
                str += scanner.next();
            }
            Log.d("TAG_READ", str);
            person = gson.fromJson(str, Person.class);
            return person;
        } catch (IOException e) {
            Log.e("TAG", "Error in Reading: " + e.getLocalizedMessage());
            return null;
        }
    }
}
