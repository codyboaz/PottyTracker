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
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.ClientProtocolException;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;

public class BathroomPage extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //boolean to see if user is logged in
    private boolean loggedIn = false;
    //Bathroom information
    static String bathN, bathA, avgRate, city, state, id;
    //shared preferences to store user and bathroom information locally
    static SharedPreferences sharedPreferences;
    //ArrayList and Listview to show comments in list
    private static ArrayList<String> listItems = new ArrayList<String>();
    private static ListView mainListView;
    private static ArrayAdapter<String> listAdapter;
    private static ArrayList<String> tempString = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Fetching value from sharedpreference
        sharedPreferences = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        //Fetching the boolean value form sharedpreferences to see if user is logged in
        setContentView(R.layout.activity_bathroom_page2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Initialize facebook sdk so user can log out
        FacebookSdk.sdkInitialize(getApplicationContext());

        //Initialize navigation drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Initialize Listview to display comments
        mainListView = (ListView) findViewById(R.id.list);

        //AsyncTask to get bathroom info from database
        new MyAsyncTask().execute();

        //Rate bathroom button
        Button rate = (Button)findViewById(R.id.rateBttn);
        rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ratePage = new Intent(BathroomPage.this, RateBathroom.class);
                startActivity(ratePage);
            }
        });

        //Back buttton
        Button back = (Button)findViewById(R.id.backBttn);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backPage = new Intent(BathroomPage.this, FindBathroom.class);
                startActivity(backPage);
            }
        });
    }

    //Adds all bathroom comments to List
    private static void addToList(String r){
        tempString.add(r);
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
            Intent home = new Intent(BathroomPage.this, HomePage.class);
            startActivity(home);
        }else if (id == R.id.update_bathroom) {
            Intent update = new Intent(BathroomPage.this, UpdateBathroom.class);
            startActivity(update);
        }else if (id == R.id.find_bathroom) {
            Intent find = new Intent(BathroomPage.this, FindBathroom.class);
            startActivity(find);
        }else if (id == R.id.your_rating) {
            Intent ratings = new Intent(BathroomPage.this, YourRatings.class);
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
                            SharedPreferences preferences = getSharedPreferences(Config.SHARED_PREF_NAME,Context.MODE_PRIVATE);
                            //Getting editor
                            SharedPreferences.Editor editor = preferences.edit();
                            //Puting the value false for loggedin
                            editor.putBoolean(Config.LOGGEDIN_SHARED_PREF, false);
                            //Putting blank value to name
                            editor.putString(Config.NAME_SHARED_PREF, "");
                            //Saving the sharedpreferences
                            editor.commit();
                            //Logs user out of facebook
                            LoginManager.getInstance().logOut();
                            //Starting login activity
                            Intent intent = new Intent(BathroomPage.this, MainActivity.class);
                            startActivity(intent);
                        }
                    });
            //if user selects not to log out
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

    //gets bathroom data from database
    class MyAsyncTask extends AsyncTask<String, String, Void> {

        InputStream inputStream = null;
        String result = "";

        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(String... params) {

            String url_select = "http://codyboaz.com/PottyTracker/bathPage.php?lat=" + sharedPreferences.getString(Config.LAT_SHARED_PREF,"Not Available") + "&lng=" + sharedPreferences.getString(Config.LONG_SHARED_PREF,"Not Available");

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
                    JSONObject jObject = jArray.getJSONObject(i);

                    //parses json response to get a bath info
                    bathN = jObject.getString("name");
                    bathA = jObject.getString("address");
                    city = jObject.getString("city");
                    state = jObject.getString("state");
                    id = jObject.getString("ID");
                    avgRate = jObject.getString("avgRate");

                    //Changes UI to have appropriate values
                    String info = bathA + ", " + city + ", " + state;
                    TextView bathName = (TextView)findViewById(R.id.bathName);
                    bathName.setText(bathN);
                    TextView rating = (TextView)findViewById(R.id.bathRating);
                    rating.setText(avgRate);
                    TextView bathAddress = (TextView)findViewById(R.id.bathAddress);
                    bathAddress.setText(info);
                    SharedPreferences preferences = getSharedPreferences(Config.SHARED_PREF_NAME,Context.MODE_PRIVATE);

                    //Getting editor
                    SharedPreferences.Editor editor = preferences.edit();
                    //Saves bath_id in shared pref to retrieve proper comments/ Rate proper bathroom
                    editor.putString(Config.BATH_ID_SHARED_PREF, id);
                    //Saving the sharedpreferences
                    editor.commit();
                }
            } catch (JSONException e) {
                Log.e("JSONException", "Error: " + e.toString());
            }
            //call to AsyncTask2 to retrieve comments data
            new MyAsyncTask2().execute();
        }
    }
    //Gets comments data
    class MyAsyncTask2 extends AsyncTask<String, String, Void> {

        InputStream inputStream = null;
        String result = "";

        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(String... params) {
            sharedPreferences = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
            //URL to get comments with specific bath_id
            String url_select = "http://codyboaz.com/PottyTracker/getComment.php?bath_id=" + sharedPreferences.getString(Config.BATH_ID_SHARED_PREF, "00");;
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
                //clear list so comment arent double posted
                tempString.clear();
                listItems.clear();
                for(int i=0; i < jArray.length(); i++) {
                    JSONObject jObject = jArray.getJSONObject(i);

                    //parse JSON response for bath comments
                    String name = jObject.getString("rating_by");
                    String rate = jObject.getString("rating");
                    String comment = jObject.getString("comment");
                    comment = rate + "                  " + name + "      " + comment;
                    //adds comment to listview
                    addToList(comment);
                }
                //if there are comment add them to the list
                if (tempString.size() > 0) {
                    listItems.addAll(tempString);
                    tempString.clear();
                }
                listAdapter = new ArrayAdapter<String>(BathroomPage.this, android.R.layout.simple_list_item_1, listItems){
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent){

                        // Get the Item from ListView
                        View view = super.getView(position, convertView, parent);
                        // Initialize a TextView for ListView each Item
                        TextView tv = (TextView) view.findViewById(android.R.id.text1);
                        // Set the text color of TextView (ListView Item)
                        tv.setTextColor(Color.parseColor("#F8F8FF"));
                        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
                        // Generate ListView Item using TextView
                        return view;
                    }
                };
                mainListView.setAdapter(listAdapter);
            } catch (JSONException e) {
                Log.e("JSONException", "Error: " + e.toString());
            }
        }
    }
}
