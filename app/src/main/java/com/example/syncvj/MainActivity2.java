package com.example.syncvj;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity2 extends AppCompatActivity {

    Button vjecbutton, vjimbutton, buttonSync;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    int ADMIN;
    ActionBarDrawerToggle toggle;
    String JSONString;

    @Override
    public void onBackPressed() {
        Toast.makeText(MainActivity2.this, "Press again to exit", Toast.LENGTH_LONG).show();

        System.exit(1);
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        ADMIN = getIntent().getIntExtra("ADMIN", 0);

        //......................HOOKS.......................

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);


        //....................TOOL bAR.........................
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //....................NAVIGATION DRAWER MENU................
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_open, R.string.navigation_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                // Handle navigation view item clicks here.

                switch (item.getItemId()) {

                    case R.id.nav_admin:
                        Intent intent = new Intent(MainActivity2.this, Login.class);
                        startActivity(intent);
                        intent.addFlags((Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        break;
                    case R.id.nav_share:
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, "GIVE URL HERE"); //GIVE URL OF APP
                        sendIntent.setType("text/plain");
                        Intent.createChooser(sendIntent, "Share via");
                        startActivity(Intent.createChooser(sendIntent, "Share SyncVj via"));
                        break;
                    case R.id.nav_about:
                        Toast.makeText(MainActivity2.this, "abc", Toast.LENGTH_SHORT).show();
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.nav_exit:
                        System.exit(1);
                        break;
                }

                return true;
            }

        });


        //................................................................
        vjecbutton = (Button) findViewById(R.id.vjecButton);
        vjimbutton = (Button) findViewById(R.id.vjimButton);
        buttonSync = (Button) findViewById(R.id.buttonSync);


        vjecbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity2.this, VjecActivity1.class);
                intent.putExtra("ADMIN", ADMIN);
                startActivity(intent);
            }
        });

        vjimbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity2.this, VjimActivity1.class);
                intent.putExtra("ADMIN", ADMIN);
                startActivity(intent);

            }
        });

        buttonSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkNetworkConnection()) {
                    DBHelper dbHelper = new DBHelper(getApplicationContext());
                    SQLiteDatabase database = dbHelper.getReadableDatabase();
                    dbHelper.onUpgrade(database, 1, 1);
                    dbHelper.close();
                    Toast.makeText(MainActivity2.this, "Please wait...SYNCING", Toast.LENGTH_SHORT).show();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    new backgroundTask().execute();
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(MainActivity2.this, "Sync Succesful", Toast.LENGTH_SHORT).show();
                }

            }

        });

    }


    class backgroundTask extends AsyncTask<Void, Void, String> {
        String j_url;
        JSONArray jarray;
        JSONObject jsonObject;


        @Override
        protected void onPreExecute() {
            j_url = DBsync.SERVER_URL_GET;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(j_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                while ((JSONString = bufferedReader.readLine()) != null) {

                    stringBuilder.append(JSONString + "\n");
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                return stringBuilder.toString();

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                jsonObject = new JSONObject(result);
                jarray = jsonObject.getJSONArray("server_response");
                int count = 0;
                String name, post, email, department;
                Long number;
                while (count < jarray.length()) {
                    JSONObject jo = jarray.getJSONObject(count);
                    name = jo.getString("Name");
                    post = jo.getString("Post");
                    number = Long.parseLong(jo.getString("Number"));
                    email = jo.getString("Email");
                    department = jo.getString("Department");

                    saveToLocalDatabase(name, post, number, email, department, DBsync.SYNC_STATUS_OK);
                    count = count + 1;


                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    public void saveToLocalDatabase(String name,String post,Long number,String email,String department,int sync_status){
        DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        dbHelper.saveToLocalDatabase(name,post,number,email,department,sync_status,database);
        dbHelper.close();
    }

    public boolean checkNetworkConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }
}
