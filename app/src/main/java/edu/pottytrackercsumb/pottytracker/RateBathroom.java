package edu.pottytrackercsumb.pottytracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.client.Firebase;

import java.util.Random;

public class RateBathroom extends AppCompatActivity {
    private static final String FIREBASE_URL = "https://potty-tracker.firebaseio.com/";

    private Firebase firebaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_bathroom);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Firebase.setAndroidContext(this);
        firebaseRef = new Firebase(FIREBASE_URL);

        EditText inputText = (EditText) findViewById(R.id.comments);
        inputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendMessage();
                }
                return true;
            }
        });

        Button update = (Button) findViewById(R.id.cancelBttn);
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RateBathroom.this, HomePage.class);
                startActivity(i);
            }
        });

        findViewById(R.id.enterBttn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    public void sendMessage(){
        EditText textInput = (EditText) findViewById(R.id.comments);
        String message = textInput.getText().toString();
        if(!message.equals("")) {
            Random rand = new Random();
            String author = "TestUser" + rand.nextInt(1000);
            ChatMessage cMessage = new ChatMessage(author, message);
            firebaseRef.push().setValue(cMessage);
            textInput.setText("");
        }
    }





}
