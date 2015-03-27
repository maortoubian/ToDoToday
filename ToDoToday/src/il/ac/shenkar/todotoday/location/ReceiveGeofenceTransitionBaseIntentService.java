package il.ac.shenkar.todotoday.location;
import static il.ac.shenkar.todotoday.Constants.CONNECTION_TIME_OUT_MS;
import static il.ac.shenkar.todotoday.Constants.GEOFENCE_DATA_ITEM_PATH;
import static il.ac.shenkar.todotoday.Constants.GEOFENCE_DATA_ITEM_URI;
import static il.ac.shenkar.todotoday.Constants.KEY_GEOFENCE_ID;
import static il.ac.shenkar.todotoday.Constants.TAG;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.concurrent.TimeUnit;

/*
 * this class Listens for geofence transition changes.
 */
public abstract class ReceiveGeofenceTransitionBaseIntentService extends
		IntentService {

	/**
	 * Sets an identifier for this class' background thread
	 */
	public ReceiveGeofenceTransitionBaseIntentService() {
		super("ReceiveGeofenceTransitionBaseIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		GeofencingEvent event = GeofencingEvent.fromIntent(intent);
		if (event != null) {

			if (event.hasError()) {
				onError(event.getErrorCode());
			} else {
				int transition = event.getGeofenceTransition();
				if (transition == Geofence.GEOFENCE_TRANSITION_ENTER
						|| transition == Geofence.GEOFENCE_TRANSITION_DWELL
						|| transition == Geofence.GEOFENCE_TRANSITION_EXIT) {
					String[] geofenceIds = new String[event.getTriggeringGeofences().size()];
					for (int index = 0; index < event.getTriggeringGeofences().size(); index++) {
						geofenceIds[index] = event.getTriggeringGeofences().get(index).getRequestId();
					}

					if (transition == Geofence.GEOFENCE_TRANSITION_ENTER
							|| transition == Geofence.GEOFENCE_TRANSITION_DWELL) {
						onEnteredGeofences(geofenceIds);
					} else if (transition == Geofence.GEOFENCE_TRANSITION_EXIT) {
						onExitedGeofences(geofenceIds);
					}
				}
			}

		}
	}

	protected abstract void onEnteredGeofences(String[] geofenceIds);

	protected abstract void onExitedGeofences(String[] geofenceIds);

	protected abstract void onError(int errorCode);
}
