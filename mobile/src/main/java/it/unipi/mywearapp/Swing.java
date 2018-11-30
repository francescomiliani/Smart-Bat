package it.unipi.mywearapp;

import java.util.Calendar;
import java.util.Date;

/**
 * This is the class container of the information sent from the wear device.
 */
public class Swing {
    private int _id;
    private float strength;
    private float angle;
    private double duration;
    private String date;//YYYY-MM-DD

    public Swing(int _id, float strength, float angle, double duration, String date ) {
        this._id = _id;
        this.strength = strength;
        this.angle = angle;
        this.duration = duration;

        if( date.equals( "" ) ) {
            this.date = CalendarActivity.getCurrentDate();
        }
        else
            this.date = date;
    }
    public Swing( float strength, float angle, double duration ) {
        this._id = 0;
        this.strength = strength;
        this.angle = angle;
        this.duration = duration;
        this.date = CalendarActivity.getCurrentDate();
    }

    public Swing( Swing s ) {
        this._id = s.get_id();
        this.strength = strength;
        this.angle = angle;
        this.duration = duration;
        this.date = date;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public float getStrength() {
        return strength;
    }

    public void setStrength(float strength) {
        this.strength = strength;
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return  "# " + _id +
                ", Strength: " + strength + " N" +
                ", Angle: " + angle + " °" +
                ", Duration: " + duration + " ms" +
                ", Date: '" + date + '\'';
    }
}
