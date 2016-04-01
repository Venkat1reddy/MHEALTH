package com.maps.mhealth;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import java.util.List;

public class History extends FragmentActivity
{
    private GoogleMap mMap;
    private Polyline line;
    ListView list;
    Button editBtn;
    Button doneBtn;
    Button deleteBtn;
    Button renameBtn;
    Button backBtn;
    Button backToHistoryBtn;
    Button startTrackBtn;
    Button visualizeBtn;

    final Context context = this;
    ArrayAdapter<String> adapter;
    List<String> fileList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history);

        list = (ListView) findViewById(R.id.activityList);
        editBtn = (Button) findViewById(R.id.editBtn);
        doneBtn = (Button) findViewById(R.id.doneBtn);
        deleteBtn = (Button) findViewById(R.id.deleteBtn);
        renameBtn = (Button) findViewById(R.id.renameBtn);
        backBtn = (Button) findViewById(R.id.backToMainActBtn);

        doneBtn.setVisibility(View.INVISIBLE);
        deleteBtn.setVisibility(View.INVISIBLE);
        renameBtn.setVisibility(View.INVISIBLE);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3) {
                String value = (String) adapter.getItemAtPosition(position);
                reDrawMap(value);
            }
        });
        editBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                list.setOnItemClickListener(null);
                backBtn.setVisibility(View.INVISIBLE);
                doneBtn.setVisibility(View.VISIBLE);
                renameBtn.setVisibility(View.VISIBLE);
                deleteBtn.setVisibility(View.VISIBLE);
            }
        });
        doneBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                backBtn.setVisibility(View.VISIBLE);
                doneBtn.setVisibility(View.INVISIBLE);
                deleteBtn.setVisibility(View.INVISIBLE);
                renameBtn.setVisibility(View.INVISIBLE);
                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3) {
                        String value = (String) adapter.getItemAtPosition(position);
                        reDrawMap(value);
                    }
                });
            }
        });
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SparseBooleanArray checkedItemPositions = list.getCheckedItemPositions();
                int itemCount = list.getCount();
                for (int i = itemCount - 1; i >= 0; i--) {
                    if (checkedItemPositions.get(i)) {
                        deleteSourceFile(fileList.get(i));
                    }
                }
                populateListView(list);
            }
        });
        renameBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SparseBooleanArray checkedItemPositions = list.getCheckedItemPositions();
                int itemCount = list.getCount();
                for (int i = itemCount - 1; i >= 0; i--) {
                    if (checkedItemPositions.get(i)) {
                        showRenameDialog(fileList.get(i));
                    }
                }
                populateListView(list);
            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(History.this, MapsActivity.class));
            }
        });

        populateListView(list);
    }

    public void showRenameDialog(final String oldName)
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle("Rename");

        final EditText input = new EditText(context);
        input.setText(oldName);
        alert.setView(input);

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String newName = input.getEditableText().toString();
                renameSourceFile(oldName, newName);
                populateListView(list);
            }
        });

        alert.setNegativeButton("CANCEL",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alert.create();
        alertDialog.show();
    }

    public void deleteSourceFile(String name)
    {
        String path = Environment.getExternalStorageDirectory() +"/TrackPath/";
        File f = new File(path+name);
        f.delete();
    }

    public void renameSourceFile(String oldName, String newName)
    {
        String path = Environment.getExternalStorageDirectory() +"/TrackPath/";
        File f = new File(path+oldName);
        File f1 = new File(f.getParent(), newName);
        f.renameTo(f1);
    }

    public void populateListView(ListView list)
    {
        String path = Environment.getExternalStorageDirectory() +"/TrackPath/";
        File dir = new File(path);
        File file[] = dir.listFiles();
        fileList = new ArrayList<String>();
        for (File f: file)
        {
            fileList.add(f.getName());
        }
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, fileList);
        list.setAdapter(adapter);
    }

    public void reDrawMap(final String file)
    {
        setContentView(R.layout.history_maps);
        backToHistoryBtn = (Button) findViewById(R.id.btn1);
        startTrackBtn = (Button) findViewById(R.id.startTrackingBtn);
        visualizeBtn = (Button) findViewById(R.id.visualizeBtn);
        backToHistoryBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(History.this, History.class));
            }
        });
        startTrackBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(History.this, MapsActivity.class));
            }
        });

        visualizeBtn.setOnClickListener(new View.OnClickListener() {
            String fileName = file;
            public void onClick(View v) {
                visualizeData(fileName);
            }
        });

        try
        {
            String path = Environment.getExternalStorageDirectory() +"/TrackPath/";
            InputStreamReader InputRead= new InputStreamReader(new FileInputStream(new File(path, file)));
            char[] inputBuffer= new char[1000];
            String s="";
            int charRead;

            while ((charRead=InputRead.read(inputBuffer))>0) {
                // char to string conversion
                String readstring=String.copyValueOf(inputBuffer,0,charRead);
                s +=readstring;
            }
            InputRead.close();

            String routeString = s.split("1.Route:")[1].split("2.StartMarkers:")[0];
            String startMarkerString = s.split("2.StartMarkers:")[1].split(";")[0];
            String endMarkerString = s.split("3.EndMarkers:")[1];

            ArrayList<LatLng> routePointsNew = new ArrayList<LatLng>();

            String routePoints[] = routeString.split(";");

//            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
//            alertDialogBuilder.setMessage(routePoints.length);
//            AlertDialog alertDialog = alertDialogBuilder.create();
//            alertDialog.show();

            List<ArrayList<LatLng>> route = new ArrayList<ArrayList<LatLng>>();
            for(int i=0;i<routePoints.length;i++)
            {
                ArrayList<LatLng> routePoints1 = new ArrayList<LatLng>();
                String route1[] = routePoints[i].split(",");
                for(int j=0;j<route1.length;j++)
                {
                    double lat = Double.parseDouble(route1[j].split("::")[0]);
                    double lang = Double.parseDouble(route1[j].split("::")[1]);
                    LatLng l = new LatLng(lat,lang);
                    routePoints1.add(l);
                }
                route.add(routePoints1);
            }

            LatLng current = null;

            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
//            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
//            alertDialogBuilder.setMessage("=>"+route.size());
//            AlertDialog alertDialog = alertDialogBuilder.create();
//            alertDialog.show();
            PolylineOptions pOptions = new PolylineOptions()
                    .width(24)
                    .color(Color.BLUE)
                    ;
            for(ArrayList<LatLng> routePoints2 : route)
            {
                for (int z = 0; z < routePoints2.size(); z++)
                {
                    LatLng point = routePoints2.get(z);
                    pOptions.add(point);
                    current = point;
                }
            }
            mMap.addPolyline(pOptions);
            String startMarkers[] = startMarkerString.split(",");
            for(int i=0;i<startMarkers.length;i++)
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

            String endMarkers[] = endMarkerString.split(",");
            for(int i=0;i<endMarkers.length;i++)
            {
                double lat = Double.parseDouble(endMarkers[i].split("::")[0]);
                double lang = Double.parseDouble(endMarkers[i].split("::")[1]);
                LatLng l = new LatLng(lat,lang);
                String makerText = endMarkers[i].split("::")[2];
                mMap.addMarker(new MarkerOptions()
                        .position(l)
                        .title("Activity Summary:")
                        .snippet(makerText)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            }

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
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(current, 16);
            mMap.animateCamera(update);
            //line = mMap.addPolyline(pOptions);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void visualizeData(String fileName)
    {
        Intent i = new Intent(History.this, Charts.class);
        i.putExtra("FileName", fileName);
        startActivity(i);
    }
}