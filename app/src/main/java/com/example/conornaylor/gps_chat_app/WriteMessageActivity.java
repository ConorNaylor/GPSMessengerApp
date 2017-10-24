package com.example.conornaylor.gps_chat_app;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class WriteMessageActivity extends AppCompatActivity implements LocationListener {

    private EditText text;
    private Button upload;
    private String message, serverMessage;
    private LocationData locationData;
    private FirebaseDatabase db;
    private DatabaseReference myRef;
    private double d1, d2;
    private boolean uniqueLoc = true;
    private String lt = "lat";
    private String lg = "lng";
    private Location currentLocation = new Location("");
    private Location serverLocation = new Location("");
    private LocationManager lm;
    private boolean locRead = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_message);

        startGettingLocations();

        text = (EditText) findViewById(R.id.message);
        upload = (Button) findViewById(R.id.upload);

        db = FirebaseDatabase.getInstance();
        myRef = db.getReference("Messages");

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message = text.getText().toString();

                if(locRead){
                    Toast.makeText(WriteMessageActivity.this, "Location Read: " + locationData,
                            Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(WriteMessageActivity.this, "Location hasn't been read!",
                            Toast.LENGTH_LONG).show();
                }

                if (uniqueLoc){
                    myRef.child(message).setValue(locationData);

                } else {
                    myRef.child(serverMessage + "   " + message).setValue(locationData);
                    myRef.child(serverMessage).removeValue();
                }

                Intent homeIntent = new Intent(WriteMessageActivity.this, HomeActivity.class);
                startActivity(homeIntent);
            }

        });
    }

    @Override
    public void onLocationChanged(Location location) {
        locationData = new LocationData(location.getLatitude(), location.getLongitude());

        Toast.makeText(WriteMessageActivity.this, "Location Received",
                Toast.LENGTH_LONG).show();

        locRead = true;

        currentLocation.setLatitude(locationData.lat);
        currentLocation.setLongitude(locationData.lng);

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {

                    d1 = new Double(child.child(lt).getValue().toString());
                    d2 = new Double(child.child(lg).getValue().toString());

                    serverLocation.setLatitude(d1);
                    serverLocation.setLongitude(d2);

                    if(currentLocation.distanceTo(serverLocation) <= 10){
                        uniqueLoc = false;
                        serverMessage = child.getKey();
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.out.println("Couldn't read database");
            }
        });
                lm.removeUpdates(this);
    }


    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    private void startGettingLocations() {

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isGPS = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetwork = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean canGetLocation = true;
        int ALL_PERMISSIONS_RESULT = 101;
        long MIN_DISTANCE_CHANGE_FOR_UPDATES = 100;// Distance in meters
        long MIN_TIME_BW_UPDATES = 1000 * 60;// Time in milliseconds

        ArrayList<String> permissions = new ArrayList<>();
        ArrayList<String> permissionsToRequest;

        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissionsToRequest = findUnAskedPermissions(permissions);


        //Check if GPS and Network are on, if not asks the user to turn on
        if (!isGPS && !isNetwork) {
            showSettingsAlert();
        } else {
            // check permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (permissionsToRequest.size() > 0) {
                    requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]),
                            ALL_PERMISSIONS_RESULT);
                    canGetLocation = false;
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this, "Permission not Granted", Toast.LENGTH_SHORT).show();

            return;
        }

        //Starts requesting location updates
        if (canGetLocation) {
            if (isNetwork) {
                // from Network Provider
                Toast.makeText(WriteMessageActivity.this, "Reading location from Network",
                        Toast.LENGTH_LONG).show();
                lm.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
            }
            else if (isGPS) {
                Toast.makeText(WriteMessageActivity.this, "Reading location from GPS",
                        Toast.LENGTH_LONG).show();
                lm.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

            }
        }
        else{
            Toast.makeText(this, "Can't get location", Toast.LENGTH_SHORT).show();
        }
    }

    private ArrayList findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList result = new ArrayList();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }
    private boolean hasPermission(String permission) {
        if (canAskPermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }
    private boolean canAskPermission() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("GPS is not Enabled!");
        alertDialog.setMessage("Do you want to turn on GPS?");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }

    public void onBackPressed(){
        finish();
    }
}
