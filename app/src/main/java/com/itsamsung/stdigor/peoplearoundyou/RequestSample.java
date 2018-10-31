package com.itsamsung.stdigor.peoplearoundyou;

import java.util.ArrayList;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;

public interface RequestSample {

    @GET("/api/reply")
    Call<ArrayList<Person>> lookForPeople(@Query("person") Person person);
}
