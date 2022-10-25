package org.itri.woundcamrtc.helper;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import org.itri.woundcamrtc.AppResultReceiver;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBTableHelper extends SQLiteOpenHelper {

    private static final String TAG = DBTableHelper.class.getSimpleName();
    public final static int NEW_VERSION = 4; // r425(2020/9/13)=3, r217(2020/6/8)=2
    public static int OLD_VERSION = NEW_VERSION;
    private static String mDatabaseName = "";
    private static DBTableHelper mInstance = null;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

//    public DBTableHelper(Context context) {
//        super(context, DATABASE_NAME, null, OLD_VERSION);
//    }

    public DBTableHelper(Context context, String databaseName) {
        super(context, databaseName, null, OLD_VERSION);
        mDatabaseName = databaseName;
    }

//    public static DBTableHelper getInstance(Context ctx) {
//        if (mInstance == null) {
//            synchronized (DBTableHelper.class) {
//                if (mInstance == null) {
//                    mInstance = new DBTableHelper(ctx.getApplicationContext());
//                }
//            }
//        }
//        return mInstance;
//    }

    public static DBTableHelper getInstance(Context ctx, String databaseName) {
        if (mInstance == null) {
            synchronized (DBTableHelper.class) {
                if (mInstance == null) {
                    DBTableHelper.OLD_VERSION = DBTableHelper.getVersion(databaseName);
                    mInstance = new DBTableHelper(ctx.getApplicationContext(), databaseName);
                    Log.i(TAG, "getInstance()");
                }
            }
        }
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.i(TAG, "onCreate");
        createTable(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        if (oldVersion == 1) {
//            dropTable(db);
//            createTable(db);
//        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        onUpgrade(db, oldVersion, newVersion);
    }

    public boolean upgradeDatabase(SQLiteDatabase db) {
        boolean ret = false;
        db.beginTransaction();
        try {
            dropTable(db);
            createTable(db);
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

    public static int getVersion(String databaseName) {
        try {
            SQLiteDatabase db = SQLiteDatabase.openDatabase(databaseName, null, SQLiteDatabase.OPEN_READONLY);
            return db.getVersion();
        } catch (Exception ex) {
            return 1;
        }
    }

    public boolean isDBOpen(SQLiteDatabase db) {
        if ((db == null) || (!db.isOpen())) {
            return false;
        }
        return true;
    }

    private void createTable(SQLiteDatabase db) {
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
                "character varchar(10)," +
                "overtime varchar(5)," +
                "firstOcurred varchar(5)," +
                "analysisTime varchar(30)," +
                "lastanalysis varchar(5)," +
                "calibrationColor varchar(5)," +
                "createdate varchar(20)," +
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
                "calibrationColor varchar(5)," +
                "lastanalysis varchar(5)," +
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


    public long addOrUpdateRaw(String tableName, ContentValues values, String selection, String[] args) {
        SQLiteDatabase database = this.getWritableDatabase();
        long nRowsEffected = database.update(tableName, values, selection, args);
        if (nRowsEffected == 0) {
            nRowsEffected = database.insert(tableName, null, values);
        }
        return nRowsEffected;
    }

    public long add(String tableName, ContentValues values, String selection, String[] args) {
        SQLiteDatabase database = this.getWritableDatabase();
        long nRowsEffected = 0;
        if (nRowsEffected == 0) {
            nRowsEffected = database.insert(tableName, null, values);
        }
        return nRowsEffected;
    }

    public long deleteRaw(String tableName, String selection, String[] args) {
        SQLiteDatabase database = this.getWritableDatabase();
        long delete = database.delete(tableName, selection, args);
        return delete;
    }

    public int sqladd(String table, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
        sqlBuilder.setTables(table);
        return 0;
    }

    public List<Map<String, Object>> querySQLData(String table, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
        sqlBuilder.setTables(table);

        SQLiteDatabase db = this.getReadableDatabase();
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

    //
//    public long addOrUpdateRaw(String group, String name, String value) {
//        SQLiteDatabase database = this.getReadableDatabase();
//        ContentValues initialValues = new ContentValues();
//        initialValues.put("_GROUP", group.toString());
//        initialValues.put("_NAME", name.toString());
//        initialValues.put("_VALUE", value.toString());
//
//        long nRowsEffected = nRowsEffected = database.update(tableName, initialValues, "_GROUP = ? and _NAME = ?", new String[]{group, name});
//        if (nRowsEffected == 0) {
//            nRowsEffected = database.insert(tableName, null, initialValues);
//        }
//        return nRowsEffected;
//    }
//
//
//    public long deleteRaw(String group, String name) {
//        SQLiteDatabase database = this.getReadableDatabase();
//        long delete = database.delete(tableName, "_GROUP = ? and _NAME = ?", new String[]{group, name});
//        return delete;
//    }
//
//    public List<String> getColumns() {
//        SQLiteDatabase database = this.getReadableDatabase();
//        List<String> columns = null;
//        Cursor cursor = null;
//        try {
//            cursor = database.rawQuery("SELECT * FROM " + tableName + " limit 0", null);
//            if (null != cursor && cursor.getColumnCount() > 0) {
//                columns = Arrays.asList(cursor.getColumnNames());
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (cursor != null)
//                cursor.close();
//            if (null == columns)
//                columns = new ArrayList<>();
//        }
//        return columns;
//    }
//
//

    //查詢病患編號  最新10筆紀錄
    public List<Map<String, Object>> querySQLDataList() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        SQLiteDatabase database = this.getReadableDatabase();
        String date1 = simpleDateFormat.format(new Date());
        Cursor cursor = database.rawQuery("SELECT * , max(number) FROM table_picNumber WHERE  date=? group by ownerId Order by evid DESC LIMIT 10 ", new String[]{date1});
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

    public List<Map<String, Object>> queryLoginInfo() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        SQLiteDatabase database = this.getReadableDatabase();
        String date1 = simpleDateFormat.format(new Date());
        Cursor cursor = database.rawQuery("SELECT *  FROM loginInfo ", new String[]{});
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
    public List<Map<String, Object>> queryHistoryRecord(String id) {

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();

        String date = simpleDateFormat.format(new Date());
        SQLiteDatabase db = this.getReadableDatabase();
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


    //  根據ownerID查詢最新紀錄
    public List<Map<String, Object>> queryHistoryRecordBypart(String id, String part) {

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();

        String date = simpleDateFormat.format(new Date());
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT *  FROM table_picNumber WHERE  ownerId=? AND part=? AND  date=? Order by number DESC LIMIT 1 ", new String[]{id, part, date});
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
    public List<Map<String, Object>> queryRecordByfilepath(String filepath) {

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();

        String date = simpleDateFormat.format(new Date());
        SQLiteDatabase db = this.getReadableDatabase();
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
    public List<Map<String, Object>> queryRecordByfilepaththm(String evlId,String itemId) {

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();

        String date = simpleDateFormat.format(new Date());
        SQLiteDatabase db = this.getReadableDatabase();
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


    //  根據ownerid查詢紀錄
    public List<Map<String, Object>> queryHistoryRecordbyevid(String ownerId) {

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();

        String date = simpleDateFormat.format(new Date());
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT *  FROM table_picNumber WHERE  ownerId=? Group by part", new String[]{ownerId});
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
    public List<Map<String, Object>> queryRecordByevid(String evid) {

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();

        String date = simpleDateFormat.format(new Date());
        SQLiteDatabase db = this.getReadableDatabase();
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

    //查詢登入資訊  最新5筆紀錄
    public List<Map<String, Object>> querySQLDataLoginList() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        SQLiteDatabase database = this.getReadableDatabase();

        Cursor cursor = database.rawQuery("SELECT * FROM loginInfoHistory  group by account Order by _id DESC LIMIT 5 ", new String[]{});
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


    //  根據ownerid查詢紀錄
    public List<Map<String, Object>> querytabpicture(String evid, String bodypart) {

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();

        String date = simpleDateFormat.format(new Date());
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT *  FROM table_picNumber WHERE  evid=? AND part=?", new String[]{evid, bodypart});
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
