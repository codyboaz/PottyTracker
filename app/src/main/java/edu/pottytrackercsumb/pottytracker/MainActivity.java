package edu.pottytrackercsumb.pottytracker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
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

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button create = (Button) findViewById(R.id.createAccountBttn);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, CreateAccount.class);
                startActivity(i);
            }
        });

        Button map = (Button) findViewById(R.id.findBathroomBttn);
        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, FindBathroom.class);
                startActivity(i);
            }
        });

        Button update = (Button) findViewById(R.id.updateButton);
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, UpdateBathroom.class);
                startActivity(i);
            }
        });


        final EditText userinput = (EditText) findViewById(R.id.username);
        final EditText password = (EditText) findViewById(R.id.password);


        // Login to database
        Button login = (Button) findViewById(R.id.loginBttn);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String restURL = "http://seansabour.net/turbofinance/get_login.php?username=" + userinput.getText().toString().trim() + "&password=" + password.getText().toString().trim();
                new RestOperation().execute(restURL);

            }
        });

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    private class RestOperation extends AsyncTask<String, Void, Void> {

        final HttpClient httpClient = new DefaultHttpClient();
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
                    String user = o.getString("username");
                    String fName = o.getString("firstName");
                    String lName = o.getString("lastName");

                    Intent myIntent = new Intent(MainActivity.this, HomePage.class);
                    myIntent.putExtra("Username", user);
                    myIntent.putExtra("First Name", fName);
                    myIntent.putExtra("Last Name", lName);

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
