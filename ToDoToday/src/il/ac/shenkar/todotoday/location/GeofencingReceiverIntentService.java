package il.ac.shenkar.todotoday.location;

import il.ac.shenkar.todotoday.DBHelper;
import il.ac.shenkar.todotoday.R;
import il.ac.shenkar.todotoday.Task;
import il.ac.shenkar.todotoday.R.drawable;

import java.util.List;

import android.R.string;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
/*
 * this is the geolocation receiver derived that
 * will pop a notification by the specific 
 * task that will be activated 
 */
public class GeofencingReceiverIntentService extends
		ReceiveGeofenceTransitionBaseIntentService {
	
	DBHelper db = new DBHelper(this);
	private NotificationManager notificationManager;

	@Override
	protected void onEnteredGeofences(String[] geoIds) {
		noteGeofence(geoIds,"just entered the area!");
	}

	@Override
	protected void onExitedGeofences(String[] geoIds) {
			noteGeofence(geoIds,"just exited the area!");
	}

	@Override
	protected void onError(int i) {
		Log.e(GeofencingReceiverIntentService.class.getName(), "Error: " + i);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	}

	private void CreateNotification(int id,String text) {

		Intent intent = new Intent(this, NotificationReceiver.class);
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

		// build notification
		// the addAction re-use the same intent to keep the example short
		Notification n = new Notification.Builder(this)
				.setContentTitle("ToDoToday notification").setContentText(text)
				.setSmallIcon(R.drawable.ic_launcher).setContentIntent(pIntent)
				.setAutoCancel(true).build();
		notificationManager.notify(id, n);
	}
	
	//sends the received massage for the specific task to the phone notifications
	public void noteGeofence(String[] geoIds,String state){
		for (int index = 0; index < geoIds.length; index++) {
			
			Task t = db.getTask(Integer.parseInt(geoIds[index]));
			
			if(t!=null){

				if(String.valueOf(t.getId()).equals(geoIds[index])){
					Log.d(GeofencingReceiverIntentService.class.getName(),"Task: " + t.getTitle());
					CreateNotification(t.getId(),"Task: ''" + t.getTitle() +"'' "+ state);				
				}
			}

		}
	}
}
