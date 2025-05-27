package com.example.emsismartpresence;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapsFragment extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private static final int LOCATION_PERMISSION_REQUEST_CODE=1;
    private LocationRequest locationRequest;
    private Marker userMarker;
    private boolean firstUpdate ;

    private Polyline routePolyline;

    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_map);
        fusedLocationClient= LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment=(SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragmentmap);
        assert mapFragment!=null;
        mapFragment.getMapAsync(this);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap=googleMap;
        setupLocationUpdates();
        LatLng emsiO = new LatLng(33.5922, -7.6328);
        Marker marker =mMap.addMarker(new MarkerOptions().position(emsiO).title("Emsi les Oranges"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(emsiO,10));
        marker.setTag("destination");
        LatLng emsiC = new LatLng(33.5954, -7.6168);
        Marker marker1 =mMap.addMarker(new MarkerOptions().position(emsiC).title("Emsi Centre"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(emsiC,10));
        marker1.setTag("destination");
        LatLng emsiM = new LatLng(33.5897, -7.6185);
        Marker marker2 =mMap.addMarker(new MarkerOptions().position(emsiM).title("Emsi Moulay Youssef"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(emsiM,10));
        marker2.setTag("destination");

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                if (!"destination".equals(marker.getTag())) return false;

                if (userMarker != null) {
                    LatLng userPos = userMarker.getPosition();
                    LatLng destinationPos = marker.getPosition();

                    if (routePolyline != null) routePolyline.remove(); // Clear old line

                    routePolyline = mMap.addPolyline(new PolylineOptions()
                            .add(userPos, destinationPos)
                            .color(Color.BLUE)
                            .width(8));
                }
                return false; // Allow default behavior (show info window)
            }
        });

    }

    private void setupLocationUpdates(){
        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION )!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;}

// Création de la LocationRequest
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000); // Mise à jour toutes les 5 secondes
        locationRequest.setFastestInterval(2000); // Minimum 2 secondes si une autre app demande
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
// Callback pour recevoir les mises à jour de position
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                for (Location location : locationResult.getLocations()) {
                    updateLocationOnMap(location);
                }
            }
        };



// Démarrer la localisation
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,null);

    }

    private void updateLocationOnMap(Location location){
        LatLng userLocation =new LatLng(location.getLatitude(),
                location.getLongitude());
        if(userMarker==null){
            userMarker = mMap.addMarker(new
                    MarkerOptions().position(userLocation).title("You are here"));
            animateCamera(userLocation, 15f);
        } else {
            // Déplacer le marker existant
            userMarker.setPosition(userLocation);
            if (firstUpdate) {
                animateCamera(userLocation, 15f);
                firstUpdate = false;
            } else {
                animateCamera(userLocation, mMap.getCameraPosition().zoom);
            }
        }
    }
    private void animateCamera(LatLng target, float zoomLevel) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(target)
                .zoom(zoomLevel) // Orientation nord
                .bearing(0)      // Orientation nord
                .tilt(45f)       // Inclinaison de la caméra pour un effet 3D
                .build();

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupLocationUpdates();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}