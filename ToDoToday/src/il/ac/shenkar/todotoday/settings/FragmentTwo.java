package il.ac.shenkar.todotoday.settings;

import il.ac.shenkar.todotoday.R;
import il.ac.shenkar.todotoday.R.layout;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
/*
 * this fragment holds the 2nd HowTo screen
 */
public class FragmentTwo extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		return inflater.inflate(R.layout.fragment_two, container,false);
	}
}
