package edu.pottytrackercsumb.pottytracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class HomePage extends AppCompatActivity {
    private String firstName, lastName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button logout = (Button) findViewById(R.id.logoutBttn);
        logout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent logout = new Intent(HomePage.this, MainActivity.class);
                startActivity(logout);
                finish();
            }
        });

        Button map = (Button) findViewById(R.id.findBttn);
        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(HomePage.this, FindBathroom.class);
                startActivity(i);
            }
        });

        Button ratings = (Button) findViewById(R.id.ratingBttn);
        ratings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(HomePage.this, YourRatings.class);
                startActivity(i);
            }
        });


        Button rate = (Button) findViewById(R.id.rateBttn);
        rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(HomePage.this, RateBathroom.class);
                startActivity(i);
            }
        });

        final TextView helloUser = (TextView) findViewById(R.id.helloUser);
        Intent myIntent = getIntent();
        firstName = myIntent.getStringExtra("First Name");
        lastName = myIntent.getStringExtra("Last Name");

        helloUser.setText(helloUser.getText() + " " + firstName + " " + lastName + "!");


    }

}
