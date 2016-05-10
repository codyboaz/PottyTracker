package edu.pottytrackercsumb.pottytracker;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;


public class CreateAccount extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    // create loggin information
    private EditText firstName, lastName, username, password, rePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //initialize navigation drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //cancel button
        Button cancel = (Button) findViewById(R.id.cancelButton);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(CreateAccount.this, MainActivity.class);
                startActivity(i);
            }
        });

        //create button
        Button create = (Button) findViewById(R.id.createBttn);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstName = (EditText) findViewById(R.id.firstName);
                lastName = (EditText) findViewById(R.id.lastName);
                username = (EditText) findViewById(R.id.username);
                password = (EditText) findViewById(R.id.password);
                rePassword = (EditText) findViewById(R.id.rePassword);

                //check if password and re-entered password are equal toast, else send data to database
                if (password.getText().toString().equals(rePassword.getText().toString())) {
                    //if one of the fields are empty
                    if (firstName.getText().toString().isEmpty() || lastName.getText().toString().isEmpty()
                            || username.getText().toString().isEmpty() || password.getText().toString().isEmpty()
                            || rePassword.getText().toString().isEmpty()) {
                        Toast.makeText(getApplicationContext(), "Please Enter all information",
                                Toast.LENGTH_LONG).show();
                    }else {
                        //build URL string for call to database
                        String restURL = "http://codyboaz.com/PottyTracker/create_user.php?username=" + username.getText().toString().trim()
                                + "&password=" + password.getText().toString().trim() + "&fName=" + firstName.getText().toString().trim()
                                + "&lName=" + lastName.getText().toString().trim();

                        SharedPreferences sharedPreferences = CreateAccount.this.getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
                        //Creating editor to store values to shared preferences
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        //stores full name in shared prefs
                        String fullName = firstName.getText().toString().trim() + " " + lastName.getText().toString().trim();

                        //Adding values to editor
                        editor.putBoolean(Config.LOGGEDIN_SHARED_PREF, true);
                        editor.putString(Config.NAME_SHARED_PREF, fullName);
                        //Saving values to editor
                        editor.commit();

                        //Call to REST operation function
                        new RestOperation().execute(restURL);
                    }
                } else { //if passwords dont match
                    Toast.makeText(getApplicationContext(), "Passwords do not match",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_page, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

       if (id == R.id.login) {
            Intent logIn = new Intent(CreateAccount.this, MainActivity.class);
            startActivity(logIn);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //REST call to store data to database
    private class RestOperation extends AsyncTask<String, Void, Void> {

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
                //get URL
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

            //Start new intent
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