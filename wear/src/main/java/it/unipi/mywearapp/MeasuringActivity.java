package it.unipi.mywearapp;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.widget.TextView;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;

import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

/**
 * The app have to measure 3 parameters:
 *  1) Swing Strength: F = ma, where m is the mass, and 'a' the acceleration
 *  2) Swing Angle: the inclination of the bat ( determined by the angle between the shoulders and the bat),
 *      It is the max angle registered within the start & the end of the swing
 *  3) Swing duration: time interval between the beginning & the maximum acceleration before the maximum acceleration,
 *      where the impact with the ball will be verify.
 *
 *  We are used as sensor: the ACCELEROMETER & the GEOMAGNETIC ROTATION VECTOR ( for measuring the angle )
 */

public class MeasuringActivity extends WearableActivity implements SensorEventListener {

    private static final String TAG = "WearSensors";
    private static final String URI_PATH = "/MaPSWearSensors";

    private static final String ACCELERATION_KEY = "it.unipi.mywearapp.acceleration";
    private static final String ANGLE_KEY = "it.unipi.mywearapp.angle";
    private static final String DURATION_KEY = "it.unipi.mywearapp.duration";
    private String NO_ACCELEROMETER_MSG, NO_GEOMAGNETIC_MSG, ACCELEROMETER_DEFAULT_VALUE, ANGLE_DEFAULT_VALUE, DURATION_DEFAULT_VALUE;

    private static final int SAMPLING_RATE = 100;//milliseconds
    private static final double SWING_TIME = 1000;//milliseconds

    /**
     * SENSOR_DELAY_NORMAL = 200,000 microseconds.
     * SENSOR_DELAY_GAME (20,000 microsecond delay)
     * SENSOR_DELAY_UI (60,000 microsecond delay).
     * SENSOR_DELAY_FASTEST (0 microsecond delay).
     */
    private static final int ANDROID_SENSOR_SAMPLING_RATE = SensorManager.SENSOR_DELAY_GAME;//milliseconds
    private static final int NO_RELEVANT_DURATION = (int)( SWING_TIME / SAMPLING_RATE );//To detect a NO ACTIVITY
    private static final double ACCELERATION_THRESHOLD = 7.0;//m/s^2
    private static final int DECREASE_SAMPLE_THRESHOLD = 3;//#samples to detect a decreasing front
    private static final float ALPHA = (float) 0.8;//Constant suggests by Android for Acceleration
    private static final float NS2S = 1.0f / 1000000000.0f;
    private static final float NANO2MILLIS = 1.0f / 100000.0f;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer, mRotationVector;

    float gravity, linearAcceleration, maxAcceleration = 0, maxANGLE = 0, lastAcceleration = 0;
    private double startSwingDuration = 0.0, swingDuration = 0.0, lastAccUpdate = 0, lastRotationUpdate = 0;//For filtering;
    private boolean swingDetected = false, swingRaising = false;
    private int movingCounter = 0, notRelevantAccelerationCounter = 0, notRelevantRotationCounter = 0;
    //private float[] linearAccelaration = new float[3];

