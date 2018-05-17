package com.hoathan.hoa.thuchanhgooglemap.app;

import android.app.Application;

import com.hoathan.hoa.thuchanhgooglemap.network.MapManagerAPIService;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Tungnguyenbk54 on 9/9/2017.
 */

public class MapManagerApplication extends Application {

    public static Retrofit mRetrofit;
    public static MapManagerAPIService apiService;
    public static final String BASE_API = "https://maps.googleapis.com/maps/";
    //https://maps.googleapis.com/maps/api/place/nearbysearch/json


    @Override
    public void onCreate() {
        super.onCreate();

        mRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_API)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = mRetrofit.create(MapManagerAPIService.class);

    }
}
