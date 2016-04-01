package com.maps.mhealth;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Utils
{
    public static float weight;
    public static Date start;
    public static GoogleMap mMap = null;
    public static LatLng prev;
    public static ArrayList<LatLng> routePoints;
    public static String startMarkerData;
    public static String endMarkerData;
    public static double distance;
    public static HashMap<LatLng, String> startMarkers = new HashMap<LatLng, String>();
    public static HashMap<LatLng, String> endMarkers = new HashMap<LatLng, String>();
    public static List<ArrayList<LatLng>> route = new ArrayList<ArrayList<LatLng>>();

    public static void reFresh()
    {
        start = null;
        mMap = null;
        prev = null;
        routePoints = null;
        startMarkerData = null;
        endMarkerData = null;
        distance = 0;
        route = null;
    }
}
