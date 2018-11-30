package it.unipi.mywearapp;

import it.unipi.mywearapp.DBManagerService.OperationType;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * This class is the first that the application show on the first appearence. Show the best values or the average ones.
 * Make use of an AsynTask for to get the swing & update the UI. The task uses the service
 */
public class MainActivity extends Activity {
    final String USER_PREFERENCES_FILE = GlobalInfoContainer.USER_PREFERENCES_FILE;
    public static DBManagerService dbService;
    private boolean connected;

    final static int REQUEST_CODE = 1;
    private Intent intent;

    public static TextView tvMainTitle, tvSwingCounter, tvStrengthValue, tvAngleValue, tvDurationValue;
    SharedPreferences pref;
    public static Swing layoutSwing;
    public static String STRENGTH_DEFAULT_VALUE, ANGLE_DEFAULT_VALUE, DURATION_DEFAULT_VALUE, BEST_TITLE, AVG_TITLE;
    String BAT_MEASURE_KEY;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pref = getApplicationContext().getSharedPreferences(USER_PREFERENCES_FILE, MODE_PRIVATE);

        init();
    }

    //Connection to the service
    @Override
    protected void onStart() {
        super.onStart();
        //START THE SERVICE
        Intent servInt = new Intent(this, DBManagerService.class);
        bindService(servInt, conn, Context.BIND_AUTO_CREATE);
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
        public void onServiceConnected(ComponentName name, IBinder service) {
            DBManagerService.DBBinder binder = (DBManagerService.DBBinder) service;
            dbService = binder.getService();
            connected = true;

            updateTextViews();

            //dbService.testInsertion();//JUST FOR TESTING
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            connected = false;
        }
    };

    @Override
    // The activity has become visible (it is now "resumed").
    protected void onResume() {
        super.onResume();

        setMainTitle();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.my_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { // Handle item selection
        switch (item.getItemId()) {

            case R.id.itemList:
                intent = new Intent(this, ListActivity.class);
                startActivity(intent);
                return true;

            case R.id.itemChart:
                intent = new Intent(this, ChartActivity.class);
                startActivity(intent);
                return true;

            case R.id.itemWatch:
                intent = new Intent(this, WatchActivity.class);
                startActivity(intent);
                return true;

            case R.id.itemSettings:
                intent = new Intent(this, SettingsActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    //Set all the parameter, listeners & graphic elements
    private void init() {

        getActionBar().setDisplayShowCustomEnabled(true);
        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setIcon(R.drawable.home_icon);

        BAT_MEASURE_KEY = getString(R.string.bat_measure_key);

        tvMainTitle = findViewById(R.id.textViewMainTitle);
        tvSwingCounter = findViewById(R.id.textViewMainSwingCounterValue);
        tvStrengthValue = findViewById(R.id.textViewSwingStrenghtValue);
        tvAngleValue = findViewById(R.id.textViewSwingAngleValue);
        tvDurationValue = findViewById(R.id.textViewSwingDurationValue);

        STRENGTH_DEFAULT_VALUE = getString(R.string.text_view_swing_strength_default_value);
        ANGLE_DEFAULT_VALUE = getString(R.string.text_view_swing_angle_default_value);
        DURATION_DEFAULT_VALUE = getString(R.string.text_view_swing_duration_default_value);
        BEST_TITLE = getString(R.string.text_view_title_best );
        AVG_TITLE = getString(R.string.text_view_title_average );
        GlobalInfoContainer.setAlertDialogTitle( getString( R.string.alert_dialog_settings_title ) );
        GlobalInfoContainer.setShowBestSwing( pref.getBoolean( BAT_MEASURE_KEY, true) );//Obtained from the SharedPreference File
        Swing defSwing = new Swing( Float.valueOf( STRENGTH_DEFAULT_VALUE ), Float.valueOf( ANGLE_DEFAULT_VALUE), Double.valueOf( DURATION_DEFAULT_VALUE) );
        GlobalInfoContainer.setBestSwing( defSwing );
        GlobalInfoContainer.setDefaultSwing( defSwing );
        setMainTitle();
    }

    private void setMainTitle() {
        System.out.println("*************** SET MAIN TITLE:  " + ((GlobalInfoContainer.isShowBestSwing()) ? "true" : "false"));
        if (GlobalInfoContainer.isShowBestSwing() == true)
            tvMainTitle.setText( BEST_TITLE );
        else
            tvMainTitle.setText( AVG_TITLE );
    }

    private void updateTextViews() {

        new SwingLoaderTask().execute( OperationType.getStringValue( OperationType.SELECT_COUNT ) );

        if( GlobalInfoContainer.isShowBestSwing() )
            new SwingLoaderTask().execute( OperationType.getStringValue( OperationType.SELECT_BEST ) );
        else
            new SwingLoaderTask().execute( OperationType.getStringValue( OperationType.SELECT_AVG ) );
    }

    /*****************************  ASYNC TASK  **************************************************************************************/
    class SwingLoaderTask extends AsyncTask<String, Long, Swing> {//Param, progress, Result
        private DBManagerService dbService;
        private Swing swing;
        private OperationType operationType;
        private int count;

        /**
         * STRING 0: ACTIVITY NAME
         * STRING 1: OPERATION TYPE
         * STRING 2: DATE
         * STRING 3: MONTH
         * STRING 4: YEAR
         * STRING 5: ID
         * According to the isClearButton of operation, not all parameters will be used
         *
         * @return
         */
        @Override
        protected Swing doInBackground(String... strings) {
            dbService = MainActivity.dbService;
            operationType = OperationType.getOperationType( strings[0] );

            if ( operationType == OperationType.SELECT_AVG)
                swing = dbService.selectAVGSwings();
            else if ( operationType == OperationType.SELECT_BEST) {
                swing = dbService.selectBestSwing();
                if( swing != null)  GlobalInfoContainer.setBestSwing( swing );
            }
            else if ( operationType == OperationType.SELECT_COUNT) count = dbService.selectCountSwings();

            return swing;
        }

        @Override
        protected void onPostExecute( Swing swing ) {

            if( operationType == OperationType.SELECT_COUNT  ) {
                if( getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT )
                    tvSwingCounter.setText( ""+count );
            } else if ( operationType == OperationType.SELECT_BEST || operationType == OperationType.SELECT_AVG ) {
                layoutSwing = swing;

                if (layoutSwing == null) {
                    tvStrengthValue.setText( STRENGTH_DEFAULT_VALUE + "  N" );
                    tvAngleValue.setText( ANGLE_DEFAULT_VALUE  + "  °" );
                    tvDurationValue.setText( DURATION_DEFAULT_VALUE + "  ms");
                } else {
                    tvStrengthValue.setText( String.format("%.2f", layoutSwing.getStrength()) + "  N" );
                    tvAngleValue.setText( String.format("%.2f", layoutSwing.getAngle()) + "  °" );
                    tvDurationValue.setText( (int) layoutSwing.getDuration() + "  ms" );
                }
            }
        }

    }
}
