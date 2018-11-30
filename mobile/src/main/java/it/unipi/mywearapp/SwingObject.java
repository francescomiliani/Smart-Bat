package it.unipi.mywearapp;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class is used for object inserted into the chart
 */
public class SwingObject implements Serializable {
    private static int count = 0;
    private String timestamp;
    private int x;
    private double y;

    public SwingObject(int x, double y ) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        timestamp = dateFormat.format(new Date()); // Find todays date
        this.x = x;
        this.y = y;
    }

    public int getCount() {
        return this.count;
    }

    public static void setCount(int c) {
        count = c;
    }
    public static void resetCount() {
        setCount(0);
    }
    public SwingObject( double y ) {
        count++;
        this.x = count;
        this.y = y;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public String toString() {
        return timestamp+"_"+"x:"+x+"-y:"+y;
    }
}