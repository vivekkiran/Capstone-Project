package com.vivek.codemozo.rest;

import com.vivek.codemozo.model.Contest;
import com.vivek.codemozo.model.Website;


import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;


public interface RestApiService {

    @GET("/api/v1/contest/")
    Observable<Contest.Response> getContestsList(
            @Query("limit") int limit,
            @Query("end__gt") String date,
            @Query("order_by") String orderBy,
            @Query("username") String userName,
            @Query("api_key") String userKey
    );

    @GET("/api/v1/contest/{id}/")
    Observable<Contest.Response> getContestsById(
            @Path("id") int id,
            @Query("username") String userName,
            @Query("api_key") String userKey
    );

    @GET("/api/v1/resource/")
    Observable<Website.Response> getResourceList(
            @Query("username") String userName,
            @Query("api_key") String userKey
    );

    @GET("/api/v1/resource/{id}/")
    Observable<Website.Response> getResourceById(
            @Path("id") int id,
            @Query("username") String userName,
            @Query("api_key") String userKey
    );

}