    private TextView tvAcc, tvAngle, tvDuration;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measuring);

        tvAcc = (TextView) findViewById(R.id.tvAccValue);
        tvAngle = (TextView) findViewById(R.id.tvAngleValue);
        tvDuration = (TextView) findViewById(R.id.tvDurationValue);

        NO_ACCELEROMETER_MSG = getString( R.string.no_accelerometer );
        NO_GEOMAGNETIC_MSG = getString( R.string.no_geomagnetic_rotation_vector );
        ACCELEROMETER_DEFAULT_VALUE = getString( R.string.text_view_accelerometer_default_value );
        ANGLE_DEFAULT_VALUE = getString( R.string.text_view_angle_default_value );
        DURATION_DEFAULT_VALUE = getString( R.string.text_view_duration_default_value );

        setupSensors();//Recovering of the sensors

    }

    private void setupSensors() {
        System.out.printf("\n\nSETUP\n\n");
        mSensorManager = (SensorManager) getSystemService( SENSOR_SERVICE );
        //Checking if the sensors are both present

        //List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        mAccelerometer = mSensorManager.getDefaultSensor( Sensor.TYPE_ACCELEROMETER );
        if( mAccelerometer == null ) {
            System.err.println( NO_ACCELEROMETER_MSG );
            Toast.makeText( getApplicationContext(), NO_ACCELEROMETER_MSG, Toast.LENGTH_SHORT).show();
            System.exit( -1 );
        }

        mRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);//Rotation Vector
        if (mRotationVector == null) {
            System.err.println( NO_GEOMAGNETIC_MSG );
            Toast.makeText(getApplicationContext(), NO_GEOMAGNETIC_MSG, Toast.LENGTH_SHORT).show();
            System.exit(-1);
        }
    }

    /**
     * Registration & setting the sampling rate
     *
     * SENSOR_DELAY_NORMAL : 200,000 microseconds.
     * SENSOR_DELAY_UI (60,000 microsecond delay),
     * SENSOR_DELAY_GAME (20,000 microsecond delay),
     * SENSOR_DELAY_FASTEST (0 microsecond delay).
     * The delay that you specify is only a suggested delay. The Android system and other applications can alter this delay. As a best practice, you should specify the largest delay that you can because the system typically uses a smaller delay than the one you specify (that is, you should choose the slowest sampling rate that still meets the needs of your application).
     * Using a larger delay imposes a lower load on the processor and therefore uses less power.

     * There is no public method for determining the rate at which the sensor framework is sending sensor events to your application; however, you can use the timestamps that are associated with each sensor event to calculate the sampling rate over several events. You should not have to change the sampling rate (delay) once you set it. If for some reason you do need to change the delay, you will have to unregister and reregister the sensor listener.
     */
    @Override
    protected void onResume() {
        super.onResume();
        //Sensor Registration
        mSensorManager.registerListener(this, mAccelerometer, ANDROID_SENSOR_SAMPLING_RATE);//This is the SAMPLE RATE
        mSensorManager.registerListener(this, mRotationVector, ANDROID_SENSOR_SAMPLING_RATE );//This is the SAMPLE RATE
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }
    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    /**
     * This method fires when a new measurement has got by the sensor
     */
    @Override
    public void onSensorChanged( SensorEvent event ) {
        if( MainActivity.enableMeasuring == false )
            return;

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER ) {
            handleAccelerometerEvent( event );
        }
        if (event.sensor.getType() == Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR ) {
            handleAngleEvent( event );
        }
    }

    //Due to the way in which the watch is positioned over the bat, will be used just the Y Axis
    private void handleAccelerometerEvent( SensorEvent event ) {
        double actualTime = event.timestamp * NANO2MILLIS;//timestamp is NANOSECOND -> convert in milliseconds

        float[] valuesClone = event.values.clone();
        float acceleration = valuesClone[ AXIS.Y ];

        //The acceleration given from the sensor Acceleration is equal to Acceleration = gravity + linear_acceleration
        // Isolate the force of gravity with the low-pass filter.
        gravity = ALPHA * gravity + (1 - ALPHA) * acceleration;

        // Remove the gravity contribution with the high-pass filter.
        //It's taken the absolute value of the acceleration because the hitter can be right hand or left hand
        linearAcceleration = Math.abs( acceleration - gravity );

        //FILTERING
        if( linearAcceleration > ACCELERATION_THRESHOLD ) {//TO AVOID THE NOISE OR OTHER SMALL MOVEMENTS
            System.out.println("ACCELERATION_EVENT:\t\t" + linearAcceleration + "\n" );

            if( linearAcceleration > maxAcceleration )
                maxAcceleration = linearAcceleration;

            swingDetection( linearAcceleration, lastAcceleration, actualTime );

            //In any case, the last measure must be updated
            lastAcceleration = linearAcceleration;//Update the last acceleration measure
        } else {

            notRelevantAccelerationCounter++;
            if( notRelevantAccelerationCounter == NO_RELEVANT_DURATION ) {
                //System.out.println("***** ********++   ACC     values reset! " + notRelevantAccelerationCounter );

                //RESET ALL THE MEASUREMENTS & THE VARIABLES
                notRelevantAccelerationCounter = 0;
                maxAcceleration = 0;
                maxANGLE = 0;
                swingDuration = 0;

                //to restart the swing detection
                swingDetected = false;
                swingRaising = false;

                tvAcc.setText( ACCELEROMETER_DEFAULT_VALUE );//Only 2 decimal digits
                tvAngle.setText( ANGLE_DEFAULT_VALUE  );//Only 2 decimal digits
                tvDuration.setText( DURATION_DEFAULT_VALUE );
            }//end if not relevant counter
        }//end else

    }

    /**
     * The geomagnetic rotation vector sensor is similar to the Rotation Vector Sensor(The rotation vector represents the orientation of the device
     * as a combination of an angle and an axis, in which the device has rotated through an angle θ around an axis (x, y, or z). ),
     * but it uses a magnetometer instead of a gyroscope.
     * The accuracy of this sensor is lower than the normal rotation vector sensor, but the power consumption is reduced.
     * Only use this sensor if you want to collect some rotation information in the background without draining too much battery.
     * @param event
     */
    //Due to the way in which the watch is positioned over the bat, will be used just the X Axis
    private void handleAngleEvent(SensorEvent event ) {
        float[] valuesClone = event.values.clone();
        float x = valuesClone[ AXIS.X ]; //We use just the x-axis due to way the watch is mounted on the bat

        //Absolute value because the hitter can be left-hand or right-hand
        float absAngle = Math.abs( x * 5/9 * 100) ;//Conversion from centesimal degrees to sexagesimal degrees

        //Angle sampling is enabled only when the swing is started!
        if( swingRaising == true ) {
            //System.out.println("\tROTATION_EVENT: \tangle: \t" + String.format ( "%.2f",  absAngle  ) + "°\n"  );
            if( Math.abs( absAngle ) > maxANGLE )//if the acceleration is good, sample the angle
            	maxANGLE = absAngle;
        }
    }

    /**********************************************************************************************/

    /**
     * This is the algorithm real time to detect a new swing. It considers the sequence of sample previously seen
     */
    private void swingDetection( float currentValue, float lastValue, double eventTimestamp ) {//
        //System.out.println("________SWING_DETECTION: curr:    "+ currentValue + "   lastValue:  " + lastValue );

        if( currentValue > lastValue ) {//While there is an increasing front --> is a possible swing!
            //System.out.println("*****  +++++ INCREASING!!! ");

            if( swingRaising == false ) {//first sample of decreasing
                startSwingDuration = eventTimestamp;
                swingRaising = true;

                System.out.println("***** TIMER STARTED!       time: " + startSwingDuration + " ms");
            }

        } else {//z is less lastValue -> decreasing front

            if( swingDetected == true ) return;//Because it may happens that new sample continue to come

            //System.out.println("*****\n\t\t ---- decreasing !!! counter:  " + movingCounter );
            movingCounter++;
            if( movingCounter == DECREASE_SAMPLE_THRESHOLD ) {//DECREASE_THRESHOLD

                swingDuration = (eventTimestamp - startSwingDuration );//in milliseconds, with "int" as recast, the decimals are removed
                if( swingDuration > 1000 ) swingDuration /= 10;//error of measurements

                System.out.println("***** TIMER STOPPED!       event_time:  " + eventTimestamp + " ms    SWING DURATION: " + (int)swingDuration );

                System.out.println("*****\n\t\t\tSWING !!! \n\n" );

                CharSequence text = "SWING: " + String.format( "%.2f", maxAcceleration ) +" N"+
                        "  ANGLE: " + String.format( "%.2f", maxANGLE ) +" °"+
                        "  SWING DURATION: " + (int)swingDuration +" ms";
                Toast.makeText( getApplicationContext(), text, Toast.LENGTH_SHORT).show();

                movingCounter = 0;//restart the counter
                swingRaising = false;
                swingDetected = true;

                tvAcc.setText( String.format( "%.2f", maxAcceleration ) + "  N" );//Only 2 decimal digits
                tvAngle.setText( String.format( "%.2f", maxANGLE ) + "  °" );//Only 2 decimal digits
                tvDuration.setText( swingDuration + "  ms" );

                //The swing has been detected! So we send the data to the smartphone
                sendMeasurementsToSmartphone();
            }
        }
    }

    private void sendMeasurementsToSmartphone( ) {
        System.out.println("  ------->      sendMeasuresToSmartphone!\n" );

        PutDataMapRequest putDataMapReq = PutDataMapRequest.create( URI_PATH );
        //Insertion of the value into pres
        putDataMapReq.getDataMap().putFloat( ACCELERATION_KEY, maxAcceleration );
        putDataMapReq.getDataMap().putFloat( ANGLE_KEY, maxANGLE );
        putDataMapReq.getDataMap().putDouble( DURATION_KEY, swingDuration );

        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();

        Task<DataItem> dataItemTask = Wearable.getDataClient(getApplicationContext()).putDataItem( putDataReq );
    }
}

abstract class AXIS {
    public final static int X = 0;
    public final static int Y = 1;
    public final static int Z = 2;
}