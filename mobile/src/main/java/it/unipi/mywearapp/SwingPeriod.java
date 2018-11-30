package it.unipi.mywearapp;

/**
 * This is an utility class, used for the constants
 */
public abstract class SwingPeriod {
    public final static int DAY = 0;
    public final static int MONTH = 1;
    public final static int YEAR = 2;
    public final static int SINGLE = 3;
    public final static int ALL = 4;

    public static String getStringFromIntegerPeriod( int p ) {
        String period = "";
        if( p == DAY) period = "DAY";
        else if( p == MONTH) period = "MONTH";
        else if( p == YEAR ) period = "YEAR";
        else if( p == SINGLE ) period = "SINGLE";
        else if( p == ALL ) period = "ALL";
        else period = null;
        return period;
    }
}