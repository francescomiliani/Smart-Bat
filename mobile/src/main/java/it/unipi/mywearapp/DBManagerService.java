package it.unipi.mywearapp;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * This class manage the DB, make the insertion, the deletion & the select of the swing.
 */
public class DBManagerService extends Service {

    public enum OperationType { SELECT_SWING_BY_ID, SELECT_SWINGS_BY_DATE, SELECT_SWINGS_BY_MONTH, SELECT_SWINGS_BY_YEAR, SELECT_ALL_SWINGS,
        SELECT_AVG, SELECT_BEST, SELECT_COUNT, DELETE_SWING_BY_ID, DELETE_SWINGS_BY_DATE, DELETE_SWINGS_BY_MONTH, DELETE_SWINGS_BY_YEAR, DELETE_ALL_SWING;

        public static OperationType getOperationType( String op ) {
            if( op.equalsIgnoreCase( "SELECT_SWING_BY_ID" ) ) return SELECT_SWING_BY_ID;
            else if( op.equalsIgnoreCase( "SELECT_SWINGS_BY_DATE" ) ) return SELECT_SWINGS_BY_DATE;
            else if( op.equalsIgnoreCase( "SELECT_SWINGS_BY_MONTH" ) ) return SELECT_SWINGS_BY_MONTH;
            else if( op.equalsIgnoreCase( "SELECT_SWINGS_BY_YEAR" ) ) return SELECT_SWINGS_BY_YEAR;
            else if( op.equalsIgnoreCase( "SELECT_ALL_SWINGS" ) ) return SELECT_ALL_SWINGS;
            else if( op.equalsIgnoreCase( "SELECT_AVG" ) ) return SELECT_AVG;
            else if( op.equalsIgnoreCase( "SELECT_BEST" ) ) return SELECT_BEST;
            else if( op.equalsIgnoreCase( "SELECT_COUNT" ) ) return SELECT_COUNT;
            else if( op.equalsIgnoreCase( "DELETE_SWING_BY_ID" ) ) return DELETE_SWING_BY_ID;
            else if( op.equalsIgnoreCase( "DELETE_SWINGS_BY_DATE" ) ) return DELETE_SWINGS_BY_DATE;
            else if( op.equalsIgnoreCase( "DELETE_SWINGS_BY_MONTH" ) ) return DELETE_SWINGS_BY_MONTH;
            else if( op.equalsIgnoreCase( "DELETE_SWINGS_BY_YEAR" ) ) return DELETE_SWINGS_BY_YEAR;
            else if( op.equalsIgnoreCase( "DELETE_ALL_SWING" ) ) return DELETE_ALL_SWING;
            else return null;
        }

        public static String getStringValue( OperationType op ) {
            if( op == SELECT_SWING_BY_ID ) return "SELECT_SWING_BY_ID";
            else if( op == SELECT_SWINGS_BY_DATE ) return "SELECT_SWINGS_BY_DATE";
            else if( op == SELECT_SWINGS_BY_MONTH ) return "SELECT_SWINGS_BY_MONTH";
            else if( op == SELECT_SWINGS_BY_YEAR ) return "SELECT_SWINGS_BY_YEAR";
            else if( op == SELECT_ALL_SWINGS) return "SELECT_ALL_SWINGS";
            else if( op == SELECT_AVG) return "SELECT_AVG";
            else if( op == SELECT_BEST ) return "SELECT_BEST";
            else if( op == SELECT_COUNT ) return "SELECT_COUNT";
            else if( op == DELETE_SWING_BY_ID ) return "DELETE_SWING_BY_ID";
            else if( op == DELETE_SWINGS_BY_DATE) return "DELETE_SWINGS_BY_DATE";
            else if( op == DELETE_SWINGS_BY_MONTH) return "DELETE_SWINGS_BY_MONTH";
            else if( op == DELETE_SWINGS_BY_YEAR ) return "DELETE_SWINGS_BY_YEAR";
            else if( op == DELETE_ALL_SWING ) return "DELETE_ALL_SWING";
            else return null;
        }
    };

