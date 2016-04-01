package com.maps.mhealth;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MapsHistory extends FragmentActivity
{
    private GoogleMap mMap;
    private Polyline line;

    Button backtoHistoryBtn;

    final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_maps);

        backtoHistoryBtn = (Button) findViewById(R.id.btn1);

        String file = this.getIntent().getStringExtra("file");
        reDrawMap(file);
    }

    public void reDrawMap(String file)
    {
        setContentView(R.layout.history_maps);
        try
        {
            String path = Environment.getExternalStorageDirectory() +"/TrackPath/";
            InputStreamReader InputRead= new InputStreamReader(new FileInputStream(new File(path, file)));
            char[] inputBuffer= new char[1000];
            String s="";
            int charRead;

            while ((charRead=InputRead.read(inputBuffer))>0)
            {
                String readstring=String.copyValueOf(inputBuffer,0,charRead);
                s +=readstring;
            }
            InputRead.close();

            String routeString = s.split(";")[0];
            String startMarkerString = s.split(";")[1];
            String endMarkerString = s.split(";")[1];

            ArrayList<LatLng> routePointsNew = new ArrayList<LatLng>();
            String routePoints[] = routeString.split("Route:")[1].split(",");

            for(int i=0;i<routePoints.length-1;i++)
            {

                double lat = Double.parseDouble(routePoints[i].split("::")[0]);
                double lang = Double.parseDouble(routePoints[i].split("::")[1]);
                LatLng l = new LatLng(lat,lang);
                routePointsNew.add(l);
            }

            PolylineOptions pOptions = new PolylineOptions()
                    .width(24)
                    .color(Color.BLUE)
                    ;
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

            mMap.addPolyline(pOptions);
            LatLng current = null;
            for (int z = 0; z < routePointsNew.size(); z++) {
                LatLng point = routePointsNew.get(z);
                pOptions.add(point);
                current = point;
            }

            String startMarkers[] = startMarkerString.split("StartMarkers")[1].split(",");
            for(int i=0;i<startMarkers.length-1;i++)
            {
                double lat = Double.parseDouble(startMarkers[i].split("::")[0]);
                double lang = Double.parseDouble(startMarkers[i].split("::")[1]);
                LatLng l = new LatLng(lat,lang);
                String makerText = startMarkers[i].split("::")[2];
                mMap.addMarker(new MarkerOptions()
                        .position(l)
                        .title("Activity Summary:")
                        .snippet(makerText));
            }

            String endMarkers[] = endMarkerString.split("EndMarkers")[1].split(",");
            for(int i=0;i<endMarkers.length-1;i++)
            {
                double lat = Double.parseDouble(endMarkers[i].split("::")[0]);
                double lang = Double.parseDouble(endMarkers[i].split("::")[1]);
                LatLng l = new LatLng(lat,lang);
                String makerText = endMarkers[i].split("::")[2];
                mMap.addMarker(new MarkerOptions()
                        .position(l)
                        .title("Activity Summary:")
                        .snippet(makerText));
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
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder.setMessage(e.getLocalizedMessage());
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }
}
