package repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.Title;

public class TitleRepositoryImpl implements TitleRepository{
	private static String connectionIP ="jdbc:mysql://localhost/eksi";
	private static String username ="root";
	private static String pass = "pass";
	private static String db = connectionIP;
	private static String myDriver = "com.mysql.jdbc.Driver";

	@Override
	public List<Title> getAllTitles() {
		try {
			String tableName = "title";
			Class.forName(myDriver);
			Connection conn = DriverManager.getConnection(db, username, pass);
			String query = "SELECT * FROM " +tableName;
			Statement st = conn.createStatement();        
			ResultSet rs = st.executeQuery(query); 
			List<Title> titleList = new ArrayList<Title>();
			while (rs.next()) {
				Title currentTitle = new Title();
				int s_id = rs.getInt("ID");
				currentTitle.setId(s_id);
				String s_name = rs.getString("name");
				currentTitle.setName(s_name);
				String s_date = rs.getString("date");
				currentTitle.setDate(s_date);
				titleList.add(currentTitle);
			}
			st.close();
			conn.close();
			return (List<Title>)titleList;
			
		} catch (Exception e) {
			 System.err.println("Database Connection Error ! TITLE TABLE");
	         System.err.println(e.getMessage());        
	         return null;
		}
	}
	
	@Override
	public Title getTitleById(int id) {
		try {
			String tableName = "title";
			Class.forName(myDriver);
			Connection conn = DriverManager.getConnection(db, username, pass);
			String query = "SELECT * FROM " +tableName + " WHERE ID=" + id;
			Statement st = conn.createStatement();        
			ResultSet rs = st.executeQuery(query);
			while (rs.next()) {
				Title currentTitle = new Title();
				int s_id = rs.getInt("ID");
				currentTitle.setId(s_id);
				String s_name = rs.getString("name");
				currentTitle.setName(s_name);
				String s_date = rs.getString("date");
				currentTitle.setDate(s_date);
				st.close();
				conn.close();
				return (Title)currentTitle;
			}
			return null;			
		} catch (Exception e) {
			System.err.println("Database Connection Error ! TITLE TABLE");
	        System.err.println(e.getMessage());        
	        return null;
		}
	}
	
	@Override
	public boolean addTitle(Title title) {
		try {
			String tableName = "title";
			Class.forName(myDriver);
			Connection conn = DriverManager.getConnection(db, username, pass);
			String q1 = "SELECT MAX(ID) +1 AS id FROM " +tableName;
			Statement st1 = conn.createStatement();	          
	        ResultSet rs = st1.executeQuery(q1);
	        int id = 1;
	        while (rs.next()){
	        	if(rs != null) {	        		
	        		id = rs.getInt("id");
	        	}
	        	if(id == 0) {
	        		id = 1;
	        	}
	        }
	        st1.close();	        
			String query = "INSERT INTO "+tableName +" (id,name,date)VALUES(";
			query += +id+",'"+title.getName()+"','"+title.getDate()+"')";
			Statement st = conn.createStatement();
			st.executeUpdate(query);
			st.close();        
			conn.close();
			return true;
			
		} catch (Exception e) {
			System.err.println("Database Connection Error ! TITLE TABLE");
	        System.err.println(e.getMessage());
			return false;
		}
	}
	
	@Override
	public Title getLastSavedTitle() {
		try {			
			String tableName = "title";
			Class.forName(myDriver);
			Connection conn = DriverManager.getConnection(db, username, pass);
			String q1 = "SELECT MAX(ID) AS id FROM " +tableName;
			Statement st1 = conn.createStatement();	          
			ResultSet rs = st1.executeQuery(q1);
			int id = 1;
			while (rs.next()){
				if(rs != null) {	        		
					id = rs.getInt("id");
				}
				if(id == 0) {
					id = 1;
				}
			}
			st1.close();
			Title lastTitle = getTitleById(id);
			return (Title)lastTitle;
		} catch (Exception e) {
			System.err.println("Database Connection Error ! TITLE TABLE");
	        System.err.println(e.getMessage());
			return null;
		}
	}
	
	@Override
	public void removeTitleWithId(int titleId) {
		try {			
			String tableName = "title";
			Class.forName(myDriver);
			Connection conn = DriverManager.getConnection(db, username, pass);
			String query = "DELETE FROM "+tableName +" WHERE id =" + titleId;
			Statement st = conn.createStatement();
			st.executeUpdate(query);
			st.close();        
			conn.close();
		} catch (Exception e) {
			System.err.println("Database Connection Error ! TITLE TABLE");
	        System.err.println(e.getMessage());
		}
	}
}
