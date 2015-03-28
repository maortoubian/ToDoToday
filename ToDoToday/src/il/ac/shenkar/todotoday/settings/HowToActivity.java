package il.ac.shenkar.todotoday.settings;

import il.ac.shenkar.todotoday.R;
import il.ac.shenkar.todotoday.R.id;
import il.ac.shenkar.todotoday.R.layout;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
/*
 * this class holds the three  HowTo fragments
 */
public class HowToActivity extends FragmentActivity {
	ViewPager viewpager;
	Button gotIt;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_how_to);
		viewpager = (ViewPager) findViewById(R.id.pager);
		gotIt = (Button) findViewById(R.id.finishHowTo);
		gotIt.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				finish();
			}
		});
		PagerAdapter padapter = new PagerAdapter(getSupportFragmentManager());
		viewpager.setAdapter(padapter);
		
	}
}
