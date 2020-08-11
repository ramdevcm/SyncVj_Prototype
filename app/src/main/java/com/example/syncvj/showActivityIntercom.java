package com.example.syncvj;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class showActivityIntercom extends AppCompatActivity {

    ListView listUser;
    EditText Name;
    EditText Post;
    EditText Int_comm;
    EditText Department;
    Button addIntercommbt;
    int ADMIN;
    ListAdapter_intercomm adapter;
    ArrayList<DBcontrol_intercom> arrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userview_intercomm);
        ADMIN = getIntent().getIntExtra("ADMIN",0);
        addIntercommbt = (Button) findViewById(R.id.addNewIntercomm);
        if(ADMIN == 0){
            addIntercommbt.setVisibility(View.GONE);
        }
        listUser = (ListView) findViewById(R.id.listUser_intercomm);
        arrayList = new ArrayList<DBcontrol_intercom>();
        adapter = new ListAdapter_intercomm(getApplicationContext(),R.layout.staff_view_intercomm,arrayList);
        listUser.setAdapter(adapter);
        readFromLocalStorage();
        addIntercommbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setContentView(R.layout.admin_departmentactivity_intercomm);
                Name = (EditText) findViewById(R.id.textView1_intercomm);
                Post = (EditText) findViewById(R.id.textView2_intercomm);
                Int_comm = (EditText) findViewById(R.id.textView3_intercomm);
                Department = (EditText) findViewById(R.id.textView4_intercomm);
            }
        });
        listUser.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    final String name = arrayList.get(i).getName();
                    String post = arrayList.get(i).getPost();
                    final Long int_comm = arrayList.get(i).getInt_comm();
                    String department = arrayList.get(i).getDepartment();
                    Intent intent = new Intent(showActivityIntercom.this,lookcloseIntercomm.class);
                    intent.putExtra("Name",name);
                    intent.putExtra("Post",post);
                    intent.putExtra("Int_comm",int_comm);
                    intent.putExtra("Department",department);
                    intent.putExtra("ADMIN",ADMIN);
                    startActivity(intent);



            }
        });
    }

    public void addNewDeptStaff(View view){

        String name = Name.getText().toString();
        String post = Post.getText().toString();
        String int_comm = (Int_comm.getText().toString());
        String department = Department.getText().toString();
        if(name.isEmpty() || post.isEmpty() || int_comm.isEmpty() || department.isEmpty()){
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show();

        }
        else{
            try {
                long num = Long.parseLong(int_comm);
                saveToAppServer(name,post, num,department);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Name.setText("");
                Post.setText("");
                Int_comm.setText("");
                Department.setText("");
                Intent intent = new Intent(showActivityIntercom.this,showActivityIntercom.class);
                intent.putExtra("ADMIN",ADMIN);
                startActivity(intent);

            } catch (NumberFormatException nfe) {
                System.out.println("Please enter a valid number");
            }
        }


    }






    private void readFromLocalStorage(){
        arrayList.clear();
        DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase database =  dbHelper.getReadableDatabase();

        Cursor cursor = dbHelper.readFromLocalDatabase_intercomm(database);

        while(cursor.moveToNext()){
            String name = cursor.getString(cursor.getColumnIndex(DBsync.NAME));
            String post = cursor.getString(cursor.getColumnIndex(DBsync.POST));
            Long int_comm = cursor.getLong(cursor.getColumnIndex(DBsync.INT_COMM));
            String department = cursor.getString(cursor.getColumnIndex(DBsync.DEPARTMENT));
            arrayList.add(new DBcontrol_intercom(name,post,int_comm,department));
        }


        adapter.notifyDataSetChanged();
        cursor.close();
        dbHelper.close();

    }

    private void saveToAppServer(final String name, final String post, final Long int_comm, final String department){


        if(true){
            StringRequest stringRequest = new StringRequest(Request.Method.POST, DBsync.SERVER_URL_SYNC_INTERCOM, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonObject =new JSONObject(response);
                        String Response = jsonObject.getString("response");
                        if(Response.equals("OK")){
                            saveToLocalDatabase(name,post,int_comm,department);
                        }
                        else{
                            saveToLocalDatabase(name,post,int_comm,department);
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    saveToLocalDatabase(name,post,int_comm,department);
                }
            })
            {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String,String> params = new HashMap<>();
                    params.put("Name",name);
                    params.put("Post",post);
                    params.put("Int_comm",String.valueOf(int_comm));
                    params.put("Department",department);

                    return params;
                }
            };
            MySingleton.getInstance(showActivityIntercom.this).adddtoRequestQueue(stringRequest);
            readFromLocalStorage();


        }
        else{
            saveToLocalDatabase(name,post,int_comm,department);

        }


    }

    public boolean checkNetworkConnection(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo!=null && networkInfo.isConnected());
    }

    public void saveToLocalDatabase(String name,String post,Long int_comm,String department){
        DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        dbHelper.saveToLocalDatabase_intercomm(name,post,int_comm,department,database);
        readFromLocalStorage();
        dbHelper.close();
    }

}