    private static final String[] projection = new String[] { Swings._ID, Swings.STRENGTH, Swings.ANGLE, Swings.DURATION, Swings.DATE };
    private static final String selectionDate = Swings.DATE + " = ?", selectionReducedDate = Swings.DATE + " LIKE ?";
    private static final String SELECT_MAX_QUERY = "SELECT MAX(" + Swings.STRENGTH + ") as " + Swings.STRENGTH + ", " +
                                                        "MIN(" + Swings.ANGLE + ") as " + Swings.ANGLE + ", " +
                                                        "MIN(" + Swings.DURATION + ") as " + Swings.DURATION + " FROM " + Swings.TABLE;
    private static final String SELECT_AVG_QUERY = "SELECT AVG(" + Swings.STRENGTH + ") as " + Swings.STRENGTH + ", " +
                                                           "AVG(" + Swings.ANGLE + ") as " + Swings.ANGLE + ", " +
                                                           "AVG(" + Swings.DURATION + ") as " + Swings.DURATION + " FROM " + Swings.TABLE;

    private static final String SELECT_COUNT_SWINGS_QUERY = "SELECT COUNT(*) FROM " + Swings.TABLE;
    private static final String DELETE_QUERY = "DELETE FROM " + Swings.TABLE + " WHERE ";

    //Messages
    private static String INSERT_PROBLEM_MSG = null;

    private static int swingCounter;


    private SwingDBOpenHelper swingDBHelper  = new SwingDBOpenHelper( this );
    private SQLiteDatabase db;

    private final IBinder dbBinder = new DBBinder();

    public DBManagerService() {}

    @Override
    public IBinder onBind(Intent intent) { return dbBinder; }

    public class DBBinder extends Binder {
        DBManagerService getService(){
            return DBManagerService.this;
        }
    }

    public void insertSwingIntoDB( double strength, double angle, double duration, String date ) {
        if( INSERT_PROBLEM_MSG == null ) INSERT_PROBLEM_MSG = getString( R.string.insert_problem);
        System.out.printf("****** INSERT THE NEW SWING INTO THE DB\n");

        db = swingDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put( Swings.STRENGTH, strength );
        values.put( Swings.ANGLE, angle );
        values.put( Swings.DURATION, duration );
        values.put( Swings.DATE, date );

        long r = db.insert( Swings.TABLE, null, values );
        if(r == -1) {
            Toast.makeText(this, INSERT_PROBLEM_MSG, Toast.LENGTH_SHORT).show();
            System.err.println( "DB SERVICE MANAGER: " + INSERT_PROBLEM_MSG );
        }
    }

    public List<Swing> selectSwingsByDate( String date ) {
        System.out.println("****** \t\tDB MANAGER SERVICE: SELECT ALL THE SWINGS OF THIS DATE: " + date + "\n");
        return selectSwings( SwingPeriod.DAY, date, -1, -1 );
    }

    public List<Swing> selectSwingsByMonth( int month, int year ) {//Month has value from 1 to 12
        if( month < 1 || month > 12 )  {
            System.err.println( "DB SERVICE MANAGER: SELECT SWINGS OF THE MONTH: month wrong! <1 OR > 12\n\n" );
            return null;
        }
        System.out.println("****** \t\tDB MANAGER SERVICE: SELECT ALL THE SWINGS OF THIS MONTH: " + month + "\n");
        return selectSwings( SwingPeriod.MONTH, "", month, year );
    }

    public List<Swing> selectSwingsByYear( int year ) {
        System.out.println("****** \t\tDB MANAGER SERVICE: SELECT ALL THE SWINGS OF THIS YEAR: " + year + "\n");
        return selectSwings( SwingPeriod.YEAR, "", -1, year );
    }

    private List<Swing> selectAllSwings( ) {
        System.out.println("****** SELECTION OF ALL THE SWINGS\n");
        return selectSwings( SwingPeriod.ALL, "", -1, -1 );
    }

