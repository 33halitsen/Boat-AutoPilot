package com.example.myapplication.Helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class RoutaDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "routa_archiv.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "routas";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "routa_name";
    private static final String COLUMN_MARKERS = "markers";

    private final Context context;

    public RoutaDbHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_NAME + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY, "
                + COLUMN_TITLE + " TEXT, "
                + COLUMN_MARKERS + " TEXT);";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
    public RoutaClass getRouteById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[]{COLUMN_ID, COLUMN_TITLE, COLUMN_MARKERS},
                COLUMN_ID + "=?", new String[]{String.valueOf(id)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String routeName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
            String markersJson = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MARKERS));
            List<MarkerOptions> markers = JsonHelper.convertToMarkerOptions(markersJson);
            cursor.close();
            return new RoutaClass(routeName, markers);
        } else {
            if (cursor != null) cursor.close();
            return null;
        }
    }
    public int getLastItemId() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_ID + " FROM " + TABLE_NAME + " ORDER BY " + COLUMN_ID + " DESC LIMIT 1";
        Cursor cursor = db.rawQuery(query, null);

        int lastId = -1;
        if (cursor != null && cursor.moveToFirst()) {
            lastId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
            cursor.close();
        }

        return lastId;
    }
    public void addRoute(List<MarkerOptions> markers, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        String markersJson = JsonHelper.convertToJSON(markers);

        contentValues.put(COLUMN_TITLE, name);
        contentValues.put(COLUMN_MARKERS, markersJson);
        contentValues.put(COLUMN_ID, getLastItemId() + 1);

        long result = db.insert(TABLE_NAME, null, contentValues);
        Toast.makeText(context, result != -1 ? "Added Successfully!" : "Failed", Toast.LENGTH_SHORT).show();
    }
    public void updateRoute(List<MarkerOptions> markers, String name, int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        String markersJson = JsonHelper.convertToJSON(markers);

        contentValues.put(COLUMN_TITLE, name);
        contentValues.put(COLUMN_MARKERS, markersJson);
        long result = db.update(TABLE_NAME, contentValues, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        Toast.makeText(context, result != -1 ? "Updated Successfully!" : "Failed to Update", Toast.LENGTH_SHORT).show();
    }

    public boolean deleteRoute(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            long result = db.delete(TABLE_NAME, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
            if (result == -1) {
                Toast.makeText(context,"Failed to Delete", Toast.LENGTH_SHORT).show();
                return false;
            }
            decrementIdsAfter(id, db);
            db.setTransactionSuccessful();
            Toast.makeText(context,"Successfully Deleted", Toast.LENGTH_SHORT).show();
            return true;
        } catch (Exception e) {
            Toast.makeText(context,"Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        } finally {
            db.endTransaction();
        }
    }

    private void decrementIdsAfter(int id, SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_ID + " FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + " > " + id, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int currentId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                ContentValues contentValues = new ContentValues();
                contentValues.put(COLUMN_ID, currentId - 1);
                db.update(TABLE_NAME, contentValues, COLUMN_ID + "=?", new String[]{String.valueOf(currentId)});
            }
            cursor.close();
        }
    }
}
