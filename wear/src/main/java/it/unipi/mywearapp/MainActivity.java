package it.unipi.mywearapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends WearableActivity implements View.OnClickListener {

    private Button startButton, stopButton;
    public static boolean enableMeasuring = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = (Button) findViewById(R.id.buttonStart);
        startButton.setOnClickListener( this );

        stopButton = (Button) findViewById(R.id.buttonStop);
        stopButton.setOnClickListener( this );

        // Enables Always-on
        setAmbientEnabled();
    }

    @Override
    public void onClick( View v ) {
        System.out.println("****** \n\n BUTTON CLICKED");
        if( v.getId() == R.id.buttonStart ) {
            enableMeasuring = true;

            Intent i = new Intent(this, MeasuringActivity.class);
            //intent.putExtra(EXTRA_MESSAGE, message);
            startActivity(i);

        } else {//Stop Button
            enableMeasuring = false;
        }
    }
}
