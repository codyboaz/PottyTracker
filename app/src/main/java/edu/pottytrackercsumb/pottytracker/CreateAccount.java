package edu.pottytrackercsumb.pottytracker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;


public class CreateAccount extends AppCompatActivity {

    private EditText firstName, lastName, username, password, rePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button cancel = (Button) findViewById(R.id.cancelButton);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(CreateAccount.this, MainActivity.class);
                startActivity(i);
            }
        });

        firstName = (EditText) findViewById(R.id.firstName);
        lastName = (EditText) findViewById(R.id.lastName);
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);


        Button create = (Button) findViewById(R.id.createBttn);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstName = (EditText) findViewById(R.id.firstName);
                lastName = (EditText) findViewById(R.id.lastName);
                username = (EditText) findViewById(R.id.username);
                password = (EditText) findViewById(R.id.password);
                rePassword = (EditText) findViewById(R.id.rePassword);

                if (password.getText().toString().equals(rePassword.getText().toString())) {
                    if (firstName.getText().toString().isEmpty() || lastName.getText().toString().isEmpty()
                            || username.getText().toString().isEmpty() || password.getText().toString().isEmpty()
                            || rePassword.getText().toString().isEmpty()) {
                        Toast.makeText(getApplicationContext(), "Please Enter all information",
                                Toast.LENGTH_LONG).show();
                    } else {
                        String restURL = "http://seansabour.net/turbofinance/create_user.php?username=" + username.getText().toString().trim()
                                + "&password=" + password.getText().toString().trim() + "&fName=" + firstName.getText().toString().trim()
                                + "&lName=" + lastName.getText().toString().trim();
                        new RestOperation().execute(restURL);

                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Passwords do not match",
                            Toast.LENGTH_SHORT).show();
                }

            }
        });



    }

    private class RestOperation extends AsyncTask<String, Void, Void> {

        final HttpClient httpClient = new DefaultHttpClient();
        String content;
        String error;
        ProgressDialog progressDialog = new ProgressDialog(CreateAccount.this);
        String data = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog.setTitle("Please wait ...");
            progressDialog.show();

        }

        @Override
        protected Void doInBackground(String... params) {
            BufferedReader br = null;

            URL url;
            try {
                url = new URL(params[0]);

                URLConnection connection = url.openConnection();
                connection.setDoOutput(true);


                OutputStreamWriter outputStreamWr = new OutputStreamWriter(connection.getOutputStream());
                outputStreamWr.write(data);
                outputStreamWr.flush();

                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = null;

                while((line = br.readLine())!=null) {
                    sb.append(line);
                    sb.append(System.getProperty("line.separator"));
                }

                content = sb.toString();



            } catch (MalformedURLException e) {
                error = e.getMessage();
                e.printStackTrace();
            } catch (IOException e) {
                error = e.getMessage();
                e.printStackTrace();
            } finally {
                try {
                    br.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            Toast.makeText(getApplicationContext(), "Account created", Toast.LENGTH_LONG).show();
            Intent i = new Intent(CreateAccount.this,HomePage.class);
            i.putExtra("Username", username.getText().toString());
            i.putExtra("First Name", firstName.getText().toString());
            i.putExtra("Last Name", lastName.getText().toString());
            i.putExtra("firstLogin",true);
            startActivity(i);
        }
    }



}
