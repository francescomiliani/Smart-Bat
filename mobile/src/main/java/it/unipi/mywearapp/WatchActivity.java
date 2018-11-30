package it.unipi.mywearapp;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

/**
 * This activity before check if the bluetooth is enabled, after that it receive the data from the Wear Device, insert them into the DB &
 * if the best measurements are improved from new ones, a notification will be triggered
 */
public class WatchActivity extends Activity implements DataClient.OnDataChangedListener  {
    final String USER_PREFERENCES_FILE = GlobalInfoContainer.USER_PREFERENCES_FILE;

    private static final String ACCELERATION_KEY = "it.unipi.mywearapp.acceleration";
    private static final String ANGLE_KEY = "it.unipi.mywearapp.angle";
    private static final String DURATION_KEY = "it.unipi.mywearapp.duration";

    private static String BAT_WEIGHT_KEY;

    private static final String TAG = "HandheldDevice";
    private static final String URI_PATH = "/MaPSWearSensors";
    private TextView tvAcc, tvAngle, tvDuration;

    private static double bestDuration;//Kg
    private static float MASS, BAT_WEIGHT, bestStrength, bestAngle;
    private static int UNIQUE_NOTIFICATION_NUMBER;
    private static SharedPreferences pref;

    private DBManagerService dbService;
    private boolean connected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_watch );

        init();

        bluetoothChecking();
    }

    //Set all the parameter, listeners & graphic elements
    private void init() {
        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setIcon( R.drawable.watch_icon);

        tvAcc = findViewById(R.id.tvAccValue);
        tvAngle = findViewById(R.id.tvAngleValue);
        tvDuration = findViewById(R.id.tvDurationValue);
        tvAcc.setText( getString( R.string.text_view_watch_strength_default_value ) + "  N" );
        tvAngle.setText( getString( R.string.text_view_watch_angle_default_value )  + "  °" );
        tvDuration.setText( getString( R.string.text_view_watch_duration_default_value )  + "  ms" );
        BAT_WEIGHT_KEY = getString( R.string.bat_weight_key );

        pref = getApplicationContext().getSharedPreferences( USER_PREFERENCES_FILE, MODE_PRIVATE );
        BAT_WEIGHT = GlobalInfoContainer.getFavouriteBat();
        if( BAT_WEIGHT == 0 ) {
            BAT_WEIGHT = Float.valueOf( pref.getString( BAT_WEIGHT_KEY, "0"));//The element 0 of the list! the default one
            if( BAT_WEIGHT == 0 )
                BAT_WEIGHT = Float.valueOf( getResources().getStringArray( R.array.bat_values_array)[0] );
            GlobalInfoContainer.setFavouriteBat( BAT_WEIGHT );
        }
        MASS = BAT_WEIGHT;//Kg
        Toast.makeText( getApplicationContext(), "Bat selected: " + String.format( "%.3f", BAT_WEIGHT ), Toast.LENGTH_SHORT).show();

        bestStrength = GlobalInfoContainer.getBestSwing().getStrength();
        bestAngle = GlobalInfoContainer.getBestSwing().getAngle();
        bestDuration = GlobalInfoContainer.getBestSwing().getDuration();
        Toast.makeText( getApplicationContext(), "Current Max Strenght: " + String.format( "%.2f", bestStrength ) +
                " Current Min Angle: " + String.format( "%.2f", bestAngle ) +
                " Current Min Duration: " + bestDuration, Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onResume() {
        super.onResume();
        Wearable.getDataClient(this).addListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.getDataClient(this).removeListener(this);
    }

    @Override
    public void onDataChanged( DataEventBuffer dataEvents ) {
        System.out.printf("\n\t WATCH ACTIVITY: WEAR Event CHANGED!!!\n");
        for( DataEvent event : dataEvents ) {
            if (event.getType() == DataEvent.TYPE_CHANGED ) {
                // DataItem changed
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo( URI_PATH ) == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();

                    //System.out.printf("MEASUREMENTS Event RECEIVED\n");
                    updateMeasures( dataMap.getFloat( ACCELERATION_KEY), dataMap.getFloat( ANGLE_KEY ), dataMap.getDouble( DURATION_KEY ) );
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED ) {
            }
            // DataItem deleted
        }
    }

    /**
     * The method performs:
     *  1)insert the new swing into the db
     *  2) update the graphics of the activity
     * @param acceleration
     * @param angle
     * @param duration
     */
    private void updateMeasures( final float acceleration, final float angle, final double duration ) {
        //The computation of the strength of the swing is simply F = m*a, where F: Force, m:mass, a:acceleration
        final float strength = MASS * acceleration;

        String dataStr = CalendarActivity.getCurrentDate();
        //INSERTION OF THE SWING JUST DETECTED INTO THE DB!
        //There is no need to use an AsyncTask as in the others activity because the insert operation is very quick & short.
        dbService.insertSwingIntoDB( strength, angle, duration, dataStr );

        CharSequence text = "NEW SWING!  STRENGTH:  " + strength + " N;  ANGLE:  " + angle + " °;  DURATION: " + duration + " ms";
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT ).show();

        runOnUiThread(new Runnable() { //UI has to be updated from the main thread
            @Override
            public void run() {
                    tvAcc.setText( String.format( "%.2f", strength ) + "  N" );
                    tvAngle.setText( String.format( "%.2f", angle ) + "  °" );
                    tvDuration.setText( (int)duration + "  ms" );
            }
        });

        checkNewBestValues( strength, angle, duration );
    }

    //Check if there are new best values: a new max strength, a new min angle & duration. If it is so, a notification will be triggered
    private void checkNewBestValues( float strength, float angle, double duration ) {
        //if( )
        if( strength > bestStrength ) {
            bestStrength = strength;
            GlobalInfoContainer.getBestSwing().setStrength( bestStrength );
            emitNotification( ACCELERATION_KEY, strength );
        }
        if( angle < bestAngle ) {
            bestAngle = angle;
            GlobalInfoContainer.getBestSwing().setAngle( bestAngle );
            emitNotification( ANGLE_KEY, angle );
        }
        if( duration < bestDuration ) {
            bestDuration = duration;
            GlobalInfoContainer.getBestSwing().setDuration( bestDuration );
            emitNotification( DURATION_KEY, duration );
        }
    }

    //Build, set & emit a new NOTIFICATION
    private void emitNotification( String type, double value ) {
        String measureUnit = "", simpleKeyName = getSimpleKeyName( type );
        if( type.equalsIgnoreCase( ACCELERATION_KEY ) ) measureUnit = "N";
        else if( type.equalsIgnoreCase( ANGLE_KEY ) ) measureUnit = "°";
        else if( type.equalsIgnoreCase( DURATION_KEY ) ) measureUnit = "ms";

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder b = new NotificationCompat.Builder( getApplicationContext(), "M_CH_ID" );

        b.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority( NotificationManager.IMPORTANCE_MAX )
                .setWhen( System.currentTimeMillis() )
                .setSmallIcon(R.drawable.bat_icon )
                .setContentTitle( getString( R.string.notification_title ))
                .setContentText( getString( R.string.notification_text) + " " + simpleKeyName + " " + String.format( "%.2f", value ) + " " + measureUnit )
                .setDefaults(Notification.DEFAULT_LIGHTS| Notification.DEFAULT_SOUND)
                .setContentIntent( contentIntent );

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("M_CH_ID", "My Notifications", NotificationManager.IMPORTANCE_HIGH);

            // Configure the notification channel.
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor( Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        notificationManager.notify(UNIQUE_NOTIFICATION_NUMBER++, b.build() );
    }

    //Check if the bluetooth is On/OFF
    private void bluetoothChecking() {
        int REQUEST_ENABLE_BT = 1;
        /**
         * The REQUEST_ENABLE_BT constant passed to startActivityForResult() is a locally defined integer that must be greater than 0.
         * The system passes this constant back to you in your onActivityResult() implementation as the requestCode parameter.

         If enabling Bluetooth succeeds, your activity receives the RESULT_OK result code in the onActivityResult() callback. If Bluetooth was not enabled due to an error (or the user responded "No") then the result code is RESULT_CANCELED.
         */
        // 1) Get the Bluetooth Adapter
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(getApplicationContext(), "The Device does not support Bluetooth", Toast.LENGTH_LONG).show();
            System.err.println("\n\n\t\tThe Device does not support Bluetooth");
        }
        else {
            System.out.println("\n\nBluetooth TEST");
            // 2) Check if bluetooth is enabled
            if (mBluetoothAdapter.isEnabled() == false) {
                System.out.println("\n\n\t\tNOT ENABLED");
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    private String getSimpleKeyName( String keyStr ) {
        if( keyStr.equals( ACCELERATION_KEY) ) return "acceleration";
        else if( keyStr.equals( ANGLE_KEY ) ) return "angle";
        else if( keyStr.equals( DURATION_KEY ) ) return "duration";
        else return null;
    }
}

