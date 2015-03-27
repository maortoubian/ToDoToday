package il.ac.shenkar.todotoday;

import android.graphics.Bitmap;
/*
 * this class hold a single icon and text for the icons adapter
 */
public class Item {
	private Bitmap image;
	private String title;
	
	public Item(Bitmap image, String title) {
		super();
		this.image = image;
		this.title = title;
	}
	public Bitmap getImage() {
		return image;
	}
	public void setImage(Bitmap image) {
		this.image = image;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
}
