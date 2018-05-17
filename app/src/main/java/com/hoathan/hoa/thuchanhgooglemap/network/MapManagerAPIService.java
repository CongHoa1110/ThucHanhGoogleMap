package com.hoathan.hoa.thuchanhgooglemap.network;

import com.hoathan.hoa.thuchanhgooglemap.model.DirectionResponse;
import com.hoathan.hoa.thuchanhgooglemap.model.GeocodingMap;
import com.hoathan.hoa.thuchanhgooglemap.model.Placenearbysearch;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Tungnguyenbk54 on 9/9/2017.
 */

public interface MapManagerAPIService {

    @GET("api/directions/json")
    Call<DirectionResponse> getMap(@Query("origin") String origin,
                                   @Query("destination") String destination,
                                   @Query("key") String key);

    @GET("api/geocode/json")
    Call<GeocodingMap> getGeocodingMap(@Query("address") String address,
                                       @Query("key") String key);
    @GET("api/place/nearbysearch/json")
    Call<Placenearbysearch> getPlacenearbysearch(@Query("location") String location,
                                                 @Query("radius") String radius,
                                                 @Query("type") String type,
                                            @Query("key") String key);


}
