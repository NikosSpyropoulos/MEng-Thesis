package com.example.findit_ocrarthesis.location;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

public class LocationMatcher {

    private Geocoder geocoder;
    private String strAddress;
    private LatLng p1;

    public LocationMatcher(Geocoder geocoder, String strAddress) {
        this.geocoder = geocoder;
        this.strAddress = strAddress;
        this.p1 = getLocation();
    }

    private LatLng getLocation() {
        List<Address> address;
        LatLng p1 = null;
        try {
            // May throw an IOException
            address = geocoder.getFromLocationName(strAddress, 5);
            if (address == null) {
                Log.d("ADDRESS", "ADDRESS IS NULL");
            }
            Address location = address.get(0);
            p1 = new LatLng(location.getLatitude(), location.getLongitude() );
            Log.d("OPA", "Latitude: " + location.getLatitude() + " Longtitude: " + location.getLongitude());

        } catch (IOException ex) {

            ex.printStackTrace();
        }
        return  p1;
    }

    public float getDistanceFrom(Location current_location) {
        float[] distance = new float[1];
        Location.distanceBetween(p1.latitude, p1.longitude, current_location.getLatitude(), current_location.getLongitude(), distance);

        return distance[0];
    }
}
