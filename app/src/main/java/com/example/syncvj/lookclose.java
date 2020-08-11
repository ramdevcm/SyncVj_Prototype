package com.example.syncvj;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class lookclose extends AppCompatActivity {

    int ADMIN;
    String department_select;
    Button editCurrent;
    TextView lookclose1;
    TextView lookclose2;
    TextView lookclose3;
    TextView lookclose4;
    TextView lookclose5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lookclose);
        ADMIN = getIntent().getIntExtra("ADMIN",0);
        department_select = getIntent().getStringExtra("DEPT");
        editCurrent = (Button) findViewById(R.id.editCurrent);
        if(ADMIN == 0){
            editCurrent.setVisibility(View.GONE);
        }
        final String name = getIntent().getStringExtra("Name");
        final String post = getIntent().getStringExtra("Post");
        final Long number = getIntent().getLongExtra("Number",0);
        final String email = getIntent().getStringExtra("Email");
        final String department = getIntent().getStringExtra("Department");
        lookclose1 = findViewById(R.id.lookView1);
        lookclose1.setText(name);
        lookclose2 = findViewById(R.id.lookView2);
        lookclose2.setText(post);
        lookclose3 = findViewById(R.id.lookView3);
        lookclose3.setText(String.valueOf(number));
        lookclose4 = findViewById(R.id.lookView4);
        lookclose4.setText(email);
        lookclose5 = findViewById(R.id.lookView5);
        lookclose5.setText(department);
        lookclose3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:"+number));
                startActivity(intent);
            }
        });
        lookclose4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto",email, null));
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
            }
        });
        if(ADMIN==1){
            editCurrent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
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
                            DBHelper dbHelper = new DBHelper(lookclose.this);
                            SQLiteDatabase database = dbHelper.getWritableDatabase();
                            dbHelper.deleteoneLocalDatabase(name, number, database);
                            dbHelper.close();
                            NetworkMonitor networkMonitor = new NetworkMonitor();
                            networkMonitor.deloneonline(name, number, getApplicationContext());
                            Toast.makeText(lookclose.this, "Deleted", Toast.LENGTH_SHORT).show();
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            Intent intent = new Intent(lookclose.this,showActivity.class);
                            intent.putExtra("ADMIN",ADMIN);
                            intent.putExtra("DEPT",department_select);
                            startActivity(intent);
                        }
                    });
                    update.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            DBHelper dbHelper = new DBHelper(lookclose.this);
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
                            Toast.makeText(lookclose.this, "Updated", Toast.LENGTH_SHORT).show();
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            Intent intent = new Intent(lookclose.this,showActivity.class);
                            intent.putExtra("ADMIN",ADMIN);
                            intent.putExtra("DEPT",department_select);
                            startActivity(intent);


                        }


                    });
                }
            });

        }
    }
}