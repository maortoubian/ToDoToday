package il.ac.shenkar.todotoday;

import android.R.bool;
import android.net.Uri;
/*
 * this class holds the task information
 */
public class Task {
	public static final int DAYS_IN_WEEK = 7;
	public static final int SUNDAY = 0;
	public static final int MONDAY = 1;
	public static final int TUESDAY = 2;
	public static final int WEDNESDAY = 3;
	public static final int THURSDAY = 4;
	public static final int FRIDAY = 5;
	public static final int SATURDAY = 6;
	
	public int				id; 		//public long id = -1;
	public String			title; 		//public String name;
	public String			description;
	public int				imageId;
	public boolean			done;
	
	//alarm information
	public int 		timeHour;
	public int		timeMinute;
	private boolean repeatingDays[];
	public boolean 	repeatWeekly;
	public Uri	 	alarmTone;
	public boolean	alarmIsEnabled;
	//location information
	public boolean	locationIsEnabled;

	
	public Task()	{
		repeatingDays = new boolean[DAYS_IN_WEEK];
	}

	public Task(String title, String description, int imageId) {
		repeatingDays = new boolean[DAYS_IN_WEEK];
		this.title = title;
		this.description = description;
		this.imageId = imageId;
	}

	public Task(int id, String title, String description, int imageId) {
		repeatingDays = new boolean[DAYS_IN_WEEK];
		this.id = id;
		this.title = title;
		this.description = description;
		this.imageId = imageId;
	}

	public void setRepeatingDay(int dayOfWeek, boolean value) {
		repeatingDays[dayOfWeek] = value;
	}
	
	public boolean getRepeatingDay(int dayOfWeek) {
		return repeatingDays[dayOfWeek];
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getImageId() {
		return imageId;
	}
	public boolean alarmIsEnabled(){
		return alarmIsEnabled;
	}
	public boolean locationIsEnabled(){
		return locationIsEnabled;
	}
	public void setImageId(int imageId) {
		this.imageId = imageId;
	}
	
	@Override
	public String toString() {
		return "Task [id=" + id + ", title=" + title + ", description="
				+ description + "]";
	}
	
}
