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
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationServices;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
/*
 * this class lets the user edit wanted task with all the given options:
 * edit task title 
 * edit task description
 * edit notification by alarm and location 
 */
public class EditTaskActivity extends Activity implements ConnectionCallbacks,
OnConnectionFailedListener, ResultCallback<Status> {
	
	public final static String TASK_EDITED_OK 	= "edited_ok";
	public final static int TASK_EDITED_OK_CODE = 3;
	public final static int CHANGE_ICON 		= 5;
	public final static int ALARM_SET 			= 10;
	public final static int LOCATION_SET 		= 100;
	public final static String TASK_ID 			= "task_id";
	private static final int GET_LOCATION 		= 100;
	
	private EditText 	editTitle;
	private EditText	editDescription;
	private Button 		saveEdit;
	private Button 		editAlarm;
	private Button 		editLocation;
	private ImageView 	editImage;
	private int 		imageId;
	
	private Task task;
	
	private boolean 	locationIsEnabled  = false;
	private double		lat = 0.0;
	private double 		lng = 0.0;
	private int 		taskId;
	
	private SimpleGeofence	geofence;
	private DBHelper db;
	
	List<Geofence> mGeofenceList;
	private PendingIntent mGeofenceRequestIntent;
	private GoogleApiClient mApiClient;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_task);
		
		mApiClient = new GoogleApiClient.Builder(getApplicationContext())
		.addApi(LocationServices.API)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .build();
		
		mGeofenceList = new ArrayList<Geofence>();
		
		db = new DBHelper(this);
		Intent intent = getIntent();
	    taskId = Integer.parseInt(intent.getStringExtra(MainActivity.EXTRA_MESSAGE));
		task = db.getTask(taskId);
		
		editTitle = (EditText) findViewById(R.id.editTitle);
		editTitle.setText(task.getTitle());
		
		editTitle.setOnKeyListener(new OnKeyListener() {
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
		editDescription = (EditText) findViewById(R.id.editDescription);
		editDescription.setText(task.getDescription());
		
		// If the event is a key-down event on the
		//"enter" button the keyboard will disappear
		editDescription.setOnKeyListener(new OnKeyListener() {
		    public boolean onKey(View v, int keyCode, KeyEvent event) {

		        if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
		            (keyCode == KeyEvent.KEYCODE_ENTER)) {
			    	InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);

			    	imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

		          return true;
		        }
		        return false;
		    }
		});
		
		imageId = task.getImageId();
		editImage = (ImageView) findViewById(R.id.editImage);
		int bitmapId = getResources().getIdentifier("act"+imageId, "drawable", getPackageName());
		editImage.setImageResource(bitmapId);
		
		//if task icon clicked call the icons activity
		editImage.setOnClickListener(new OnClickListener(){
			
			public void onClick(View v) {
				Intent intent = new Intent(EditTaskActivity.this, ChooseImageActivity.class);
		    	startActivityForResult(intent, CHANGE_ICON);
			}
		});
		
		//if saveEdit button clicked all the current data of the task will be saved
		saveEdit = (Button) findViewById(R.id.saveEdit);
		saveEdit.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				String title = editTitle.getText().toString();
				if(!title.equals("")) {
					String description = editDescription.getText().toString(); 
					task.setTitle(title);
					task.setDescription(description);
					task.setImageId(imageId);
					
					//add geofencing if needed
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

					db = new DBHelper(EditTaskActivity.this);
					db.updateTask(task);
					AlarmManagerHelper.setAlarms(EditTaskActivity.this);
					Toast.makeText(EditTaskActivity.this, "'" + title + "'" + " saved in task list!", Toast.LENGTH_LONG).show();
					Intent resultIntent = new Intent();
					resultIntent.putExtra(TASK_EDITED_OK, TASK_EDITED_OK_CODE);
					setResult(Activity.RESULT_OK, resultIntent);
					finish();
				} else {
					Toast.makeText(EditTaskActivity.this, "Task title can't be empty!", Toast.LENGTH_LONG).show();
				}
			}
		});
		
		//if editAlarm longclick : offers the user to delete
		//the alarm notification if the alarm is set
		//if editAlarm click : call the alarm screen
		editAlarm = (Button) findViewById(R.id.editAlarm);
		boolean alarmOn=task.alarmIsEnabled();	
		
		if(alarmOn){
			editAlarm.setBackgroundResource(R.color.green);
			
			editAlarm.setOnLongClickListener(new OnLongClickListener() {
				
			public boolean onLongClick(View v) {
				
				AlertDialog.Builder bAlert = new AlertDialog.Builder(EditTaskActivity.this);
				bAlert.setMessage("Are you sure to cancel this notification?");
				bAlert.setCancelable(true);
				bAlert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int id) {
	                	dialog.cancel();
						DBHelper db = new DBHelper(EditTaskActivity.this);
						
						task.alarmIsEnabled = false;
						db.updateTask(task);
						editAlarm.setBackgroundResource(R.color.blue);
						AlarmManagerHelper.deleteAlarms(EditTaskActivity.this);
	                }
	            });
				bAlert.setNegativeButton("No", new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int id) {
	                    dialog.cancel();
	                }
	            });
	            AlertDialog alert = bAlert.create();
	            alert.show();
	            return true;
				}
			});			
		}
		
		editAlarm.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent(EditTaskActivity.this, SetAlarm.class);
				
				DBHelper db = new DBHelper(EditTaskActivity.this);
				
				task.done = false;
				db.updateTask(task);
				AlarmManagerHelper.setAlarms(EditTaskActivity.this);
				
				intent.putExtra(SetAlarm.EDIT_ALARM, true);
				intent.putExtra(TASK_ID, task.id);
		    	startActivityForResult(intent, ALARM_SET);
			}
		});	

		//if editLocation longclick : offers the user to delete
		//the location notification if the location is set
		//if editLocation click : call the geolocation screen
		editLocation = (Button) findViewById(R.id.geobtn);
		boolean locationOn=task.locationIsEnabled();	
		if(locationOn){
			editLocation.setBackgroundResource(R.color.green);

			editLocation.setOnLongClickListener(new OnLongClickListener() {
				
			public boolean onLongClick(View v) {
				
				AlertDialog.Builder bAlert = new AlertDialog.Builder(EditTaskActivity.this);
				bAlert.setMessage("Are you sure to cancel this notification?");
				bAlert.setCancelable(true);
				bAlert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int id) {
	                	dialog.cancel();
						DBHelper db = new DBHelper(EditTaskActivity.this);
	
						task.locationIsEnabled = false;
						
						mApiClient.connect();
						
						db.updateTask(task);
						editLocation.setBackgroundResource(R.color.blue);

	                }
	            });
				bAlert.setNegativeButton("No", new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int id) {
	                    dialog.cancel();
	                }
	            });
	            AlertDialog alert = bAlert.create();
	            alert.show();
	            return true;
				}
			});	
		}
		
		editLocation.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent(EditTaskActivity.this, GeoLocation.class);
				
				DBHelper db = new DBHelper(EditTaskActivity.this);
				
				task.done = false;
				db.updateTask(task);
				AlarmManagerHelper.setAlarms(EditTaskActivity.this);
				startActivityForResult(intent, LOCATION_SET);
			}
		});
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {     
		super.onActivityResult(requestCode, resultCode, data); 
		switch(requestCode) { 
			//this called when the user resume from the icons screen
	    	case (CHANGE_ICON) : { 
	    		if (resultCode == Activity.RESULT_OK) { 
	    			imageId = data.getIntExtra(ChooseImageActivity.IMAGE_CHOSEN_OK, 1) + 1;	    			
	    			int bitmapId = getResources().getIdentifier("act"+imageId, "drawable", getPackageName());
	    			editImage.setImageResource(bitmapId);
	    		} 
	    		break; 
	    	}
	    	//this called when the user resume with the geolocation data
	    	case (GET_LOCATION) : { 
	    		if (resultCode == Activity.RESULT_OK) { 
	    			lat = data.getDoubleExtra(GeoLocation.GEO_LAT,0);
	    			lng = data.getDoubleExtra(GeoLocation.GEO_LNG,0);	
	    			task.locationIsEnabled = data.getBooleanExtra(GeoLocation.GEO_ENABLED, false);
	    		} 
	    		break; 
	    	}
	    	//this called when the user resume with the alarm data
	    	case (ALARM_SET) : { 
	    		if (resultCode == Activity.RESULT_OK) {
	    			Toast.makeText(getApplicationContext(),"Alarm set!", Toast.LENGTH_SHORT).show();
	    			
		    		task.timeHour = data.getIntExtra(SetAlarm.ALARM_HOUR, 0);
		    		task.timeMinute = data.getIntExtra(SetAlarm.ALARM_MINUTE, 0);
		    		task.repeatWeekly = data.getBooleanExtra(SetAlarm.ALARM_REPEAT_WEEKLY, false);
		    		task.alarmTone = Uri.parse(data.getStringExtra(SetAlarm.ALARM_TONE));
		    		task.alarmIsEnabled = data.getBooleanExtra(SetAlarm.ALARM_ENABLED, false);
		    		
		    		String repeatingDays = data.getStringExtra(SetAlarm.ALARM_REPEATING_DAY);
		    		String repDay[] = repeatingDays.split(",");
		    		
		    		for (int i=0; i<Task.DAYS_IN_WEEK; i++) {
		    			if (repDay[i].equals("true")) task.setRepeatingDay(i, true);
		    			else task.setRepeatingDay(i, false);
		    		}	   
		    		
	    		}
	    		break;
	    	}
		} 
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		//if alarm is set the alarm button will turn green
		if (task.alarmIsEnabled) {
			editAlarm.setBackgroundResource(R.color.green);
		}
		//if geolocation is set the geolocation button will turn green
		if(task.locationIsEnabled){
			editLocation.setBackgroundResource(R.color.green);
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
		
		if(task.locationIsEnabled){
			LocationServices.GeofencingApi.addGeofences(mApiClient, mGeofenceList,mGeofenceRequestIntent).setResultCallback(this);
		}
		else{
			LocationServices.GeofencingApi.removeGeofences(mApiClient,mGeofenceRequestIntent).setResultCallback(this);
		}
	}
	
	@Override
	public void onStop() {
		if(mApiClient.isConnected()){
			mApiClient.disconnect();
		}
	    super.onStop();
	}
	
	public void onResult(Status arg0) {
		// TODO Auto-generated method stub	
	}

	public void onConnectionFailed(ConnectionResult result) {
		// TODO Auto-generated method stub	
	}

	public void onConnectionSuspended(int cause) {
		// TODO Auto-generated method stub	
	}
}
