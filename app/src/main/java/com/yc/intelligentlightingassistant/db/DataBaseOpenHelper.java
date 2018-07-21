package com.yc.intelligentlightingassistant.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库帮助类
 */
public class DataBaseOpenHelper extends SQLiteOpenHelper {

    //数据库名称
    private static final String DB_NAME = "device_info.db";
    //数据库版本
    private static final int VERSION = 2;
    //建表语句
    private static final String CREATE_TABLE = "create table device_info(id integer primary key autoincrement," +
            "ip text, frequency real, duty real, voltage real, aisle integer, name text)";
    //删除表语句
    private static final String DROP_TABLE = "drop table if exists device_info";

    private static DataBaseOpenHelper mHelper;

    private DataBaseOpenHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    public static DataBaseOpenHelper getInstance(Context context) {
        if (mHelper == null) {
            mHelper = new DataBaseOpenHelper(context);
        }
        return mHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE);
        db.execSQL(CREATE_TABLE);
    }
}
