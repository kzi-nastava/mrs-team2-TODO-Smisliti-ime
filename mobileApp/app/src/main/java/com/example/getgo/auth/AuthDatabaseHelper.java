package com.example.getgo.auth;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AuthDatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "getgo_auth.db";
    private static final int DB_VERSION = 1;

    public AuthDatabaseHelper(Context ctx) {
        super(ctx, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // users table: email unique, password plaintext for now (TODO: hash), role text, active int (0/1)
        db.execSQL("CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "email TEXT UNIQUE," +
                "password TEXT," +
                "firstName TEXT," +
                "lastName TEXT," +
                "address TEXT," +
                "phone TEXT," +
                "role TEXT," +
                "active INTEGER DEFAULT 0," +
                "avatarUri TEXT" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // simple strategy
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }
}

