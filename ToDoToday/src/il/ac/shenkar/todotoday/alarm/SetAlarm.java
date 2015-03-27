package il.ac.shenkar.todotoday.alarm;

import il.ac.shenkar.todotoday.DBHelper;
import il.ac.shenkar.todotoday.EditTaskActivity;
import il.ac.shenkar.todotoday.R;
import il.ac.shenkar.todotoday.Task;
import il.ac.shenkar.todotoday.R.id;
import il.ac.shenkar.todotoday.R.layout;
import il.ac.shenkar.todotoday.R.menu;
import android.app.Activity;
import android.app.AlarmManager;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;
import android.provider.Settings;
import android.provider.Settings.System;
/*
 * this class holds the alarm configuration in the tasks
 * for the user to initiate alarm base notifications
 */
public class SetAlarm extends Activity {
	public final static String ALARM_HOUR = "alarm_hour";
	public final static String ALARM_MINUTE = "alarm_minute";
	public final static String ALARM_REPEAT_WEEKLY = "alarm_repeat_weekly";
	public final static String ALARM_TONE = "alarm_tone";
	public final static String ALARM_ENABLED = "alarm_enabled";
	public final static String ALARM_REPEATING_DAY = "alarm_repeating_day";

	public final static String EDIT_ALARM = "edit_alarm";
	final static String ALARM_SET = "alarm_set_ok";
	final static int TONE_PICK = 12;

	Task task;
	DBHelper db = null;

	String repeatingDay = null;
	boolean repeatWeekly = false;
	boolean[] repeatingDays = null;
	Uri tone = null;

