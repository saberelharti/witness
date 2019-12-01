package net.dm73.plainpress.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import static net.dm73.plainpress.dao.DataHelper.PREFERENCE_NAME;
import static net.dm73.plainpress.dao.DataHelper.PREFERENCE_TABLE;
import static net.dm73.plainpress.dao.DataHelper.PREFERENCE_VALUE;


public class DaoPreference {

    private DataHelper mDataHelper;
    private String categoryType = "category";
    private String nearbType = "nearby";
    private String starterGuide = "starter_guide";

    public DaoPreference(Context context){
        mDataHelper = new DataHelper(context);
    }


    public void saveCategoriesPreference(List<Integer> preferenceValue){

        SQLiteDatabase db = mDataHelper.getWritableDatabase();

        db.execSQL(String.format("delete from %s where %s = \"%s\"", PREFERENCE_TABLE, PREFERENCE_NAME,categoryType));

        if ( preferenceValue != null) {
            // Create a new map of value
            ContentValues values = new ContentValues();
            for(int value : preferenceValue) {
                values.put(PREFERENCE_NAME, categoryType);
                values.put(PREFERENCE_VALUE, value);

                //Insert the data
                db.insert(PREFERENCE_TABLE, null, values);
            }
        }

        db.close();
    }

    public void saveNearbyPreference(int preferenceNearby){

        SQLiteDatabase db = mDataHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(PREFERENCE_NAME, nearbType);
        values.put(PREFERENCE_VALUE, preferenceNearby);
        db.replace(PREFERENCE_TABLE, null, values);

        db.close();
    }

    public int getNearbyPreference(){

        SQLiteDatabase db = mDataHelper.getReadableDatabase();

        String query = String.format("select %s from %s where %s like \"%s\"", PREFERENCE_VALUE, PREFERENCE_TABLE, PREFERENCE_NAME, nearbType);

        Cursor cursor = db.rawQuery(query, null);
        if(cursor!=null){
            while(cursor.moveToFirst()){
                return cursor.getInt(cursor.getColumnIndex(PREFERENCE_VALUE));
            }
            return 25;
        }

        db.close();

        return 25;
    }

    public List<Integer> getCategoriesPreference(){

        SQLiteDatabase db = mDataHelper.getReadableDatabase();

        String query = String.format("select %s from %s where %s like \"%s\"", PREFERENCE_VALUE, PREFERENCE_TABLE, PREFERENCE_NAME, categoryType);
        Cursor cursor = db.rawQuery(query, null);
        List<Integer> listCategories = new ArrayList<>();
        if(cursor!=null){
            while(cursor.moveToNext()){
                listCategories.add(cursor.getColumnIndex(PREFERENCE_VALUE));
            }
        }

        db.close();

        return listCategories;

    }

    public void saveStarterGuideDisplayed(){
        SQLiteDatabase db = mDataHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(PREFERENCE_NAME, starterGuide);
        values.put(PREFERENCE_VALUE, 1);
        db.replace(PREFERENCE_TABLE, null, values);

        db.close();
    }

    public int isStarterGuideWatched(){
        SQLiteDatabase db = mDataHelper.getReadableDatabase();

        String query = String.format("select %s from %s where %s like \"%s\"", PREFERENCE_VALUE, PREFERENCE_TABLE, PREFERENCE_NAME, starterGuide);
        Cursor cursor = db.rawQuery(query, null);
        if(cursor!=null && cursor.getCount()!=0){
            cursor.moveToNext();
            db.close();
            return cursor.getInt(cursor.getColumnIndex(PREFERENCE_VALUE));
        }else{
            db.close();
            return 0;
        }
    }

}






























