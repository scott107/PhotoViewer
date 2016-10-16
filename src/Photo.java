import javax.swing.ImageIcon;
import javax.swing.JLabel;

import java.io.Serializable;
import java.util.Date;

public class Photo implements Serializable{


	private static final long serialVersionUID = 123L;

	public Photo(ImageIcon image, String desc, String dat) {
		picture = image;
		description = desc;
		date = dat;
	}
	public String description;
	public String date;
	public ImageIcon picture;
	
	public void EditPhotoInfo(String desc, String dat){
		description = desc;
		date = dat;
	}
	
}
