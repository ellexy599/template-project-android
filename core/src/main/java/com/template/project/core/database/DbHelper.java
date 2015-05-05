package com.template.project.core.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * SqliteDatabase Helper class for sql transactions.
 */
public class DbHelper extends SQLiteOpenHelper {

    private static final String TAG = DbHelper.class.getSimpleName();

    private static final String sDatabaseName = "TemplateDb";
    private static int sDatabaseVersion = 1;

    private static DbHelper sDbHelperInstance;

    // TODO set the correct data type of each column base on model class

    // queries of tables to be created
    private final String SQL_CREATE_USER_TBL = new StringBuilder()
            .append("CREATE TABLE IF NOT EXISTS ")
            .append(DbTables.TBL_USER)
            .append("(_id INTEGER PRIMARY KEY AUTOINCREMENT,")
            .append(DbColumn.COL_USER_ID).append(",")
            .append(DbColumn.COL_USER_EMAIL).append(",")
            .append(DbColumn.COL_USER_PASSWORD)
            .append(")").toString();

    private final String SQL_DROP_USER_TBL = new StringBuilder()
            .append("DROP TABLE IF EXISTS ")
            .append(DbTables.TBL_USER).toString();

    private DbHelper(Context context) {
        super(context, sDatabaseName, null, sDatabaseVersion);
    }

    public static DbHelper init(Context context, int databaseVersion) throws NullPointerException {
        if (context == null) {
            throw new NullPointerException();
        }
        sDatabaseVersion = databaseVersion;
        sDbHelperInstance = new DbHelper(context);
        return sDbHelperInstance;
    }

    public static DbHelper getInstance(Context ctx) {
        if (sDbHelperInstance == null) {
            Log.e(DbHelper.class.getSimpleName(), "instance of null");
            return DbHelper.init(ctx, sDatabaseVersion);
        }
        return sDbHelperInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_USER_TBL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion != newVersion) {
            // migration if there's changes in schema
        }
    }

    @Override
    public synchronized SQLiteDatabase getWritableDatabase() {
        return super.getWritableDatabase();
    }

    @Override
    public synchronized SQLiteDatabase getReadableDatabase() {
        return super.getReadableDatabase();
    }

}
