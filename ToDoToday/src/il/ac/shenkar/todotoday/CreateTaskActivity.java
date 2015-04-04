package il.ac.shenkar.todotoday;


import il.ac.shenkar.todotoday.alarm.AlarmManagerHelper;
import il.ac.shenkar.todotoday.alarm.SetAlarm;
import il.ac.shenkar.todotoday.location.GeoLocation;
import il.ac.shenkar.todotoday.location.GeofencingReceiverIntentService;
import il.ac.shenkar.todotoday.location.SimpleGeofence;

import java.util.ArrayList;
import java.util.List;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationServices;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
/*
 * this class lets the user create new task with all the given options:
 * make task title 
 * make task description
 * make notification by alarm and location 
 */
public class CreateTaskActivity extends Activity implements ConnectionCallbacks,
OnConnectionFailedListener, ResultCallback<Status> {
	
	public final static String TASK_CREATED_OK		= "created_ok";
	public final static int TASK_CREATED_OK_CODE 	= 1;
	public final static int GET_ICON 				= 4;
	public final static int GET_ALARM 				= 9;
	public final static int GET_LOCATION			= 100;
	
	private EditText 	createTitle;
	private EditText 	createDescription;
	private Button 		createTask;
	private int 		imageId = 1;
	private ImageView 	createImage;
	
	private Button 		geobtn;
	private Button 		createAlarm;
	private int 		timeHour = 0;
	private int 		timeMinute = 0;
	private boolean 	repeatWeekly = false;
	private Uri			alarmTone = null;
	private boolean 	alarmIsEnabled  = false;
	private boolean 	repeatingDay[];
	
	private boolean 	locationIsEnabled  = false;
	private double lat = 0.0;
	private double lng = 0.0;
	private long taskId;
	private SimpleGeofence	geofence;
	
	List<Geofence> mGeofenceList;
	private PendingIntent mGeofenceRequestIntent;
	private GoogleApiClient mApiClient;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_task);	
		
		//connecting to googleapiclient
		mApiClient = new GoogleApiClient.Builder(getApplicationContext())
		.addApi(LocationServices.API)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .build();
		
		
		repeatingDay = new boolean[Task.DAYS_IN_WEEK];
		for (int i=0; i<Task.DAYS_IN_WEEK; i++){
			repeatingDay[i]=false;
		}
		
		mGeofenceList = new ArrayList<Geofence>();
		
		geobtn = (Button) findViewById(R.id.geobtn);
		geobtn.setOnClickListener(new OnClickListener() {	
			public void onClick(View v) {
				Intent intent = new Intent(CreateTaskActivity.this, GeoLocation.class);
		    	startActivityForResult(intent, GET_LOCATION);
			}
		});
		
		createAlarm = (Button) findViewById(R.id.createAlarm);
		createAlarm.setOnClickListener(new OnClickListener() {		
			public void onClick(View v) {
				Intent intent = new Intent(CreateTaskActivity.this, SetAlarm.class);
				intent.putExtra(SetAlarm.EDIT_ALARM, false);
		    	startActivityForResult(intent, GET_ALARM);
			}
		});
		
		createTitle = (EditText) findViewById(R.id.createTitle);
		
		createTitle.setOnKeyListener(new OnKeyListener() {
		    public boolean onKey(View v, int keyCode, KeyEvent event) {
		        // If the event is a key-down event on the "enter" button
		        if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
		            (keyCode == KeyEvent.KEYCODE_ENTER)) {
			    	InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);

			    	imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

		          return true;
		        }
		        return false;
		    }
		});
		
		createDescription = (EditText) findViewById(R.id.createDescription);
		
		createDescription.setOnKeyListener(new OnKeyListener() {
		    public boolean onKey(View v, int keyCode, KeyEvent event) {
		        // If the event is a key-down event on the "enter" button
		        if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
		            (keyCode == KeyEvent.KEYCODE_ENTER)) {
			    	InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);

			    	imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

		          return true;
		        }
		        return false;
		    }
		});
		
		createTask = (Button) findViewById(R.id.createTask);
		createTask.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				String title = createTitle.getText().toString();
				if(!title.equals("")) {
					Task task = new Task();
					task.title = title;
					task.description = createDescription.getText().toString();
					task.imageId = imageId;
					task.done = false;
					
					task.timeHour = timeHour;
					task.timeMinute = timeMinute;
					task.repeatWeekly = repeatWeekly;
					task.alarmTone = alarmTone;
					task.alarmIsEnabled = alarmIsEnabled;
					task.locationIsEnabled = locationIsEnabled;
					
					
					for (int i=0; i<Task.DAYS_IN_WEEK; i++) {
						if (repeatingDay[i]) { 
							task.setRepeatingDay(i, true);
						} else {
							task.setRepeatingDay(i, false);
						}
					}				
					DBHelper db = new DBHelper(CreateTaskActivity.this);
					taskId = db.addTask(task);
					
					//add geofencing to service if needed
					if(lat!=0 && lng!=0){

						geofence = new SimpleGeofence(
								 		String.valueOf(taskId),
								 		lat,
								 		lng,
								 		80.0f,
								 		Geofence.NEVER_EXPIRE,
								 		Geofence.GEOFENCE_TRANSITION_ENTER|
								 		Geofence.GEOFENCE_TRANSITION_EXIT);
						 
						 mGeofenceList.add(geofence.toGeofence());
						 mApiClient.connect();
					}				
					
					Toast.makeText(CreateTaskActivity.this, "'" + title + "'" + " saved in task list!", Toast.LENGTH_LONG).show();

					AlarmManagerHelper.setAlarms(CreateTaskActivity.this);
					
					Intent resultIntent = new Intent();
					resultIntent.putExtra(TASK_CREATED_OK, TASK_CREATED_OK_CODE);
					setResult(Activity.RESULT_OK, resultIntent);
					finish();
					
				} else {
					Toast.makeText(CreateTaskActivity.this, "Task title can't be empty!", Toast.LENGTH_LONG).show();
				}
			}
		});
		
		createImage = (ImageView) findViewById(R.id.createImage);
		createImage.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent(CreateTaskActivity.this, ChooseImageActivity.class);
		    	startActivityForResult(intent, GET_ICON);
			}			
		});
		
	}
    @Override
    protected void onStart() {
        super.onStart();
    }
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {     
		super.onActivityResult(requestCode, resultCode, data); 
		switch(requestCode) { 
			//called when user return from changing task icon
	    	case (GET_ICON) : { 
	    		if (resultCode == Activity.RESULT_OK) { 
	    			imageId = data.getIntExtra(ChooseImageActivity.IMAGE_CHOSEN_OK, 1) + 1;  			
	    			int bitmapId = getResources().getIdentifier("act"+imageId, "drawable", getPackageName());
	    			createImage.setImageResource(bitmapId);
	    		} 
	    		break; 
	    	} 
	    	//called when user return from making a geofence notification
	    	case (GET_LOCATION) : { 
	    		if (resultCode == Activity.RESULT_OK) { 
	    			lat = data.getDoubleExtra(GeoLocation.GEO_LAT,0);
	    			lng = data.getDoubleExtra(GeoLocation.GEO_LNG,0);
	    			locationIsEnabled = data.getBooleanExtra(GeoLocation.GEO_ENABLED, false);
	    		} 
	    		break; 
	    	} 
	    	//called when user return making alarm notification
	    	case (GET_ALARM) : { 
	    		if (resultCode == Activity.RESULT_OK) { 
	    			timeHour = data.getIntExtra(SetAlarm.ALARM_HOUR, 0);
	    			timeMinute = data.getIntExtra(SetAlarm.ALARM_MINUTE, 0);
	    			repeatWeekly = data.getBooleanExtra(SetAlarm.ALARM_REPEAT_WEEKLY, false);
	    			alarmTone = Uri.parse(data.getStringExtra(SetAlarm.ALARM_TONE));
	    			alarmIsEnabled = data.getBooleanExtra(SetAlarm.ALARM_ENABLED, false);
	    			String repeatingDays = data.getStringExtra(SetAlarm.ALARM_REPEATING_DAY);
	    			String repDay[] = repeatingDays.split(",");
	    			for (int i=0; i<Task.DAYS_IN_WEEK; i++) {
	    				if (repDay[i].equals("true")) repeatingDay[i]=true;
	    				else repeatingDay[i]=false;
	    			}	    			
	    		} 
	    		break; 
	    	} 
		} 
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		//if alarm is activated change the button color
		if (alarmIsEnabled) {
			createAlarm.setBackgroundResource(R.color.green);
		}
		//if location is activated change the button color
		if(locationIsEnabled){
			geobtn.setBackgroundResource(R.color.green);
		}
	}
	
	//creating pending intent for geolocation future notification
	private PendingIntent getGeofenceTransitionPendingIntent() {
		Intent intent = new Intent(this, GeofencingReceiverIntentService.class);
		return PendingIntent.getService(this, (int) taskId, intent,PendingIntent.FLAG_UPDATE_CURRENT);
	}
	
	//after client connected,sends the geolocation pending intent to the service 
	public void onConnected(Bundle arg0) {
		mGeofenceRequestIntent = getGeofenceTransitionPendingIntent();
		LocationServices.GeofencingApi.addGeofences(mApiClient, mGeofenceList,mGeofenceRequestIntent).setResultCallback(this);
	}
	
	@Override
	public void onStop() {
	    mApiClient.disconnect();
	    super.onStop();
	}

	public void onConnectionSuspended(int cause) {
		// TODO Auto-generated method stub	
	}

	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub	
	}
	public void onResult(Status result) {
		// TODO Auto-generated method stub	
	}
	
}
