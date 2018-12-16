package com.itsamsung.stdigor.peoplearoundyou;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.Scanner;

public final class JSONFileLoader<T> {

    String file;
    Type type;
    Context context;

    public JSONFileLoader(String name, Type t, Context ctx){
        file = name;
        type = t;
        context = ctx;
    }

    public T get(){
        T ret = null;
        FileInputStream stream;
        try {
            stream = context.openFileInput(file);
            String str = read(stream);
            Log.d("JFL_GET", new Gson().toJson(str));
            ret = new Gson().fromJson(str, type);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public void save(T data){
        FileOutputStream stream;
        try {
            stream = context.openFileOutput(file, Context.MODE_PRIVATE);
            write(stream, data);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void write(FileOutputStream stream, T data){
        PrintWriter writer = new PrintWriter(stream);
        writer.print(new Gson().toJson(data));
        writer.flush();
        writer.close();
    }

    private String read(FileInputStream stream){
        String ret = "";
        try {
            if (stream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(stream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString;
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                stream.close();
                ret = stringBuilder.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }
}
