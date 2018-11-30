package it.unipi.mywearapp;

import it.unipi.mywearapp.DBManagerService.OperationType;

import android.app.DialogFragment;
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
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * This class show the swings performed in current day, and or in another date.
 * It communicate with DBManagerService for interact with the DB
 * Make use of an AsynTask for to get the swing & update the UI. The task uses the service
 */
public class ListActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener, MyDialogFragment.NoticeDialogListener {
    final String DATA_KEY = GlobalInfoContainer.DATA_KEY;
    final String PERIOD_KEY = GlobalInfoContainer.PERIOD_KEY;
    public static String LIST_HEADER_TEXT, NO_SWINGS_PRESENT_MSG, ALERT_DIALOG_TITLE, ALERT_DIALOG_LIST_PERIOD, ALERT_DIALOG_LIST_ITEM, ALL_SWINGS_DELETED_TEXT,SWING_DELETED_SUCCESSFULLY_TEXT;
    public static ListView listView;
    public static TextView tvHeader;
    public static Button clearButton, dataButton;
    public static ArrayList<Swing> swingList;//LIST OF ARRAY STRINGS WHICH WILL SERVE AS LIST ITEMS
    public static ArrayAdapter<Swing> adapter;//DEFINING A STRING ADAPTER WHICH WILL HANDLE THE DATA OF THE LISTVIEW
    private String selectedData = ""; //Format YYYY-MM-DD
    private int selectedPeriod = 1, selectedItemID = -1;//Default
    public static int rowCounter = 0;
    public boolean dataIsSelected = false, isClearButton;
    public static Swing swingToBeRemove = null;
    static String[] dateTokens = null;


