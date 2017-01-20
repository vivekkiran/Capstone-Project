package com.vivek.codemozo.rest;


import com.vivek.codemozo.utils.AppUtils;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RestApiClient {
    private static final HashMap<Class, Object> apiClients = new HashMap<>();
    private static final String API_BASE_URL = AppUtils.BASE_URL;


    static {
        setupRestClient(RestApiService.class);
    }

    public static RestApiService getRestApiService() {
        return (RestApiService) apiClients.get(RestApiService.class);
    }

    public static Retrofit getInstance() {
        final OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(5, TimeUnit.MINUTES)
                .connectTimeout(5, TimeUnit.MINUTES)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(API_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit;
    }


    private static void setupRestClient(Class type) {
        apiClients.put(type, getInstance().create(type));
    }
}
