package com.maps.mhealth;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.Menu;
import android.view.MenuItem;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Charts extends FragmentActivity implements OnChartValueSelectedListener {
    private PieChart mChart;
    public String[] mParties = new String[]{
            "Path A", "Path B", "Path C", "Path D", "Path E", "Path F", "Path G", "Path H",
            "Path I", "Path J", "Path K", "Path L", "Path M", "Path N", "Path O", "Path P",
            "Path Q", "Path R", "Path S", "Path T", "Path U", "Path V", "Path W", "Path X",
            "Path Y", "Path Z"
    };
    private Typeface tf;
    private String fileName;
    private float totalDistance = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chart_view);
        fileName = getIntent().getStringExtra("FileName");
        mChart = (PieChart) findViewById(R.id.chart1);
        mChart.setUsePercentValues(true);
        mChart.setDescription("");
        mChart.setExtraOffsets(5, 10, 5, 5);
        mChart.setDragDecelerationFrictionCoef(0.95f);
        tf = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");



        mChart.setRotationAngle(0);
        // enable rotation of the chart by touch
        mChart.setRotationEnabled(true);
        mChart.setHighlightPerTapEnabled(true);

        // add a selection listener
        mChart.setOnChartValueSelectedListener(this);

        setData(20, 100, 1);
        drawCenterText(1);
        mChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        // mChart.spin(2000, 0, 360);

        Legend l = mChart.getLegend();
        l.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.piechartmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.distance: {

                setData(20, 100, 1);
                drawCenterText(1);
                mChart.animateXY(1400, 1400);
                break;
            }
            case R.id.startHeartRate: {

                setData(20, 100, 2);
                drawCenterText(2);
                mChart.animateXY(1400, 1400);
                break;
            }
            case R.id.endHeartRate: {

                setData(20, 100, 3);
                drawCenterText(3);
                mChart.animateXY(1400, 1400);
                break;
            }
            case R.id.caloriesBurned: {

                setData(20, 100, 4);
                drawCenterText(4);
                mChart.animateXY(1400, 1400);
                break;
            }
        }
        return true;
    }

    private void drawCenterText(int flag)
    {
        mChart.setCenterTextTypeface(Typeface.createFromAsset(getAssets(), "OpenSans-Light.ttf"));
        mChart.setCenterText(generateCenterSpannableText(flag));

        mChart.setDrawHoleEnabled(true);
        mChart.setHoleColorTransparent(true);

        mChart.setTransparentCircleColor(Color.WHITE);
        mChart.setTransparentCircleAlpha(110);

        mChart.setHoleRadius(58f);
        mChart.setTransparentCircleRadius(61f);

        mChart.setDrawCenterText(true);
        totalDistance = 0;
    }

    private void setData(int count, float range, int flag)
    {
        ArrayList<Entry> yVals1 = new ArrayList<Entry>();;
        if(flag == 1)
        {
            yVals1 = getDistanceData();
        }
        else if(flag == 2)
        {
            yVals1 = getStartHeartRateData();
        }
        else if(flag == 3)
        {
            yVals1 = getEndHeartRateData();
        }
        else if(flag == 4)
        {
            yVals1 = getCaloriesData();
        }
        ArrayList<String> xVals = new ArrayList<String>();

        for (int i = 0; i < count + 1; i++)
            xVals.add(mParties[i % mParties.length]);

        PieDataSet dataSet = new PieDataSet(yVals1, "User Tracks");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        // add a lot of colors

        ArrayList<Integer> colors = new ArrayList<Integer>();

        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);

        colors.add(ColorTemplate.getHoloBlue());

        dataSet.setColors(colors);
        //dataSet.setSelectionShift(0f);

        PieData data = new PieData(xVals, dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.BLACK);
        data.setValueTypeface(tf);
        mChart.setData(data);

        // undo all highlights
        mChart.highlightValues(null);

        mChart.invalidate();
    }

    private SpannableString generateCenterSpannableText(int flag)
    {
        String str = "";
        if(flag == 1)
        {
            str = "Distance:"+Float.toString(totalDistance)+"Mts.";
        }
        else if(flag == 2)
        {
            str = "Start Heart Rate";
        }
        else if(flag == 3)
        {
            str = "End Heart Rate";
        }
        else if(flag == 4)
        {
            str = "Calories Burned";
        }
        SpannableString s = new SpannableString(str);
        s.setSpan(new RelativeSizeSpan(1.7f), 0, str.length(), 0);
        s.setSpan(new StyleSpan(Typeface.NORMAL), str.length(), s.length(), 0);
        s.setSpan(new ForegroundColorSpan(Color.GRAY), str.length(), s.length(), 0);
        s.setSpan(new RelativeSizeSpan(.8f), str.length(), s.length(), 0);
        s.setSpan(new StyleSpan(Typeface.ITALIC), s.length(), s.length(), 0);
        s.setSpan(new ForegroundColorSpan(ColorTemplate.getHoloBlue()), s.length(), s.length(), 0);
        return s;
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h)
    {
        if (e == null)
            return;
//        Log.i("VAL SELECTED",
//                "Value: " + e.getVal() + ", xIndex: " + e.getXIndex()
//                        + ", DataSet index: " + dataSetIndex);
    }

    @Override
    public void onNothingSelected()
    {
//        Log.i("PieChart", "nothing selected");
    }

    private ArrayList<Entry> getDistanceData()
    {
        ArrayList<Entry> yVals1 = new ArrayList<Entry>();
        String s = getFileData(fileName);
        String endMarkers[] = s.split("3.EndMarkers:")[1].split(",");
        for(int i=0;i<endMarkers.length;i++)
        {
            float distance = Float.parseFloat(endMarkers[i].split("::")[2].split(";")[0].split("Distance Travelled:")[1].split("Mt.")[0]);
            totalDistance = totalDistance+distance;
            yVals1.add(new Entry(distance, i));
        }
        return yVals1;
    }

    private ArrayList<Entry> getStartHeartRateData()
    {
        ArrayList<Entry> yVals1 = new ArrayList<Entry>();
        String s = getFileData(fileName);
        String endMarkers[] = s.split("2.StartMarkers:")[1].split(";")[0].split(",");
        for(int i=0;i<endMarkers.length;i++)
        {
            float distance = Float.parseFloat(endMarkers[i].split("::")[2].split("Heart Rate:")[1]);
            yVals1.add(new Entry(distance, i));
        }
        return yVals1;
    }

    private ArrayList<Entry> getEndHeartRateData()
    {
        ArrayList<Entry> yVals1 = new ArrayList<Entry>();
        String s = getFileData(fileName);
        String endMarkers[] = s.split("3.EndMarkers:")[1].split(",");
        for(int i=0;i<endMarkers.length;i++)
        {
            float distance = Float.parseFloat(endMarkers[i].split("::")[2].split(";")[3].split("Heart Rate:")[1]);
            yVals1.add(new Entry(distance, i));
        }
        return yVals1;
    }

    private ArrayList<Entry> getCaloriesData()
    {
        ArrayList<Entry> yVals1 = new ArrayList<Entry>();
        String s = getFileData(fileName);
        String endMarkers[] = s.split("3.EndMarkers:")[1].split(",");
        for(int i=0;i<endMarkers.length;i++)
        {
            float distance = Float.parseFloat(endMarkers[i].split("::")[2].split(";")[4].split("Calories Burned:")[1].split("KCal")[0]);
            yVals1.add(new Entry(distance, i));
        }
        return yVals1;
    }

    private String getFileData(String file)
    {
        String s="";
        try
        {
            String path = Environment.getExternalStorageDirectory() +"/TrackPath/";
            InputStreamReader InputRead= new InputStreamReader(new FileInputStream(new File(path, file)));
            char[] inputBuffer= new char[1000];

            int charRead;

            while ((charRead=InputRead.read(inputBuffer))>0) {
                // char to string conversion
                String readstring=String.copyValueOf(inputBuffer,0,charRead);
                s +=readstring;
            }
            InputRead.close();
            return s;
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
