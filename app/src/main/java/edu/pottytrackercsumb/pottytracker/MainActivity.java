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
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private boolean loggedIn = false;
    private CallbackManager callbackManager;
    private String firstName, user_id;
    private Profile profile;
    private ProfileTracker mProfileTracker;
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharedPreferences = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);



        Button create = (Button) findViewById(R.id.createAccountBttn);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, CreateAccount.class);
                startActivity(i);
            }
        });



        final EditText userinput = (EditText) findViewById(R.id.username);
        final EditText password = (EditText) findViewById(R.id.password);
        password.setTransformationMethod(new PasswordTransformationMethod());


        // Login to database
        Button login = (Button) findViewById(R.id.loginBttn);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String restURL = "http://codyboaz.com/PottyTracker/get_login.php?username=" + userinput.getText().toString().trim() + "&password=" + password.getText().toString().trim();
                new RestOperation().execute(restURL);

            }
        });

        // connect with facebook
        LoginButton loginButton = (LoginButton)findViewById(R.id.login_button);
        loginButton.setReadPermissions("user_friends");


        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                loginResult.getAccessToken().getToken();

                GraphRequest.newMeRequest(
                        loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {

                            @Override
                            public void onCompleted(JSONObject me, GraphResponse response) {
                                if (response.getError() != null) {
                                    Log.e("test2", "Did i get here");
                                } else {
                                    if(Profile.getCurrentProfile() == null) {
                                        Log.e("test", "Did i get here 2");
                                        mProfileTracker = new ProfileTracker() {
                                            @Override
                                            protected void onCurrentProfileChanged(Profile profile, Profile profile2) {
                                                Log.v("facebook - profile", profile2.getFirstName());
                                                mProfileTracker.stopTracking();
                                            }
                                        };
                                        mProfileTracker.startTracking();
                                    } else {
                                        profile = Profile.getCurrentProfile();
                                        firstName = profile.getName();
                                        user_id = profile.getId();
                                        Log.e("test", firstName);

                                        //Creating editor to store values to shared preferences
                                        SharedPreferences.Editor editor = sharedPreferences.edit();

                                        //Adding values to editor
                                        editor.putBoolean(Config.LOGGEDIN_SHARED_PREF, true);
                                        editor.putString(Config.NAME_SHARED_PREF, firstName);
                                        editor.putString(Config.ID_SHARED_PREF, user_id);


                                        //Saving values to editor
                                        editor.commit();
                                    }
                                }
                                Intent intent = new Intent(MainActivity.this, HomePage.class);

                                startActivity(intent);
                            }


                        }).executeAsync();







            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException e) {

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //In onresume fetching value from sharedpreference
        SharedPreferences sharedPreferences = getSharedPreferences(Config.SHARED_PREF_NAME,Context.MODE_PRIVATE);

        //Fetching the boolean value form sharedpreferences
        loggedIn = sharedPreferences.getBoolean(Config.LOGGEDIN_SHARED_PREF, false);


        //If we will get true
        if(loggedIn){
            //We will start the Profile Activity
            Intent intent = new Intent(MainActivity.this, HomePage.class);
            startActivity(intent);
        }
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

        if (id == R.id.find_bathroom) {
            Intent find = new Intent(MainActivity.this, FindBathroom.class);
            startActivity(find);
        } else if (id == R.id.update_bathroom) {
            Intent update = new Intent(MainActivity.this, UpdateBathroom.class);
            startActivity(update);
        } else if (id == R.id.create) {
            Intent create = new Intent(MainActivity.this, CreateAccount.class);
            startActivity(create);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private class RestOperation extends AsyncTask<String, Void, Void> {

        String content;
        String error;
        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        String data = "";
        final EditText userinput = (EditText) findViewById(R.id.username);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog.setTitle("Please wait ...");
            progressDialog.show();

            try {
                data += "&" + URLEncoder.encode("data", "UTF-8") + "=" + userinput.getText();
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
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

            if(error!=null) {
                // Set error if something happens!?
            } else {
                try {
                    JSONObject o = new JSONObject(content);
                    String fName = o.getString("firstName");
                    String lName = o.getString("lastName");
                    String id = o.getString("ID");

                    String fullName = fName + " " + lName;

                    Intent myIntent = new Intent(MainActivity.this, HomePage.class);

                    //Creating editor to store values to shared preferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();

                    //Adding values to editor
                    editor.putBoolean(Config.LOGGEDIN_SHARED_PREF, true);
                    editor.putString(Config.NAME_SHARED_PREF, fullName);
                    editor.putString(Config.ID_SHARED_PREF, id);

                    //Saving values to editor
                    editor.commit();

                    progressDialog.setTitle("Sucessful Login ...");
                    progressDialog.show();
                    startActivity(myIntent);
                    progressDialog.dismiss();
                    finish();
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Incorrect username or password",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }




}
