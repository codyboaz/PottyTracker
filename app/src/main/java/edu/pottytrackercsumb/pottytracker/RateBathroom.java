package edu.pottytrackercsumb.pottytracker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.ClientProtocolException;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;

public class RateBathroom extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //Initialize radio buttons
    private RadioGroup radioRatingsGroup;
    private RadioButton radioRatingsButton;
    private RadioButton tempRadioButton;
    //manages color of radio button when selected
    final private ArrayList <Integer> rColors = new ArrayList<Integer>();
    //bathroom rating info to database
    static int rate;
    static int bath_id;
    static SharedPreferences sharedPreferences;
    static String id, user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_bathroom);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Initialize facebook SDK
        FacebookSdk.sdkInitialize(getApplicationContext());

        //Initialize navigation drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Gets bathroom id of bathroom being rated based of lat and lng
        new MyAsyncTask().execute();

        //gets user id from shared prefs
        sharedPreferences = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        user_id = sharedPreferences.getString(Config.ID_SHARED_PREF,"Not Available");

        tempRadioButton = (RadioButton) findViewById(R.id.radioButton);
        tempRadioButton.setTextColor(Color.parseColor("#F4A460"));
        rColors.add(R.id.radioButton);

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioRatings);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                rColors.add(checkedId);

                for (int i = 0; i < rColors.size(); i++)
                {
                    tempRadioButton = (RadioButton) findViewById(rColors.get(i));
                    if (rColors.get(i) == checkedId)
                    {
                        tempRadioButton.setTextColor(Color.parseColor("#F4A460"));
                    }else
                        tempRadioButton.setTextColor(Color.parseColor("#F8F8FF"));

                }

            }


        });
        //enter button
        Button enter = (Button) findViewById(R.id.enterBttn);
        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onEnter();
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

        if (id == R.id.home) {
            Intent find = new Intent(RateBathroom.this, HomePage.class);
            startActivity(find);
        } else if (id == R.id.update_bathroom) {
            Intent update = new Intent(RateBathroom.this, UpdateBathroom.class);
            startActivity(update);
        }else if (id == R.id.find_bathroom) {
            Intent find = new Intent(RateBathroom.this, FindBathroom.class);
            startActivity(find);
        } else if (id == R.id.your_rating) {
            Intent ratings = new Intent(RateBathroom.this, YourRatings.class);
            startActivity(ratings);
        } else if (id == R.id.logout) {
            //Creating an alert dialog to confirm logout
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage("Are you sure you want to logout?");
            alertDialogBuilder.setPositiveButton("Yes",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                            //Getting out sharedpreferences
                            SharedPreferences preferences = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
                            //Getting editor
                            SharedPreferences.Editor editor = preferences.edit();
                            //Puting the value false for loggedin
                            editor.putBoolean(Config.LOGGEDIN_SHARED_PREF, false);
                            //Putting blank value to name
                            editor.putString(Config.NAME_SHARED_PREF, "");
                            //Saving the sharedpreferences
                            editor.commit();
                            //log user out of facebook
                            LoginManager.getInstance().logOut();
                            //Starting login activity
                            Intent intent = new Intent(RateBathroom.this, MainActivity.class);
                            startActivity(intent);
                        }
                    });

            alertDialogBuilder.setNegativeButton("No",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                        }
                    });

            //Showing the alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //on enter clicked listener
    public void onEnter(){

        radioRatingsGroup = (RadioGroup) findViewById(R.id.radioRatings);

        // get selected radio button from radioGroup
        int selectedId = radioRatingsGroup.getCheckedRadioButtonId();

        // find the radiobutton by returned id
        radioRatingsButton = (RadioButton) findViewById(selectedId);
        final String stars = radioRatingsButton.getText().toString();

        //user comment review
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText edittext = new EditText(this);
        alert.setMessage("Add a review (optional)");
        alert.setTitle(stars);
        alert.setView(edittext);
        rate = Integer.valueOf(stars);

        alert.setPositiveButton("Rate", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                //gets user comments
                String comment = edittext.getText().toString();
                comment = comment.replaceAll(" ","%20");
                sharedPreferences = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);

                String rating_by = sharedPreferences.getString(Config.NAME_SHARED_PREF, "Not Available");
                rating_by = rating_by.replaceAll(" ","%20");

                //build URL to save rating in database
                String restURL = "http://codyboaz.com/PottyTracker/rating.php?user_id=" + user_id
                        + "&bath_id=" + bath_id + "&rating=" + rate + "&comment=" + comment + "&rating_by=" + rating_by;
                //call to REST opp
                new RestOperation().execute(restURL);
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        alert.show();


    }

    //REST opp to send rating info to database
    private class RestOperation extends AsyncTask<String, Void, Void> {

        String content;
        String error;
        String data = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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
            //after rating complete goes back to bathroom page
            Intent i = new Intent(RateBathroom.this, BathroomPage.class);
            startActivity(i);
        }
    }

    //get bathroom info that needs to be stored with rating info in database
    class MyAsyncTask extends AsyncTask<String, String, Void> {

        InputStream inputStream = null;
        String result = "";

        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(String... params) {

            String url_select = "http://codyboaz.com/PottyTracker/bathInfo.php?lat=" + sharedPreferences.getString(Config.LAT_SHARED_PREF,"Not Available") + "&lng=" + sharedPreferences.getString(Config.LONG_SHARED_PREF,"Not Available");
            ArrayList<NameValuePair> param = new ArrayList<NameValuePair>();
            try {
                // HttpClient is more then less deprecated. Need to change to URLConnection
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(url_select);
                httpPost.setEntity(new UrlEncodedFormEntity(param));
                HttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();
                // Read content & Log
                inputStream = httpEntity.getContent();
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            } catch (ClientProtocolException e2) {
                Log.e("ClientProtocolException", e2.toString());
                e2.printStackTrace();
            } catch (IllegalStateException e3) {
                Log.e("IllegalStateException", e3.toString());
                e3.printStackTrace();
            } catch (IOException e4) {
                Log.e("IOException", e4.toString());
                e4.printStackTrace();
            }
            // Convert response to string using String Builder
            try {
                BufferedReader bReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"), 8);
                StringBuilder sBuilder = new StringBuilder();

                String line = null;
                while ((line = bReader.readLine()) != null) {
                    sBuilder.append(line + "\n");
                }
                inputStream.close();
                result = sBuilder.toString();

            } catch (Exception e) {
            }
            return null;
        }

        protected void onPostExecute(Void v) {
            //parse JSON data
            try {
                JSONArray jArray = new JSONArray(result);
                for(int i=0; i < jArray.length(); i++) {
                    //get bath ID
                    JSONObject jObject = jArray.getJSONObject(i);
                    id = jObject.getString("ID");
                    bath_id = Integer.valueOf(id);
                }
            } catch (JSONException e) {
                Log.e("JSONException", "Error: " + e.toString());
            }
        }
    }
}