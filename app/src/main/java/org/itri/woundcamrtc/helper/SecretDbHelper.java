package org.itri.woundcamrtc.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;


import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;
import net.sqlcipher.database.SQLiteQueryBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SecretDbHelper extends SQLiteOpenHelper {
    private static SecretDbHelper instance;
    public final static int NEW_VERSION = 4; // r425(2020/9/13)=3, r217(2020/6/8)=2
    public static int OLD_VERSION = NEW_VERSION;
    public static final String DATABASE_NAME = "FeedReader.db";
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static final String TEXT_TYPE = " TEXT";



    public SecretDbHelper(Context context) {
        super(context, DATABASE_NAME, null, OLD_VERSION);
    }

    static public synchronized SecretDbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SecretDbHelper(context);
        }
        return instance;
    }
    public boolean upgradeDatabase(SQLiteDatabase db) {
        boolean ret = false;
        db.beginTransaction();
        try {
            dropTable(db);
            onCreate(db);
            db.setVersion(NEW_VERSION);
            db.setTransactionSuccessful();
            ret = true;
        } catch (Exception ex) {
            throw new RuntimeException("Database upgrade failed! " + ex.getLocalizedMessage());
        } finally {
            db.endTransaction();
        }
        return ret;
    }

    public void onCreate(SQLiteDatabase db) {
        String tableName = "ConfigData";
        //"alter table Book add column name text"
        //insert into “B表表名”(_id,“B表列名1”,“B表列名2”) select null,“A表列名1”,“A表列名2” from “A表表名”
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + "( " +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "_GROUP VARCHAR(16) NOT NULL, " +
                "_NAME VARCHAR(16) NOT NULL, " +
                "_MODIFIEDTIME DEFAULT CURRENT_TIMESTAMP, " +
                "_VALUE TEXT" +
                ");";
        db.execSQL(sql);

        sql = "CREATE INDEX IF NOT EXISTS " + tableName + "_IDX ON " + tableName + "(_GROUP,_NAME);";
        db.execSQL(sql);


        sql = "CREATE TABLE IF NOT EXISTS table_measure (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "caseId varchar(32)," +
                "barcodeType varchar(6)," +
                "nurseId INTEGER," +
                "ownerId INTEGER," +
                "evalDate LONG," +
                "valueType TEXT," +
                "value TEXT," +
                "uploadId REAL" +
                ");";
        db.execSQL(sql);


        sql = "CREATE TABLE IF NOT EXISTS loginInfo (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "username varchar(32)," +
                "account varchar(32)," +
                "password varchar(32)," +
                "period INTEGER," +
                "userid INTEGER," +
                "roleid INTEGER," +
                "evalDate varchar(30)" +

                ");";
        db.execSQL(sql);

        sql = "CREATE TABLE IF NOT EXISTS loginInfoHistory (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "username varchar(32)," +
                "account varchar(32)," +
                "password varchar(32)," +
                "period INTEGER," +
                "userid INTEGER," +
                "roleid INTEGER," +
                "evalDate varchar(30)" +

                ");";
        db.execSQL(sql);


        sql = "CREATE TABLE IF NOT EXISTS table_picNumberHistory (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "height varchar(20)," +
                "width varchar(20)," +
                "area varchar(20)," +
                "depth varchar(20)," +
                "epithelium varchar(20)," +
                "granular varchar(20)," +
                "slough varchar(20)," +
                "eschar varchar(20)," +
                "fever varchar(5)," +
                "smell varchar(5)," +
                "level varchar(10)," +
                "createdate varchar(20)," +
                "character varchar(10)," +
                "overtime varchar(5)," +
                "firstOcurred varchar(5)," +
                "analysisTime varchar(30)," +
                "lastanalysis varchar(5)," +
                "calibrationColor varchar(5)," +
                "total varchar(60)" +
                ");";
        db.execSQL(sql);

        sql = "CREATE TABLE IF NOT EXISTS table_picNumber (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "evid varchar(30)," +
                "info TEXT," +
                "date varchar(20)," +
                "createdate varchar(20)," +
                "ownerId varchar(20)," +
                "part varchar(8)," +
                "number INTEGER," +
                "type varchar(10)," +
                "userid INTEGER," +
                "heightPixel varchar(20)," +
                "widthPixel varchar(20)," +
                "distance varchar(20) ," +
                "blueArea varchar(20) ," +
                "height varchar(20)," +
                "width varchar(20)," +
                "area varchar(20)," +
                "depth varchar(20)," +
                "epithelium varchar(20)," +
                "granular varchar(20)," +
                "slough varchar(20)," +
                "eschar varchar(20)," +
                "fever varchar(5)," +
                "smell varchar(5)," +
                "level varchar(10)," +
                "character varchar(10)," +
                "overtime varchar(5)," +
                "firstOcurred varchar(5)," +
                "analysisTime varchar(30)," +
                "lastanalysis varchar(5)," +
                "calibrationColor varchar(5)," +
                "total varchar(60)" +
                ");";
        db.execSQL(sql);
        sql = "CREATE TABLE table_nurse (" +
                "_id	INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                "nurseId	INTEGER," +
                "nurseName	TEXT," +
                "babyNurse	TEXT," +
                "momNurse	TEXT" +
                ")";
        db.execSQL(sql);

        sql = "CREATE INDEX IF NOT EXISTS table_nurse_IDX ON table_nurse(nurseId);";
        db.execSQL(sql);


        sql = "CREATE TABLE table_owner (" +
                "_id	INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                "ownerId	INTEGER," +
                "ownerName	TEXT," +
                "roomName	TEXT," +
                "roleName	TEXT" +
                ")";
        db.execSQL(sql);

        sql = "CREATE INDEX IF NOT EXISTS table_owner_IDX ON table_owner(ownerId);";
        db.execSQL(sql);
    }
    private void dropTable(SQLiteDatabase db) {
        String sql = "";
        try {
            sql = "DROP TABLE IF EXISTS table_measure ";
            db.execSQL(sql);
        } catch (Exception ex) {
        }
        try {
            sql = "DROP INDEX IF EXISTS table_measure_IDX ";
            db.execSQL(sql);
        } catch (Exception ex) {
        }
        try {
            sql = "DROP TABLE IF EXISTS table_nurse ";
            db.execSQL(sql);
        } catch (Exception ex) {
        }
        try {
            sql = "DROP INDEX IF EXISTS table_nurse_IDX ";
            db.execSQL(sql);
        } catch (Exception ex) {
        }
        try {
            sql = "DROP TABLE IF EXISTS table_owner ";
            db.execSQL(sql);
        } catch (Exception ex) {
        }
        try {
            sql = "DROP INDEX IF EXISTS table_owner_IDX ";
            db.execSQL(sql);
        } catch (Exception ex) {
        }
        try {
            sql = "DROP INDEX IF EXISTS table_picNumber ";
            db.execSQL(sql);
        } catch (Exception ex) {
        }
    }
    public long deleteRaw(SQLiteDatabase db,String tableName, String selection, String[] args) {

        long delete = db.delete(tableName, selection, args);
        return delete;
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public long add(SQLiteDatabase db,String tableName, ContentValues values, String selection, String[] args) {

        long nRowsEffected = 0;
        if (nRowsEffected == 0) {
            nRowsEffected = db.insert(tableName, null, values);
        }
        return nRowsEffected;
    }

    public long addOrUpdateRaw(SQLiteDatabase db,String tableName, ContentValues values, String selection, String[] args) {

        long nRowsEffected = db.update(tableName, values, selection, args);
        if (nRowsEffected == 0) {
            nRowsEffected = db.insert(tableName, null, values);
        }
        return nRowsEffected;
    }
    //  根據ownerid查詢紀錄
    public List<Map<String, Object>> querytabpicture(SQLiteDatabase db,String table, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();

        Cursor cursor = sqlBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        if (cursor.moveToFirst()) {
            String[] coloms = cursor.getColumnNames();//[_id,name]
            do {
                Map<String, Object> map = new HashMap<String, Object>();
                for (int i = 0; i < coloms.length; i++) {
                    String key = coloms[i];
                    Object value = null;
                    int type = cursor.getType(i);//得到数据的类型
                    switch (type) {
                        case Cursor.FIELD_TYPE_BLOB:
                            value = cursor.getBlob(i);//根据下标i得到values
                            break;
                        case Cursor.FIELD_TYPE_FLOAT:
                            value = cursor.getFloat(i);
                            break;
                        case Cursor.FIELD_TYPE_INTEGER:
                            if (key.equals("evalDate"))
                                value = cursor.getLong(i);
                            else
                                value = cursor.getInt(i);
                            break;
                        case Cursor.FIELD_TYPE_STRING:
                            value = cursor.getString(i);
                            break;

                        default:
                            break;
                    }

                    map.put(key, value);
                }
                list.add(map);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public List<Map<String, Object>> querySQLData(SQLiteDatabase db,String table, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
        sqlBuilder.setTables(table);


        Cursor cursor = sqlBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        if (cursor.moveToFirst()) {
            String[] coloms = cursor.getColumnNames();//[_id,name]
            do {
                Map<String, Object> map = new HashMap<String, Object>();
                for (int i = 0; i < coloms.length; i++) {
                    String key = coloms[i];
                    Object value = null;
                    int type = cursor.getType(i);//得到数据的类型
                    switch (type) {
                        case Cursor.FIELD_TYPE_BLOB:
                            value = cursor.getBlob(i);//根据下标i得到values
                            break;
                        case Cursor.FIELD_TYPE_FLOAT:
                            value = cursor.getFloat(i);
                            break;
                        case Cursor.FIELD_TYPE_INTEGER:
                            if (key.equals("evalDate"))
                                value = cursor.getLong(i);
                            else
                                value = cursor.getInt(i);
                            break;
                        case Cursor.FIELD_TYPE_STRING:
                            value = cursor.getString(i);
                            break;

                        default:
                            break;
                    }

                    map.put(key, value);
                }
                list.add(map);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }


    public List<Map<String, Object>> querySQLDataLoginList(SQLiteDatabase db) {

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();


        Cursor cursor = db.rawQuery("SELECT * FROM loginInfoHistory  group by account Order by _id DESC LIMIT 5 ", new String[]{});
//        Cursor cursor = database.query(_TableName, null, "_GROUP=? and _NAME=?", new String[]{group, name}, null, null, "_NAME");

        if (cursor.moveToFirst()) {
            String[] coloms = cursor.getColumnNames();//[_id,name]
            do {
                Map<String, Object> map = new HashMap<String, Object>();
                for (int i = 0; i < coloms.length; i++) {
                    String key = coloms[i];
                    Object value = null;
                    int type = cursor.getType(i);//得到数据的类型
                    switch (type) {
                        case Cursor.FIELD_TYPE_BLOB:
                            value = cursor.getBlob(i);//根据下标i得到values
                            break;
                        case Cursor.FIELD_TYPE_FLOAT:
                            value = cursor.getFloat(i);
                            break;
                        case Cursor.FIELD_TYPE_INTEGER:
                            value = cursor.getInt(i);
                            break;
                        case Cursor.FIELD_TYPE_STRING:
                            value = cursor.getString(i);
                            break;

                        default:
                            break;
                    }
                    map.put(key, value);
                }
                list.add(map);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }



    //  根據ownerID查詢最新紀錄
    public List<Map<String, Object>> queryHistoryRecord(SQLiteDatabase db,String id) {

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();

        String date = simpleDateFormat.format(new Date());

        Cursor cursor = db.rawQuery("SELECT *  FROM table_picNumber WHERE  ownerId=? AND  date=? Order by number DESC LIMIT 1 ", new String[]{id, date});
        if (cursor.moveToFirst()) {
            String[] coloms = cursor.getColumnNames();//[_id,name]
            do {
                Map<String, Object> map = new HashMap<String, Object>();
                for (int i = 0; i < coloms.length; i++) {
                    String key = coloms[i];
                    Object value = null;
                    int type = cursor.getType(i);//得到数据的类型
                    switch (type) {
                        case Cursor.FIELD_TYPE_BLOB:
                            value = cursor.getBlob(i);//根据下标i得到values
                            break;
                        case Cursor.FIELD_TYPE_FLOAT:
                            value = cursor.getFloat(i);
                            break;
                        case Cursor.FIELD_TYPE_INTEGER:
                            if (key.equals("evalDate"))
                                value = cursor.getLong(i);
                            else
                                value = cursor.getInt(i);
                            break;
                        case Cursor.FIELD_TYPE_STRING:
                            value = cursor.getString(i);
                            break;

                        default:
                            break;
                    }

                    map.put(key, value);
                }
                list.add(map);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }
    //查詢病患編號  最新10筆紀錄
    public List<Map<String, Object>> querySQLDataList(SQLiteDatabase db) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

        String date1 = simpleDateFormat.format(new Date());
        Cursor cursor = db.rawQuery("SELECT * , max(number) FROM table_picNumber WHERE  date=? group by ownerId Order by evid DESC LIMIT 10 ", new String[]{date1});
//        Cursor cursor = database.query(_TableName, null, "_GROUP=? and _NAME=?", new String[]{group, name}, null, null, "_NAME");

        if (cursor.moveToFirst()) {
            String[] coloms = cursor.getColumnNames();//[_id,name]
            do {
                Map<String, Object> map = new HashMap<String, Object>();
                for (int i = 0; i < coloms.length; i++) {
                    String key = coloms[i];
                    Object value = null;
                    int type = cursor.getType(i);//得到数据的类型
                    switch (type) {
                        case Cursor.FIELD_TYPE_BLOB:
                            value = cursor.getBlob(i);//根据下标i得到values
                            break;
                        case Cursor.FIELD_TYPE_FLOAT:
                            value = cursor.getFloat(i);
                            break;
                        case Cursor.FIELD_TYPE_INTEGER:
                            value = cursor.getInt(i);
                            break;
                        case Cursor.FIELD_TYPE_STRING:
                            value = cursor.getString(i);
                            break;

                        default:
                            break;
                    }
                    map.put(key, value);
                }
                list.add(map);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }
    //  根據檔名查詢最新紀錄
    public List<Map<String, Object>> queryRecordByfilepath(SQLiteDatabase db,String filepath) {

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();

        String date = simpleDateFormat.format(new Date());

        Cursor cursor = db.rawQuery("SELECT *  FROM table_picNumber WHERE  total=? Order by number DESC LIMIT 1 ", new String[]{filepath});
        if (cursor.moveToFirst()) {
            String[] coloms = cursor.getColumnNames();//[_id,name]
            do {
                Map<String, Object> map = new HashMap<String, Object>();
                for (int i = 0; i < coloms.length; i++) {
                    String key = coloms[i];
                    Object value = null;
                    int type = cursor.getType(i);//得到数据的类型
                    switch (type) {
                        case Cursor.FIELD_TYPE_BLOB:
                            value = cursor.getBlob(i);//根据下标i得到values
                            break;
                        case Cursor.FIELD_TYPE_FLOAT:
                            value = cursor.getFloat(i);
                            break;
                        case Cursor.FIELD_TYPE_INTEGER:
                            if (key.equals("evalDate"))
                                value = cursor.getLong(i);
                            else
                                value = cursor.getInt(i);
                            break;
                        case Cursor.FIELD_TYPE_STRING:
                            value = cursor.getString(i);
                            break;

                        default:
                            break;
                    }

                    map.put(key, value);
                }
                list.add(map);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return list;
    }
    //  根據檔名查詢最新紀錄
    public List<Map<String, Object>> queryRecordByfilepaththm(SQLiteDatabase db,String evlId,String itemId) {

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        android.database.sqlite.SQLiteQueryBuilder sqlBuilder = new android.database.sqlite.SQLiteQueryBuilder();

        String date = simpleDateFormat.format(new Date());

        Cursor cursor = db.rawQuery("SELECT *  FROM table_picNumber WHERE  evid=? AND number=?", new String[]{evlId,itemId});
        if (cursor.moveToFirst()) {
            String[] coloms = cursor.getColumnNames();//[_id,name]
            do {
                Map<String, Object> map = new HashMap<String, Object>();
                for (int i = 0; i < coloms.length; i++) {
                    String key = coloms[i];
                    Object value = null;
                    int type = cursor.getType(i);//得到数据的类型
                    switch (type) {
                        case Cursor.FIELD_TYPE_BLOB:
                            value = cursor.getBlob(i);//根据下标i得到values
                            break;
                        case Cursor.FIELD_TYPE_FLOAT:
                            value = cursor.getFloat(i);
                            break;
                        case Cursor.FIELD_TYPE_INTEGER:
                            if (key.equals("evalDate"))
                                value = cursor.getLong(i);
                            else
                                value = cursor.getInt(i);
                            break;
                        case Cursor.FIELD_TYPE_STRING:
                            value = cursor.getString(i);
                            break;

                        default:
                            break;
                    }

                    map.put(key, value);
                }
                list.add(map);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return list;
    }

    //  根據EVID查詢最新紀錄
    public List<Map<String, Object>> queryRecordByevid(SQLiteDatabase db,String evid) {

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();

        String date = simpleDateFormat.format(new Date());

        Cursor cursor = db.rawQuery("SELECT *  FROM table_picNumber WHERE  evid=? Order by number DESC LIMIT 1 ", new String[]{evid});
        if (cursor.moveToFirst()) {
            String[] coloms = cursor.getColumnNames();//[_id,name]
            do {
                Map<String, Object> map = new HashMap<String, Object>();
                for (int i = 0; i < coloms.length; i++) {
                    String key = coloms[i];
                    Object value = null;
                    int type = cursor.getType(i);//得到数据的类型
                    switch (type) {
                        case Cursor.FIELD_TYPE_BLOB:
                            value = cursor.getBlob(i);//根据下标i得到values
                            break;
                        case Cursor.FIELD_TYPE_FLOAT:
                            value = cursor.getFloat(i);
                            break;
                        case Cursor.FIELD_TYPE_INTEGER:
                            if (key.equals("evalDate"))
                                value = cursor.getLong(i);
                            else
                                value = cursor.getInt(i);
                            break;
                        case Cursor.FIELD_TYPE_STRING:
                            value = cursor.getString(i);
                            break;

                        default:
                            break;
                    }

                    map.put(key, value);
                }
                list.add(map);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return list;
    }
}

