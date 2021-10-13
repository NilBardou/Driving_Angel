package com.nilbardou.drivingangel;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.audiofx.BassBoost;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class MapsActivity extends Fragment {

    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE = 101;
    private Context mContext;

    public MapsActivity() {

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_maps_activity, container, false);
        mContext = container.getContext();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity()
                , Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            getCurrentLocation();

        }else{
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
        }


        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);

        //Async Map
        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull final GoogleMap googleMap) {

                Handler handler2 = new Handler();
                handler2.postDelayed(new Runnable() {
                    public void run() {
                        if (currentLocation != null) {
                            LatLng latLng2 = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            MarkerOptions markerOptions = new MarkerOptions().position(latLng2).title("I am here.");
                            googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng2));
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng2, 15));
                            googleMap.addMarker(markerOptions);

                            //obtenerAreaServicio();
                        }
                    }
                }, 500);


                //when map is loaded
                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

                    @Override
                    public void onMapClick(@NonNull LatLng latLng) {
                        //when click in map
                        //Initialize marker options
                        MarkerOptions markerOptions = new MarkerOptions();
                        //set postion of marker
                        markerOptions.position(latLng);
                        //set title of marker
                        markerOptions.title(latLng.latitude + " : " + latLng.longitude);
                        //remove all marker
                        googleMap.clear();
                        //animating to zoom marker
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                        //add marker on map
                        googleMap.addMarker(markerOptions);
                    }

                });
            }
        });

        return view;
    }


    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        //Initialize loaction manager
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        //check condition
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            //when location service is enabled
            //Get last loaction
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    //Initialize location
                    Location location = task.getResult();
                    if (location != null){
                        currentLocation = location;
                    }else{
                        LocationRequest locationRequest = new LocationRequest().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                                .setInterval(10000).setFastestInterval(1000).setNumUpdates(1);

                        LocationCallback locationCallback = new LocationCallback(){
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                Location location1 = locationResult.getLastLocation();
                            }
                        };

                        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());

                    }

                }
            });
        }else{
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }

    }
/*
    public void obtenerAreaServicio(){
        //Initialize url
        String url = "https://maps.googleapis.com/maps/api/..." +
                "?location=" + currentLocation.getLatitude() + "," + currentLocation.getLongitude() + //location
                "&radius=5000" + "&type=" + placeTypeList[i] +   //nearby radius + place type
                "&sensor=true" + "&key=" + getResources().getString(R.string.google_maps_key);  // sensor + google map key

        //execute place task method to download json data
        new PlaceTask().execute(url);

    }


    private class PlaceTask extends AsyncTask<String,Integer,String> {

        @Override
        protected String doInBackground(String... strings) {
            String data = null;
            //Initialize data
            try {
                data = downloadUrl(strings[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return data;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

    private String downloadUrl(String string) throws IOException {
        //Initialize url
        URL url = new URL(string);
        //Initialize connection
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        //Connect connection
        connection.connect();
        //Initialize input stream
        InputStream stream = connection.getInputStream();
        //Initialize buffer reader
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        //Initialize string builder
        StringBuilder builder = new StringBuilder();
        //Initialize string variable
        String line = "";
        //Use while loop
        while ((line = reader.readLine()) != null){
            //Append line
            builder.append(line);
        }
        //Get append data
        String data = builder.toString();
        //closer reader
        reader.close();
        return data;
    }*/
}