package il.ac.shenkar.todotoday;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

/*
 * this class holds the icons for the tasks
 * with this class the user can choose one of the app built in 
 * icons when we creates task or when he edit
 */


public class ChooseImageActivity extends Activity {
	public final static String IMAGE_CHOSEN_OK = "chosen_ok";
	
	private GridView gridView;
	private ArrayList<Item> gridArray = new ArrayList<Item>();
	private IconAdapter customGridAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choose_image);
		
		addIconsToArray();
		
		gridView = (GridView) findViewById(R.id.imageViewGrid);
		
		customGridAdapter = new IconAdapter(this, R.layout.row_grid, gridArray);
		gridView.setAdapter(customGridAdapter);
		gridView.setOnItemClickListener(new OnItemClickListener() {

			
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent resultIntent = new Intent();
				resultIntent.putExtra(IMAGE_CHOSEN_OK, position);
				setResult(Activity.RESULT_OK, resultIntent);
				finish();
			}
		});
	}
	
	//list of icons
	private void addIconsToArray() {
		Bitmap icon1 = BitmapFactory.decodeResource(ChooseImageActivity.this.getResources(), R.drawable.act1);
		gridArray.add(new Item(icon1, "2D2D"));
		Bitmap icon2 = BitmapFactory.decodeResource(ChooseImageActivity.this.getResources(), R.drawable.act2);
		gridArray.add(new Item(icon2, "Play"));
		Bitmap icon3 = BitmapFactory.decodeResource(ChooseImageActivity.this.getResources(), R.drawable.act3);
		gridArray.add(new Item(icon3, "Buy"));
		Bitmap icon4 = BitmapFactory.decodeResource(ChooseImageActivity.this.getResources(), R.drawable.act4);
		gridArray.add(new Item(icon4, "Emotional"));
		Bitmap icon5 = BitmapFactory.decodeResource(ChooseImageActivity.this.getResources(), R.drawable.act5);
		gridArray.add(new Item(icon5, "Study"));
		Bitmap icon6 = BitmapFactory.decodeResource(ChooseImageActivity.this.getResources(), R.drawable.act6);
		gridArray.add(new Item(icon6, "Important"));
		Bitmap icon7 = BitmapFactory.decodeResource(ChooseImageActivity.this.getResources(), R.drawable.act7);
		gridArray.add(new Item(icon7, "Favorite"));
		Bitmap icon8 = BitmapFactory.decodeResource(ChooseImageActivity.this.getResources(), R.drawable.act8);
		gridArray.add(new Item(icon8, "Run"));
	}
}
