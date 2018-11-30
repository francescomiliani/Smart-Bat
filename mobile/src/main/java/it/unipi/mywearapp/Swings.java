package it.unipi.mywearapp;

import android.provider.BaseColumns;

/**
 * This Class is used from the DB to get the name of the table & the several columns
 */
public abstract class Swings implements BaseColumns {
    public static final String TABLE = "swings";
    public static final String STRENGTH = "strength";
    public static final String ANGLE = "angle";
    public static final String DURATION = "duration";
    public static final String DATE = "s_date";
}
