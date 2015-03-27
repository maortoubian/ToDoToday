package il.ac.shenkar.todotoday.widget;
/*
 * this class hold the widget task row information
 */
public class WidgetItem {
    public String text;
    public int imageId;
    public String desc;

    public WidgetItem(String text, int imageId,String desc) {
        this.text = text;
        this.imageId = imageId;
        this.desc = desc;
    }
}