package it.unipi.mywearapp;

import android.graphics.Color;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineDataSet;

/**
 * Set the style of the LineChart and of the Dataset
 */
public class ChartStyleBuilder {
    private LineChart chart;
    private LineDataSet dataSet;

    public ChartStyleBuilder(LineChart c, LineDataSet lds) {
        this.chart = c;
        this.dataSet = lds;
        setStyle();
    }

    private void setStyle() {
        dataSet.setColor( Color.rgb(0xFF,0xA5, 0) );//Orange
        dataSet.setValueTextColor( Color.WHITE );

        dataSet.setLineWidth( 2 );
        dataSet.setValueTextSize( 14 );

        Legend l = chart.getLegend();
        l.setFormSize( 10f ); // set the size of the legend forms/shapes
        l.setForm(Legend.LegendForm.CIRCLE); // set what type of form/shape should be used
        l.setTextSize( 20f );
        l.setTextColor(Color.WHITE);
        //l.setXEntrySpace(5f); // set the space between the legend entries on the x-axis
        //l.setYEntrySpace(5f);

        Description d = new Description();
        d.setTextSize( 20 );
        d.setText( ChartActivity.chartDescription );
        d.setTextColor( Color.WHITE );

        chart.setDescription( d );
        chart.setAutoScaleMinMaxEnabled( true );
        chart.setNoDataText( ChartActivity.chartNoDataText );//Sets the text that should appear if the chart is empty.

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition( XAxis.XAxisPosition.BOTTOM );
        xAxis.setTextSize( 20f );
        xAxis.setGranularity( 1f ); // minimum axis-step (interval) is 1
        xAxis.setTextColor( Color.rgb(0xFF,0xA5, 0) );//Orange
        xAxis.setDrawAxisLine( true );
        xAxis.setDrawGridLines( true );
        xAxis.setXOffset( 10);
        xAxis.setLabelCount( 10 ); //# of label, OK WORKS


        YAxis yAxis = chart.getAxis(YAxis.AxisDependency.LEFT);

        chart.getAxisRight().setEnabled( false ); // no right axis

        yAxis.setTextSize( 20f ); // set the text size
        yAxis.setAxisMinimum( 0f ); // start at zero
        //  yAxis.setAxisMaximum(10f); // the axis maximum is 100 !!!! OK REGULATE THIS ONE TO SET THE NUMBER OF ETICHETTE SULLA SX
        yAxis.setTextColor( Color.RED );//Orange
        yAxis.setGranularity(1f); // interval 1
        yAxis.setLabelCount(10, true); // force 6 labels

        chart.setBackgroundColor( Color.DKGRAY );
    }
}
