package com.template.project.core.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.template.project.core.entity.User;
import com.template.project.core.utils.FieldValidator;

import java.util.ArrayList;

public class DbAdapter implements AdapterCommand {

    private static DbAdapter mDbAdapter;
    private static SQLiteDatabase mDb;
    private static Context mCtx;

    public static DbAdapter getInstance(Context ctx) {
        if (mDbAdapter == null) {
            mDbAdapter = new DbAdapter();
            DbAdapter.mCtx = ctx;
            mDb = DbHelper.getInstance(mCtx).getWritableDatabase();
        }
        return mDbAdapter;
    }

    // Close database connection
    private synchronized void closeDbConn(SQLiteDatabase db) {
        try {
            if (db != null && !db.isOpen())
                return;
            db.close();
        } catch (Exception e) {
            Log.e(DbHelper.class.getSimpleName(), "closeDbConn() ERROR closing database connection");
        }
    }

    @Override
    public void addOrUpdateUser(User user) {
        if (mDb != null && mDb.isOpen() && user != null) {
            String tableName = DbTables.TBL_USER.toString();
            ContentValues cv = new ContentValues();

            if (FieldValidator.isEmailValid(user.getEmail())) {
                cv.put(DbColumn.COL_USER_EMAIL.toString(), user.getEmail());
            }
            if (FieldValidator.isDesiredPasswdValid(user.getPassword())) {
                cv.put(DbColumn.COL_USER_PASSWORD.toString(), user.getPassword());
            }

            if (cv != null) {
                mDb.beginTransaction();
                final String whereQuery = DbColumn.COL_USER_EMAIL + "=?";
                final String[] whereArgs = new String[] { user.getEmail() };
                int rowsAffected = mDb.update(tableName, cv, whereQuery, whereArgs);
                // insert record of player if no existing record of login user in database
                if(rowsAffected == 0) {
                    mDb.insert(tableName, "", cv);
                }
                mDb.setTransactionSuccessful();
                mDb.endTransaction();
            }

            closeDbConn(mDb);
        }
    }

    @Override
    public User getUser(String email) {
        User user = null;
        if(mDb != null && mDb.isOpen()) {
            mDb.beginTransaction();
            String query = "SELECT * FROM " + DbTables.TBL_USER.toString()
                    + "WHERE " + DbColumn.COL_USER_EMAIL + " = '" + email + "'";
            Cursor c = mDb.rawQuery(query, null);
            if(c.getCount() > 0) {
                c.moveToFirst();
                user = getUserDetailsFromCursor(c);
            }
            c.close();
            mDb.setTransactionSuccessful();
            mDb.endTransaction();
            closeDbConn(mDb);
        }
        return user;
    }

    @Override
    public ArrayList<User> getUsers() {
        ArrayList<User> arrUsers = new ArrayList<>();
        if(mDb != null && mDb.isOpen()) {
            mDb.beginTransaction();
            String query = "SELECT * FROM " + DbTables.TBL_USER.toString();
            Cursor c = mDb.rawQuery(query, null);
            if(c.getCount() > 0) {
                c.moveToFirst();
                while (c.isAfterLast() == false) {
                    User user = getUserDetailsFromCursor(c);
                    arrUsers.add(user);
                    c.moveToNext();
                }
            }
            c.close();
            mDb.setTransactionSuccessful();
            mDb.endTransaction();
            closeDbConn(mDb);
        }
        return arrUsers;
    }

    @Override
    public void deleteUser(User userToDelete) {
        if(mDb != null && mDb.isOpen()) {
            mDb.beginTransaction();
            User user = this.getUser(userToDelete.getEmail());

            String deleteUserCmd = "DELETE FROM " + DbTables.TBL_USER.toString() +
                    " WHERE " + DbColumn.COL_USER_EMAIL + "='"+ user.getEmail() + "'";
            mDb.execSQL(deleteUserCmd);
            mDb.setTransactionSuccessful();
            mDb.endTransaction();
            closeDbConn(mDb);
        }
    }

    // Return User with details extracted from Cursor
    private User getUserDetailsFromCursor(Cursor c) {
        String userId = c.getString(c.getColumnIndex(DbColumn.COL_USER_ID.toString()));
        String email = c.getString(c.getColumnIndex(DbColumn.COL_USER_EMAIL.toString()));
        String password = c.getString(c.getColumnIndex(DbColumn.COL_USER_PASSWORD.toString()));
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        return user;
    }

}
