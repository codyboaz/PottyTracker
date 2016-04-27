package edu.pottytrackercsumb.pottytracker;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class UpdateBathroom extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private boolean loggedIn = false;

    EditText addressEditText, cityEditText, stateEditText, nameEditText;

    // Used to utilize map capabilities
    private GoogleMap map;

    // Stores latitude and longitude data for addresses
    LatLng addressPos;

    // Used to place Marker on my map
    Marker addressMarker;

    private String Name;

    double lat, lng;

    static String Lat, Lng, newAddress , newCity, newState, newBathroom;
    static SharedPreferences preferences;
    static String user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //In onresume fetching value from sharedpreference
        SharedPreferences sharedPreferences = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        //Fetching the boolean value form sharedpreferences
        loggedIn = sharedPreferences.getBoolean(Config.LOGGEDIN_SHARED_PREF, false);
        if(loggedIn) {
            setContentView(R.layout.activity_update_bathroom);
        }else{
            setContentView(R.layout.activity_update_bathroom2);
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        preferences = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        user_id = preferences.getString(Config.ID_SHARED_PREF,"Not Available");

        FacebookSdk.sdkInitialize(getApplicationContext());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        // Initialize my Google Map
        try{

            if(map == null){

                map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

            }

            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            map.setMyLocationEnabled(true);
            map.setTrafficEnabled(true);
            map.setIndoorEnabled(true);
            map.setBuildingsEnabled(true);
            map.getUiSettings().setZoomControlsEnabled(true);

        }
        catch(Exception e){

            e.printStackTrace();

        }
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();

        LatLng loc = new LatLng(latitude, longitude);




        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.setMyLocationEnabled(true);
        map.setTrafficEnabled(true);
        map.setIndoorEnabled(true);
        map.setBuildingsEnabled(true);

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(loc)      // Sets the center of the map to location user
                .zoom(18)                   // Sets the zoom
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

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
            Intent home = new Intent(UpdateBathroom.this, HomePage.class);
            startActivity(home);
        } else if (id == R.id.login) {
            Intent login = new Intent(UpdateBathroom.this, MainActivity.class);
            startActivity(login);
        }else if (id == R.id.find_bathroom) {
            Intent find = new Intent(UpdateBathroom.this, FindBathroom.class);
            startActivity(find);
        }else if (id == R.id.create) {
            Intent create = new Intent(UpdateBathroom.this, CreateAccount.class);
            startActivity(create);
        } else if (id == R.id.your_rating) {
            Intent ratings = new Intent(UpdateBathroom.this, YourRatings.class);
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
                            preferences = getSharedPreferences(Config.SHARED_PREF_NAME,Context.MODE_PRIVATE);
                            //Getting editor
                            SharedPreferences.Editor editor = preferences.edit();

                            //Puting the value false for loggedin
                            editor.putBoolean(Config.LOGGEDIN_SHARED_PREF, false);

                            //Putting blank value to email
                            editor.putString(Config.NAME_SHARED_PREF, "");

                            //Saving the sharedpreferences
                            editor.commit();

                            LoginManager.getInstance().logOut();

                            //Starting login activity
                            Intent intent = new Intent(UpdateBathroom.this, MainActivity.class);
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


    // Called when getAddressButton is clicked
    public void showAddressMarker(View view) {

        // Initialize my EditTexts
        addressEditText = (EditText) findViewById(R.id.addressEditText);
        cityEditText = (EditText) findViewById(R.id.cityEditText);
        stateEditText = (EditText) findViewById(R.id.stateEditText);
        nameEditText = (EditText) findViewById(R.id.bathroomName);

        // Get the street address entered

        newAddress = addressEditText.getText().toString().trim();
        newCity = cityEditText.getText().toString().trim();
        newState = stateEditText.getText().toString().trim();
        newBathroom = nameEditText.getText().toString().trim();


        if(!newAddress.isEmpty() && !newCity.isEmpty() && !newState.isEmpty() && !newBathroom.isEmpty()){
            new PlaceAMarker().execute(newAddress, newCity, newState, newBathroom);

            CharSequence[] items = {" TAP HERE IF CORRECT LOCATION", "TAP HERE IF INCORRECT LOCATION"};
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {

                    if(item == 0) {
                        preferences = getSharedPreferences(Config.SHARED_PREF_NAME,Context.MODE_PRIVATE);
                        //Getting editor
                        SharedPreferences.Editor editor = preferences.edit();

                        //Putting blank value to email
                        editor.putString(Config.LAT_SHARED_PREF, Lat);

                        //Putting blank value to email
                        editor.putString(Config.LONG_SHARED_PREF, Lng);

                        //Saving the sharedpreferences
                        editor.commit();

                        newAddress = newAddress.replaceAll(" ","%20");
                        newCity = newCity.replaceAll(" ","%20");
                        newState = newState.replaceAll(" ","%20");
                        newBathroom = newBathroom.replaceAll(" ","%20");

                        String restURL = "http://codyboaz.com/PottyTracker/update_bathroom.php?name=" + newBathroom
                                + "&city=" + newCity + "&state=" + newState + "&lng=" + Lng + "&lat=" + Lat
                                + "&address=" + newAddress + "&user_ID=" + user_id;
                        new RestOperation().execute(restURL);

                    } else if(item == 1) {

                    } else if(item == 2) {

                    }
                }
            });

            AlertDialog dialog = builder.create();
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();

            wmlp.gravity = Gravity.BOTTOM | Gravity.LEFT;
            wmlp.x = 100;   //x position
            wmlp.y = 100;   //y position

            dialog.show();


        }
        else{
            Toast.makeText(UpdateBathroom.this, "Enter all fields", Toast.LENGTH_SHORT).show();
        }

    }


    class PlaceAMarker extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {

            // Get the 1st address passed
            String Address = params[0];
            String City = params[1];
            String State = params[2];
            Name = params[3];

            // Replace the spaces with %20
            Address = Address.replaceAll(" ","%20");
            City = City.replaceAll(" ","%20");
            State = State.replaceAll(" ","%20");

            // Call for the latitude and longitude and pass in that
            // we don't want directions
            getLatLong(Address,City,State, false);

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            // Draw the marker on the screen
            addressMarker = map.addMarker(new MarkerOptions()
                    .position(addressPos)
                    .title(Name)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.bathroom2))
                    .anchor(0.0f, 1.0f));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    addressPos, 16));


        }

    }


    protected void getLatLong(String address, String city, String state, boolean setDestination){

        // Define the uri that is used to get lat and long for our address
        String uri = "http://maps.google.com/maps/api/geocode/json?address=" +
                address + "?" + city + "?" + state + "&sensor=false";

        // Use the get method to retrieve our data
        HttpGet httpGet = new HttpGet(uri);

        // Acts as the client which executes HTTP requests
        HttpClient client = new DefaultHttpClient();

        // Receives the response from our HTTP request
        HttpResponse response;

        // Will hold the data received
        StringBuilder stringBuilder = new StringBuilder();

        try {

            // Get the response of our query
            response = client.execute(httpGet);

            // Receive the entity information sent with the HTTP message
            HttpEntity entity = response.getEntity();

            // Holds the sent bytes of data
            InputStream stream = entity.getContent();
            int byteData;

            // Continue reading data while available
            while ((byteData = stream.read()) != -1) {
                stringBuilder.append((char) byteData);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Holds key value mappings
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(stringBuilder.toString());




            // Get the returned latitude and longitude
            lng = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
                    .getJSONObject("geometry").getJSONObject("location")
                    .getDouble("lng");

            lat = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
                    .getJSONObject("geometry").getJSONObject("location")
                    .getDouble("lat");

            // Change the lat and long depending on if we want to set the
            // starting or ending destination


            Lat = String.valueOf(lat);
            Lng = String.valueOf(lng);

            addressPos = new LatLng(lat, lng);


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private class RestOperation extends AsyncTask<String, Void, Void> {

        String content;
        String error;
        ProgressDialog progressDialog = new ProgressDialog(UpdateBathroom.this);
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
            Toast.makeText(getApplicationContext(), "Bathroom Created", Toast.LENGTH_LONG).show();
            addressEditText.setText("");
            cityEditText.setText("");
            stateEditText.setText("");
            nameEditText.setText("");
            Intent i = new Intent(UpdateBathroom.this, RateBathroom.class);
            startActivity(i);

        }
    }

}
