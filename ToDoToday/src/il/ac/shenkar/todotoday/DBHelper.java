package il.ac.shenkar.todotoday;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
/*
 * this class manage the app db
 * holds all the saved task the contents of each task
 * and some db management functions like remove,find,add
 */
public class DBHelper extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION 	= 25;
	private static final String DATABASE_NAME 	= "toDoTasksDB";
	
	private Context context;
	
	private static final String TABLE_TASKS 	= "tasks";
	private static final String ID 				= "id";
	private static final String TITLE 			= "title";
	private static final String DESCRIPTION 	= "description";
	private static final String IMAGE_ID 		= "image_id";
	private static final String TIME_HOUR 		= "time_hour";
	private static final String TIME_MINUTE 	= "time_minute";
	private static final String REPEAT_DAYS 	= "repeat_days";
	private static final String REPEAT_WEEKLY 	= "repeat_weekly";
	private static final String ALARM_TONE 		= "alarm_tone";
	private static final String ALARM_ENABLED 	= "alarm_enabled";
	private static final String LOCATION_ENABLED = "location_enabled";
	private static final String TASK_DONE 		= "done";
	
	private static final String CREATE_TASK_TABLE = "CREATE TABLE " + TABLE_TASKS 
									+ "("
            						+ ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            						+ TITLE + " TEXT, " 							
            						+ DESCRIPTION + " TEXT, "
            						+ IMAGE_ID + " INTEGER, "
            						+ TIME_HOUR + " INTEGER,"
            						+ TIME_MINUTE + " INTEGER, "
            						+ REPEAT_DAYS + " TEXT, " 
            						+ REPEAT_WEEKLY + " BOOLEAN, " 
            						+ ALARM_TONE + " TEXT, "
            						+ ALARM_ENABLED + " BOOLEAN, "
            						+ LOCATION_ENABLED + " BOOLEAN, "
            						+ TASK_DONE + " BOOLEAN"
            						+ ")";
	
	public DBHelper(Context ctx) {
		super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = ctx;
	}
	
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TASK_TABLE);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        onCreate(db);
    }
    //add task to db
    public long addTask(Task task) { 	
    	SQLiteDatabase db = this.getWritableDatabase();
    	ContentValues values = new ContentValues();
    	values.put(TITLE, task.getTitle());
    	values.put(DESCRIPTION, task.getDescription());
    	values.put(IMAGE_ID, task.getImageId());
    	values.put(TIME_HOUR, task.timeHour);
   		values.put(TIME_MINUTE, task.timeMinute);
   		values.put(ALARM_TONE, task.alarmTone != null ? task.alarmTone.toString() : "");
        String repeatingDays = "";
        for (int i = 0; i < 7; ++i) {
        	repeatingDays += task.getRepeatingDay(i) + ",";
        }
        values.put(REPEAT_DAYS, repeatingDays);
		values.put(REPEAT_WEEKLY, task.repeatWeekly); 
		values.put(ALARM_ENABLED, task.alarmIsEnabled);
		values.put(LOCATION_ENABLED, task.locationIsEnabled);
		values.put(TASK_DONE, task.done);
    	long rowId = db.insert(TABLE_TASKS, null, values);
    	db.close();
    	return rowId;
    }
    
    //return wanted task from db
    public Task getTask(int id) {
		SQLiteDatabase db = this.getWritableDatabase();
		Task task = new Task();
    	
    	Cursor cursor = db.query(TABLE_TASKS, new String[] { 
    			ID, TITLE, DESCRIPTION, IMAGE_ID, TIME_HOUR, TIME_MINUTE,
    			REPEAT_DAYS, REPEAT_WEEKLY, ALARM_TONE, ALARM_ENABLED,LOCATION_ENABLED, TASK_DONE
    				}, ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
    	if (cursor != null)	cursor.moveToFirst();
    	
    	task.id = (int) cursor.getLong(cursor.getColumnIndex(ID));
		task.title = cursor.getString(cursor.getColumnIndex(TITLE));
		task.description = cursor.getString(cursor.getColumnIndex(DESCRIPTION));
		task.imageId = cursor.getInt(cursor.getColumnIndex(IMAGE_ID));
		task.timeHour = cursor.getInt(cursor.getColumnIndex(TIME_HOUR));
		task.timeMinute = cursor.getInt(cursor.getColumnIndex(TIME_MINUTE));
		task.repeatWeekly = cursor.getInt(cursor.getColumnIndex(REPEAT_WEEKLY)) == 0 ? false : true;
		task.alarmTone = cursor.getString(cursor.getColumnIndex(ALARM_TONE)) != "" ? Uri.parse(cursor.getString(cursor.getColumnIndex(ALARM_TONE))) : null;
		task.alarmIsEnabled = cursor.getInt(cursor.getColumnIndex(ALARM_ENABLED)) == 0 ? false : true;
		task.locationIsEnabled = cursor.getInt(cursor.getColumnIndex(LOCATION_ENABLED)) == 0 ? false : true;
		task.done = cursor.getInt(cursor.getColumnIndex(TASK_DONE))== 0 ? false : true;
		
		
		String[] repeatingDays = cursor.getString(cursor.getColumnIndex(REPEAT_DAYS)).split(",");
		for (int i = 0; i < repeatingDays.length; ++i) {
			task.setRepeatingDay(i, repeatingDays[i].equals("false") ? false : true);
		}
    	
    	db.close();
    	return task;
    }
    
    //return the wanted task title
    public String getTaskTitle(String id) {
		SQLiteDatabase db = this.getWritableDatabase();
		String title;
    	Cursor cursor = db.query(TABLE_TASKS, new String[] { 
    			ID, TITLE, DESCRIPTION, IMAGE_ID, TIME_HOUR, TIME_MINUTE,
    			REPEAT_DAYS, REPEAT_WEEKLY, ALARM_TONE, ALARM_ENABLED,
    			LOCATION_ENABLED,TASK_DONE
    				}, ID + "=?",
                new String[] { id }, null, null, null, null);
    	if (cursor != null)	cursor.moveToFirst();
    	
		title = cursor.getString(cursor.getColumnIndex(TITLE));
    	
    	db.close();
    	return title;
    }
    
    //return all db tasks
    public List<Task> getAllTasks() {
    	List<Task> taskList = new ArrayList<Task>();
    	String selectQuery = "SELECT  * FROM " + TABLE_TASKS;
    	
    	SQLiteDatabase db = this.getWritableDatabase();
    	Cursor cursor = db.rawQuery(selectQuery, null);
    	
    	if (cursor.moveToFirst()) {
    		do {
    			Task task = new Task();
    			task.id = (int) cursor.getLong(cursor.getColumnIndex(ID));
    			task.title = cursor.getString(cursor.getColumnIndex(TITLE));
    			task.description = cursor.getString(cursor.getColumnIndex(DESCRIPTION));
    			task.imageId = cursor.getInt(cursor.getColumnIndex(IMAGE_ID));
    			task.timeHour = cursor.getInt(cursor.getColumnIndex(TIME_HOUR));
    			task.timeMinute = cursor.getInt(cursor.getColumnIndex(TIME_MINUTE));
    			task.repeatWeekly = cursor.getInt(cursor.getColumnIndex(REPEAT_WEEKLY)) == 0 ? false : true;
    			task.alarmTone = cursor.getString(cursor.getColumnIndex(ALARM_TONE)) != "" ? Uri.parse(cursor.getString(cursor.getColumnIndex(ALARM_TONE))) : null;
    			task.alarmIsEnabled = cursor.getInt(cursor.getColumnIndex(ALARM_ENABLED)) == 0 ? false : true;
    			task.locationIsEnabled = cursor.getInt(cursor.getColumnIndex(LOCATION_ENABLED)) == 0 ? false : true;
    			task.done = cursor.getInt(cursor.getColumnIndex(TASK_DONE)) == 0 ? false : true;
    			
    			String[] repeatingDays = cursor.getString(cursor.getColumnIndex(REPEAT_DAYS)).split(",");
    			for (int i = 0; i < repeatingDays.length; ++i) {
    				task.setRepeatingDay(i, repeatingDays[i].equals("false") ? false : true);
    			}    			
    			taskList.add(task);
    		} while(cursor.moveToNext());
    	}
    	db.close();
    	return taskList;
    }
    
    //returns the tasks number in the db
    public int getTasksCount() {
    	String countQuery = "SELECT  * FROM " + TABLE_TASKS;
    	SQLiteDatabase db = this.getReadableDatabase();
    	Cursor cursor = db.rawQuery(countQuery, null);
    	cursor.close();
    	return cursor.getCount();
    }
    
    // Updating single task in db
    public int updateTask(Task task) {
    	SQLiteDatabase db = this.getWritableDatabase();
    	 
        ContentValues values = new ContentValues();
        values.put(TITLE, task.getTitle());
    	values.put(DESCRIPTION, task.getDescription());
    	values.put(IMAGE_ID, task.getImageId());
    	values.put(TIME_HOUR, task.timeHour);
   		values.put(TIME_MINUTE, task.timeMinute);
   		values.put(ALARM_TONE, task.alarmTone != null ? task.alarmTone.toString() : "");
        String repeatingDays = "";
        for (int i = 0; i < 7; ++i) {
        	repeatingDays += task.getRepeatingDay(i) + ",";
        }
        values.put(REPEAT_DAYS, repeatingDays);
		values.put(REPEAT_WEEKLY, task.repeatWeekly); 
		values.put(ALARM_ENABLED, task.alarmIsEnabled);
		values.put(LOCATION_ENABLED, task.locationIsEnabled);
		values.put(TASK_DONE, task.done);

        return db.update(TABLE_TASKS, values, ID + " = ?",
                new String[] { String.valueOf(task.getId()) });
    }
     
    // Deleting single task from db
    public void deleteTask(Task task) {
    	SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASKS, ID + " = ?",
                new String[] { String.valueOf(task.getId()) });
        db.close();
    }
}
