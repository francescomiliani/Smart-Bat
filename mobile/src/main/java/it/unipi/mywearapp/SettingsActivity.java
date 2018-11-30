package it.unipi.mywearapp;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.widget.Toast;

/**
 * This activity is used for manage the information of the file SharedPreference. Uses a DialogFragment to ask the confirmation of the deletion to the user
 */
public class SettingsActivity extends PreferenceActivity implements MyDialogFragment.NoticeDialogListener {
    final String USER_PREFERENCES_FILE = GlobalInfoContainer.USER_PREFERENCES_FILE;

    static String HITTER_NAME_KEY, HITTER_NAME_TITLE, BAT_MEASURE_KEY, BAT_WEIGHT_KEY, CLEAR_KEY;
    public static String ALERT_DIALOG_SETTINGS_TITLE, ALERT_DIALOG_SETTINGS_TEXT, ALL_SWINGS_DELETED_MSG, PROBLEM_WITH_DELETION_MSG, FAVOURITE_BAT_CHANGED_MSG;
    public static float FAVOURITE_BAT_WEIGHT;

    private static DBManagerService dbService;
    private boolean connected;
    public static SharedPreferences pref;
    public static SharedPreferences.Editor editor;
    static Context context;
    static FragmentManager fragmentManager;

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);

        init();//set graphics

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction().replace( android.R.id.content, new MyPreferenceFragment()).commit();

        //Get shared preference
        pref = getApplicationContext().getSharedPreferences( USER_PREFERENCES_FILE, MODE_PRIVATE );
        editor = pref.edit();

        fragmentManager = getFragmentManager();
    }

    //Connection to the service
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
    }

    //Nested Class Service Connection
    public ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service ) {
            DBManagerService.DBBinder binder = (DBManagerService.DBBinder) service;
            dbService = binder.getService();
            connected = true;
        }

        @Override
        public void onServiceDisconnected( ComponentName name ) {
            connected = false;
        }
    };

    //Set all the parameter, listeners & graphic elements
    private void init() {
        context = SettingsActivity.this;

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayShowHomeEnabled(true);

        getActionBar().setIcon( R.drawable.settings_icon );

        HITTER_NAME_KEY = getString( R.string.hitter_name_key );
        HITTER_NAME_TITLE = getString(R.string.text_view_hitter_name);
        BAT_MEASURE_KEY = getString( R.string.bat_measure_key );
        BAT_WEIGHT_KEY = getString( R.string.bat_weight_key );
        CLEAR_KEY = getString( R.string.clear_key );

        ALERT_DIALOG_SETTINGS_TITLE = getString( R.string.alert_dialog_settings_title);
        ALERT_DIALOG_SETTINGS_TEXT = getString( R.string.alert_dialog_settings_text);
        ALL_SWINGS_DELETED_MSG = getString( R.string.deletion_msg);
        PROBLEM_WITH_DELETION_MSG = getString( R.string.deletion_problem );
        FAVOURITE_BAT_CHANGED_MSG = getString( R.string.favourite_bat_change_msg );
    }

    //Use a Bundle Object to send a parameter to the Dialog Fragment
    private static void emitAlertDialog () {
        Bundle args = new Bundle();

        args.putBoolean( GlobalInfoContainer.IS_CLEAR_BUTTON_KEY, false);
        args.putBoolean( GlobalInfoContainer.IS_CLEAR_ALL_SWINGS_KEY, true );

        DialogFragment di = new MyDialogFragment( );
        di.setArguments( args );
        di.show( fragmentManager, "mydialog");
    }

    /*********************** DIALOG INTERFACE IMPLEMENTATION **********************/
    @Override
    public void onDialogPositiveClick( DialogFragment dialog ) {
        new SwingLoaderTask().execute( );//delete all the swings in the DB
    }

    @Override
    public void onDialogNegativeClick( DialogFragment dialog ) {
        dialog.dismiss();
    }
    /*********************** END IMPLEMENTATION **********************/

    /*****************************   FRAGMENT    ***************************************************************/

    //Fragment
    public static class MyPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener {

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource( R.xml.preferences );// Load the preferences from an XML resource

            loadModifiedPreferences();

            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener( this );

            Preference clear_pref = findPreference( CLEAR_KEY );
            clear_pref.setOnPreferenceClickListener( this );

            context = getContext();
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener( this );
        }

        @Override
        public void onPause() {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener( this );
            super.onPause();
        }

        @Override
        public void onSharedPreferenceChanged( SharedPreferences sharedPreferences, String key ) {
            System.out.println("\n\n************* onSharedPreferenceChanged\n\n");
            String text = "key=" + key + " val=";

            //We need to check which is the field changed
            //KEEP MIND! IF I DO NOT WRITE WITH THE EDITOR THE NEW VALUE, THE CHANGES WILL NOT BE PERSISTENT IN THE FUTURE!
            if( key.equals( HITTER_NAME_KEY ) ) {
                Preference modified_pref = findPreference( key );
                String modif_string = sharedPreferences.getString( key, getString( R.string.text_view_hitter_name_default_value ));
                modified_pref.setTitle( HITTER_NAME_TITLE + "\t  "+ modif_string );
                text += modified_pref.getTitle();

                editor.putString( HITTER_NAME_KEY, modif_string );//Make persistent the changes
                editor.commit(); // commit changes
            }
            else if(key.equals( BAT_MEASURE_KEY ) ) {
                Boolean value = sharedPreferences.getBoolean( key, true);
                if( value == true ) editor.putBoolean( BAT_MEASURE_KEY, true);//Make persistent the changes
                else    editor.putBoolean( BAT_MEASURE_KEY, false);
                editor.commit();

                GlobalInfoContainer.setShowBestSwing( value );
                text += (value)?"true":"false";

            }else if( key.equals( BAT_WEIGHT_KEY ) ) {
                FAVOURITE_BAT_WEIGHT = Float.valueOf( sharedPreferences.getString( key, "" ) );
                editor.putString( BAT_WEIGHT_KEY, String.valueOf( FAVOURITE_BAT_WEIGHT));//Make persistent the changes
                editor.commit();
                GlobalInfoContainer.setFavouriteBat( FAVOURITE_BAT_WEIGHT );
                text += FAVOURITE_BAT_WEIGHT;

                Toast.makeText(context, FAVOURITE_BAT_CHANGED_MSG + " " + FAVOURITE_BAT_WEIGHT + "Kg", Toast.LENGTH_SHORT).show();
            }

            System.out.println("\n***** SETTINGS ACTIVITY: Modify the following field: " + text );

        }


        private void loadModifiedPreferences() {
            Preference modified_pref = findPreference( HITTER_NAME_KEY );
            String modif_string = pref.getString( HITTER_NAME_KEY, getString( R.string.text_view_hitter_name_default_value ));
            modified_pref.setTitle( HITTER_NAME_TITLE + "\t  "+ modif_string );

            GlobalInfoContainer.setShowBestSwing( pref.getBoolean( BAT_MEASURE_KEY, Boolean.valueOf( getString( R.string.bat_measure_default_value ) ) ) );

        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            System.err.println("SETTINGS ACTIVITY! CLICKED THE ELEMENT WITH TRASH, key= " + preference.getKey() );
            if( preference.getKey() == CLEAR_KEY )
                 emitAlertDialog();
            return false;
        }
    }//End fragment


    /*****************************   ASYNC TASK  ****************************************************************************************************/
    static class SwingLoaderTask extends AsyncTask< String, Void, Long> {//Param, progress, Result
        private DBManagerService dbService;

        /**
         * STRING 0: ACTIVITY NAME
         * STRING 1: OPERATION TYPE
         * STRING 2: DATE
         * STRING 3: MONTH
         * STRING 4: YEAR
         * STRING 5: ID
         * According to the type of operation, not all parameters will be used
         *
         * @return
         */
        @Override
        protected Long doInBackground(String... strings) {
            dbService = SettingsActivity.dbService;
            GlobalInfoContainer.setBestSwing( GlobalInfoContainer.getDefaultSwing() );

            return dbService.deleteAllSwings();
        }

        @Override
        protected void onPostExecute( Long result ) {
            if( result == -1) Toast.makeText(context, PROBLEM_WITH_DELETION_MSG, Toast.LENGTH_SHORT).show();
            else  Toast.makeText(context, result + " " + ALL_SWINGS_DELETED_MSG, Toast.LENGTH_SHORT).show();
        }

    }
}
