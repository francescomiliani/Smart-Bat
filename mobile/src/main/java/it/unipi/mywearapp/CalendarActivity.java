package it.unipi.mywearapp;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

/**
 * This class simply shows a Calendar View & return back to the List Activity the selected Data
 */
public class CalendarActivity extends Activity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    Button b;
    CalendarView myCal;
    String selectedDate = "";
    int selectedItem = -1;
    private Spinner spinner;
    private CharSequence[] spinnerItems;
    private String NO_DATE_SELECTED_MSG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        init();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        System.out.println("CHART ACTIVITY: Selected SPINNER item: " + pos + " id: " + id );
        switch( pos ) {
            case SwingPeriod.DAY://Day
                selectedItem = SwingPeriod.DAY;

                break;
            case SwingPeriod.MONTH://Day
                selectedItem = SwingPeriod.MONTH;
                break;
            case SwingPeriod.YEAR://Day
                selectedItem = SwingPeriod.YEAR;
                break;

            default:
                System.err.println("CHART ACTIVITY: error in pos of the SPINNER \n");

        }
        Toast.makeText( getApplicationContext(), "SELECTED  ITEM:  " + selectedItem, Toast.LENGTH_SHORT );
    }

    @Override //Called when there is no selected element (e.g. the list becomes empty)
    public void onNothingSelected(AdapterView<?> arg0) {
        System.out.println("CHART ACTIVITY:  Nothing selected in the spinner\n"); }

    @Override
    public void onClick( View v ) {
        if( selectedDate.equals( "" ) ) {
            Toast.makeText( getApplicationContext(), NO_DATE_SELECTED_MSG, Toast.LENGTH_SHORT).show();
            return;//Not selected
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra( GlobalInfoContainer.DATA_KEY, selectedDate );
        resultIntent.putExtra( GlobalInfoContainer.PERIOD_KEY, selectedItem );
        setResult( Activity.RESULT_OK, resultIntent );

        finish();//Terminate the activity & come back to the list
    }

    //Set all the parameter, listeners & graphic elements
    private void init() {
        b = findViewById( R.id.buttonDateCorfirm );
        b.setOnClickListener( this );

        selectedItem = -1;

        spinner = findViewById(R.id.spinnerCalendar);
        spinnerItems = getResources().getStringArray(R.array.period_array);
        ArrayAdapter<CharSequence> ad = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, spinnerItems);
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(ad);
        spinner.setOnItemSelectedListener(this);

        myCal = findViewById( R.id.calendar );
        myCal.setOnDateChangeListener( new CalendarView.OnDateChangeListener() {

            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {//The month starts from 0
                String monthStr = ""+(month+1), dayStr = ""+ dayOfMonth;//Conversion to SQL DATE
                if( (month +1)< 10 ) monthStr = "0" + monthStr;
                if( dayOfMonth < 10 ) dayStr = "0" + dayStr;

                selectedDate = Integer.toString( year ) + "-" + monthStr + "-" + dayStr;
            }
        });

        NO_DATE_SELECTED_MSG = getString( R.string.no_date_selected );
    }
    public static String getCurrentDate () {
        int month, day;

        Calendar c = Calendar.getInstance();
        c.setTime( new Date() );

        month = c.get(Calendar.MONTH)+1;
        String monthStr = "" + month;
        if( month < 10 ) monthStr = "0"+monthStr;

        day = c.get( Calendar.DAY_OF_MONTH );
        String dayStr = "" + day;
        if( day < 10 ) dayStr = "0"+ dayStr;
        return new String ( c.get( Calendar.YEAR) + "-" + monthStr + "-" + dayStr );
    }

    public static String getCurrentMonth() {
        int month;
        Calendar c = Calendar.getInstance();
        c.setTime( new Date() );

        month = c.get(Calendar.MONTH)+1;
        String monthStr = "" + month;
        if( month < 10 ) monthStr = "0"+monthStr;

        return new String ( monthStr);
    }

    public static String getCurrentYear() {
        Calendar c = Calendar.getInstance();
        c.setTime( new Date() );

        return new String ( String.valueOf( c.get( Calendar.YEAR) ));
    }
}