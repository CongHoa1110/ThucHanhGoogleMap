package com.hoathan.hoa.thuchanhgooglemap;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.hoathan.hoa.thuchanhgooglemap.app.MapManagerApplication;
import com.hoathan.hoa.thuchanhgooglemap.model.DirectionResponse;
import com.hoathan.hoa.thuchanhgooglemap.model.GeocodingMap;
import com.hoathan.hoa.thuchanhgooglemap.model.Placenearbysearch;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.hoathan.hoa.thuchanhgooglemap.R.id.map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private LinearLayout lnDirction, lnPlace;
    private EditText edtSource,edtDesation,edtGeocoding;
    private TextView txvClock,txvDistance;
    private ProgressDialog progressDialog;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private GoogleMap mMap;
    private static int PLACE_AUTOCOMPLETE_REQUEST_CODE = 12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);
        inUnit();

    }

    private void inUnit() {
        lnDirction = (LinearLayout) findViewById(R.id.ln_direction);
        lnPlace = (LinearLayout) findViewById(R.id.ln_place);
        edtSource = (EditText) findViewById(R.id.edt_diemDi);
        edtDesation = (EditText) findViewById(R.id.edt_diemden);
        txvClock = (TextView) findViewById(R.id.txv_clock);
        txvDistance = (TextView) findViewById(R.id.txv_distance);
        edtGeocoding = (EditText) findViewById(R.id.edt_place);
        edtSource.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                placeAutoComplete();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_next:
                lnDirction.setVisibility(View.GONE);
                lnPlace.setVisibility(View.VISIBLE);
                break;
            case R.id.img_back:
                lnPlace.setVisibility(View.GONE);
                lnDirction.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_search:
                progressDialog = ProgressDialog.show(this, "load.",
                        "direction..!", true);
                handleDirection();
                removeMapSearch();

                break;
            case R.id.img_goto:
                progressDialog = ProgressDialog.show(this, "load.",
                        "direction..!", true);
               // geoCodingMap();
                placeMap();
                Toast.makeText(this, "place", Toast.LENGTH_SHORT).show();
                break;

        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng sydney = new LatLng(21.027863, 105.836791);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,9));
        mMap.addMarker(new MarkerOptions().position(sydney)
                .title("hoa than")
                .snippet("aaaaaa"))
        ;

    }

    private void handleDirection(){
        String source = edtSource.getText().toString().trim();
        String desation = edtDesation.getText().toString().trim();

        Call<DirectionResponse> call = MapManagerApplication.apiService.getMap(source,desation,
                "AIzaSyAWXKL6njcdeyUkE5CVrZW-djCt_62aq3o"
                );
        call.enqueue(new Callback<DirectionResponse>() {
            @Override
            public void onResponse(Call<DirectionResponse> call, Response<DirectionResponse> response) {
                String points = response.body().getRoutes().get(0).getOverviewPolyline().getPoints();
                Log.d("TAG", points);

                List<LatLng> pointLatLngs = decodePoly(points);
               // Log.d("TAG", "Danh sach cac diem di qua: " + pointLatLngs.get(0));
                drawPath(pointLatLngs);
                String clock = response.body().getRoutes().get(0).getLegs().get(0).getDuration().getText();
                txvClock.setText(clock);
                String distance = response.body().getRoutes().get(0).getLegs().get(0).getDistance().getText();
                txvDistance.setText(distance);
            }

            @Override
            public void onFailure(Call<DirectionResponse> call, Throwable t) {

            }
        });
    }

    private void drawPath(List<LatLng> pointLatLngs){
        // Instantiates a new Polyline object and adds points to define a rectangle
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();
        PolylineOptions rectOptions = new PolylineOptions().geodesic(true).
                color(Color.RED).
                width(25);
        for (int i = 0; i < pointLatLngs.size(); i++) {

            rectOptions.add(pointLatLngs.get(i));


        }
     /*   LatLng sydney = pointLatLngs.get(0);
        mMap.addMarker(new MarkerOptions().position(sydney)
                .title(edtSource.getText().toString().trim())
                .snippet("aaaaaa"))
        ;
        LatLng haiPhong = pointLatLngs.get(pointLatLngs.size()-1);
        mMap.addMarker(new MarkerOptions().position(haiPhong)
                .title(edtDesation.getText().toString().trim())
                .snippet("aaaaaa"))
        ;*/

        originMarkers.add(mMap.addMarker(new MarkerOptions()
                .title(edtSource.getText().toString().trim())
                .position(pointLatLngs.get(0))));
        destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                .title(edtDesation.getText().toString().trim())
                .position( pointLatLngs.get(pointLatLngs.size()-1))));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pointLatLngs.get(0),12));

        //Get back the mutable Polyline
         //mMap.addPolyline(rectOptions);
        polylinePaths.add(mMap.addPolyline(rectOptions));
        progressDialog.dismiss();
        edtDesation.setText("");
        edtSource.setText("");
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }
    private void removeMapSearch(){

        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline:polylinePaths ) {
                polyline.remove();
            }
        }
    }
    private void geoCodingMap(){
        String geocoding = edtGeocoding.getText().toString().trim();
        Call<GeocodingMap> call = MapManagerApplication.apiService.getGeocodingMap(geocoding,
                "AIzaSyAWXKL6njcdeyUkE5CVrZW-djCt_62aq3o"
        );
        call.enqueue(new Callback<GeocodingMap>() {
            @Override
            public void onResponse(Call<GeocodingMap> call, Response<GeocodingMap> response) {
               Double lat = response.body().getResults().get(0).getGeometry().getLocation().getLat();
                Log.d("lat", "onResponse: " + lat);

                Double lng = response.body().getResults().get(0).getGeometry().getLocation().getLng();

                Log.d("lat", "onResponse: " + lng);
                LatLng sydney = new LatLng(lat, lng);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,15));
                mMap.addMarker(new MarkerOptions().position(sydney)
                        .title(response.body().getResults().get(0).getFormattedAddress()))
                ;
                progressDialog.dismiss();

            }

            @Override
            public void onFailure(Call<GeocodingMap> call, Throwable t) {

            }
        });

    }
    private void placeMap(){
        String location = "21.033192, 105.798276";
        String radius = "5000";
        String type = "restaurant";
        Call<Placenearbysearch> call = MapManagerApplication.apiService.getPlacenearbysearch(location,radius,type,
                "AIzaSyAWXKL6njcdeyUkE5CVrZW-djCt_62aq3o");
        call.enqueue(new Callback<Placenearbysearch>() {
            @Override
            public void onResponse(Call<Placenearbysearch> call, Response<Placenearbysearch> response) {
                List<Placenearbysearch.Result> result = response.body().getResults();
                for (int i = 0; i < result.size(); i++) {
                    double lat = result.get(i).getGeometry().getLocation().getLat();
                    double lng = result.get(i).getGeometry().getLocation().getLng();
                    LatLng vitri = new LatLng(lat, lng);
                    mMap.addMarker(new MarkerOptions().position(vitri)
                            .title(result.get(i).getName()).snippet(result.get(i).getVicinity()));
                    if (i == 0) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(vitri,15));
                    }
                }
               progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<Placenearbysearch> call, Throwable t) {

            }
        });
    }
    private void placeAutoComplete(){
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                edtSource.setText(place.getName());
                Log.i("success", "Place: " + place.getName());

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i("fail", status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

}
