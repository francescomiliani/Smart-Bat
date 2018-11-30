package it.unipi.mywearapp;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is a fragment, is called from the Chart Activity & simply loads the swing contained into the Chart Activity
 */
public class AngleFragment extends Fragment {
    private List<SwingObject> dataObjects;
    private LineChart chart;
    private List<Entry> entries;
    private LineData lineData;
    private LineDataSet dataSet;

    public AngleFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_angle, container, false);

        createChart( v );
        return v;
    }

    private void createChart( View v ) {

        // in this example, a LineChart is initialized from xml
        chart = v.findViewById( R.id.angleChart );

        dataObjects = new ArrayList<SwingObject>();

        SwingObject.resetCount();
        List<Swing> swingList = ChartActivity.swingList;
        for( Swing s : swingList)
            dataObjects.add( new SwingObject( s.getAngle() ) );//This is the actual difference among all the chart about the representation

        entries = new ArrayList<Entry>();
        for (SwingObject data : dataObjects )
            entries.add( new Entry( data.getX(), (float)data.getY() ) );// turn your data into Entry objects

        //System.out.println( "Mio Entry object: " + entries.toString() );
        dataSet = new LineDataSet( entries, "Swing Angle" ); // add entries to dataset

        //Set the style of the chart
        new ChartStyleBuilder( chart, dataSet );

        lineData = new LineData( dataSet );

        chart.setData( lineData );
        chart.invalidate(); // refresh
    }

}