    /**
     * Depend from the period type, each parater can be either different from 0 or equal to 0
     * @param period
     * @param date can be ""
     * @param month can be -1
     * @param year can be -1
     * @return
     */
    //Date is in this format: YYYY-MM-DD
    private List<Swing> selectSwings( int period, String date, int month, int year ) {
        Cursor c = null;
        db = swingDBHelper.getReadableDatabase();
        String monthStr = ""+month, conditionStr = selectionReducedDate;
        String [] selectionArgs = null;
        if (month < 10) monthStr = "0" + month;//Month must be on two digits

        if( period == SwingPeriod.DAY ) {
            conditionStr = selectionDate;
            selectionArgs = new String[]{date};
        }else if( period == SwingPeriod.MONTH ) {
            selectionArgs = new String[]{ year + "-" + monthStr + "-%"};
        }else if( period == SwingPeriod.YEAR ) {
            selectionArgs = new String[]{ year + "-%" };
        }else if( period == SwingPeriod.ALL ) {
            conditionStr = null;
            selectionArgs = null;
        } else {
            System.err.println("****** \t\tDB MANAGER SERVICE: ERROR in DELETION ALL THE SWINGS OF THIS DATE, --> period is wrong!\n");
            return  null;
        }

        c = db.query(Swings.TABLE, projection, conditionStr, selectionArgs, null, null, Swings.DATE + " ASC");

        List<Swing> swingList = new ArrayList<>();
        int indexID = c.getColumnIndex( Swings._ID );
        int indexStrength = c.getColumnIndex( Swings.STRENGTH );
        int indexAngle = c.getColumnIndex( Swings.ANGLE );
        int indexDuration = c.getColumnIndex( Swings.DURATION );
        int indexDate = c.getColumnIndex( Swings.DATE );

        String swing = "", dateOfSwing = "";
        float strength, angle;
        double duration;
        int _id;

        while( c.moveToNext() ) {
            _id = c.getInt( indexID );
            strength = c.getFloat( indexStrength );
            angle = c.getFloat( indexAngle );
            duration = c.getDouble( indexDuration );
            dateOfSwing = c.getString( indexDate );

            swingList.add( new Swing(_id, strength, angle, duration, dateOfSwing ));
            swing += _id + " " + strength + " " + angle + " " + duration + " " + dateOfSwing + "\n";
        }

        c.close();

        return swingList;

    }

    //Date is in this format: YYYY-MM-DD
    private long deleteSwings( int period, String date, int month, int year, int id ) {
        System.out.println("****** \t\tDB MANAGER SERVICE: DELETION ALL THE SWINGS\n");
        String conditionStr = selectionReducedDate, monthStr = ""+month;
        String selectionArguments[] = null;
        if( month < 10 ) monthStr = "0"+monthStr;

        db = swingDBHelper.getWritableDatabase();
        if( period == SwingPeriod.DAY ) {
            conditionStr = selectionDate;
            selectionArguments = new String[]{ date };
        }
        else if( period == SwingPeriod.MONTH ) selectionArguments = new String[]{ year + "-" + monthStr + "-%"};
        else if( period == SwingPeriod.YEAR ) selectionArguments = new String[]{ year + "-%" };
        else if( period == SwingPeriod.SINGLE ) {
            conditionStr = Swings._ID + " = ?";
            selectionArguments = new String[]{ ""+id };
        } else if( period == SwingPeriod.ALL ) {
            conditionStr = null;
            selectionArguments = null;
        }
        long r =  db.delete( Swings.TABLE, conditionStr, selectionArguments );//r = # of deleted rows

        if( r == -1 )  System.err.println("\nDB MANAGER SERVICE: a problem during swings deletion is verified" );
        else System.out.println("\nDB MANAGER SERVICE: "+ r + " swings deleted successfully from the DB" );

        return r;
    }

    public long deleteSwingByID( int id ) {
        System.out.println("****** DELETION ALL THE SWINGS WITH ID: " + id + "\n");
        return deleteSwings( SwingPeriod.SINGLE, "", -1, -1, id );
    }

    public long deleteSwingByDate( String date ) {
        System.out.println("****** DELETION ALL THE SWINGS OF THIS DATE: " + date + "\n");
        return deleteSwings( SwingPeriod.DAY, date, -1, -1, -1);
    }

    public long deleteSwingByMonth( int month, int year ) {
        System.out.println("****** DELETION ALL THE SWINGS OF THIS MONTH #: " + month + " of the year " + year + "\n");
        return deleteSwings( SwingPeriod.MONTH, "", month, year, -1);
    }

