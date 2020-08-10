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

public class showActivity extends AppCompatActivity {

    ListView listAdmin;
    ListView listUser;
    EditText Name;
    EditText Post;
    EditText Number;
    EditText Email;
    EditText Department;
    String department_select;
    Button addDeptStaffBt;
    int ADMIN;
    ListAdapter adapter;
    ArrayList<DBcontrol> arrayList;
    String JSONString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userview);
        ADMIN = getIntent().getIntExtra("ADMIN",0);
        addDeptStaffBt = (Button) findViewById(R.id.addNewDepartmentStaff);
        if(ADMIN == 0){
            addDeptStaffBt.setVisibility(View.GONE);
        }
        department_select = getIntent().getStringExtra("DEPT");
        listUser = (ListView) findViewById(R.id.listUser);
        arrayList = new ArrayList<DBcontrol>();
        adapter = new ListAdapter(getApplicationContext(),R.layout.staff_view,arrayList);
        listUser.setAdapter(adapter);
        readFromLocalStorage();
        addDeptStaffBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setContentView(R.layout.admin_departmentactivity);
                Name = (EditText) findViewById(R.id.textView1);
                Post = (EditText) findViewById(R.id.textView2);
                Number = (EditText) findViewById(R.id.textView3);
                Email = (EditText) findViewById(R.id.textView4);
                Department = (EditText) findViewById(R.id.textView5);
            }
        });
        listUser.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (ADMIN == 1){

                final String name = arrayList.get(i).getName();
                String post = arrayList.get(i).getPost();
                final Long number = arrayList.get(i).getNumber();
                String email = arrayList.get(i).getEmail();
                String department = arrayList.get(i).getDepartment();
                setContentView(R.layout.update);
                final EditText update1 = (EditText) findViewById(R.id.updateView1);
                update1.setText(name);
                final EditText update2 = (EditText) findViewById(R.id.updatetView2);
                update2.setText(post);
                final EditText update3 = (EditText) findViewById(R.id.updateView3);
                update3.setText(String.valueOf(number));
                final EditText update4 = (EditText) findViewById(R.id.updateView4);
                update4.setText(email);
                final EditText update5 = (EditText) findViewById(R.id.updateView5);
                update5.setText(department);
                Button update = findViewById(R.id.update);
                Button delete = findViewById(R.id.delete);
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DBHelper dbHelper = new DBHelper(showActivity.this);
                        SQLiteDatabase database = dbHelper.getWritableDatabase();
                        dbHelper.deleteoneLocalDatabase(name, number, database);
                        dbHelper.close();
                        NetworkMonitor networkMonitor = new NetworkMonitor();
                        networkMonitor.deloneonline(name, number, getApplicationContext());
                        Toast.makeText(showActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(showActivity.this,showActivity.class);
                        intent.putExtra("ADMIN",ADMIN);
                        intent.putExtra("DEPT",department_select);
                        startActivity(intent);
                    }
                });
                update.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DBHelper dbHelper = new DBHelper(showActivity.this);
                        SQLiteDatabase database = dbHelper.getWritableDatabase();
                        String namenew = update1.getText().toString();
                        String postnew = update2.getText().toString();
                        Long numbernew = Long.parseLong(update3.getText().toString());
                        String emailnew = update4.getText().toString();
                        String departmentnew = update5.getText().toString();
                        dbHelper.updateoneLocalDatabase(name, number, namenew, postnew, numbernew, emailnew, departmentnew, database);
                        dbHelper.close();
                        NetworkMonitor networkMonitor = new NetworkMonitor();
                        networkMonitor.updateoneonline(name, number, namenew, postnew, numbernew, emailnew, departmentnew, getApplicationContext());
                        Toast.makeText(showActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(showActivity.this,showActivity.class);
                        intent.putExtra("ADMIN",ADMIN);
                        intent.putExtra("DEPT",department_select);
                        startActivity(intent);


                    }


                });

            }
            }
        });
    }

    public void addNewDeptStaff(View view){

        String name = Name.getText().toString();
        String post = Post.getText().toString();
        String number = (Number.getText().toString());
        String email = Email.getText().toString();
        String department = Department.getText().toString();
        if(name.isEmpty() || post.isEmpty() || number.isEmpty() || email.isEmpty() || department.isEmpty()){
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show();

        }
        else{
            try {
                long num = Long.parseLong(number);
                saveToAppServer(name,post, num,email,department);
                Name.setText("");
                Post.setText("");
                Number.setText("");
                Email.setText("");
                Department.setText("");
                Intent intent = new Intent(showActivity.this,showActivity.class);
                intent.putExtra("ADMIN",ADMIN);
                intent.putExtra("DEPT",department_select);
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

        Cursor cursor = dbHelper.readFromLocalDatabase(database,department_select);

        while(cursor.moveToNext()){
            String name = cursor.getString(cursor.getColumnIndex(DBsync.NAME));
            String post = cursor.getString(cursor.getColumnIndex(DBsync.POST));
            Long number = cursor.getLong(cursor.getColumnIndex(DBsync.NUMBER));
            String email = cursor.getString(cursor.getColumnIndex(DBsync.EMAIL));
            String department = cursor.getString(cursor.getColumnIndex(DBsync.DEPARTMENT));
            int sync_status = cursor.getInt(cursor.getColumnIndex(DBsync.SYNC_STATUS));
            arrayList.add(new DBcontrol(name,post,number,email,department,sync_status));
        }


        adapter.notifyDataSetChanged();
        cursor.close();
        dbHelper.close();

    }

    private void saveToAppServer(final String name, final String post, final Long number, final String email, final String department){


        if(true){
            StringRequest stringRequest = new StringRequest(Request.Method.POST, DBsync.SERVER_URL_SYNC, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonObject =new JSONObject(response);
                        String Response = jsonObject.getString("response");
                        if(Response.equals("OK")){
                            saveToLocalDatabase(name,post,number,email,department,DBsync.SYNC_STATUS_OK);
                        }
                        else{
                            saveToLocalDatabase(name,post,number,email,department,DBsync.SYNC_STATUS_FAILED);
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    saveToLocalDatabase(name,post,number,email,department,DBsync.SYNC_STATUS_FAILED);
                }
            })
            {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String,String> params = new HashMap<>();
                    params.put("Name",name);
                    params.put("Post",post);
                    params.put("Number",String.valueOf(number));
                    params.put("Email",email);
                    params.put("Department",department);

                    return params;
                }
            };
            MySingleton.getInstance(showActivity.this).adddtoRequestQueue(stringRequest);
            readFromLocalStorage();


        }
        else{
            saveToLocalDatabase(name,post,number,email,department,DBsync.SYNC_STATUS_FAILED);

        }


    }

    public boolean checkNetworkConnection(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo!=null && networkInfo.isConnected());
    }

    public void saveToLocalDatabase(String name,String post,Long number,String email,String department,int sync_status){
        DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        dbHelper.saveToLocalDatabase(name,post,number,email,department,sync_status,database);
        readFromLocalStorage();
        dbHelper.close();
    }

    public void syncStaff(View view){
        //NetworkMonitor networkMonitor = new NetworkMonitor();
        //networkMonitor.syncAll(getApplicationContext());
    }
}
