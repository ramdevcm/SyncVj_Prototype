package com.example.syncvj;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String CREATE_TABLE = "create table "+ DBsync.TABLE_NAME+"( "+ DBsync.NAME+" text,"+ DBsync.POST+" text,"+ DBsync.NUMBER+" integer primary key, "+ DBsync.EMAIL+" text,"+ DBsync.DEPARTMENT+" text , "+ DBsync.SYNC_STATUS+" integer);";
    private static final String DROP_TABLE = "drop table if exists "+ DBsync.TABLE_NAME;

    public DBHelper(Context context){
        super(context, DBsync.DATABASE_NAME,null,DATABASE_VERSION);

    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {

        db.execSQL(DROP_TABLE);
        onCreate(db);
    }

    public void saveToLocalDatabase(String name, String post, Long number, String email, String department, int sync_status, SQLiteDatabase database)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBsync.NAME,name);
        contentValues.put(DBsync.POST,post);
        contentValues.put(DBsync.NUMBER,number);
        contentValues.put(DBsync.EMAIL,email);
        contentValues.put(DBsync.DEPARTMENT,department);
        contentValues.put(DBsync.SYNC_STATUS,sync_status);
        database.insert(DBsync.TABLE_NAME,null,contentValues);
    }

    public Cursor readFromLocalDatabase(SQLiteDatabase database,String department_select){
        String[] projection = {DBsync.NAME,DBsync.POST, DBsync.NUMBER,DBsync.EMAIL,DBsync.DEPARTMENT,DBsync.SYNC_STATUS};
        String whereClause = DBsync.DEPARTMENT+"=?";
        String whereArgs[] = {department_select};
        return (database.query(DBsync.TABLE_NAME,projection,whereClause,whereArgs,null,null,DBsync.NAME));
    }

    public void updateoneLocalDatabase(String nameold, Long numberold, String namenew, String postnew, Long numbernew, String emailnew, String departmentnew, SQLiteDatabase database){
        ContentValues contentValues = new ContentValues();
        String whereClause = DBsync.NUMBER+"=?"+" AND "+DBsync.NAME+"=?";
        String whereArgs[] = {numberold.toString(),nameold};
        database.delete(DBsync.TABLE_NAME, whereClause, whereArgs);
        contentValues.put(DBsync.NAME,namenew);
        contentValues.put(DBsync.POST,postnew);
        contentValues.put(DBsync.NUMBER,numbernew);
        contentValues.put(DBsync.EMAIL,emailnew);
        contentValues.put(DBsync.DEPARTMENT,departmentnew);
        database.insert(DBsync.TABLE_NAME,null,contentValues);

    }
    public void deleteoneLocalDatabase(String nameold, Long numberold, SQLiteDatabase database){
        ContentValues contentValues = new ContentValues();
        String whereClause = DBsync.NUMBER+"=?"+" AND "+DBsync.NAME+"=?";
        String whereArgs[] = {numberold.toString(),nameold};
        database.delete(DBsync.TABLE_NAME, whereClause, whereArgs);

    }
}
