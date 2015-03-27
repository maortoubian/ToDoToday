package il.ac.shenkar.todotoday.location;

import il.ac.shenkar.todotoday.R;
import il.ac.shenkar.todotoday.R.id;
import il.ac.shenkar.todotoday.R.layout;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
/*
 * this class hold the map in which the user will put a maker for the geofenbcing notifications
 */
public class GeoLocation extends FragmentActivity implements
OnMapLongClickListener,
LocationListener,
ConnectionCallbacks,
OnConnectionFailedListener,
ResultCallback<Status>{
	
	public final static String GEO_LAT = "geo_lat";
	public final static String GEO_LNG = "geo_lng";
	public final static String GEO_ENABLED = "geo_enabled";
	private static final int GPS_ERR = 9001;
	
	GoogleMap map;
	Location location;
	Button GO;
	Button fenceButton;
	EditText et;
	GoogleApiClient mApiClient ;
	SupportMapFragment mapFrag;
	LocationRequest mLocationRequest;
	LocationManager locationManager;
	Marker marker;
	PendingIntent mGeofencePendingIntent;
	double lat = 0.0;
	double lng = 0.0;
	int count = 0;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		
        super.onCreate(savedInstanceState);
        
        //LocationServices 
		mApiClient = new GoogleApiClient.Builder(getApplicationContext())
		.addApi(LocationServices.API)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .build();
		
        if(servicesOK()){
        	setContentView(R.layout.geolocation);
        	SupportMapFragment mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.googleMap);
            map = mapFrag.getMap();		
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            map.setMyLocationEnabled(true);
            
	        GO = (Button) findViewById(R.id.gobtn);
	        fenceButton = (Button) findViewById(R.id.geofencebtn);

	        location = getLocation();
	        
	        //zoom to the user current location
	        if (location != null)
	        { 
				map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));
		        CameraPosition cameraPosition = new CameraPosition.Builder()
		        .target(new LatLng(location.getLatitude(), location.getLongitude()))// Sets the center of the map to location user
		        .zoom(17)                   // Sets the zoom
		        .bearing(0)                	// Sets the orientation of the camera to north
		        .tilt(0)                   	// Sets the tilt of the camera to 0 degrees
		        .build();                   // Creates a CameraPosition from the builder
		        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
		        map.setOnMapLongClickListener(this); 	            
	        }
	        else{
	        	Toast.makeText(this, "No service available right now", Toast.LENGTH_LONG).show();
	        }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        mApiClient.connect();
    }
     
    //onlongclick add geofence maker on map
    public void onMapLongClick(LatLng point) {
    	if(count==0){
    		marker = map.addMarker(new MarkerOptions()
    		.draggable(true).title("geofence marker")
    		.position(new LatLng(point.latitude, point.longitude)));
    		lat = marker.getPosition().latitude;
    		lng = marker.getPosition().longitude;
    		count++;
    	}else{
    		marker.remove();
       		marker = map.addMarker(new MarkerOptions()
    		.draggable(true).title("geofence marker")
    		.position(new LatLng(point.latitude, point.longitude)));
    		lat = marker.getPosition().latitude;
    		lng = marker.getPosition().longitude;
    	}
    }
       
    //save the geofence data
    public void geofence(View v){
    	if(lat!=0.0 && lng !=0.0){
			Intent resultIntent = new Intent();
			resultIntent.putExtra(GEO_LAT,marker.getPosition().latitude);
			resultIntent.putExtra(GEO_LNG,marker.getPosition().longitude);
			resultIntent.putExtra(GEO_ENABLED,true);
			setResult(Activity.RESULT_OK, resultIntent);
			finish();
    	}
    	else{
			Toast.makeText(this, "No geofence marker was put",Toast.LENGTH_LONG).show();
    	}
    }
    
    //onConnection make the geofence and send it to the service
	public void onConnected(Bundle arg0) {
	    mLocationRequest = new LocationRequest();
	    mLocationRequest.setInterval(10000);
	    mLocationRequest.setFastestInterval(5000);
	    mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		LocationServices.FusedLocationApi.requestLocationUpdates(mApiClient, mLocationRequest, this);	
	}
	
	//check if google services is ok 
	public boolean servicesOK(){
		int isAv = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if(isAv == ConnectionResult.SUCCESS){
			return true;
		}
		else if(GooglePlayServicesUtil.isUserRecoverableError(isAv)){
			Dialog d = GooglePlayServicesUtil.getErrorDialog(isAv, this, GPS_ERR);
			d.show();
		}
		else{
			Toast.makeText(this, "cant connect to google play",Toast.LENGTH_LONG).show();
		}
		return false;
	}
	
	 //go the location that the user picked
	 public void gotoLocation(double lat,double lon){
	    	LatLng ll= new LatLng(lat,lon);
	    	CameraUpdate ud = CameraUpdateFactory.newLatLngZoom(ll, 17);
	    	map.animateCamera(ud);  	
	 }
	  
	  //get the location that the user picked
	  public void geoLocate(View v) throws IOException{
	    	hideKeys(v);
	    	et = (EditText) findViewById(R.id.searchAddress);
	    	String loc = et.getText().toString();  	
	    	Geocoder geocoder = new Geocoder(this);
	    	try {
	    	    List<Address> geoResults = geocoder.getFromLocationName(loc, 1);
	    	    while (geoResults.size()==0) {
	    	        geoResults = geocoder.getFromLocationName(loc, 1);
	    	    }
	    	    if (geoResults.size()>0) {
	    	        Address add = geoResults.get(0);
	    	    	double lat = add.getLatitude();
	    	    	double lon = add.getLongitude();
	    	    	gotoLocation(lat,lon);
	    	    }
	    	} catch (Exception e) {
	    	    System.out.print(e.getMessage());
	    	}
	    }
	    
	  	//hides the keyboard
	    private void hideKeys(View v){
	    	InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
	    	imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
	    }
	    
	    //checks which provider is available for showing the map (GPS || METWORK)
	    private Location getLocation(){   
	    	Location loc = null;
	    	Location locGps = null;
	        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	        
	        // getting GPS status
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

	        if(!isGPSEnabled && !isNetworkEnabled ){
	        	Toast.makeText(this, "no GPS or NETWORK connection", Toast.LENGTH_LONG); 	
	        }
	        else if(isGPSEnabled){
	        	locGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	        	if(locGps != null){
	        		loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	        	}
	        	else{
	        		loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	        	}
	        }
	        return loc;
	    }
	    
	    @Override
		protected void onPause() {
		    super.onPause();
		    stopLocationUpdates();
		}
	    
	    @Override
		public void onResume() {
		    super.onResume();
		    if (mApiClient.isConnected()) {
		    	LocationServices.FusedLocationApi.requestLocationUpdates(mApiClient, mLocationRequest, this);
		    }
		}
	    
	    public void onConnectionSuspended(int arg0) {
			// TODO Auto-generated method stub		
		}
	    
	    protected void stopLocationUpdates() {
		    LocationServices.FusedLocationApi.removeLocationUpdates(mApiClient, this);
		}

		public void onResult(Status arg0) {
			// TODO Auto-generated method stub		
		}
		public void onConnectionFailed(ConnectionResult arg0) {
			// TODO Auto-generated method stub	
		}
		
		public void onDisconnected() {
			// TODO Auto-generated method stub	
		}
		
		public void onLocationChanged(Location clocation) {
			location=clocation;
		}
}
