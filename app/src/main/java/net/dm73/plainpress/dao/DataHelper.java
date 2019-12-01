package net.dm73.plainpress.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DataHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "witness.db";
    public static final String PREFERENCE_TABLE = "preference";
    public static String PREFERENCE_NAME = "preference_name";
    public static String PREFERENCE_VALUE = "preference_value";
    private static int databaseVersion = 1;

    public DataHelper(Context context){
        super(context, DATABASE_NAME, null, databaseVersion);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        String sql_create_prefrence = String.format("create table %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT, %s INTEGER)", PREFERENCE_TABLE, "id", PREFERENCE_NAME, PREFERENCE_VALUE);
        db.execSQL(sql_create_prefrence);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
