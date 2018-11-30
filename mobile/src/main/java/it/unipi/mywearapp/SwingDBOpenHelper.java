package it.unipi.mywearapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * This class extends the SQLiteOpenHelper, implements the creation of the db
 */
public class SwingDBOpenHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "swing.db";
    private static final int VERSION = 2;

    private static final String DB_CREATE_SWING_TABLE = "CREATE TABLE " + Swings.TABLE + "("
                                                    + Swings._ID + " INTEGER PRIMARY KEY, "
                                                    + Swings.STRENGTH + " FLOAT, "
                                                    + Swings.ANGLE + " FLOAT, "
                                                    + Swings.DURATION + " FLOAT, "
                                                    + Swings.DATE + " TEXT "
                                                    + ")";

    //Date is in this format: YYYY-MM-DD

    public SwingDBOpenHelper( Context context ) {
        super( context, DB_NAME, null, VERSION );
    }

    @Override
    public void onCreate( SQLiteDatabase db ) {
        db.execSQL( DB_CREATE_SWING_TABLE );
    }

    @Override
    // code for updating the database structure
    public void onUpgrade( SQLiteDatabase db, int oldVers, int newVers ) {
        // Here, for simplicity, just drop and recreate
        db.execSQL("DROP TABLE IF EXISTS " + Swings.TABLE );
        onCreate( db );
    }
}
