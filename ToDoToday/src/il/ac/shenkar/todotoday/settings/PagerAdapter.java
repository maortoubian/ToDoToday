package il.ac.shenkar.todotoday.settings;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
/*
 * this adapter holds the three HowTo fragments
 * and initialize the swipe gestures in needed 
 */
public class PagerAdapter extends FragmentPagerAdapter {

	public PagerAdapter(FragmentManager fm) {
		super(fm);		
	}

	@Override
	public Fragment getItem(int arg0) {
		switch (arg0) {
		case 0:		
			return new FragmentOne();
		case 1:
			return new FragmentTwo();
		case 2:
			return new FragmentThree();
		default:
			break;
		}
		return null;
	}

	@Override
	public int getCount() {
		return 3;
	}
}
