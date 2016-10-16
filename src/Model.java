import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class Model{
	private int photonumber;
    private Connection con;
    private Statement stmt;
    
    public Model() throws SQLException{
        String url = "jdbc:mysql://kc-sce-appdb01.kc.umkc.edu/slnz8b";
        String userID = "slnz8b";
        String password = "tZrFLVzffV";
   
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch(java.lang.ClassNotFoundException e) {
            System.out.println(e);
            System.exit(0);
        }
       
        con = DriverManager.getConnection(url,userID,password);
        stmt = con.createStatement();
        
        try {
        	ResultSet tempRS = stmt.executeQuery("SELECT count FROM location;");
        	tempRS.next();
        	photonumber = tempRS.getInt(1);
        	//tempRS = stmt.executeQuery("SELECT 1 FROM photo_album LIMIT 1");
        	int num = 0;
        	tempRS = stmt.executeQuery("SELECT COUNT(*) FROM photo_album;");
        	if (tempRS.next()){
        		num = tempRS.getInt(1);
        	}
			stmt.executeUpdate("update location set total = " + (num ) +";");
			// verify the current photo number count isn't out of bounds
			if (photonumber < 0 || photonumber > num){
				photonumber =num;
			}
        }
        catch (SQLException e){
        	System.out.print(e);
        }
    }
	
	public Photo Getcurrentphoto(){
		int c;
		Photo temp = new Photo(null, null, null);
       	try {
			ResultSet tempRS = stmt.executeQuery("SELECT photo, description, date FROM photo_album WHERE ID = " + (photonumber) + ";");
			boolean there = tempRS.next();
			if (there){
				ByteArrayOutputStream bos = new ByteArrayOutputStream();

				InputStream in = tempRS.getBinaryStream("photo");

				while ((c = in.read()) != -1)
					bos.write(c);
				byte[] rawimage = null;
				rawimage = bos.toByteArray();
				temp.picture = new ImageIcon(rawimage);
				temp.description = new String (tempRS.getString(2));
				temp.date = new String (tempRS.getString(3));
			}
			else{
				Photo tempnull = null;
				return (tempnull);
			}
			
		} catch (SQLException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		}
		return temp;
	}
	public Photo Getphoto(int a){
		photonumber = a;
		return Getcurrentphoto();
	}
	public Photo Previous(){
		photonumber--;
		return Getcurrentphoto();
	}
	public Photo Next(){
		photonumber++;
		return Getcurrentphoto();
	}
	public Photo Deletecurrent(){
		try {		
		stmt.executeUpdate("delete from photo_album where id = " + photonumber + ";");
		
		// adjust id in database

		stmt.executeUpdate("update photo_album set id = id - 1 where id > " + photonumber + ";");
		stmt.executeUpdate("update location set total = total - 1 ;");
		} catch (SQLException e1) {
			System.out.println(e1);
		}
		// verify you didn't delete the last photo
		int currenttotal = 0;
		try {
		ResultSet tempRS = stmt.executeQuery("select total from location;");
			if (tempRS.next()){
				currenttotal = tempRS.getInt(1);
			}
		} catch (SQLException e) {
			System.out.println(e);
		}
		if (photonumber > currenttotal){
			photonumber = currenttotal;
		}
		return Getcurrentphoto();
	}
	public int Numberofphotos(){
		int nums = 0;
		try {
        	ResultSet tempRS = stmt.executeQuery("SELECT total FROM location;");
			boolean there = (tempRS.next());
			if (there){
				nums = tempRS.getInt(1);
			}				
		} catch (SQLException e) {
			System.out.println(e);
		}
		return nums;
	}
	public int Getcurrentphotonumber(){
		return (photonumber);
	}
	public void Addphoto(byte[] data, String desc, String date){
		String sql = "insert into photo_album (id, description, date, photo) values (?,?,?,?)";
		java.sql.PreparedStatement pstmt = null;
		try {
			
			// adjust id in database
			stmt.executeUpdate("update photo_album set id = id + 1 where id > " + photonumber + ";");
			
			pstmt = con.prepareStatement(sql);
			ByteArrayInputStream bis = new ByteArrayInputStream(data);
			pstmt.setBinaryStream(4, bis, (int) data.length);
			pstmt.setInt(1, (photonumber+1));
			pstmt.setString(2, desc);
			pstmt.setString(3, date);
			pstmt.executeUpdate();
			pstmt.close();
			int num = 0;
        	ResultSet tempRS = stmt.executeQuery("SELECT total FROM location;");
			boolean there = (tempRS.next());
			if (there){
				num = tempRS.getInt(1);
			}
			stmt.executeUpdate("update location set total = " + (num +1) +";");
		} catch (SQLException e){
			System.out.println(e);
		}
	}
	public void EditPhoto(String desc, String dat){
		System.out.println(desc);
		System.out.println(dat);
		System.out.println(photonumber);
		try {
			if (desc != ""){
			stmt.executeUpdate("UPDATE photo_album SET description = '" + desc + "' WHERE id = " + (photonumber) + ";");}
			if (dat != ""){
			stmt.executeUpdate("UPDATE photo_album SET date = '" + dat + "' WHERE id = " + (photonumber) + ";");}
		} catch (SQLException e) {
			System.out.println(e);
		}
	}
	public void close(){
		try {
			stmt.executeUpdate("UPDATE location set count = " + photonumber + ";");
		} catch (SQLException e){
			System.out.println(e);
		}
	}
	
}
