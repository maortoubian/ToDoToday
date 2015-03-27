package il.ac.shenkar.todotoday;

import il.ac.shenkar.todotoday.settings.AboutActivity;
import il.ac.shenkar.todotoday.settings.HowToActivity;
import il.ac.shenkar.todotoday.widget.StackWidgetProvider;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;



import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
/*
 * this class is the main activity that initiate the google analytics
 * and holds the main list of the tasks
 */
public class MainActivity extends Activity {

	public final static String EXTRA_MESSAGE 	= "il.ac.shenkar.todotoday.MESSAGE";
	public final static int CREATE_TASK 		= 1;
	public final static int VIEW_TASK 			= 2;
	
	public  MainActivity 		CustomListView = null;
    public  ArrayList<RowModel> CustomListViewValuesArr = new ArrayList<RowModel>();

	private Button 		addTask;
	private ListView 	list;
	private ListAdapter adapter;
    
	//first use for how to screen
	public static final String FIRST_USE = "first_use";
	public Tracker t;
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//how to to do today screen
		final SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
		final SharedPreferences.Editor editor = sharedPref.edit();
		if (sharedPref.getBoolean(FIRST_USE, true)) {
			editor.putBoolean(FIRST_USE, false);
			editor.commit();
			Intent intent = new Intent(MainActivity.this, HowToActivity.class);
			startActivity(intent);
		}	

		
		//get google analytics tracker
		Tracker tracker = ((todotoday) getApplication()).getTracker(todotoday.TrackerName.GLOBAL_TRACKER);

		tracker.send(new HitBuilders.ScreenViewBuilder().build());
		

		CustomListView = this;
		DBHelper db = new DBHelper(this);

		setAdapterFromArray(db);
		addTask = (Button) findViewById(R.id.addTask);
		addTask.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, CreateTaskActivity.class);
				startActivityForResult(intent, CREATE_TASK);
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

		return true;
	}
	
	//open the wanted task
    public void onItemClick(int mPosition)
    {
    	if (mPosition==-2) {
    		Intent intent = new Intent(MainActivity.this, CreateTaskActivity.class);
			startActivityForResult(intent, CREATE_TASK);
    	} else {
    		RowModel tempRow = (RowModel) CustomListViewValuesArr.get(mPosition);
    		Intent intent = new Intent(this, SingleTaskActivity.class);
    		intent.putExtra(EXTRA_MESSAGE, Integer.toString(tempRow.getId()));
    		startActivityForResult(intent, VIEW_TASK);
    	}
    }
    
    //if the done box is checked then all the notification will be canceled 
    public void onItemCheck(int mPosition, boolean checked) {
    	RowModel tempRow = (RowModel) CustomListViewValuesArr.get(mPosition);
    	DBHelper db = new DBHelper(this);
    	Task task = db.getTask(tempRow.getId());
    	task.done = checked;
    	
    	if (checked) {
    		tempRow.setDone(true);
    		tempRow.setAlarm(false);
    		tempRow.setLocation(false);		
    		task.alarmIsEnabled = false;
    		task.locationIsEnabled = false;
	
    		Toast.makeText(this, "Task marked as done, notifications will not work",Toast.LENGTH_LONG).show();	
    	} 
    	else {  
    		tempRow.setDone(false);
    		Toast.makeText(this, "Task marked as not done, but no alarm was set!",Toast.LENGTH_LONG).show();	
    	}
    	db.updateTask(task);
    	
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_howto) {
			Intent intent = new Intent(MainActivity.this, HowToActivity.class);
			startActivity(intent);
			return true;
		}
		if (id == R.id.action_about) {
			Intent intent = new Intent(MainActivity.this, AboutActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	//initiate all the tasks from the db to the list of tasks
	public void setAdapterFromArray(DBHelper db){
		List<Task> tasks = db.getAllTasks(); 
		
		for (Task t : tasks) {
            final RowModel row = new RowModel();  
            row.setId(t.id);
            row.setTitle(t.title);
            row.setDone(t.done);
            row.setImageId(t.imageId);
            row.setNote(t.description); 
            row.setAlarm(t.alarmIsEnabled);
            row.setLocation(t.locationIsEnabled);
            CustomListViewValuesArr.add(row);
        }
		
        Resources res = getResources();
        list = (ListView)findViewById(R.id.list);

        adapter = new ListAdapter(CustomListView, CustomListViewValuesArr, res);
        list.setAdapter(adapter);
	}
	
	
	@Override 
	public void onActivityResult(int requestCode, int resultCode, Intent data) {     
		super.onActivityResult(requestCode, resultCode, data); 
		switch(requestCode) { 
	    	case (CREATE_TASK) : { 
	    		if (resultCode == Activity.RESULT_OK) { 
	    			int tabIndex = data.getIntExtra(CreateTaskActivity.TASK_CREATED_OK, CreateTaskActivity.TASK_CREATED_OK_CODE);
	    		} 
	    		break; 
	    	} 
	    	case (VIEW_TASK) : { 
	    		if (resultCode == Activity.RESULT_OK) { 
	    			int tabIndex = data.getIntExtra(SingleTaskActivity.TASK_DELETED_OK, SingleTaskActivity.TASK_DELETED_OK_CODE);
	    		} 
	    		break; 
	    	} 
		} 
	}
	
	@Override
	public void onResume(){
		super.onResume();

		//recreate the list
		CustomListViewValuesArr = new ArrayList<RowModel>();
		DBHelper db = new DBHelper(this);
		setAdapterFromArray(db);
		
		
		Intent intent = new Intent(this,StackWidgetProvider.class);
		intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		// Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
		// since it seems the onUpdate() is only fired on that:
		int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), StackWidgetProvider.class));
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,ids);
		sendBroadcast(intent);
		
		updateAllWidgets();
	}

	//call widget to update if needed
	private void updateAllWidgets(){	
	    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
	    int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, StackWidgetProvider.class));
	    if (appWidgetIds.length > 0) {
	        new StackWidgetProvider().onUpdate(this, appWidgetManager, appWidgetIds);
	    }
	}
	
	@Override
	protected void onStart() {
		super.onStart();
	//	GoogleAnalytics.getInstance(this).reportActivityStart(this);

	}	
	@Override
	protected void onStop() {
		super.onStop();
		//GoogleAnalytics.getInstance(this).reportActivityStop(this);
	}	
	@Override
	protected void onDestroy() {
	    super.onDestroy();
	}
}
