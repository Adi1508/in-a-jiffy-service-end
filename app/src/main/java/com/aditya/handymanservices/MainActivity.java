package com.aditya.handymanservices;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    LocationManager locationManager;
    String lattitude,longitude;
    private static final int REQUEST_LOCATION = 1;

    Button register;
    EditText name;
    EditText mobile;
    Spinner serviceProvided;
    private RequestQueue rQueue;
    String selected_service;
    String status, data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        register = findViewById(R.id.register);
        name = findViewById(R.id.name);
        mobile = findViewById(R.id.mobile);
        serviceProvided = findViewById(R.id.service_spinner);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    buildAlertMessageNoGps();

                } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    data = getLocation();
                    System.out.println(data);
                    String[] arr = data.split(" ");
                    //api call send the data
                    selected_service = serviceProvided.getSelectedItem().toString();
                    try {
                        String url = "https://handymanv1.herokuapp.com/api/worker/" + name.getText().toString() + "/" + arr[0] + "/" + arr[1]+"/"+mobile.getText().toString() +"/"+selected_service.toLowerCase(); //insert api url

                        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String s) {
                                System.out.println("Response " + s);
                                try {
                                    JSONObject jsonObject = new JSONObject(s);
                                    String status = String.valueOf(jsonObject.get("response"));

                                    System.out.println("Result: "+status);

                                    if (status.equals("true")) {

                                        //starting the background service
                                        Intent service = new Intent(getApplicationContext(), NotifierService.class);
                                        service.putExtra("location_details",data);
                                        service.putExtra("services_req", selected_service.toLowerCase());
                                        startService(service);

                                        Intent intent = new Intent(MainActivity.this, LandingActivity.class);
                                        intent.putExtra("location_details", data);
                                        startActivity(intent);

                                    }else{
                                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                        startActivity(intent);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                Log.d("Error", volleyError.toString());
                            }
                        });

                        getRequestQueue().add(request);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        });
    }

    public RequestQueue getRequestQueue() {
        if (rQueue == null) {
            rQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return rQueue;
    }

    private String getLocation() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        } else {
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            Location location1 = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            Location location2 = locationManager.getLastKnownLocation(LocationManager. PASSIVE_PROVIDER);

            if (location != null) {
                double latti = location.getLatitude();
                double longi = location.getLongitude();
                lattitude = String.valueOf(latti);
                longitude = String.valueOf(longi);

                //Toast.makeText(this,lattitude+" "+longitude,Toast.LENGTH_SHORT).show();
                return lattitude+" "+longitude;

            } else  if (location1 != null) {
                double latti = location1.getLatitude();
                double longi = location1.getLongitude();
                lattitude = String.valueOf(latti);
                longitude = String.valueOf(longi);

                //Toast.makeText(this,lattitude+" "+longitude,Toast.LENGTH_SHORT).show();
                return lattitude+" "+longitude;


            } else  if (location2 != null) {
                double latti = location2.getLatitude();
                double longi = location2.getLongitude();
                lattitude = String.valueOf(latti);
                longitude = String.valueOf(longi);

                //Toast.makeText(this,lattitude+" "+longitude,Toast.LENGTH_SHORT).show();
                return lattitude+" "+longitude;

            }else{

                Toast.makeText(this,"Unble to Trace your location",Toast.LENGTH_SHORT).show();

            }
        }

        return lattitude+" "+longitude;
    }

    protected void buildAlertMessageNoGps() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please Turn ON your GPS Connection")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
}
