package com.maps.mhealth;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.format.Time;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MapsActivity extends FragmentActivity {
    //implements  OnMapReadyCallback
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private int flag = 0;
    private int startMarkerFlag = 0;
    private double distance = 0;
    private LatLng prev;
    private Polyline line;
    private ArrayList<LatLng> routePoints = new ArrayList<LatLng>();
    String startMarkerData;
    String endMarkerData;
    UiSettings settings;
    CameraUpdate zoom;
    Button startBtn;
    Button stopBtn;
    Button historyBtn;
    Button saveBtn;
    final Context context = this;
    Date start;
    Date stop;
    long timeDiffer;
    DateFormat df = new SimpleDateFormat("hh:mm:ss");
    public String heartRate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if(Utils.mMap!=null)
        {
            reDrawMap(Utils.mMap);
        }
        startBtn = (Button) findViewById(R.id.startBtn);
        stopBtn = (Button) findViewById(R.id.stopBtn);
        historyBtn = (Button) findViewById(R.id.historyBtn);
        saveBtn = (Button) findViewById(R.id.saveBtn);

        startBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = getIntent();
                if(!i.hasExtra("HeartBeatStart"))
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Confirm");
                    builder.setMessage("Do you want to measure your Heart Rate?");
                    builder.setPositiveButton("YES", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            Intent i = new Intent(MapsActivity.this, HeartRateMonitor.class);
                            i.putExtra("ActivityStatus", "Start");
                            startActivity(i);
                        }
                    });

                    builder.setNegativeButton("NO", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
                            if ( Build.VERSION.SDK_INT >= 23 &&
                                    ContextCompat.checkSelfPermission( context, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                                    ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                return  ;
                            }
                            mMap.setMyLocationEnabled(true);
                            if (mMap != null)
                            {
                                mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                                    @Override
                                    public void onMyLocationChange(Location location) {
                                        if(startMarkerFlag == 0)
                                        {
                                            startMarkerFlag = 1;
                                            addStartMarker(mMap, location);
                                        }
                                        drawMap(location);
                                    }
                                });
                            }
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
                else
                {
                    heartRate = getIntent().getStringExtra("HeartBeatStart");
                    heartRate = (heartRate == null) ? "-1" : heartRate;
                    getIntent().removeExtra("HeartBeatStart");
                    mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
                    if ( Build.VERSION.SDK_INT >= 23 &&
                            ContextCompat.checkSelfPermission( context, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return  ;
                    }
                    mMap.setMyLocationEnabled(true);
                    if (mMap != null)
                    {
                        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                            @Override
                            public void onMyLocationChange(Location location) {
                                if(startMarkerFlag == 0)
                                {
                                    startMarkerFlag = 1;
                                    addStartMarker(mMap, location);
                                }
                                drawMap(location);
                            }
                        });
                    }
                }
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Intent i = getIntent();
                if(!i.hasExtra("HeartBeatStop"))
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Confirm");
                    builder.setMessage("Do you want to measure your Heart Rate?");
                    builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Utils.mMap = mMap;
                            Utils.prev = prev;
                            Utils.start = start;
                            Utils.routePoints = routePoints;
                            Utils.distance = distance;
                            Utils.endMarkerData = getEndMarkerData();
                            //Utils.endMarkers.put(prev, endMarkerData);
                            Intent i = new Intent(MapsActivity.this, HeartRateMonitor.class);
                            i.putExtra("ActivityStatus", "Stop");
                            startActivity(i);
                        }
                    });

                    builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            startMarkerFlag = 0;
                            addEndMarker(mMap, distance);
                            distance = 0;
                            mMap.setOnMyLocationChangeListener(null);
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
                else
                {
                    heartRate = getIntent().getStringExtra("HeartBeatStop");
                    heartRate = (heartRate == null) ? "-1" : heartRate;
                    getIntent().removeExtra("HeartBeatStop");
                    mMap = Utils.mMap;
                    prev = Utils.prev;
                    Utils.endMarkerData = getEndMarkerData();
                    Utils.endMarkers.put(prev, endMarkerData);
                    routePoints = Utils.routePoints;
                    startMarkerFlag = 0;
                    Utils.route.add(routePoints);
                    reDrawMap(mMap);
                    distance = 0;
                    mMap.setOnMyLocationChangeListener(null);
                }
            }
        });

        historyBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(MapsActivity.this, History.class));
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
                saveData(mMap);
                //mMap.clear();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public String getCurrentTimes()
    {
        Time today = new Time(Time.getCurrentTimezone());
        today.setToNow();
        return  today.format("%k:%M:%S");
    }

    private void drawMap(Location location)
    {
        LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
        float[] results = new float[1];
        if(flag!=0)
        {
            distance = distance + calculateDistance(current, prev);
        }

        if(flag==0)
        {
            prev=current;
            flag=1;
        }
        prev=current;
        routePoints.add(current);
        PolylineOptions pOptions = new PolylineOptions()
                .width(24)
                .color(Color.BLUE)
                ;
        mMap.addPolyline(pOptions);
        for (int z = 0; z < routePoints.size(); z++)
        {
            LatLng point = routePoints.get(z);
            pOptions.add(point);
        }
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(current, 16);
        mMap.animateCamera(update);
        line = mMap.addPolyline(pOptions);
    }

    public double calculateDistance(LatLng current, LatLng prev)
    {
        double Radius=6371000;
        double lat1 = current.latitude;
        double lat2 = prev.latitude;
        double lon1 = current.longitude;
        double lon2 = prev.longitude;
        double dLat = Math.toRadians(lat2-lat1);
        double dLon = Math.toRadians(lon2-lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult= Radius*c;
        double km=valueResult/1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec =  Integer.valueOf(newFormat.format(km));
        double meter=valueResult%1000;
        int  meterInDec= Integer.valueOf(newFormat.format(meter));
        //Log.i("Radius Value",""+valueResult+"   KM  "+kmInDec+" Meter   "+meterInDec);
        return Radius*c;
    }

    private void saveData(GoogleMap mMap)
    {
        try
        {
            String latlangString = "1.Route:";
            for(ArrayList<LatLng> route : Utils.route)
            {
                for(int i=0;i<route.size();i++)
                {
                    LatLng l = route.get(i);
                    if(i == route.size()-1)
                    {
                        latlangString = latlangString+l.latitude+"::"+l.longitude;
                    }
                    else
                    {
                        latlangString = latlangString+l.latitude+"::"+l.longitude+",";
                    }
                }
                latlangString = latlangString+";";
            }

            latlangString = latlangString + "2.StartMarkers:";
            List<LatLng> startKeys = new ArrayList<LatLng>(Utils.startMarkers.keySet());
            for(int i=0;i< startKeys.size(); i++)
            {
                if(i == (startKeys.size()-1))
                {
                    latlangString = latlangString + startKeys.get(i).latitude+"::"+startKeys.get(i).longitude+"::"+Utils.startMarkers.get(startKeys.get(i));
                }
                else
                {
                    latlangString = latlangString + startKeys.get(i).latitude+"::"+startKeys.get(i).longitude+"::"+Utils.startMarkers.get(startKeys.get(i))+",";
                }
            }

            latlangString = latlangString + ";3.EndMarkers:";
            List<LatLng> endKeys = new ArrayList<LatLng>(Utils.endMarkers.keySet());
            for(int i=0;i< endKeys.size(); i++)
            {
                if(i == (endKeys.size()-1))
                {
                    latlangString = latlangString + endKeys.get(i).latitude+"::"+endKeys.get(i).longitude+"::"+Utils.endMarkers.get(endKeys.get(i));
                }
                else
                {
                    latlangString = latlangString + endKeys.get(i).latitude+"::"+endKeys.get(i).longitude+"::"+Utils.endMarkers.get(endKeys.get(i))+",";
                }
            }
            String fileName = DateFormat.getDateTimeInstance().format(new Date());
            String path = Environment.getExternalStorageDirectory() +"/TrackPath/";
            File file = new File(path, fileName+".txt");
            FileOutputStream  outputStream = new FileOutputStream(file);
            outputStream.write(latlangString.getBytes());
            outputStream.close();
            mMap.clear();
            Utils.reFresh();
        }
        catch (Exception e)
        {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder.setMessage(e.getLocalizedMessage());
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }

    public void addStartMarker(GoogleMap mMap, Location location)
    {
        try
        {
            String dt = getCurrentTimes();
            start = df.parse(dt);
            startMarkerData = "Heart Rate:" + heartRate;
            Utils.startMarkerData = startMarkerData;
            LatLng startPos = new LatLng(location.getLatitude(), location.getLongitude());
            Utils.startMarkers.put(startPos, startMarkerData);
            mMap.addMarker(new MarkerOptions()
                    .position(startPos)
                    .snippet(startMarkerData)
                    .title(dt));
            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker arg0) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {

                    LinearLayout info = new LinearLayout(context);
                    info.setOrientation(LinearLayout.VERTICAL);

                    TextView title = new TextView(context);
                    title.setTextColor(Color.BLACK);
                    title.setGravity(Gravity.CENTER);
                    title.setTypeface(null, Typeface.BOLD);
                    title.setText(marker.getTitle());

                    TextView snippet = new TextView(context);
                    snippet.setTextColor(Color.GRAY);
                    snippet.setText(marker.getSnippet());

                    info.addView(title);
                    info.addView(snippet);

                    return info;
                }
            });
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
    }

    public void addEndMarker(GoogleMap mMap, double distance)
    {
        try
        {
            DecimalFormat decimalFormat = new DecimalFormat("#.00");
            String dt = getCurrentTimes();
            stop = df.parse(dt);
            start = Utils.start;
            timeDiffer = stop.getTime() - start.getTime();
            long diffSeconds = timeDiffer / 1000 % 60;
            long diffMinutes = timeDiffer / (60 * 1000) % 60;
            //long diffHours = timeDiffer / (60 * 60 * 1000) % 24;
            double speed = distance/(diffMinutes*60+diffSeconds);

            endMarkerData = "Distance Travelled:-" + decimalFormat.format(distance) + "Mt." + ";\nTime Taken:-" + diffMinutes + "minutes and " + diffSeconds +" seconds" + ";\nSpeed:-" + decimalFormat.format(speed) +"m/s;\nHeart Rate:"+heartRate;
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
    }

    public String getEndMarkerData()
    {
        try
        {
            DecimalFormat decimalFormat = new DecimalFormat("#.00");
            String dt = getCurrentTimes();
            stop = df.parse(dt);
            start = Utils.start;
            distance = Utils.distance;
            timeDiffer = stop.getTime() - start.getTime();
            long diffSeconds = timeDiffer / 1000 % 60;
            long diffMinutes = timeDiffer / (60 * 1000) % 60;
            String calories = ""+(0.75*distance*0.00062137*Utils.weight);
            double speed = distance/(diffMinutes*60+diffSeconds);
            endMarkerData = "Distance Travelled:" + decimalFormat.format(distance) + "Mt." + ";\nTime Taken:" + diffMinutes + "minutes and " + diffSeconds +" seconds" + ";\nSpeed:" + decimalFormat.format(speed) +"m/s;\nHeart Rate:"+heartRate+";\nCalories Burned:"+calories+"KCal";
            return endMarkerData;
        }
        catch (ParseException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onBackPressed()
    {
        startActivity(new Intent(MapsActivity.this, BmiCalculator.class));
    }

    public void reDrawMap(GoogleMap mMap)
    {
        try
        {
            PolylineOptions pOptions = new PolylineOptions()
                    .width(24)
                    .color(Color.BLUE)
                    ;
            LatLng current = null;
            for(ArrayList<LatLng> routePointsNew : Utils.route)
            {
                for (int z = 0; z < routePointsNew.size(); z++)
                {
                    LatLng point = routePointsNew.get(z);
                    pOptions.add(point);
                    current = point;
                }
            }
            //ArrayList<LatLng> routePointsNew = Utils.routePoints;
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            mMap.addPolyline(pOptions);

            for(LatLng start : Utils.startMarkers.keySet())
            {
                mMap.addMarker(new MarkerOptions()
                        .position(start)
                        .title("Activity Summary:")
                        .snippet(Utils.startMarkers.get(start))
                );
            }

            for(LatLng end : Utils.endMarkers.keySet())
            {
                mMap.addMarker(new MarkerOptions()
                                .position(end)
                                .title("Activity Summary:")
                                .snippet(Utils.endMarkers.get(end))
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                );
            }

            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker arg0) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker)
                {
                    LinearLayout info = new LinearLayout(context);
                    info.setOrientation(LinearLayout.VERTICAL);

                    TextView title = new TextView(context);
                    title.setTextColor(Color.BLACK);
                    title.setGravity(Gravity.CENTER);
                    title.setTypeface(null, Typeface.BOLD);
                    title.setText(marker.getTitle());

                    TextView snippet = new TextView(context);
                    snippet.setTextColor(Color.GRAY);
                    snippet.setText(marker.getSnippet());

                    info.addView(title);
                    info.addView(snippet);

                    return info;
                }
            });

            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(current, 16);
            mMap.animateCamera(update);
            line = mMap.addPolyline(pOptions);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}