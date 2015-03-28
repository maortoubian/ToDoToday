package il.ac.shenkar.todotoday;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
/*
 * this adapter holds the list of all the tasks in the main activity
 */
public class ListAdapter extends BaseAdapter implements OnClickListener {

	private Activity activity;
    private ArrayList data;
    private static LayoutInflater inflater=null;
    public Resources res;
    RowModel tempValues=null;
    int i=0;
    
    public ListAdapter(Activity activity, ArrayList data,Resources resLocal) {
		this.activity = activity;
		this.data = data;
		this.res = resLocal;
		inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
    
    public int getCount() {
        if(data.size()<=0) return 1;
        return data.size();
    }
    
    public Object getItem(int position) {
        return position;
    }
 
    public long getItemId(int position) {
        return position;
    }
    
    //inner class that holds the tasks info
    public static class ViewHolder{
        public TextView title;
        public TextView note;
        public ImageView image;
    	public ImageView alarm;
    	public ImageView location;
        public CheckBox done;
    }
    
    //returns the wanted task view
	public View getView(int position, View convertView, ViewGroup parent) {
		View vi = convertView;		
		ViewHolder holder;
		
		if (convertView == null) {
			vi = inflater.inflate(R.layout.single_task_row, null);
			holder = new ViewHolder();
			holder.title = (TextView) vi.findViewById(R.id.title);
			holder.note = (TextView) vi.findViewById(R.id.note);
			holder.image = (ImageView) vi.findViewById(R.id.image);
			holder.alarm = (ImageView) vi.findViewById(R.id.alarmSign);
			holder.location = (ImageView) vi.findViewById(R.id.locationSign);
			holder.done = (CheckBox) vi.findViewById(R.id.doneChkBx);
			vi.setTag(holder);
		} else
			holder = (ViewHolder) vi.getTag();

		if (data.size() <= 0) {
			holder.title.setText("Press to add new records!");
			holder.done.setVisibility(CheckBox.INVISIBLE);
			holder.note.setVisibility(TextView.INVISIBLE);
			holder.image.setImageResource(res.getIdentifier("com.androidexample.customlistview:drawable/act1",null,null));
			vi.setOnClickListener(new OnItemClickListener(-2));
		} else {
			tempValues = null;
			tempValues = (RowModel) data.get(position);
			holder.title.setText(tempValues.getTitle());
			holder.note.setText(tempValues.getNote());
			holder.done.setChecked(tempValues.isDone());
			if(tempValues.isDone()){
				holder.alarm.setImageResource(R.drawable.blank);
				holder.location.setImageResource(R.drawable.blank);
			}
			if(!tempValues.isDone()){
				if(tempValues.getAlarm()){
					holder.alarm.setImageResource(R.drawable.alarmon);
				}
				else{holder.alarm.setImageResource(R.drawable.blank);}

				if(tempValues.getLocation()){
					holder.location.setImageResource(R.drawable.locationon);
				}
				else{holder.location.setImageResource(R.drawable.blank);}
			}
			final int pos = position;
			final CheckBox h = holder.done;
			final ImageView ivAlarm = holder.alarm;
			final ImageView ivLocation = holder.location;
			holder.done.setOnClickListener(new OnClickListener(){
				
				public void onClick(View v) {
					
						MainActivity ma = (MainActivity)activity;
						ma.onItemCheck(pos, h.isChecked());
						
						if(h.isChecked()){
							tempValues.setAlarm(false);
							ivAlarm.setImageResource(R.drawable.blank);
							
							tempValues.setLocation(false);
							ivLocation.setImageResource(R.drawable.blank);
						}	
				}
			});
		
			int bitmapId = (activity).getResources().getIdentifier(
					"act" + tempValues.getImageId(), "drawable",
					activity.getPackageName());
			holder.image.setImageResource(bitmapId);
			vi.setOnClickListener(new OnItemClickListener(position));
		}
		return vi;
	}
 
    public void onClick(View v) {	}
     
    private class OnItemClickListener  implements OnClickListener	{  
    	
        private int mPosition;
        OnItemClickListener(int position){
             mPosition = position;
        }
        public void onClick(View arg0) {
        	MainActivity ma = (MainActivity)activity;
          	ma.onItemClick(mPosition);
        }               
    }   
}