	Button setAlarm, setTone;
	TimePicker timePicker;
	CheckBox checkBoxWeekly, checkDay0, checkDay1, checkDay2, checkDay3,
			checkDay4, checkDay5, checkDay6;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_set_alarm);

		repeatingDays = new boolean[Task.DAYS_IN_WEEK];
		for (int i = 0; i < Task.DAYS_IN_WEEK; ++i) {
			repeatingDays[i] = false;
		}
		repeatingDay = new String("");
		timePicker = (TimePicker) findViewById(R.id.timePicker);

		initCheckBoxes();
		CheckBoxListener listener = new CheckBoxListener();
		setCheckBoxListeners(listener);

		setTone = (Button) findViewById(R.id.setTone);
		setTone.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent(
						RingtoneManager.ACTION_RINGTONE_PICKER);
				startActivityForResult(intent, TONE_PICK);
			}
		});

		Intent intent = getIntent();
		boolean editMode = intent.getBooleanExtra(EDIT_ALARM, false);
		if (editMode) {
			int id = intent.getIntExtra(EditTaskActivity.TASK_ID, -1);
			if (id != -1) {
				db = new DBHelper(SetAlarm.this);
				task = db.getTask(id);

				timePicker.setCurrentHour(task.timeHour);
				timePicker.setCurrentMinute(task.timeMinute);
				checkBoxWeekly.setChecked(task.repeatWeekly);

				checkDay0.setChecked(task.getRepeatingDay(0));
				checkDay1.setChecked(task.getRepeatingDay(1));
				checkDay2.setChecked(task.getRepeatingDay(2));
				checkDay3.setChecked(task.getRepeatingDay(3));
				checkDay4.setChecked(task.getRepeatingDay(4));
				checkDay5.setChecked(task.getRepeatingDay(5));
				checkDay6.setChecked(task.getRepeatingDay(6));
				
				for (int i = 0; i < Task.DAYS_IN_WEEK; ++i) {
					repeatingDays[i] = task.getRepeatingDay(i);
				}
			}
		}

		setAlarm = (Button) findViewById(R.id.setAlarm);
		setAlarm.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				boolean allDaysNotSet = true;
				for (boolean repD : repeatingDays) {
					if (repD) {
						allDaysNotSet = false;
						break;
					}
				}
				if (allDaysNotSet) {
					Toast.makeText(SetAlarm.this, "Please choose at least one day!", Toast.LENGTH_LONG).show();
				} else {
					Intent resultIntent = new Intent();
					resultIntent.putExtra(ALARM_HOUR,
							timePicker.getCurrentHour());
					resultIntent.putExtra(ALARM_MINUTE,
							timePicker.getCurrentMinute());
					resultIntent.putExtra(ALARM_REPEAT_WEEKLY, repeatWeekly);
					// get default tone if not chosen
					if ((tone == null) || ((tone != null) & (tone.equals(""))))
						resultIntent.putExtra(ALARM_TONE, "default");
					else
						resultIntent.putExtra(ALARM_TONE, tone.toString());

					resultIntent.putExtra(ALARM_ENABLED, true);
					for (int i = 0; i < Task.DAYS_IN_WEEK; ++i) {
						repeatingDay += repeatingDays[i] + ",";
					}
					resultIntent.putExtra(ALARM_REPEATING_DAY, repeatingDay);
					setResult(Activity.RESULT_OK, resultIntent);
					finish();
				}
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			switch (requestCode) {
				case TONE_PICK: {
					tone = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
					break;
				}
				default: {
					break;
				}
			}
		}
	}

	//initiate the days for alarm to false
	private void initCheckBoxes() {
		checkDay0 = (CheckBox) findViewById(R.id.checkDay0);
		checkDay0.setChecked(false);
		checkDay1 = (CheckBox) findViewById(R.id.checkDay1);
		checkDay1.setChecked(false);
		checkDay2 = (CheckBox) findViewById(R.id.checkDay2);
		checkDay2.setChecked(false);
		checkDay3 = (CheckBox) findViewById(R.id.checkDay3);
		checkDay3.setChecked(false);
		checkDay4 = (CheckBox) findViewById(R.id.checkDay4);
		checkDay4.setChecked(false);
		checkDay5 = (CheckBox) findViewById(R.id.checkDay5);
		checkDay5.setChecked(false);
		checkDay6 = (CheckBox) findViewById(R.id.checkDay6);
		checkDay6.setChecked(false);
		checkBoxWeekly = (CheckBox) findViewById(R.id.checkBoxWeekly);
		checkBoxWeekly.setChecked(false);
	}
	
	//a click listener for the days in which the user pick
	private class CheckBoxListener implements OnClickListener {
		
		public void onClick(View v) {
			CheckBox checkDay = (CheckBox) findViewById(v.getId());
			switch (v.getId()) {
			case (R.id.checkDay0): {
				repeatingDays[0] = checkDay.isChecked();
				break;
			}
			case (R.id.checkDay1): {
				repeatingDays[1] = checkDay.isChecked();
				break;
			}
			case (R.id.checkDay2): {
				repeatingDays[2] = checkDay.isChecked();
				break;
			}
			case (R.id.checkDay3): {
				repeatingDays[3] = checkDay.isChecked();
				break;
			}
			case (R.id.checkDay4): {
				repeatingDays[4] = checkDay.isChecked();
				break;
			}
			case (R.id.checkDay5): {
				repeatingDays[5] = checkDay.isChecked();
				break;
			}
			case (R.id.checkDay6): {
				repeatingDays[6] = checkDay.isChecked();
				break;
			}
			case (R.id.checkBoxWeekly): {
				repeatWeekly = checkDay.isChecked();
				break;
			}
			}
		}
	};

	private void setCheckBoxListeners(CheckBoxListener listener) {
		checkBoxWeekly.setOnClickListener(listener);
		checkDay0.setOnClickListener(listener);
		checkDay1.setOnClickListener(listener);
		checkDay2.setOnClickListener(listener);
		checkDay3.setOnClickListener(listener);
		checkDay4.setOnClickListener(listener);
		checkDay5.setOnClickListener(listener);
		checkDay6.setOnClickListener(listener);
	}
}
