package il.ac.shenkar.todotoday;

import il.ac.shenkar.todotoday.alarm.AlarmManagerHelper;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
/*
 * this class shows the wanted task data in a task specific activity 
 */
public class SingleTaskActivity extends Activity {
	public final static String TASK_DELETED_OK 		= "deleted_ok";
	public final static int TASK_DELETED_OK_CODE 	= 2;
	public final static int EDIT_TASK 				= 3;
	
	private DBHelper  	db;
	private Button    	deleteTask;
	private Button    	okTask;
	private Button    	editTask;
	private TextView 	alarmStatus;
	private TextView 	locationStatus;
	private TextView  	title;
	private TextView  	description;
	private ImageView 	taskImage;
	private int 		taskId;
	private Task 		task;
	boolean[] 			repeatingDay;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_single_task);
		db = new DBHelper(this);
		
		Intent intent = getIntent();
	    taskId = Integer.parseInt(intent.getStringExtra(MainActivity.EXTRA_MESSAGE));
		task = db.getTask(taskId);
		
		repeatingDay = new boolean[Task.DAYS_IN_WEEK];
		for (int i=0; i<Task.DAYS_IN_WEEK; i++){
			repeatingDay[i]=false;
		}
		
		title = (TextView) findViewById(R.id.single_task_title);
		description = (TextView) findViewById(R.id.single_task_description);
		
		title.setText(task.getTitle());
		description.setText(task.getDescription());
		
		taskImage = (ImageView)findViewById(R.id.single_task_image);
		int imageId = task.getImageId();
		
		int bitmapId = getResources().getIdentifier("act"+imageId, "drawable", getPackageName());
		taskImage.setImageResource(bitmapId);
		
		
		//if deleteTask ask the user if he wants to delete this task 
		//if deletion wanted the task will be deleted from db
		//and the task row will be deleted
		deleteTask = (Button) findViewById(R.id.deleteTask);
		deleteTask.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				final String title = task.getTitle();
				AlertDialog.Builder bAlert = new AlertDialog.Builder(SingleTaskActivity.this);
				bAlert.setMessage("Are you sure to delete '" + title + "'?");
				bAlert.setCancelable(true);
				bAlert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int id) {
	                	dialog.cancel();
	                	deleteTask(title);
	                	
	                }
	            });
				bAlert.setNegativeButton("No", new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int id) {
	                    dialog.cancel();
	                }
	            });
	            AlertDialog alert = bAlert.create();
	            alert.show();
			}
		});
		
		//if editTask click edit task activity will be called will the specific task data
		editTask = (Button) findViewById(R.id.editTask);
		editTask.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent(SingleTaskActivity.this, EditTaskActivity.class);
				intent.putExtra(MainActivity.EXTRA_MESSAGE, Integer.toString(task.getId()));
		    	startActivityForResult(intent, EDIT_TASK);
			}
		});
		//id okTask click the main activity will be called 
		okTask = (Button) findViewById(R.id.okTask);
		okTask.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				finish();
			}
		});
		
		//if alarm notification is on the alarm data will be shown
		String alarm;
		if (task.alarmIsEnabled) {
			alarm = "Alarm on\n " + String.format("%02d : %02d", task.timeHour, task.timeMinute);
			if (task.repeatWeekly) alarm += " every";
			else alarm += " this";
			if (task.getRepeatingDay(0)) alarm += " Sunday";
			if (task.getRepeatingDay(1)) alarm += " Monday";
			if (task.getRepeatingDay(2)) alarm += " Tuesday";
			if (task.getRepeatingDay(3)) alarm += " Wednesday";
			if (task.getRepeatingDay(4)) alarm += " Thursday";
			if (task.getRepeatingDay(5)) alarm += " Friday";
			if (task.getRepeatingDay(6)) alarm += " Saturday";
		} else alarm = "Alarm off";
		
		alarmStatus = (TextView) findViewById(R.id.alarmStatus);
		alarmStatus.setText(alarm);
		
		//if geolocation notification is on the show "geofence on"
		locationStatus = (TextView)findViewById(R.id.locationStatus);
		if(task.locationIsEnabled){		
			locationStatus.setText("geofence on");
		} else locationStatus.setText("geofence off");
		
	}
	
	protected void onResume() {
		super.onResume();
		
		//called when resume from alarm notification making
		String alarm;
		if (task.alarmIsEnabled) {
			alarm = "Alarm on\n " + String.format("%02d : %02d", task.timeHour, task.timeMinute);
			if (task.repeatWeekly) alarm += " every";
			else alarm += " this";
			if (task.getRepeatingDay(0)) alarm += " Sunday";
			if (task.getRepeatingDay(1)) alarm += " Monday";
			if (task.getRepeatingDay(2)) alarm += " Tuesday";
			if (task.getRepeatingDay(3)) alarm += " Wednesday";
			if (task.getRepeatingDay(4)) alarm += " Thursday";
			if (task.getRepeatingDay(5)) alarm += " Friday";
			if (task.getRepeatingDay(6)) alarm += " Saturday";
		} else alarm = "Alarm off";
		alarmStatus = (TextView) findViewById(R.id.alarmStatus);
		alarmStatus.setText(alarm);
		
		//called when resume from geolocation notification making
		if (task.locationIsEnabled ){
			locationStatus = (TextView) findViewById(R.id.locationStatus);
			locationStatus.setText("geofence on");
		}
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	//the function that will delete the task if user ask for deletion
	public void deleteTask(String title) {
		db.deleteTask(task);
		AlarmManagerHelper.setAlarms(SingleTaskActivity.this);
		Intent resultIntent = new Intent();
		resultIntent.putExtra(TASK_DELETED_OK, TASK_DELETED_OK_CODE);
		setResult(Activity.RESULT_OK, resultIntent);
		Toast.makeText(SingleTaskActivity.this, "'" + title + "'" + " deleted from task list!", Toast.LENGTH_LONG).show();
		finish();
	}
	
	@Override 
	public void onActivityResult(int requestCode, int resultCode, Intent data) {     
		super.onActivityResult(requestCode, resultCode, data); 
		switch(requestCode) { 
			
	    	case (EDIT_TASK) : { 
	    		if (resultCode == Activity.RESULT_OK) { 
	    			int tabIndex = data.getIntExtra(EditTaskActivity.TASK_EDITED_OK, EditTaskActivity.TASK_EDITED_OK_CODE);
	    			//recreate the view
	    			task = db.getTask(taskId);
	    			title.setText(task.getTitle());
	    			description.setText(task.getDescription());
	    			int imageId = task.getImageId();
	    			int bitmapId = getResources().getIdentifier("act"+imageId, "drawable", getPackageName());
	    			taskImage.setImageResource(bitmapId);
	    			if(!task.locationIsEnabled){
	    				locationStatus.setText("geofence off");
	    			}
	    			if(!task.alarmIsEnabled){
	    				alarmStatus.setText("alarm off");
	    			}
	    		} 
	    		break; 
	    	} 
		} 
	}
}
