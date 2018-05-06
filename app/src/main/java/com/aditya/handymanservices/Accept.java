package com.aditya.handymanservices;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import static android.util.Log.*;

public class Accept extends AppCompatActivity {

    Button accept;
    String current_location, destination;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept);

        accept = findViewById(R.id.accept);

        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                current_location = getIntent().getStringExtra("worker_location");
                destination = getIntent().getStringExtra("destination_details");

                String[] arr1=current_location.split(" ");
                String[] arr2=destination.split(" ");

                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?saddr="+arr1[0]+","+arr1[1]+"&daddr="+arr2[0]+","+arr2[1]));
                startActivity(intent);

            }
        });

    }
}
