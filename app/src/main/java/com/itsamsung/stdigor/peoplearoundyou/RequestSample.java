package com.itsamsung.stdigor.peoplearoundyou;

import java.util.ArrayList;

import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;

public interface RequestSample {

    @POST("/lookforpeople")
    Call<ArrayList<Person>> lookForPeople(@Body Person person);

    @POST("/lookforpeople")
    Call<Person> test(@Body Person person);
}
