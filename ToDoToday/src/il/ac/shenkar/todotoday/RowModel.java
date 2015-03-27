package il.ac.shenkar.todotoday;
/*
 * this class holds all of the task row information
 * 
 */
public class RowModel {
	private int id;
	private String title;
	private String note;
	private boolean done;
	private int imageId;
	private boolean alarm;
	private boolean location;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public boolean getAlarm() {
		return alarm;
	}
	public void setAlarm(boolean alarm) {
		this.alarm = alarm;
	}
	public boolean getLocation() {
		return location;
	}
	public void setLocation(boolean location) {
		this.location = location;
	}
	public int getImageId() {
		return imageId;
	}
	public void setImageId(int imageId) {
		this.imageId = imageId;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public boolean isDone() {
		return done;
	}
	public void setDone(boolean done) {
		this.done = done;
	}
	
}
