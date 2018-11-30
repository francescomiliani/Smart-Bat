package it.unipi.mywearapp;

import it.unipi.mywearapp.DBManagerService.OperationType;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.os.IBinder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * This class on base to the choice of the user, shows the correct Fragment. Each fragment loads the swing loaded before from this class.
 * Make use of an AsynTask for to get the swing & update the UI. The task uses the service
 */
public class ChartActivity extends Activity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private static int selectedChart = R.id.strengthChart;
    private Button strengthButton, angleButton, durationButton;
    private Spinner spinner;
    private CharSequence[] spinnerItems;
    String currentDate, currentDay, currentMonth, currentYear;
    public static String chartDescription, chartNoDataText, NO_SWINGS_PRESENT_MSG;
    private int itemSelected;
    public static List<Swing> swingList;
    public static DBManagerService dbService;
    private boolean connected;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_chart );

        init();//Set each listener and obtain each object

        Intent servInt = new Intent(this, DBManagerService.class);
        bindService( servInt, conn, Context.BIND_AUTO_CREATE);
    }
    //Connection to the service
    @Override
    protected void onStart() {
        super.onStart();
    }

    //Disconnection from the service
    @Override
    protected void onStop() {
        super.onStop();
        if (connected == true) {
            unbindService(conn);
            connected = false;
        }
    }

    //Nested Class Service Connection
    public ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service ) {
            DBManagerService.DBBinder binder = (DBManagerService.DBBinder) service;
            dbService = binder.getService();
            connected = true;

            if( swingList == null)
                new SwingLoaderTask().execute( OperationType.getStringValue( OperationType.SELECT_SWINGS_BY_DATE ), currentDate );
        }

        @Override
        public void onServiceDisconnected( ComponentName name ) {
            connected = false;
        }
    };

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        System.out.println("CHART ACTIVITY: Selected SPINNER item: " + pos + " id: " + id + "--- > " + SwingPeriod.getStringFromIntegerPeriod( pos ) );
        itemSelected = pos;

        if( itemSelected == SwingPeriod.DAY ) {
            new SwingLoaderTask().execute( OperationType.getStringValue( OperationType.SELECT_SWINGS_BY_DATE ), currentDate );
        } else if( itemSelected == SwingPeriod.MONTH ) {
            new SwingLoaderTask().execute( OperationType.getStringValue( OperationType.SELECT_SWINGS_BY_MONTH ), currentMonth, currentYear );
        } else if( itemSelected == SwingPeriod.YEAR ) {
            new SwingLoaderTask().execute( OperationType.getStringValue( OperationType.SELECT_SWINGS_BY_YEAR ), currentYear );
        }

        //The control of list == empty is made into the async task
    }

    @Override //Called when there is no selected element (e.g. the list becomes empty)
    public void onNothingSelected(AdapterView<?> arg0) {
        System.out.println("CHART ACTIVITY:  Nothing selected in the spinner\n");
    }

    @Override
    public void onClick( View v ) {
        if( swingList.isEmpty() == true ) {
            Toast.makeText( getApplicationContext(), getString( R.string.chart_no_data_text ), Toast.LENGTH_SHORT ).show();
            return;
        }

        switch( v.getId() ) {
            case R.id.buttonStrengthChart:
                System.out.println("CHART ACTIVITY: strength button pressed");
                selectedChart = R.id.strengthChart;
                break;

            case R.id.buttonAngleChart:
                System.out.println("CHART ACTIVITY: angle button pressed");
                selectedChart = R.id.angleChart;
                break;

            case R.id.buttonDurationChart:
                System.out.println("CHART ACTIVITY: timing button pressed");
                selectedChart = R.id.durationChart;
                break;

            default:
                System.err.println("CHART ACTIVITY: ERROR: wrong click ID");
                return;
        }//end switch

        changeFragment( selectedChart );
    }

    private void changeFragment( int selChart ){
        Fragment frag = null;

        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        switch( selChart ) {
            case R.id.strengthChart:
                System.out.println("CHART ACTIVITY: strength button pressed");
                frag = new StrengthFragment( );
                break;

            case R.id.angleChart:
                System.out.println("CHART ACTIVITY: angle button pressed");
                frag = new AngleFragment();
                break;

            case R.id.durationChart:
                System.out.println("CHART ACTIVITY: timing button pressed");
                frag = new DurationFragment();
                break;

            default:
                System.err.println("CHART ACTIVITY: ERROR: wrong click ID");
                return;
        }//end switch

        transaction.replace( R.id.fragmentPlace, frag );
        transaction.addToBackStack( null );

        // Commit the transaction
        transaction.commit();
    }

    //Set all the parameter, listeners & graphic elements
    private void init() {
        getActionBar().setDisplayHomeAsUpEnabled(true);

        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setIcon( R.drawable.chart_icon );

        spinner = (Spinner) findViewById(R.id.spinnerSwingPeriod);
        spinnerItems = getResources().getStringArray(R.array.period_array);
        ArrayAdapter<CharSequence> ad = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, spinnerItems);
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(ad);
        spinner.setOnItemSelectedListener(this);

        strengthButton = findViewById(R.id.buttonStrengthChart);
        angleButton = findViewById(R.id.buttonAngleChart);
        durationButton = findViewById(R.id.buttonDurationChart);

        strengthButton.setOnClickListener( this );
        angleButton.setOnClickListener( this );
        durationButton.setOnClickListener( this );


        currentDate = CalendarActivity.getCurrentDate();//YYYY-MM-DD
        String[] tokens = currentDate.split("-");
        currentYear = tokens[ 0 ];//YYYY
        currentMonth = tokens[ 1 ];//MM
        currentDay = tokens[ 2 ];//DD

        chartDescription = getString( R.string.chart_description);
        chartNoDataText = getString( R.string.chart_no_data_text);
        NO_SWINGS_PRESENT_MSG = getString( R.string.chart_no_data_text);
    }

    /**********************************   ASYNC TASK    ***************************************************************************/
    class SwingLoaderTask extends AsyncTask< String, Long, List<Swing> > {//Param, progress, Result
        private DBManagerService dbService;

        /**
         * STRING 0: OPERATION TYPE
         * STRING 1: DATE || MONTH || YEAR || ID
         * STRING 2: YEAR
         * According to the type of operation, not all parameters will be used
         *
         * @return
         */
        @Override
        protected List<Swing> doInBackground(String... strings) {
            dbService = ChartActivity.dbService;
            List<Swing> taskSwingList = null;
            OperationType operationType = OperationType.getOperationType( strings[ 0 ] );

            if (operationType == OperationType.SELECT_SWINGS_BY_DATE )
                taskSwingList = dbService.selectSwingsByDate(strings[1]);
            else if (operationType == OperationType.SELECT_SWINGS_BY_MONTH )
                taskSwingList = dbService.selectSwingsByMonth(Integer.valueOf( strings[ 1 ] ), Integer.valueOf( strings[ 2 ] ) );//Month & Year
            else if (operationType == OperationType.SELECT_SWINGS_BY_YEAR)
                taskSwingList = dbService.selectSwingsByYear(Integer.valueOf( strings[1] ) );//YEAR

            return taskSwingList;
        }

        @Override
        protected void onPostExecute( List<Swing> listLoaded ) {
            swingList = new ArrayList<>( listLoaded );

            if( swingList == null || swingList.isEmpty() ) {//Is empty! there are NO swings
                System.err.println("CHART ACTIVITY: " + NO_SWINGS_PRESENT_MSG );
                Toast.makeText( getApplicationContext(), NO_SWINGS_PRESENT_MSG, Toast.LENGTH_SHORT ).show();
                return;
            }

            //Ok, there at least a swing
            changeFragment( selectedChart );

        }
    }

}