    public long deleteSwingByYear( int year ) {
        System.out.println("****** DELETION ALL THE SWINGS OF THIS YEAR: " + year + "\n");
        return deleteSwings( SwingPeriod.YEAR, "", -1, year, -1 );
    }

    public long deleteAllSwings(  ) {
        System.out.println("****** DELETION ALL THE SWINGS\n");
        return deleteSwings(SwingPeriod.ALL, "", -1, -1, -1);
    }

    public int selectCountSwings( ) {
        db = swingDBHelper.getReadableDatabase();
        Cursor c = db.rawQuery( SELECT_COUNT_SWINGS_QUERY, null);
        c.moveToFirst();
        int count = c.getInt(0);
        c.close();

        swingCounter = count;
        return count;
    }

    private Swing selectAggregationFunction( OperationType operation ) {
        if(  swingCounter == 0) return null;//There is no swing!

        String swing = "", date = "";
        float strength = 0, angle = 0;
        double duration = 0;
        int _id, indexStrength, indexAngle, indexDuration, indexDate;
        Cursor c = null;

        db = swingDBHelper.getReadableDatabase();

        if( operation == OperationType.SELECT_BEST )
            c = db.rawQuery( SELECT_MAX_QUERY, null);
        else  // AVERAGE
            c = db.rawQuery( SELECT_AVG_QUERY, null);

        c.moveToFirst();//Positioning on the unique tupla returned

        if ((indexStrength = c.getColumnIndex(Swings.STRENGTH)) == -1) {
            System.err.println("DB SERVICE MANAGER: return wrong STRENGTH index\n\n");
            return null;
        }

        System.err.println("DB SERVICE MANAGER: " + c.getString(0));
        strength = c.getFloat( indexStrength );

        if ((indexAngle = c.getColumnIndex(Swings.ANGLE)) == -1) {
            System.err.println("DB SERVICE MANAGER: return wrong ANGLE index\n\n");
            return null;
        }
        angle = c.getFloat( indexAngle );

        if ((indexDuration = c.getColumnIndex(Swings.DURATION)) == -1) {
            System.err.println("DB SERVICE MANAGER: return wrong DURATION index\n\n");
            return null;
        }
        duration = c.getDouble(indexDuration);

        c.close();

        return new Swing(0, strength, angle, duration, date );
    }

    public Swing selectBestSwing( ) {
        return selectAggregationFunction( OperationType.SELECT_BEST );
    }

    public Swing selectAVGSwings( ) {
        return selectAggregationFunction( OperationType.SELECT_AVG );
    }

    /**********************************  TESTING ******************************************************************++****************/
    public void testInsertion() {
        String dataStr = CalendarActivity.getCurrentDate();

        for(int i = 0; i < 5; i++ ) insertSwingIntoDB( Math.floor(Math.random()*100), Math.floor(Math.random()*100), Math.floor(Math.random()*1000), dataStr );

        for(int i = 0; i < 5; i++ ) insertSwingIntoDB( Math.floor(Math.random()*100), Math.floor(Math.random()*100), Math.floor(Math.random()*1000), "2018-04-03" );

        for(int i = 0; i < 5; i++ ) insertSwingIntoDB( Math.floor(Math.random()*100), Math.floor(Math.random()*100), Math.floor(Math.random()*1000), "2017-12-31" );

        for(int i = 0; i < 5; i++ ) insertSwingIntoDB( Math.floor(Math.random()*100), Math.floor(Math.random()*100), Math.floor(Math.random()*1000), "2018-03-01" );


    }

    private void testShow() {
        String dataStr = CalendarActivity.getCurrentDate();

        selectSwingsByDate( dataStr );
    }

    public void testDeletion() {
        String dataStr = CalendarActivity.getCurrentDate();

        //deleteSwingByDate( dataStr );
        deleteAllSwings();


        System.out.println("DB MANAGER SERVICE: TEST DELETION\n\tlist got by selectAllSwing\n\n\t" + selectAllSwings() );
        //selectSwingOfDay( dataStr );
    }
}
