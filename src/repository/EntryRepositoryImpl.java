package repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.Entry;

public class EntryRepositoryImpl implements EntryRepository{
	String connectionIP ="jdbc:mysql://localhost/eksi";
	String username ="root";
	String pass = "pass";
	String db = connectionIP;
	String myDriver = "com.mysql.jdbc.Driver";
	
	TitleRepository titleRepository;
	
	UserRepository userRepository;

	@Override
	public List<Entry> getAllEntries() {
		try {
			String tableName = "entry";
			Class.forName(myDriver);
			Connection conn = DriverManager.getConnection(db, username, pass);
			String query = "SELECT * FROM " +tableName;
			Statement st = conn.createStatement();        
			ResultSet rs = st.executeQuery(query); 
			List<Entry> entryList = new ArrayList<Entry>();
			while (rs.next()) {
				Entry currentEntry = new Entry();
				int s_id = rs.getInt("ID");
				currentEntry.setId(s_id);
				String s_description = rs.getString("description");
				currentEntry.setDescription(s_description);
				String s_date = rs.getString("date");
				currentEntry.setDate(s_date);
				int fk_titleId = rs.getInt("fk_title_id");
				currentEntry.setTitle(titleRepository.getTitleById(fk_titleId));
				int fk_userId = rs.getInt("fk_user_id");
				currentEntry.setUser(userRepository.getUserById(fk_userId));
				entryList.add(currentEntry);
			}
			st.close();
			conn.close();
			return entryList;
			
		} catch (Exception e) {
			System.err.println("Database Connection Error ! ENTRY TABLE");
	        System.err.println(e.getMessage());        
	        return null;
		}
	}
	
	@Override
	public boolean addEntry(Entry entry) {
		try {
			String tableName = "entry";
			Class.forName(myDriver);
			Connection conn = DriverManager.getConnection(db, username, pass);
			String q1 = "SELECT MAX(ID) +1 AS id FROM " +tableName;
			Statement st1 = conn.createStatement();	          
	        ResultSet rs = st1.executeQuery(q1);
	        int id = 0;
	        while (rs.next()){
	        	id = rs.getInt("id");
	        }
	        st1.close();
	        
			String query = "INSERT INTO "+tableName +" (id,description,date,fk_title_id,fk_user_id,entry_link)VALUES(";
			query += id +",'" +entry.getDescription()+"','"+entry.getDate()+"',"+entry.getTitleId()+","+entry.getUserId()+",'"+entry.getEntryLink()+"');";
			Statement st = conn.createStatement();
			st.executeUpdate(query);
			st.close();        
			conn.close();
			return true;
			
		} catch (Exception e) {
			System.err.println("Database Connection Error ! ENTRY TABLE");
	        System.err.println(e.getMessage()); 
	        return false;
		}
	}
	
	@Override
	public int getEntryWithEntryLink(String entryUrl) {
		try {
		String tableName = "entry";
		Class.forName(myDriver);
		Connection conn = DriverManager.getConnection(db, username, pass);
		String query = "SELECT * FROM " +tableName+ " WHERE entry_link='"+entryUrl+"'";
		Statement st = conn.createStatement();        
		ResultSet rs = st.executeQuery(query); 
		Entry currentEntry = new Entry();
		int id = 0;
		while (rs.next()) {
			id = rs.getInt("ID");
			currentEntry.setId(id);
			}
			st.close();
			conn.close();
			return id;

		} catch (Exception e) {
			System.err.println("Database Connection Error ! ENTRY TABLE");
			System.err.println(e.getMessage());
			return 0;
		}
	}
		
}