    public static Context context;
    static DBManagerService dbService;
    boolean connected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        init();
    }

    //Not write here the code which you want is executed on the start of the service! Write it below into Service connection
    @Override
    protected void onStart() {
        super.onStart();
        //START THE SERVICE
        Intent servInt = new Intent(this, DBManagerService.class);
        bindService( servInt, conn, Context.BIND_AUTO_CREATE);

    }

    //Disconnection from the service
    @Override
    protected void onStop() {
        super.onStop();
        if (connected == true) {
            unbindService(conn);
            connected = false;
        }

        dataIsSelected = false;
    }

    //Nested Class Service Connection
    //Connection to the service & disconnection
    public ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected( ComponentName name, IBinder service ) {
            DBManagerService.DBBinder binder = (DBManagerService.DBBinder) service;
            dbService = binder.getService();
            connected = true;
        }

        @Override
        public void onServiceDisconnected( ComponentName name ) {
            connected = false;
        }
    };

    //This method is callback when the method finish is called by CalendarActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                selectedData = data.getStringExtra( DATA_KEY );
                selectedPeriod = data.getIntExtra( PERIOD_KEY , 0);
                dataIsSelected = true;
                dateTokens = selectedData.split("-");//YYYY-MM-DD

                switch ( selectedPeriod ) {
                    case SwingPeriod.DAY:
                        new SwingLoaderTask().execute( OperationType.getStringValue( OperationType.SELECT_SWINGS_BY_DATE ), selectedData );
                        break;
                    case SwingPeriod.MONTH:
                        new SwingLoaderTask().execute( OperationType.getStringValue( OperationType.SELECT_SWINGS_BY_MONTH ), dateTokens[1], dateTokens[0] );//MONTH & YEAR
                        break;
                    case SwingPeriod.YEAR:
                        new SwingLoaderTask().execute( OperationType.getStringValue( OperationType.SELECT_SWINGS_BY_YEAR ), dateTokens[0] );//YEAR
                        break;
                }//end switch

                Toast.makeText( getApplicationContext(), "Data selected: " + selectedData + "  Period:  " + SwingPeriod.getStringFromIntegerPeriod( selectedPeriod ),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onClick( View v ) {
        if( v.getId() == R.id.buttonData ) {
            isClearButton = false;

            Intent intent = new Intent(this, CalendarActivity.class);
            startActivityForResult(intent,  1);
            return;

        } else if( v.getId() == R.id.clearListButton ) {
            emitAlertDialog( v, -1);
            isClearButton = true;
        }
    }

    //Set all the parameter, listeners & graphic elements
    private void init() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setIcon(R.drawable.list_icon);//Set the list icon near the name of the activity into the action bar

        listView = findViewById( R.id.listView );
        listView.setOnItemClickListener( this );
        tvHeader = findViewById( R.id.tvHeader );

        clearButton = findViewById( R.id.clearListButton );
        clearButton.setOnClickListener( this );
        clearButton.setVisibility( View.INVISIBLE );

        dataButton = findViewById( R.id.buttonData );
        dataButton.setOnClickListener( this );

        dataIsSelected = false;
        LIST_HEADER_TEXT = getString( R.string.text_view_list_text_above_swings);
        NO_SWINGS_PRESENT_MSG = getString( R.string.chart_no_data_text);
        ALERT_DIALOG_LIST_PERIOD = getString( R.string.alert_dialog_list_period );
        ALERT_DIALOG_LIST_ITEM = getString( R.string.alert_dialog_list_single_item );
        ALL_SWINGS_DELETED_TEXT = getString( R.string.all_swing_deleted_text );
        SWING_DELETED_SUCCESSFULLY_TEXT = getString( R.string.swing_deleted_successfully );

        context = getApplicationContext();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id ) {
        swingToBeRemove =  adapter.getItem( pos );
        int _id = swingToBeRemove.get_id();
        selectedItemID = _id;
        emitAlertDialog( view, _id );
    }

    //Use a Bundle Object to send a parameter to the Dialog Fragment
    private void emitAlertDialog( View v, final int id ) {

        Bundle args = new Bundle();
        //Set the variable for create the correct dialog fragment
        args.putBoolean( GlobalInfoContainer.IS_CLEAR_ALL_SWINGS_KEY, false );
        args.putInt( GlobalInfoContainer.SELECTED_PERIOD_KEY, selectedPeriod );
        if (v.getId() != R.id.clearListButton) {//IT'S AN ITEM OF THE LIST
            args.putBoolean( GlobalInfoContainer.IS_CLEAR_BUTTON_KEY, false);
            args.putInt( GlobalInfoContainer.ITEM_ID_KEY, id );
        }
        else
            args.putBoolean( GlobalInfoContainer.IS_CLEAR_BUTTON_KEY, true );

        DialogFragment di = new MyDialogFragment( );
        di.setArguments( args );
        di.show( getFragmentManager(), "mydialog");
    }

    /*********************** DIALOG INTERFACE IMPLEMENTATION **********************/
    @Override
    public void onDialogPositiveClick( DialogFragment dialog ) {
        if( isClearButton == true ) {
            //Delete all swings from the  DB
            if( selectedPeriod == SwingPeriod.DAY )
                new SwingLoaderTask().execute( OperationType.getStringValue( OperationType.DELETE_SWINGS_BY_DATE ), selectedData );
            else if( selectedPeriod == SwingPeriod.MONTH)
                new SwingLoaderTask().execute( OperationType.getStringValue( OperationType.DELETE_SWINGS_BY_MONTH ), dateTokens[ 1 ], dateTokens[ 0 ] );
            else if( selectedPeriod == SwingPeriod.YEAR )
                new SwingLoaderTask().execute( OperationType.getStringValue( OperationType.DELETE_SWINGS_BY_YEAR ), dateTokens[ 0 ] );


            Toast.makeText( ListActivity.this, ALL_SWINGS_DELETED_TEXT, Toast.LENGTH_SHORT );
            // o rimettere ListActivity.this
        }  else {//IT'S AN ITEM OF THE LIST

            new SwingLoaderTask().execute( OperationType.getStringValue( OperationType.DELETE_SWING_BY_ID ), "" + selectedItemID );
            Toast.makeText( ListActivity.this, SWING_DELETED_SUCCESSFULLY_TEXT, Toast.LENGTH_SHORT );
        }

    }

    @Override
    public void onDialogNegativeClick( DialogFragment dialog ) {
        dialog.dismiss();
    }

    /***************** END IMPLEMENTATION *********************/



    public static void setListAdapter( ListAdapter adapter ) {
        getListView().setAdapter( adapter );
    }

    protected static ListView getListView() {
        return listView;
    }

    /*******************************   ASYNC TASK   *********************************************************************/
    class SwingLoaderTask extends AsyncTask< String, Long, List<Swing>> {//Param, progress, Result
        private DBManagerService dbService;
        private List<Swing> taskSwingList;
        private OperationType operationType;

        /**
         * STRING 0: OPERATION TYPE
         * STRING 1: DATE || MONTH || YEAR || ID
         * STRING 2: YEAR
         * According to the isClearButton of operation, not all parameters will be used
         *
         * @return
         */
        @Override
        protected List<Swing> doInBackground(String... strings) {
            dbService = ListActivity.dbService;
            operationType = OperationType.getOperationType( strings[0] );

            if (operationType == OperationType.SELECT_SWINGS_BY_DATE)
                taskSwingList = dbService.selectSwingsByDate( strings[ 1 ] );
            else if (operationType == OperationType.SELECT_SWINGS_BY_MONTH)
                taskSwingList = dbService.selectSwingsByMonth(Integer.valueOf(strings[1]), Integer.valueOf(strings[2]));//MONTH & YEAR
            else if (operationType == OperationType.SELECT_SWINGS_BY_YEAR)
                taskSwingList = dbService.selectSwingsByYear(Integer.valueOf( strings[ 1 ] ) );//JUST YEAR

            else if (operationType == OperationType.DELETE_SWING_BY_ID)
                dbService.deleteSwingByID(Integer.valueOf( strings[ 1 ] ) );//ID
            else if (operationType == OperationType.DELETE_SWINGS_BY_DATE)
                dbService.deleteSwingByDate( strings[ 1 ] );//DATE
            else if (operationType == OperationType.DELETE_SWINGS_BY_MONTH)
                dbService.deleteSwingByMonth(Integer.valueOf( strings[ 1 ] ), Integer.valueOf( strings[ 2 ] ) );//MONTH & YEAR
            else if (operationType == OperationType.DELETE_SWINGS_BY_YEAR)
                dbService.deleteSwingByYear(Integer.valueOf( strings[ 1 ] ) );//ONLY THE YEAR

            return taskSwingList;
        }

        @Override
        protected void onPostExecute( List<Swing> list ) {
            if (operationType == OperationType.SELECT_SWINGS_BY_DATE
                    || operationType == OperationType.SELECT_SWINGS_BY_MONTH
                    || operationType == OperationType.SELECT_SWINGS_BY_YEAR) {

                if( list == null || list.isEmpty() ) {
                    Toast.makeText( getApplicationContext(), NO_SWINGS_PRESENT_MSG, Toast.LENGTH_SHORT).show();
                    clearButton.setVisibility( View.INVISIBLE );
                }
                else
                    loadSwings( list );

            } else if (operationType == OperationType.DELETE_SWING_BY_ID) {

                adapter.remove(swingToBeRemove);//Delete all swing from the list
                if( adapter.getCount() == 0) {
                    clearButton.setVisibility(View.INVISIBLE);
                    tvHeader.setText(LIST_HEADER_TEXT);
                }
                selectedItemID = -1;

            } else if (operationType == OperationType.DELETE_SWINGS_BY_DATE
                    || operationType == OperationType.DELETE_SWINGS_BY_MONTH
                    || operationType == OperationType.DELETE_SWINGS_BY_YEAR) {

                adapter.clear();//Delete all swing from the list

                clearButton.setVisibility(View.INVISIBLE);
                tvHeader.setText(LIST_HEADER_TEXT);
            }

        }

        private void loadSwings( List<Swing> list ) {
            System.out.println("**   LIST ACTIVITY: list of the swing got from the DB MANAGER SERVICE: " + swingList + "\n");

            swingList = new ArrayList<>( list );
            adapter = new ArrayAdapter<Swing>( getApplicationContext(), android.R.layout.simple_list_item_1, swingList );
            setListAdapter( adapter );

            adapter.notifyDataSetChanged();
            rowCounter = adapter.getCount();

            clearButton.setVisibility( View.VISIBLE );
            String periodStr = "";
            if( selectedPeriod == SwingPeriod.DAY ) periodStr = selectedData;
            else if(selectedPeriod == SwingPeriod.MONTH ) periodStr = dateTokens[ 1 ];
            else if( selectedPeriod == SwingPeriod.YEAR ) periodStr = dateTokens[ 0 ];
            tvHeader.setText( tvHeader.getText() + " " + SwingPeriod.getStringFromIntegerPeriod( selectedPeriod ) + " " + periodStr );
        }
    }
}
